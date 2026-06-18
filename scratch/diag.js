const https = require('https');

function request(method, url, body) {
  return new Promise((resolve, reject) => {
    const urlObj = new URL(url);
    const options = {
      hostname: urlObj.hostname,
      port: urlObj.port || 443,
      path: urlObj.pathname + urlObj.search,
      method,
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }
    };
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => resolve({ statusCode: res.statusCode, body: data }));
    });
    req.on('error', reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

async function main() {
  const BASE = 'https://api-gateway2-catch-go.up.railway.app';
  
  console.log("1. GET /profiles/user/debug-test-001");
  const r1 = await request('GET', `${BASE}/profiles/user/debug-test-001`);
  console.log("Status:", r1.statusCode, "Body:", r1.body.substring(0, 200));
  
  console.log("\n2. GET /profiles/user/2");
  const r2 = await request('GET', `${BASE}/profiles/user/2`);
  console.log("Status:", r2.statusCode, "Body:", r2.body.substring(0, 200));

  console.log("\n3. GET /jobs");
  const r3 = await request('GET', `${BASE}/jobs`);
  console.log("Status:", r3.statusCode, "Body:", r3.body.substring(0, 300));

  console.log("\n4. GET /matching");
  const r4 = await request('GET', `${BASE}/matching`);
  console.log("Status:", r4.statusCode, "Body:", r4.body.substring(0, 200));

  console.log("\n5. GET /auth/user/2");
  const r5 = await request('GET', `${BASE}/auth/user/2`);
  console.log("Status:", r5.statusCode, "Body:", r5.body);
}

main().catch(console.error);
