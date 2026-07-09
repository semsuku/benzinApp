import os
import math
import sqlite3
import base64
import json
import logging
from typing import List, Optional
from fastapi import FastAPI, Header, HTTPException, Depends, File, UploadFile, Query
from pydantic import BaseModel
import httpx
from dotenv import load_dotenv

# Load .env variables at startup
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("benzinapp-api")

DB_PATH = os.getenv("DB_PATH", "/app/data/fuel.db")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
BACKEND_API_KEY = os.getenv("BACKEND_API_KEY", "benzinapp_secret_key_123")
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

app = FastAPI(title="BenzinApp API", version="1.0.0")

# Security Dependency
async def verify_api_key(x_api_key: str = Header(...)):
    if x_api_key != BACKEND_API_KEY:
        logger.warning(f"Unauthorized access attempt with key: {x_api_key}")
        raise HTTPException(status_code=401, detail="Invalid API Key")
    return x_api_key

# Request/Response schemas
class FuelPrice(BaseModel):
    fuel_type: str
    price: float
    is_self: bool
    dt_comu: str

class StationResponse(BaseModel):
    id: int
    operator: str
    brand: str
    name: str
    type: str
    address: str
    city: str
    province: str
    latitude: float
    longitude: float
    distance_km: float
    price: float
    is_self: bool
    dt_comu: str

class RefuelingExtractionResponse(BaseModel):
    liters: Optional[float] = None
    totalPrice: Optional[float] = None

class OdometerExtractionResponse(BaseModel):
    km: Optional[int] = None

