const fetch = require('node-fetch'); // wait, node-fetch might not be installed, let's use standard dynamic import or https module

const https = require('https');

function get(url) {
  return new Promise((resolve, reject) => {
    https.get(url, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        resolve({
          statusCode: res.statusCode,
          headers: res.headers,
          body: data
        });
      });
    }).on('error', (err) => {
      reject(err);
    });
  });
}

async function main() {
  try {
    console.log("Fetching GET /profiles/user/2...");
    const profileRes = await get("https://api-gateway2-catch-go.up.railway.app/profiles/user/2");
    console.log("Status Code:", profileRes.statusCode);
    console.log("Body:", profileRes.body);

    console.log("\nFetching GET /jobs/applications/user/2...");
    const appRes = await get("https://api-gateway2-catch-go.up.railway.app/jobs/applications/user/2");
    console.log("Status Code:", appRes.statusCode);
    console.log("Body:", appRes.body);

    console.log("Fetching GET /profiles...");
    const profilesAll = await get("https://api-gateway2-catch-go.up.railway.app/profiles");
    console.log("Profiles All Status Code:", profilesAll.statusCode);
    console.log("Profiles All Body:", profilesAll.body);

    console.log("\nFetching GET /jobs...");
    const jobsAll = await get("https://api-gateway2-catch-go.up.railway.app/jobs");
    console.log("Jobs All Status Code:", jobsAll.statusCode);
    console.log("Jobs All Body:", jobsAll.body);

    console.log("\nFetching GET /profiles/actuator/health...");
    const profilesHealth = await get("https://api-gateway2-catch-go.up.railway.app/profiles/actuator/health");
    console.log("Profiles Health Status Code:", profilesHealth.statusCode);
    console.log("Profiles Health Body:", profilesHealth.body);

    console.log("\nFetching GET /jobs/actuator/health...");
    const jobsHealth = await get("https://api-gateway2-catch-go.up.railway.app/jobs/actuator/health");
    console.log("Jobs Health Status Code:", jobsHealth.statusCode);
    console.log("Jobs Health Body:", jobsHealth.body);

    console.log("\nFetching GET /auth/actuator/health...");
    const authHealth = await get("https://api-gateway2-catch-go.up.railway.app/auth/actuator/health");
    console.log("Auth Health Status Code:", authHealth.statusCode);
    console.log("Auth Health Body:", authHealth.body);

    console.log("\nFetching GET /actuator/health...");
    const gatewayHealth = await get("https://api-gateway2-catch-go.up.railway.app/actuator/health");
    console.log("Gateway Health Status Code:", gatewayHealth.statusCode);
    console.log("Gateway Health Body:", gatewayHealth.body);

  } catch (err) {
    console.error(err);
  }
}

main();
