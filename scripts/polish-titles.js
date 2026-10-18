const fs = require('fs');
const path = require('path');
const https = require('https');

// Helper: Ambil API Key Gemini
function getGeminiApiKey() {
  const keyArgIdx = process.argv.indexOf('--key');
  if (keyArgIdx !== -1 && process.argv[keyArgIdx + 1]) {
    return process.argv[keyArgIdx + 1].trim();
  }

  if (process.env.GEMINI_API_KEY) return process.env.GEMINI_API_KEY.trim();
  if (process.env.GOOGLE_API_KEY) return process.env.GOOGLE_API_KEY.trim();

  const envPath = path.join(__dirname, '..', '.env');
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    const match = envContent.match(/(?:GEMINI_API_KEY|GOOGLE_API_KEY|API_KEY)\s*=\s*([^\r\n]+)/i);
    if (match && match[1] && !match[1].includes('your_gemini_api_key_here')) {
      return match[1].replace(/['"]/g, '').trim();
    }
  }

  const freeLlmPath = path.join(__dirname, '..', 'freellmapi');
  if (fs.existsSync(freeLlmPath)) {
    const content = fs.readFileSync(freeLlmPath, 'utf8').trim();
    const match = content.match(/AIzaSy[A-Za-z0-9_-]{33}/);
    if (match) return match[0];
  }

  return null;
}

// Helper: Ambil Cloudflare Config sebagai cadangan
function getCloudflareConfig() {
  let token = null;
  let accountId = null;

  const envPath = path.join(__dirname, '..', '.env');
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    const tokenMatch = envContent.match(/(?:CLOUDFLARE_API_TOKEN|CF_API_TOKEN|CF_TOKEN)\s*=\s*([^\r\n]+)/i);
    const accMatch = envContent.match(/(?:CLOUDFLARE_ACCOUNT_ID|CF_ACCOUNT_ID|CF_ACCOUNT)\s*=\s*([^\r\n]+)/i);
    if (tokenMatch && tokenMatch[1]) token = tokenMatch[1].replace(/['"]/g, '').trim();
    if (accMatch && accMatch[1]) accountId = accMatch[1].replace(/['"]/g, '').trim();
  }

  if (token && accountId) return { token, accountId };
  return null;
}

function makeHttpRequest(options, payload) {
  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      const chunks = [];
      res.on('data', d => chunks.push(d));
      res.on('end', () => {
        const buffer = Buffer.concat(chunks);
        resolve({
          status: res.statusCode,
          headers: res.headers,
          body: buffer.toString('utf8')
        });
      });
    });
    req.on('error', reject);
    if (payload) req.write(payload);
    req.end();
  });
}

// Model Gemini resmi terkini
const GEMINI_MODELS = [
  'gemini-3.8-flash'
];

