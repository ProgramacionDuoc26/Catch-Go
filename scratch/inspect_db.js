const { Client } = require('pg');

const client = new Client({
  connectionString: "postgresql://postgres.hqddvvppesfdjygepklp:Supabase2026@aws-1-us-east-1.pooler.supabase.com:6543/postgres",
  ssl: {
    rejectUnauthorized: false
  }
});

async function main() {
  try {
    await client.connect();
    console.log("Successfully connected to Supabase Session Pooler on port 6543!");

    const res = await client.query("SELECT COUNT(*) FROM profiles");
    console.log("Profiles count:", res.rows[0].count);

  } catch (err) {
    console.error("Connection error on port 6543:", err.message);
  } finally {
    await client.end();
  }
}

main();
