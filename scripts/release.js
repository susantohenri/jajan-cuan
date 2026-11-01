const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');
const crypto = require('crypto');
const { execSync } = require('child_process');

// sharp dipakai untuk membuat thumbnail (dev dependency). Import malas agar
// pipeline tetap bisa membangun index/meta walau sharp bermasalah.
let sharp = null;
function loadSharp() {
  if (sharp) return sharp;
  try {
    sharp = require('sharp');
  } catch (e) {
    sharp = false;
  }
  return sharp;
}

console.log('====================================================');
console.log('🚀 Jajan Cuan — Automated One-Click Release Pipeline');
console.log('====================================================\n');

const ROOT = path.join(__dirname, '..');
const RTDB_BASE = 'https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app';

// Opsional: lewati thumbnail yang sudah ada (untuk rebuild paksa).
const FORCE_THUMBS = process.argv.includes('--force-thumbs');

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

console.log(`📦 [1/N] Menyiapkan ${activeCount} resep terkurasi...`);

// --- Fungsi bantu URL gambar -----------------------------------------------
// Cover PERSIS seperti yang dipakai kartu web (lihat DATA_SCHEMA.md bagian b).
function resolveCover(recipe) {
  return (recipe.media?.status === 'approved' && recipe.media?.ai_polished_photo)
    ? recipe.media.ai_polished_photo
    : (recipe.media?.source_photo || recipe.og_image || '');
}

// Ubah nilai field cover menjadi URL absolut yang bisa di-unduh.
// - path lokal "img/..." -> https://jajan-cuan.web.app/<path>
// - sudah http(s) -> dipakai langsung
function coverToUrl(cover) {
  if (!cover) return '';
  if (/^https?:\/\//i.test(cover)) return cover;
  return `https://jajan-cuan.web.app/${cover.replace(/^\//, '')}`;
}

// --- Unduh bytes dengan mengikuti redirect ---------------------------------
function httpGetBuffer(inputUrl, timeoutMs = 15000, redirectsLeft = 5) {
  return new Promise((resolve, reject) => {
    const mod = inputUrl.startsWith('https') ? https : http;
    const doRequest = (url, remaining) => {
      const req = mod.get(url, { timeout: timeoutMs }, (res) => {
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location && remaining > 0) {
          res.resume();
          let next;
          try {
            next = new URL(res.headers.location, url).toString();
          } catch (e) {
            return reject(new Error(`redirect URL tidak valid: ${res.headers.location}`));
          }
          return doRequest(next, remaining - 1);
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          res.resume();
          return reject(new Error(`HTTP ${res.statusCode} untuk ${url}`));
        }
        const chunks = [];
        res.on('data', (d) => chunks.push(d));
        res.on('end', () => resolve(Buffer.concat(chunks)));
        res.on('error', reject);
      });
      req.on('timeout', () => req.destroy(new Error(`timeout ${timeoutMs}ms untuk ${url}`)));
      req.on('error', reject);
    };
    doRequest(inputUrl, redirectsLeft);
  });
}

// --- Thumbnail -------------------------------------------------------------
const THUMBS_DIR = path.join(ROOT, 'web', 'img', 'recipes', 'thumbs');
const THUMB_STATE_PATH = path.join(THUMBS_DIR, '.gen-state.json');
const THUMB_TARGET_BYTES = 40 * 1024; // target <= 40 KB

function thumbRelPath(slug) {
  return `img/recipes/thumbs/${slug}.webp`;
}

function loadThumbState() {
  try {
    return JSON.parse(fs.readFileSync(THUMB_STATE_PATH, 'utf8'));
  } catch (e) {
    return {};
  }
}

function saveThumbState(state) {
  fs.mkdirSync(THUMBS_DIR, { recursive: true });
  fs.writeFileSync(THUMB_STATE_PATH, JSON.stringify(state, null, 2), 'utf8');
}

