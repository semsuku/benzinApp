import os
import sqlite3
import urllib.request
import csv
import io
import time
from dotenv import load_dotenv

# Load .env variables at startup
load_dotenv()

DB_PATH = os.getenv("DB_PATH", "/app/data/fuel.db")
STATIONS_URL = "https://www.mimit.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv"
PRICES_URL = "https://www.mimit.gov.it/images/exportCSV/prezzo_alle_8.csv"

def init_db(conn):
    cursor = conn.cursor()
    # Create stations table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS stations (
            id INTEGER PRIMARY KEY,
            operator TEXT,
            brand TEXT,
            name TEXT,
            type TEXT,
            address TEXT,
            city TEXT,
            province TEXT,
            latitude REAL,
            longitude REAL
        )
    """)
    # Create prices table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS prices (
            station_id INTEGER,
            fuel_type TEXT,
            price REAL,
            is_self INTEGER,
            dt_comu TEXT,
            PRIMARY KEY (station_id, fuel_type, is_self),
            FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
        )
    """)
    # Create indexes for fast geospatial bounding box search and sorting
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_stations_coords ON stations(latitude, longitude)")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_prices_search ON prices(fuel_type, price)")
    conn.commit()

def download_file_stream(url):
    """
    Downloads file in chunks and returns a line-by-line generator to save memory.
    """
    print(f"Downloading {url}...")
    req = urllib.request.Request(
        url, 
        headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
    )
    response = urllib.request.urlopen(req)
    # Read as text stream
    return io.TextIOWrapper(response, encoding='utf-8', errors='replace')

def run_ingestion():
    # Make sure target folder exists
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    
    print(f"Opening database connection at {DB_PATH}")
    conn = sqlite3.connect(DB_PATH)
    init_db(conn)
    
    start_time = time.time()
    
    # 1. Ingest Stations
    try:
        stream = download_file_stream(STATIONS_URL)
        reader = csv.reader(stream, delimiter='|')
        
        stations_data = []
        batch_size = 1000
        count = 0
        
        # Skip the header or skip non-numeric ids
        for row in reader:
            if not row or len(row) < 10:
                continue
            
            # Check if it's the header row
            if row[0].strip().lower() in ("id impianto", "idimpianto") or not row[0].strip().isdigit():
                continue
                
            try:
                station_id = int(row[0])
                operator = row[1].strip()
                brand = row[2].strip()
                name = row[3].strip()
                st_type = row[4].strip()
                address = row[5].strip()
                city = row[6].strip()
                province = row[7].strip()
                
                # Check for empty coords and parse
                lat_str = row[8].strip()
                lon_str = row[9].strip()
                if not lat_str or not lon_str:
                    continue
                
                latitude = float(lat_str)
                longitude = float(lon_str)
                
                # Basic geographical validation for Italy (lat: 35 to 48, lon: 6 to 19)
                if not (35.0 <= latitude <= 48.0) or not (6.0 <= longitude <= 19.0):
                    continue
                    
                stations_data.append((
                    station_id, operator, brand, name, st_type,
                    address, city, province, latitude, longitude
                ))
                
                if len(stations_data) >= batch_size:
                    conn.executemany("""
                        INSERT OR REPLACE INTO stations 
                        (id, operator, brand, name, type, address, city, province, latitude, longitude)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, stations_data)
                    conn.commit()
                    count += len(stations_data)
                    stations_data = []
                    
            except ValueError:
                # Catch float/int parse errors for corrupted lines
                continue
        
        # Insert remaining stations
        if stations_data:
            conn.executemany("""
                INSERT OR REPLACE INTO stations 
                (id, operator, brand, name, type, address, city, province, latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, stations_data)
            conn.commit()
            count += len(stations_data)
            
        print(f"Successfully ingested {count} stations.")
    except Exception as e:
        print(f"Error ingesting stations: {e}")
        
    # 2. Ingest Prices
    try:
        stream = download_file_stream(PRICES_URL)
        reader = csv.reader(stream, delimiter='|')
        
        prices_data = []
        batch_size = 5000
        count = 0
        
        # Clean current prices first so we don't keep obsolete ones (optional, but keeps DB small)
        conn.execute("DELETE FROM prices")
        conn.commit()
        
        for row in reader:
            if not row or len(row) < 5:
                continue
                
            # Skip header
            if row[0].strip().lower() in ("id impianto", "idimpianto") or not row[0].strip().isdigit():
                continue
                
            try:
                station_id = int(row[0])
                fuel_type = row[1].strip()
                price = float(row[2])
                is_self = int(row[3])
                dt_comu = row[4].strip()
                
                # Skip invalid prices
                if price <= 0.0 or price > 5.0:
                    continue
                    
                prices_data.append((
                    station_id, fuel_type, price, is_self, dt_comu
                ))
                
                if len(prices_data) >= batch_size:
                    conn.executemany("""
                        INSERT OR REPLACE INTO prices 
                        (station_id, fuel_type, price, is_self, dt_comu)
                        VALUES (?, ?, ?, ?, ?)
                    """, prices_data)
                    conn.commit()
                    count += len(prices_data)
                    prices_data = []
                    
            except ValueError:
                continue
                
        # Insert remaining prices
        if prices_data:
            conn.executemany("""
                INSERT OR REPLACE INTO prices 
                (station_id, fuel_type, price, is_self, dt_comu)
                VALUES (?, ?, ?, ?, ?)
            """, prices_data)
            conn.commit()
            count += len(prices_data)
            
        print(f"Successfully ingested {count} price records.")
    except Exception as e:
        print(f"Error ingesting prices: {e}")
        
    conn.close()
    elapsed = time.time() - start_time
    print(f"Ingestion completed in {elapsed:.2f} seconds.")

if __name__ == "__main__":
    run_ingestion()