async function callGeminiBatch(apiKey, batchItems, retryCount = 0) {
  const prompt = `Anda adalah copywriter profesional dan konsultan branding menu UMKM kuliner di Indonesia untuk aplikasi "Jajan Cuan".
Aplikasi "Jajan Cuan" adalah platform ide bisnis kuliner viral modal kecil.

TUGAS ANDA:
Ubah judul resep mentah hasil scraping Cookpad berikut menjadi nama menu jajanan yang komersial, profesional, menggugah selera (appetizing), dan siap dijual.

ATURAN WAJIB:
1. HAPUS SEMUA frasa obrolan sosial, noise, dan keyword stuffing Cookpad:
   - "ide jualan", "resep jualan", "bisa untuk jualan", "untuk jualan"
   - "nih bunda", "ala emak", "ala abang2", "ala abang jualan"
   - "part 1", "part 2", "versi x"
   - Kata-kata berlebihan seperti: "dijamin laris manis", "auto nagih", "super enak", "layak jual", "(kalo dijual 5rb sebiji juga rugi)"
   - Emotikon atau tanda baca aneh di judul
2. Format penamaan:
   - Gunakan format Title Case (Huruf Kapital di Awal Tiap Kata Penting).
   - Panjang ideal: 2 - 5 kata.
   - Tetap pertahankan identitas inti makanan (jangan ubah jenis makanan aslinya!).
   - Boleh tonjolkan tekstur / varian rasa / kemasan jika relevan (misal: "Crispy", "Lumer", "Mini Cup", "Pedas Manis", "Bumbu Balado", "Ekonomis").
3. OUTPUT HARUS BERUPA ARRAY JSON murni persis sesuai urutan item input:
[
  { "id": "<id_resep>", "polished_title": "<judul_baru>" }
]

Berikut daftar resep yang harus dipoles:
${JSON.stringify(batchItems, null, 2)}
`;

  let lastErr = null;
  for (const model of GEMINI_MODELS) {
    try {
      const payload = JSON.stringify({
        contents: [{
          parts: [{ text: prompt }]
        }],
        generationConfig: {
          temperature: 0.2,
          responseMimeType: "application/json"
        }
      });

      const res = await makeHttpRequest({
        hostname: 'generativelanguage.googleapis.com',
        path: `/v1beta/models/${model}:generateContent?key=${apiKey}`,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-goog-api-key': apiKey,
          'Content-Length': Buffer.byteLength(payload)
        }
      }, payload);

      if (res.status === 200) {
        const parsed = JSON.parse(res.body);
        const text = parsed.candidates?.[0]?.content?.parts?.[0]?.text;
        if (text) {
          try {
            const resultList = JSON.parse(text);
            if (Array.isArray(resultList)) {
              return { success: true, results: resultList, modelUsed: model };
            }
          } catch(e) {
            const match = text.match(/\[[\s\S]*\]/);
            if (match) {
              return { success: true, results: JSON.parse(match[0]), modelUsed: model };
            }
          }
        }
      } else if (res.status === 429) {
        console.log(`   ⚠️ HTTP 429 Body: ${res.body}`);
        if (retryCount < 2) {
          console.log(`   ⏳ Rate limit terdeteksi. Menunggu 20 detik sebelum mencoba lagi...`);
          await new Promise(r => setTimeout(r, 20000));
          return await callGeminiBatch(apiKey, batchItems, retryCount + 1);
        }
        lastErr = new Error(`HTTP 429: ${res.body.slice(0, 200)}`);
      } else {
        lastErr = new Error(`HTTP ${res.status}: ${res.body.slice(0, 200)}`);
      }
    } catch(err) {
      lastErr = err;
    }
  }

  throw lastErr || new Error('Semua model Gemini gagal merespons.');
}