// Encode WebP 400x400 center-crop, turunkan quality sampai <= target.
async function encodeThumb(buffer) {
  const sh = loadSharp();
  if (!sh) throw new Error('sharp tidak tersedia (npm install --save-dev sharp)');
  const levels = [82, 74, 64, 52, 40];
  let best = null;
  for (const q of levels) {
    const out = await sh(buffer)
      .rotate() // hormati EXIF orientation
      .resize(400, 400, { fit: 'cover', position: 'center' })
      .webp({ quality: q })
      .toBuffer();
    if (!best || out.length < best.length) best = out;
    if (out.length <= THUMB_TARGET_BYTES) break;
  }
  return best;
}

// Bangun semua thumbnail; return { map: {slug: relPath|null}, ok, fail, failed:[] }
async function buildThumbnails(posts) {
  const result = { map: {}, ok: 0, fail: 0, failed: [] };
  const state = FORCE_THUMBS ? {} : loadThumbState();
  fs.mkdirSync(THUMBS_DIR, { recursive: true });

  let idx = 0;
  const total = Object.keys(posts).length;
  for (const [slug, recipe] of Object.entries(posts)) {
    idx++;
    const rel = thumbRelPath(slug);
    const destPath = path.join(THUMBS_DIR, `${slug}.webp`);
    const cover = resolveCover(recipe);
    const coverUrl = coverToUrl(cover);
    const already = state[slug] && state[slug].url === coverUrl && fs.existsSync(destPath);

    if (already) {
      result.map[slug] = rel;
      result.ok++;
      if (idx % 25 === 0) console.log(`   thumbs ${idx}/${total} (skip-existing)...`);
      continue;
    }

    try {
      if (!coverUrl) throw new Error('cover kosong');
      const buf = await httpGetBuffer(coverUrl);
      const webp = await encodeThumb(buf);
      if (!webp || webp.length === 0) throw new Error('hasil encode kosong');
      fs.writeFileSync(destPath, webp);
      state[slug] = { url: coverUrl, hash: crypto.createHash('sha1').update(buf).digest('hex') };
      result.map[slug] = rel;
      result.ok++;
    } catch (err) {
      result.map[slug] = null;
      result.fail++;
      result.failed.push(slug);
      console.warn(`   ⚠️ thumb gagal (${slug}): ${err.message}`);
    }

    if (idx % 25 === 0) console.log(`   thumbs ${idx}/${total}...`);
  }

  saveThumbState(state);
  return result;
}

function dirSizeBytes(dir) {
  let total = 0;
  if (!fs.existsSync(dir)) return 0;
  for (const name of fs.readdirSync(dir)) {
    if (name.startsWith('.')) continue;
    const p = path.join(dir, name);
    try {
      if (fs.statSync(p).isFile()) total += fs.statSync(p).size;
    } catch (e) { /* skip */ }
  }
  return total;
}

// --- Index & Meta (kontrak data AGENTS.md §5) ------------------------------
// ingredients_text: nama bahan lowercase, digabung spasi (DATA_SCHEMA.md §5).
function buildIngredientsText(recipe) {
  const names = Array.isArray(recipe.ingredients)
    ? recipe.ingredients.map((ing) => (ing.name || '').trim()).filter(Boolean)
    : [];
  return names.join(' ').toLowerCase().trim();
}

function buildSearchIndex(posts) {
  const index = {};
  for (const [id, recipe] of Object.entries(posts)) {
    const tags = Array.isArray(recipe.tags) ? recipe.tags : [];
    index[id] = {
      id: recipe.id ?? id,
      slug: recipe.slug ?? id,
      title: recipe.title ?? '',
      category: recipe.category ?? '',
      tags,
      ingredients_text: buildIngredientsText(recipe),
      cover: resolveCover(recipe),
      thumb: null, // diisi setelah thumbnail jadi
      est_capital: recipe.business_analysis?.estimated_initial_capital || 35000,
      portions: recipe.default_portions || 20,
      is_viral: tags.includes('viral')
    };
  }
  return index;
}

function buildMeta(count) {
  return { version: new Date().toISOString(), count, schema: 1 };
}

