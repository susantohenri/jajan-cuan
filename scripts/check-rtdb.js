const https = require('https');

function testQuery(name, path) {
  return new Promise((resolve) => {
    https.get(`https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app/posts.json?${path}`, (res) => {
      let raw = '';
      res.on('data', d => raw += d);
      res.on('end', () => {
        try {
          const parsed = JSON.parse(raw);
          console.log(`\n=== ${name} ===`);
          if (parsed.error) {
            console.log('ERROR:', parsed.error);
          } else {
            const keys = Object.keys(parsed);
            console.log('Count:', keys.length);
            keys.slice(0, 5).forEach(k => console.log(' -', k, '| cat:', parsed[k].category, '| title:', parsed[k].title));
          }
        } catch (e) {
          console.log('Parse error:', e.message, raw.slice(0, 100));
        }
        resolve();
      });
    });
  });
}

async function run() {
  await testQuery('ALL (orderByKey limit 5)', 'orderBy="$key"&limitToFirst=5');
  await testQuery('KUE (orderBy category equalTo kue)', 'orderBy="category"&equalTo="kue"&limitToFirst=5');
  await testQuery('GORENGAN (orderBy category equalTo gorengan)', 'orderBy="category"&equalTo="gorengan"&limitToFirst=5');
  await testQuery('MINUMAN (orderBy category equalTo minuman)', 'orderBy="category"&equalTo="minuman"&limitToFirst=5');
  await testQuery('KUE (startAt kue endAt kue)', 'orderBy="category"&startAt="kue"&endAt="kue"&limitToFirst=5');
}

run();
