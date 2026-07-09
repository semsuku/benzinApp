import sqlite3
import math

# Use in-memory SQLite DB for testing
conn = sqlite3.connect(":memory:")
conn.row_factory = sqlite3.Row

# Initialize schemas
cursor = conn.cursor()
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
cursor.execute("""
    CREATE TABLE IF NOT EXISTS prices (
        station_id INTEGER,
        fuel_type TEXT,
        price REAL,
        is_self INTEGER,
        dt_comu TEXT,
        PRIMARY KEY (station_id, fuel_type, is_self)
    )
""")
cursor.execute("CREATE INDEX IF NOT EXISTS idx_stations_coords ON stations(latitude, longitude)")
cursor.execute("CREATE INDEX IF NOT EXISTS idx_prices_search ON prices(fuel_type, price)")
conn.commit()

# Insert test data:
# User location: Rome (41.9028, 12.4964)
# Station 1: Rome Center (41.9000, 12.4900) ~ 0.6 km away. Price: 1.85 EUR/L.
# Station 2: Rome Outskirts (41.9200, 12.4500) ~ 4.3 km away. Price: 1.75 EUR/L (Cheaper but further).
# Station 3: Milan (45.4642, 9.1900) ~ 480 km away. Price: 1.60 EUR/L (Cheaper but way out of radius).
cursor.execute("""
    INSERT INTO stations (id, operator, brand, name, type, address, city, province, latitude, longitude)
    VALUES 
    (1, 'Eni Roma', 'Eni', 'Roma Centro', 'Stradale', 'Via Nazionale', 'Roma', 'RM', 41.9000, 12.4900),
    (2, 'IP Roma', 'IP', 'Roma Ovest', 'Stradale', 'Via Aurelia', 'Roma', 'RM', 41.9200, 12.4500),
    (3, 'Q8 Milano', 'Q8', 'Milano Duomo', 'Stradale', 'Via Torino', 'Milano', 'MI', 45.4642, 9.1900)
""")

cursor.execute("""
    INSERT INTO prices (station_id, fuel_type, price, is_self, dt_comu)
    VALUES 
    (1, 'Benzina', 1.85, 1, '2026-07-08 08:00:00'),
    (2, 'Benzina', 1.75, 1, '2026-07-08 08:00:00'),
    (3, 'Benzina', 1.60, 1, '2026-07-08 08:00:00')
""")
conn.commit()

def haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = (math.sin(dlat / 2) ** 2 +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

# Test querying nearest stations
user_lat = 41.9028
user_lon = 12.4964
radius_km = 10
fuel_type = "Benzina"

# Calculate bounding box
delta_lat = radius_km / 111.0
delta_lon = radius_km / (111.0 * math.cos(math.radians(user_lat)))

min_lat = user_lat - delta_lat
max_lat = user_lat + delta_lat
min_lon = user_lon - delta_lon
max_lon = user_lon + delta_lon

cursor.execute("""
    SELECT s.id, s.name, s.latitude, s.longitude, p.price, p.is_self
    FROM stations s
    JOIN prices p ON s.id = p.station_id
    WHERE s.latitude BETWEEN ? AND ?
      AND s.longitude BETWEEN ? AND ?
      AND p.fuel_type = ?
""", (min_lat, max_lat, min_lon, max_lon, fuel_type))
rows = cursor.fetchall()

results = []
for row in rows:
    dist = haversine(user_lat, user_lon, row["latitude"], row["longitude"])
    if dist <= radius_km:
        results.append({
            "id": row["id"],
            "name": row["name"],
            "distance_km": round(dist, 2),
            "price": row["price"],
            "is_self": bool(row["is_self"])
        })

# Sort by price, then distance
results.sort(key=lambda x: (x["price"], x["distance_km"]))

# Output verification
print("--- VERIFICATION RESULTS ---")
print(f"User is at Rome: {user_lat}, {user_lon}")
print(f"Querying for: {fuel_type} within {radius_km} km")
print(f"Found {len(results)} stations within radius:")
for idx, res in enumerate(results, 1):
    print(f"{idx}. {res['name']} - Dist: {res['distance_km']} km - Price: {res['price']} EUR - Self: {res['is_self']}")

assert len(results) == 2, "Should find exactly 2 stations (Rome Center and Rome Outskirts)"
assert results[0]["id"] == 2, "Cheapest station should be first (IP Roma Ovest - 1.75 EUR)"
assert results[1]["id"] == 1, "More expensive station should be second (Eni Roma Centro - 1.85 EUR)"
print("Verification checks PASSED successfully!")
conn.close()