// --- Tulis ke RTDB (metode sama dengan /posts: PUT unauthenticated) --------
function rtdbPut(node, data) {
  const url = `${RTDB_BASE}/${node}.json`;
  return new Promise((resolve, reject) => {
    const payload = JSON.stringify(data);
    const req = https.request(url, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(payload)
      }
    }, (res) => {
      let body = '';
      res.on('data', (d) => body += d);
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) resolve(true);
        else reject(new Error(`HTTP ${res.statusCode}: ${body}`));
      });
    });
    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

// --- Update seed-database.html --------------------------------------------
function updateSeedHtml(finalPosts, searchIndex, meta) {
  const seederHtmlPath = path.join(__dirname, 'seed-database.html');
  if (!fs.existsSync(seederHtmlPath)) return;
  let html = fs.readFileSync(seederHtmlPath, 'utf8');

  const payloadRegex = /const SEED_PAYLOAD = \{[\s\S]*?\n    \};/;
  html = html.replace(payloadRegex, `const SEED_PAYLOAD = ${JSON.stringify(finalPosts, null, 2)};`);

  const indexRegex = /const SEED_SEARCH_INDEX = \{[\s\S]*?\n    \};/;
  html = html.replace(indexRegex, `const SEED_SEARCH_INDEX = ${JSON.stringify(searchIndex, null, 2)};`);

  const metaRegex = /const SEED_META = \{[\s\S]*?\};/;
  html = html.replace(metaRegex, `const SEED_META = ${JSON.stringify(meta, null, 2)};`);

  fs.writeFileSync(seederHtmlPath, html, 'utf8');
}

