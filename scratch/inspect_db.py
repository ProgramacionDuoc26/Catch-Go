import psycopg2

try:
    conn = psycopg2.connect(
        host="aws-1-us-east-1.pooler.supabase.com",
        port=5432,
        database="postgres",
        user="postgres.hqddvvppesfdjygepklp",
        password="Supabase2026",
        sslmode="require"
    )
    cur = conn.cursor()
    print("Successfully connected to Supabase!")
    
    # List tables
    cur.execute("""
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public'
    """)
    tables = cur.fetchall()
    print("\nTables in 'public' schema:")
    for t in tables:
        print(f" - {t[0]}")
        
    # Inspect Flyway history of each microservice if they exist
    for table_name in ['flyway_schema_history_auth', 'flyway_schema_history_profiles', 'flyway_schema_history_jobs', 'flyway_schema_history_matching']:
        try:
            cur.execute(f"SELECT COUNT(*) FROM {table_name}")
            cnt = cur.fetchone()[0]
            print(f"\nTable {table_name} exists, row count: {cnt}")
            cur.execute(f"SELECT script, success FROM {table_name} ORDER BY installed_rank DESC LIMIT 5")
            for r in cur.fetchall():
                print(f"   {r[0]}: success={r[1]}")
        except Exception as e:
            print(f"\nTable {table_name} does not exist or error: {e}")
            conn.rollback()

    cur.close()
    conn.close()
except Exception as e:
    print(f"Error connecting/querying: {e}")
