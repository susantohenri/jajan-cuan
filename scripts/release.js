const fs = require('fs');
const path = require('path');
const https = require('https');
const { execSync } = require('child_process');

console.log('====================================================');
console.log('🚀 Jajan Cuan — Automated One-Click Release Pipeline');
console.log('====================================================\n');

// 1. Baca draft data terkurasi
const draftsPath = path.join(__dirname, '..', 'data', 'scraped-drafts.json');
if (!fs.existsSync(draftsPath)) {
  console.error('❌ File data/scraped-drafts.json tidak ditemukan!');
  process.exit(1);
}

const drafts = JSON.parse(fs.readFileSync(draftsPath, 'utf8'));
const finalPosts = {};

let activeCount = 0;
for (const [slug, recipe] of Object.entries(drafts)) {
  if (recipe.published !== false) {
    // Pastikan foto cover terisi dengan foto polesan jika approved, atau foto asli
    const coverPhoto = (recipe.media?.status === 'approved' && recipe.media?.ai_polished_photo)
      ? recipe.media.ai_polished_photo
      : (recipe.media?.source_photo || recipe.og_image || '');

    finalPosts[slug] = {
      ...recipe,
      og_image: coverPhoto,
      last_updated: new Date().toISOString()
    };
    activeCount++;
  }
}

console.log(`📦 [1/4] Menyiapkan ${activeCount} resep terkurasi...`);

// 2. Simpan ke data/seed-posts.json
const seedPostsPath = path.join(__dirname, '..', 'data', 'seed-posts.json');
fs.writeFileSync(seedPostsPath, JSON.stringify(finalPosts, null, 2), 'utf8');
console.log(`   ✅ Tersimpan rapi di: ${seedPostsPath}`);

// 3. Update payload di scripts/seed-database.html secara otomatis
const seederHtmlPath = path.join(__dirname, 'seed-database.html');
if (fs.existsSync(seederHtmlPath)) {
  let html = fs.readFileSync(seederHtmlPath, 'utf8');
  const payloadRegex = /const SEED_PAYLOAD = \{[\s\S]*?\n    \};/;
  const newPayloadStr = `const SEED_PAYLOAD = ${JSON.stringify(finalPosts, null, 2)};`;
  html = html.replace(payloadRegex, newPayloadStr);
  fs.writeFileSync(seederHtmlPath, html, 'utf8');
  console.log(`   ✅ Seeder tool (seed-database.html) otomatis disinkronkan.`);
}

// 4. Push ke Firebase Realtime Database via REST API
console.log(`\n☁️ [2/4] Mengirim ${activeCount} resep ke Firebase RTDB (/posts)...`);
const rtdbUrl = 'https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app/posts.json';

function pushToRtdb(data) {
  return new Promise((resolve, reject) => {
    const payload = JSON.stringify(data);
    const req = https.request(rtdbUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(payload)
      }
    }, (res) => {
      let body = '';
      res.on('data', d => body += d);
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(true);
        } else {
          reject(new Error(`HTTP ${res.statusCode}: ${body}`));
        }
      });
    });

    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

async function runRelease() {
  let dbSuccess = false;
  let deploySuccess = false;

  try {
    await pushToRtdb(finalPosts);
    console.log(`   ✅ BERHASIL PUSH! Seluruh resep kini live di Firebase Realtime Database.`);
    dbSuccess = true;
  } catch (err) {
    console.warn(`   ⚠️ Catatan RTDB REST Push: ${err.message}`);
    console.log(`   👉 Buka scripts/seed-database.html di browser Anda dan klik '🚀 Push Seed Data ke RTDB'.`);
  }

  // 5. Deploy ke Firebase Hosting
  console.log(`\n🌐 [3/4] Melakukan build & deploy ke Firebase Hosting...`);
  try {
    console.log('   Menjalankan: firebase deploy --only hosting ...');
    let deployOutput;
    try {
      deployOutput = execSync('firebase deploy --only hosting', { encoding: 'utf8', cwd: path.join(__dirname, '..') });
    } catch (e1) {
      console.log('   Mencoba via npx firebase-tools ...');
      deployOutput = execSync('npx -p firebase-tools firebase deploy --only hosting', { encoding: 'utf8', cwd: path.join(__dirname, '..') });
    }
    console.log(deployOutput);
    console.log(`   ✅ DEPLOY HOSTING BERHASIL!`);
    deploySuccess = true;
  } catch (err) {
    console.warn(`   ⚠️ Catatan Deploy: Firebase CLI belum login atau butuh otentikasi.`);
    console.log(`   Penyebab: ${err.message.split('\n')[0]}`);
    console.log(`   👉 Jalankan 'npx firebase-tools login' sekali di terminal untuk login.`);
  }

  console.log(`\n====================================================`);
  if (deploySuccess && dbSuccess) {
    console.log(`🎉 RELEASE SELESAI & LIVE DI CLOUD!`);
  } else if (dbSuccess) {
    console.log(`⚠️ DATA DATABASE LIVE, TAPI HOSTING BELUM TER-DEPLOY!`);
  } else {
    console.log(`❌ RELEASE BELUM SELESAI.`);
  }
  console.log(`- Total Resep Siap: ${activeCount} resep.`);
  console.log(`- Database Cloud: jajan-cuan-default-rtdb (asia-southeast1) node /posts`);
  console.log(`- Web Live URL: https://jajan-cuan.web.app/experience.html`);
  console.log(`- Aplikasi Android: Otomatis membaca resep baru seketika!`);
  console.log(`====================================================\n`);
}

runRelease().catch(console.error);