// --- Report ----------------------------------------------------------------
function humanBytes(n) {
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / (1024 * 1024)).toFixed(2)} MB`;
}

function printIndexStats(searchIndex, meta, thumbs) {
  const jsonSize = Buffer.byteLength(JSON.stringify(searchIndex), 'utf8');
  const thumbDirTotal = dirSizeBytes(THUMBS_DIR);
  const sample = Object.values(searchIndex).slice(0, 2);

  console.log('\n----------------------------------------------------');
  console.log('📇 /search_index');
  console.log(`   item (resep published): ${Object.keys(searchIndex).length}`);
  console.log(`   ukuran JSON: ${humanBytes(jsonSize)} (${jsonSize} byte)`);
  console.log('   contoh 2 item:');
  for (const it of sample) {
    console.log(`     - ${it.slug}`);
    console.log(`        cover=${it.cover}`);
    console.log(`        thumb=${it.thumb}`);
    console.log(`        est_capital=${it.est_capital} portions=${it.portions} is_viral=${it.is_viral}`);
    console.log(`        ingredients_text="${it.ingredients_text.slice(0, 80)}${it.ingredients_text.length > 80 ? '…' : ''}"`);
  }
  console.log('\n🏷️  /meta');
  console.log(`   ${JSON.stringify(meta)}`);
  console.log('\n🖼️  Thumbnail');
  console.log(`   berhasil: ${thumbs.ok}   gagal: ${thumbs.fail}`);
  console.log(`   total ukuran folder thumbs: ${humanBytes(thumbDirTotal)}`);
  if (thumbs.failed.length) {
    console.log(`   gagal (${thumbs.failed.length}): ${thumbs.failed.slice(0, 10).join(', ')}${thumbs.failed.length > 10 ? ' …' : ''}`);
  }
  console.log('----------------------------------------------------');

  return { jsonSize };
}

async function runRelease(dryRun) {
  // 2. Thumbnail (dibangun di kedua mode; dry-run hanya lokal, tanpa RTDB/deploy)
  console.log(`\n🖼️  [2/N] Membuat thumbnail (400x400 WebP, target <=40KB)...`);
  const thumbs = await buildThumbnails(finalPosts);

  // 3. Index + Meta
  const searchIndex = buildSearchIndex(finalPosts);
  for (const [id, item] of Object.entries(searchIndex)) {
    item.thumb = thumbs.map[id] || null;
  }
  const meta = buildMeta(Object.keys(searchIndex).length);

  printIndexStats(searchIndex, meta, thumbs);

  if (dryRun) {
    console.log('\n[DRY-RUN] Tidak menulis ke RTDB dan tidak deploy. Thumbnail lokal sudah dibuat.');
    console.log('----------------------------------------------------\n');
    return;
  }

  // 4. Simpan seed-posts.json + sinkron seed-database.html (perilaku lama)
  const seedPostsPath = path.join(ROOT, 'data', 'seed-posts.json');
  fs.writeFileSync(seedPostsPath, JSON.stringify(finalPosts, null, 2), 'utf8');
  console.log(`\n💾 [3/N] seed-posts.json tersimpan (${humanBytes(fs.statSync(seedPostsPath).size)}).`);
  updateSeedHtml(finalPosts, searchIndex, meta);
  console.log(`   ✅ seed-database.html disinkronkan (SEED_PAYLOAD + SEED_SEARCH_INDEX + SEED_META).`);

  // 5. Push ke RTDB: /posts, lalu /search_index (PUT seluruh node), lalu /meta TERAKHIR
  console.log(`\n☁️ [4/N] Mengirim data ke Firebase RTDB (/posts -> /search_index -> /meta)...`);
  const results = { posts: false, index: false, meta: false };
  try {
    await rtdbPut('posts', finalPosts);
    results.posts = true;
    console.log('   ✅ /posts live.');
  } catch (err) {
    console.warn(`   ⚠️ /posts gagal: ${err.message}`);
    console.log('   👉 Buka scripts/seed-database.html dan klik tombol push, atau login Firebase CLI.');
  }
  try {
    await rtdbPut('search_index', searchIndex);
    results.index = true;
    console.log('   ✅ /search_index live.');
  } catch (err) {
    console.warn(`   ⚠️ /search_index gagal: ${err.message}`);
  }
  try {
    await rtdbPut('meta', meta);
    results.meta = true;
    console.log('   ✅ /meta live (terakhir).');
  } catch (err) {
    console.warn(`   ⚠️ /meta gagal: ${err.message}`);
  }

  // 6. Deploy ke Firebase Hosting
  console.log(`\n🌐 [5/N] Melakukan build & deploy ke Firebase Hosting...`);
  let deploySuccess = false;
  try {
    console.log('   Menjalankan: firebase deploy --only hosting ...');
    let deployOutput;
    try {
      deployOutput = execSync('firebase deploy --only hosting', { encoding: 'utf8', cwd: ROOT });
    } catch (e1) {
      console.log('   Mencoba via npx firebase-tools ...');
      deployOutput = execSync('npx -p firebase-tools firebase deploy --only hosting', { encoding: 'utf8', cwd: ROOT });
    }
    console.log(deployOutput);
    deploySuccess = true;
    console.log(`   ✅ DEPLOY HOSTING BERHASIL!`);
  } catch (err) {
    console.warn(`   ⚠️ Catatan Deploy: Firebase CLI belum login atau butuh otentikasi.`);
    console.log(`   Penyebab: ${err.message.split('\n')[0]}`);
    console.log(`   👉 Jalankan 'npx firebase-tools login' sekali di terminal untuk login.`);
  }

  console.log(`\n====================================================`);
  if (results.posts && results.index && results.meta && deploySuccess) {
    console.log(`🎉 RELEASE SELESAI & LIVE DI CLOUD!`);
  } else if (results.posts && results.index && results.meta) {
    console.log(`⚠️ DATA DATABASE LIVE, TAPI HOSTING BELUM TER-DEPLOY!`);
  } else {
    console.log(`❌ RELEASE BELUM SELESAI.`);
  }
  console.log(`- Total Resep Siap: ${activeCount} resep.`);
  console.log(`- Thumbnail: ${thumbs.ok} berhasil, ${thumbs.fail} gagal.`);
  console.log(`- Database Cloud: jajan-cuan-default-rtdb (asia-southeast1) node /posts, /search_index, /meta`);
  console.log(`- Web Live URL: https://jajan-cuan.web.app/experience`);
  console.log(`- Aplikasi Android: membaca /meta + /search_index, lalu /posts/{id}.`);
  console.log(`====================================================\n`);
}

const DRY_RUN = process.argv.includes('--dry-run');
runRelease(DRY_RUN).catch((err) => {
  console.error(err);
  process.exit(1);
});