// Fallback jika tidak ada Gemini API Key: Cloudflare Workers AI Llama
async function callCloudflareBatch(cfToken, cfAccountId, batchItems) {
  const prompt = `Anda adalah kurator nama menu jajanan viral Indonesia. Ubah nama resep mentah ini menjadi nama menu yang bersih, komersial, tanpa kata "ide jualan", "resep jualan", "nih bunda", atau emotikon.
Kembalikan HANYA array JSON:
[{"id": "...", "polished_title": "..."}]

Input:
${JSON.stringify(batchItems, null, 2)}`;

  const payload = JSON.stringify({
    messages: [
      { role: "system", content: "You output only valid JSON arrays." },
      { role: "user", content: prompt }
    ]
  });

  const res = await makeHttpRequest({
    hostname: 'api.cloudflare.com',
    path: `/client/v4/accounts/${cfAccountId}/ai/run/@cf/meta/llama-3.1-8b-instruct`,
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${cfToken}`,
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(payload)
    }
  }, payload);

  if (res.status === 200) {
    const data = JSON.parse(res.body);
    const responseText = data.result?.response || '';
    const match = responseText.match(/\[[\s\S]*\]/);
    if (match) {
      return { success: true, results: JSON.parse(match[0]), modelUsed: 'cloudflare-llama-3.1-8b' };
    }
  }
  throw new Error(`Cloudflare AI Error HTTP ${res.status}: ${res.body.slice(0, 150)}`);
}

async function main() {
  const isDryRun = process.argv.includes('--dry-run');
  const apiKey = getGeminiApiKey();
  const cfConfig = getCloudflareConfig();

  console.log('====================================================');
  console.log('✨ Jajan Cuan — AI Recipe Title Polisher');
  console.log('====================================================\n');

  if (isDryRun) {
    console.log('🔍 MODE DRY-RUN AKTIF: Perubahan hanya ditampilkan, tidak disimpan ke disk.\n');
  }

  if (!apiKey && !cfConfig) {
    console.error('❌ Tidak ditemukan API Key Gemini atau Cloudflare AI di .env!');
    console.log('\nCara Menjalankan:');
    console.log('1. Menggunakan argumen CLI:');
    console.log('   node scripts/polish-titles.js --key <GEMINI_API_KEY>');
    console.log('2. Atau masukkan ke file .env:');
    console.log('   GEMINI_API_KEY=AIzaSy...\n');
    process.exit(1);
  }

  const draftsPath = path.join(__dirname, '..', 'data', 'scraped-drafts.json');
  const webJsPath = path.join(__dirname, '..', 'web', 'js', 'scraped-drafts.js');

  if (!fs.existsSync(draftsPath)) {
    console.error(`❌ File ${draftsPath} tidak ditemukan.`);
    process.exit(1);
  }

  const drafts = JSON.parse(fs.readFileSync(draftsPath, 'utf8'));
  const allList = Object.values(drafts);

  // Ambil resep yang belum pernah dipoles atau ingin di-repolish
  const isForceAll = process.argv.includes('--all') || process.argv.includes('--force');
  const limitIdx = process.argv.indexOf('--limit');
  const limitCount = limitIdx !== -1 && process.argv[limitIdx + 1] ? parseInt(process.argv[limitIdx + 1], 10) : Infinity;

  const targetList = allList.filter(r => {
    if (isForceAll) return true;
    // Jika belum ada original_title atau judul masih mengandung kata-kata khas UGC Cookpad
    const lower = (r.title || '').toLowerCase();
    const hasUgcNoise = lower.includes('ide jualan') || lower.includes('untuk jualan') || lower.includes('nih bunda') || lower.includes('part 1') || lower.includes('resep jualan') || lower.includes('bakulan') || lower.includes('layak jual') || lower.includes('laris manis') || lower.includes('ekonomis untuk jualan');
    return !r.title_polished || hasUgcNoise;
  }).slice(0, limitCount);

  console.log(`📋 Total resep di draft: ${allList.length}`);
  console.log(`🎯 Resep yang memerlukan polesan: ${targetList.length}\n`);

  if (targetList.length === 0) {
    console.log('🎉 Semua judul resep sudah rapi dan terpoles!');
    return;
  }

  const BATCH_SIZE = 10;
  let updatedCount = 0;

  for (let i = 0; i < targetList.length; i += BATCH_SIZE) {
    const chunk = targetList.slice(i, i + BATCH_SIZE);
    const batchPayload = chunk.map(r => ({
      id: r.id,
      current_title: r.title,
      category: r.category,
      key_ingredients: (r.ingredients || []).slice(0, 3).map(ing => ing.name).join(', ')
    }));

    console.log(`🚀 [Batch ${Math.floor(i / BATCH_SIZE) + 1}/${Math.ceil(targetList.length / BATCH_SIZE)}] Memproses ${chunk.length} judul resep...`);

    let response;
    try {
      if (apiKey) {
        response = await callGeminiBatch(apiKey, batchPayload);
      } else if (cfConfig) {
        console.log('   (Menggunakan Cloudflare Llama fallback)');
        response = await callCloudflareBatch(cfConfig.token, cfConfig.accountId, batchPayload);
      }
    } catch(err) {
      console.error(`   ❌ Gagal pada batch ini: ${err.message}`);
      continue;
    }

    if (response && response.results) {
      for (const item of response.results) {
        const recipe = drafts[item.id];
        if (recipe && item.polished_title) {
          const oldTitle = recipe.title;
          const newTitle = item.polished_title.trim();

          console.log(`   📝 "${oldTitle}"`);
          console.log(`      ↳ ✨ "${newTitle}"\n`);

          if (!isDryRun) {
            if (!recipe.original_title) {
              recipe.original_title = oldTitle;
            }
            recipe.title = newTitle;
            recipe.title_polished = true;
            recipe.title_polished_at = new Date().toISOString();

            // Perbarui juga kalimat business analysis jika sebelumnya menempel judul lama
            if (recipe.business_analysis && recipe.business_analysis.market_demand) {
              recipe.business_analysis.market_demand = recipe.business_analysis.market_demand.replace(oldTitle, newTitle);
            }

            drafts[item.id] = recipe;
            updatedCount++;
          }
        }
      }

      if (!isDryRun) {
        fs.writeFileSync(draftsPath, JSON.stringify(drafts, null, 2), 'utf8');
        fs.writeFileSync(webJsPath, 'window.SCRAPED_DRAFTS = ' + JSON.stringify(drafts, null, 2) + ';', 'utf8');
      }
    }

    // Jeda 2 detik antar batch untuk rate safety (15 RPM)
    if (i + BATCH_SIZE < targetList.length) {
      await new Promise(r => setTimeout(r, 2000));
    }
  }

  console.log('====================================================');
  if (isDryRun) {
    console.log(`🔍 DRY RUN SELESAI. Tidak ada file yang diubah.`);
  } else {
    console.log(`🎉 SUKSES! Berhasil memperbarui ${updatedCount} judul resep di scraped-drafts.json.`);
    console.log(`💡 ID & Slug tetap utuh, sehingga semua file foto & link database 100% aman.`);
    console.log(`👉 Buka web app: http://localhost:3000/experience?admin=true untuk melihat hasilnya.`);
  }
  console.log('====================================================\n');
}

main().catch(console.error);
