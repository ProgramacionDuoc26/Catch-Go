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
    console.log("Connected!");

    // Inspect columns of user_accounts
    const userCols = await client.query(`
      SELECT column_name, data_type, is_nullable
      FROM information_schema.columns
      WHERE table_name = 'user_accounts'
    `);
    console.log("\nuser_accounts columns:");
    for (const col of userCols.rows) {
      console.log(` - ${col.column_name}: ${col.data_type} (nullable=${col.is_nullable})`);
    }

    // Inspect columns of profiles
    const profileCols = await client.query(`
      SELECT column_name, data_type, is_nullable
      FROM information_schema.columns
      WHERE table_name = 'profiles'
    `);
    console.log("\nprofiles columns:");
    for (const col of profileCols.rows) {
      console.log(` - ${col.column_name}: ${col.data_type} (nullable=${col.is_nullable})`);
    }

    // Inspect columns of job_applications
    const appCols = await client.query(`
      SELECT column_name, data_type, is_nullable
      FROM information_schema.columns
      WHERE table_name = 'job_applications'
    `);
    console.log("\njob_applications columns:");
    for (const col of appCols.rows) {
      console.log(` - ${col.column_name}: ${col.data_type} (nullable=${col.is_nullable})`);
    }

  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

main();
