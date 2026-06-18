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
    console.log("Connected to Supabase!");

    // List all tables
    const tablesRes = await client.query(`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public'
    `);
    console.log("Tables:");
    for (const row of tablesRes.rows) {
      console.log(` - ${row.table_name}`);
    }

    // Inspect user_accounts
    try {
      const usersRes = await client.query("SELECT * FROM user_accounts");
      console.log("\nUser Accounts:");
      console.log(usersRes.rows);
    } catch (err) {
      console.log("\nError reading user_accounts:", err.message);
    }

    // Inspect profiles
    try {
      const profilesRes = await client.query("SELECT * FROM profiles");
      console.log("\nProfiles:");
      console.log(profilesRes.rows);
    } catch (err) {
      console.log("\nError reading profiles:", err.message);
    }

    // Inspect Flyway Schema History tables
    const histories = ['flyway_schema_history_auth', 'flyway_schema_history_profiles', 'flyway_schema_history_jobs', 'flyway_schema_history_matching'];
    for (const history of histories) {
      try {
        const histRes = await client.query(`SELECT script, success FROM ${history} ORDER BY installed_rank DESC LIMIT 5`);
        console.log(`\nHistory for ${history}:`);
        console.log(histRes.rows);
      } catch (err) {
        console.log(`\nNo table ${history} or error:`, err.message);
      }
    }

  } catch (err) {
    console.error("Error:", err);
  } finally {
    await client.end();
  }
}

main();