# Geospatial calculations
def haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    R = 6371.0  # Earth radius in kilometers
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = (math.sin(dlat / 2) ** 2 +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

@app.get("/api/v1/stations/nearest", response_model=List[StationResponse], dependencies=[Depends(verify_api_key)])
async def get_nearest_stations(
    lat: float = Query(..., description="Latitude of user"),
    lon: float = Query(..., description="Longitude of user"),
    fuel_type: str = Query(..., description="Type of fuel, e.g. Benzina, Gasolio"),
    radius_km: int = Query(10, description="Radius in km")
):
    # Ensure database exists
    if not os.path.exists(DB_PATH):
        raise HTTPException(status_code=503, detail="Database not ready. Run ingestion first.")

    # Calculate bounding box for SQLite pre-filtering (highly efficient index scan)
    delta_lat = radius_km / 111.0
    delta_lon = radius_km / (111.0 * math.cos(math.radians(lat)))

    min_lat = lat - delta_lat
    max_lat = lat + delta_lat
    min_lon = lon - delta_lon
    max_lon = lon + delta_lon

    # Normalize fuel type search
    fuel_search = f"%{fuel_type.strip()}%"

    conn = get_db_connection()
    try:
        cursor = conn.cursor()
        
        # SQLite join query restricted within bounding box
        query = """
            SELECT s.id, s.operator, s.brand, s.name, s.type, s.address, s.city, s.province, s.latitude, s.longitude,
                   p.price, p.is_self, p.dt_comu, p.fuel_type
            FROM stations s
            JOIN prices p ON s.id = p.station_id
            WHERE s.latitude BETWEEN ? AND ?
              AND s.longitude BETWEEN ? AND ?
              AND p.fuel_type LIKE ?
        """
        cursor.execute(query, (min_lat, max_lat, min_lon, max_lon, fuel_search))
        rows = cursor.fetchall()
        
        results = []
        for row in rows:
            dist = haversine(lat, lon, row["latitude"], row["longitude"])
            if dist <= radius_km:
                results.append(
                    StationResponse(
                        id=row["id"],
                        operator=row["operator"],
                        brand=row["brand"],
                        name=row["name"],
                        type=row["type"],
                        address=row["address"],
                        city=row["city"],
                        province=row["province"],
                        latitude=row["latitude"],
                        longitude=row["longitude"],
                        distance_km=round(dist, 2),
                        price=row["price"],
                        is_self=bool(row["is_self"]),
                        dt_comu=row["dt_comu"]
                    )
                )
        
        # Sort by price first, then by distance
        results.sort(key=lambda x: (x.price, x.distance_km))
        
        # Return top 5 cheapest
        return results[:5]

    except Exception as e:
        logger.error(f"Error querying SQLite database: {e}")
        raise HTTPException(status_code=500, detail="Internal database query error")
    finally:
        conn.close()

@app.post("/api/v1/gemini/extract", dependencies=[Depends(verify_api_key)])
async def proxy_gemini_extract(
    task_type: str = Query(..., regex="^(refueling|odometer)$"),
    file: UploadFile = File(...)
):
    if not GEMINI_API_KEY:
        logger.error("GEMINI_API_KEY environment variable is not set")
        raise HTTPException(status_code=500, detail="Gemini proxy is misconfigured (missing key)")

    # 1. Select the correct prompt
    if task_type == "refueling":
        prompt = (
            "Analizza questa immagine di un display di una pompa di benzina o scontrino di rifornimento.\n"
            "Estrai e restituisci il risultato **esclusivamente** in formato JSON valido, senza testo aggiuntivo "
            "(niente markdown, niente backticks), con le seguenti chiavi numeriche:\n"
            "- \"liters\" (float, litri erogati, es. 20.50)\n"
            "- \"totalPrice\" (float, costo totale in valuta locale, es. 40.00)\n\n"
            "Se un valore non è leggibile, metti null. Assicurati che i decimali usino il punto."
        )
    else:  # odometer
        prompt = (
            "Analizza questa immagine del contachilometri di una macchina.\n"
            "Estrai e restituisci il numero totale di chilometri percorsi (odometro/chilometraggio totale) "
            "**esclusivamente** in formato JSON valido, senza testo aggiuntivo (niente markdown, niente backticks), "
            "con la seguente chiave:\n"
            "- \"km\" (integer, chilometri totali, es. 124500)\n\n"
            "Se il valore non è leggibile o non è presente, metti null."
        )

    # 2. Read file and encode to base64
    try:
        file_bytes = await file.read()
        base64_data = base64.b64encode(file_bytes).decode("utf-8")
    except Exception as e:
        logger.error(f"Error reading uploaded file: {e}")
        raise HTTPException(status_code=400, detail="Failed to read uploaded image")

    # 3. Call Google Gemini REST API using httpx (asynchronous client)
    gemini_url = f"https://generativelanguage.googleapis.com/v1beta/models/{GEMINI_MODEL}:generateContent?key={GEMINI_API_KEY}"
    
    payload = {
        "contents": [
            {
                "parts": [
                    {"text": prompt},
                    {
                        "inlineData": {
                            "mimeType": file.content_type or "image/jpeg",
                            "data": base64_data
                        }
                    }
                ]
            }
        ]
    }

    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(gemini_url, json=payload, timeout=30.0)
            
            if response.status_code != 200:
                logger.error(f"Gemini API returned status {response.status_code}: {response.text}")
                raise HTTPException(status_code=502, detail="Error communicating with Gemini API")
                
            response_json = response.json()
            
            # Extract content from response
            candidates = response_json.get("candidates", [])
            if not candidates:
                logger.warning(f"No candidates returned in Gemini response: {response_json}")
                raise HTTPException(status_code=502, detail="Gemini returned an empty result")
                
            raw_text = candidates[0]["content"]["parts"][0]["text"].strip()
            
            # Clean markdown JSON block formatting if present
            cleaned_text = raw_text
            if cleaned_text.startswith("```json"):
                cleaned_text = cleaned_text[7:]
            if cleaned_text.endswith("```"):
                cleaned_text = cleaned_text[:-3]
            cleaned_text = cleaned_text.strip()
            
            # Parse text into JSON before returning to ensure structured format
            parsed_data = json.loads(cleaned_text)
            return parsed_data
            
        except httpx.RequestError as exc:
            logger.error(f"HTTP request to Gemini failed: {exc}")
            raise HTTPException(status_code=502, detail="Connection to Gemini API failed")
        except (KeyError, IndexError) as exc:
            logger.error(f"Failed parsing Gemini API response format: {exc}")
            raise HTTPException(status_code=502, detail="Invalid response structure from Gemini API")
        except json.JSONDecodeError as exc:
            logger.error(f"Gemini response was not a valid JSON string: {exc} | Raw content: {cleaned_text}")
            raise HTTPException(status_code=502, detail="Failed to parse Gemini result into structured JSON")
