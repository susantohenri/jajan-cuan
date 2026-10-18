const fs = require('fs');
const path = require('path');
const https = require('https');

// Helper: Cari API Key dari berbagai kemungkinan sumber lokal
function getApiKey() {
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
    if (match && match[1]) return match[1].replace(/['"]/g, '').trim();
  }

  const freeLlmPath = path.join(__dirname, '..', 'freellmapi');
  if (fs.existsSync(freeLlmPath)) {
    const content = fs.readFileSync(freeLlmPath, 'utf8').trim();
    const match = content.match(/AIzaSy[A-Za-z0-9_-]{33}/);
    if (match) return match[0];
    if (content.length > 20 && !content.includes('\n')) return content;
  }

  return null;
}

function getCloudflareConfig() {
  let token = null;
  let accountId = null;

  const tokenIdx = process.argv.indexOf('--cf-token');
  if (tokenIdx !== -1 && process.argv[tokenIdx + 1]) token = process.argv[tokenIdx + 1].trim();

  const accIdx = process.argv.indexOf('--cf-account');
  if (accIdx !== -1 && process.argv[accIdx + 1]) accountId = process.argv[accIdx + 1].trim();

  if (!token) token = process.env.CLOUDFLARE_API_TOKEN || process.env.CF_API_TOKEN || process.env.CF_TOKEN;
  if (!accountId) accountId = process.env.CLOUDFLARE_ACCOUNT_ID || process.env.CF_ACCOUNT_ID || process.env.CF_ACCOUNT;

  const envPath = path.join(__dirname, '..', '.env');
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    if (!token) {
      const match = envContent.match(/(?:CLOUDFLARE_API_TOKEN|CF_API_TOKEN|CF_TOKEN)\s*=\s*([^\r\n]+)/i);
      if (match && match[1]) token = match[1].replace(/['"]/g, '').trim();
    }
    if (!accountId) {
      const match = envContent.match(/(?:CLOUDFLARE_ACCOUNT_ID|CF_ACCOUNT_ID|CF_ACCOUNT)\s*=\s*([^\r\n]+)/i);
      if (match && match[1]) accountId = match[1].replace(/['"]/g, '').trim();
    }
  }

  if (token && accountId) {
    return { token, accountId };
  }
  return null;
}

// Helper download image to Buffer
function downloadImageBuffer(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'Mozilla/5.0' } }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return downloadImageBuffer(res.headers.location).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) {
        return reject(new Error(`Failed to download image: HTTP ${res.statusCode}`));
      }
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => resolve(Buffer.concat(chunks)));
    }).on('error', reject);
  });
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
          buffer,
          body: buffer.toString('utf8')
        });
      });
    });
    req.on('error', reject);
    if (payload) req.write(payload);
    req.end();
  });
}

// Cek daftar model yang tersedia untuk API key ini
async function getAvailableModels(apiKey) {
  try {
    const res = await makeHttpRequest({
      hostname: 'generativelanguage.googleapis.com',
      path: `/v1beta/models?key=${apiKey}`,
      method: 'GET'
    });
    const json = JSON.parse(res.body);
    if (json.models) {
      return json.models.map(m => m.name.replace('models/', ''));
    }
  } catch(e) {}
  return [];
}

function extractErrorMsg(body) {
  try {
    const json = JSON.parse(body);
    if (json.error && json.error.message) return json.error.message;
    if (json.message) return json.message;
  } catch(e) {}
  return body ? body.substring(0, 100).replace(/\s+/g, ' ') : 'Unknown error';
}

// Call Google AI Studio / Gemini Image Polish
async function callGeminiImagePolish(apiKey, base64Image, recipe) {
  const title = recipe.title || 'Indonesian street food';
  const portions = recipe.default_portions || 20;

  const prompt = `Commercial food photography retouch of this exact Indonesian street food: "${title}".
CRITICAL GROUNDING RULES:
1. PRESERVE ORIGINAL FOOD & PACKAGING: Keep the exact food items, portion size (~${portions} pcs), and container type (e.g. clear plastic mika box, cup, or street snack wrapper) intact. DO NOT convert it into a luxury fine-dining ceramic plate.
2. ENHANCE VISUAL QUALITY: Apply warm commercial studio lighting, crisp texture highlights, and vibrant appetizing food colors.
3. CLEAN BACKGROUND: Place it neatly on a clean minimalist wooden cafe tabletop.
4. Professional commercial advertising look, authentic, delicious street food presentation.`;

  const lastErrors = [];

  // METODE 1: Google GenAI generateContent (gemini-3.1-flash-image & gemini-2.5-flash-image)
  const candidateModels = ['gemini-3.1-flash-image', 'gemini-2.5-flash-image'];
  for (const model of candidateModels) {
    try {
      const payload = JSON.stringify({
        contents: [{
          parts: [
            { text: prompt },
            {
              inlineData: {
                mimeType: "image/jpeg",
                data: base64Image
              }
            }
          ]
        }],
        generationConfig: {
          responseModalities: ["IMAGE", "TEXT"]
        }
      });

      const res = await makeHttpRequest({
        hostname: 'generativelanguage.googleapis.com',
        path: `/v1beta/models/${model}:generateContent?key=${apiKey}`,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload)
        }
      }, payload);

      if (res.status === 200) {
        const data = JSON.parse(res.body);
        for (const candidate of (data.candidates || [])) {
          for (const part of (candidate.content?.parts || [])) {
            if (part.inlineData && part.inlineData.data) {
              return Buffer.from(part.inlineData.data, 'base64');
            }
          }
        }
      } else {
        lastErrors.push(`${model} generateContent: HTTP ${res.status} (${extractErrorMsg(res.body)})`);
      }
    } catch(err) {
      lastErrors.push(`${model} generateContent: ${err.message}`);
    }
  }

  // METODE 2: Native Interactions API (gemini-3.1-flash-image)
  try {
    const payload = JSON.stringify({
      model: "gemini-3.1-flash-image",
      input: [
        { type: "text", text: prompt },
        { type: "image", data: base64Image, mime_type: "image/jpeg" }
      ]
    });

    const res = await makeHttpRequest({
      hostname: 'generativelanguage.googleapis.com',
      path: `/v1beta/interactions?key=${apiKey}`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-goog-api-key': apiKey,
        'Content-Length': Buffer.byteLength(payload)
      }
    }, payload);

    if (res.status === 200) {
      const data = JSON.parse(res.body);
      if (data.output_image && data.output_image.data) {
        return Buffer.from(data.output_image.data, 'base64');
      }
    } else {
      lastErrors.push(`Interactions API: HTTP ${res.status} (${extractErrorMsg(res.body)})`);
    }
  } catch(e) {
    lastErrors.push(`Interactions API: ${e.message}`);
  }

  // METODE 3: OpenAI Compatible Endpoint (gemini-2.5-flash-image)
  try {
    const payload = JSON.stringify({
      model: "gemini-2.5-flash-image",
      prompt: prompt,
      response_format: "b64_json",
      n: 1
    });

    const res = await makeHttpRequest({
      hostname: 'generativelanguage.googleapis.com',
      path: `/v1beta/openai/images/generations`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`,
        'Content-Length': Buffer.byteLength(payload)
      }
    }, payload);

    if (res.status === 200) {
      const data = JSON.parse(res.body);
      if (data.data && data.data[0] && data.data[0].b64_json) {
        return Buffer.from(data.data[0].b64_json, 'base64');
      }
    } else {
      lastErrors.push(`OpenAI endpoint: HTTP ${res.status} (${extractErrorMsg(res.body)})`);
    }
  } catch(e) {
    lastErrors.push(`OpenAI endpoint: ${e.message}`);
  }

  // METODE 4: imagen-3.0-generate-002:predict
  try {
    const payload = JSON.stringify({
      instances: [{ prompt: prompt }],
      parameters: { sampleCount: 1, aspectRatio: "1:1" }
    });

    const res = await makeHttpRequest({
      hostname: 'generativelanguage.googleapis.com',
      path: `/v1beta/models/imagen-3.0-generate-002:predict?key=${apiKey}`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(payload)
      }
    }, payload);

    if (res.status === 200) {
      const data = JSON.parse(res.body);
      if (data.predictions && data.predictions[0] && data.predictions[0].bytesBase64Encoded) {
        return Buffer.from(data.predictions[0].bytesBase64Encoded, 'base64');
      }
    } else {
      lastErrors.push(`Imagen endpoint: HTTP ${res.status} (${extractErrorMsg(res.body)})`);
    }
  } catch(e) {
    lastErrors.push(`Imagen endpoint: ${e.message}`);
  }

  // Jika semua metode di atas gagal, periksa model yang didukung API key ini
  const models = await getAvailableModels(apiKey);
  const imageModels = models.filter(m => m.includes('image') || m.includes('imagen'));

  let errMsg = `Model generasi gambar tidak tersedia pada API Key ini.\n`;
  if (lastErrors.length > 0) {
    errMsg += `   Detail respon API:\n   * ` + lastErrors.join('\n   * ') + '\n';
  }
  if (imageModels.length === 0) {
    errMsg += `   ℹ️ Akun Anda terdeteksi berada di Free Tier (Tier Gratis).\n`;
    errMsg += `   Berdasarkan ketentuan Google AI Studio, model generasi gambar (Nano Banana / Imagen) hanya aktif pada akun dengan billing aktif (Paid Tier / Pay-as-you-go).\n`;
    errMsg += `   💡 Solusi Tanpa Billing: Anda dapat menjalankan dengan opsi '--use-source' atau '--fallback-source' untuk langsung menggunakan foto asli Cookpad beresolusi tinggi tanpa biaya!`;
  }
  throw new Error(errMsg);
}

// Call Cloudflare Workers AI Image Polish (Flux-1-schnell / SDXL)
async function callCloudflareImagePolish(cfToken, cfAccount, recipe) {
  const title = recipe.title || 'Indonesian street food';
  const portions = recipe.default_portions || 20;

  const prompt = `Commercial food photography retouch of Indonesian street food: "${title}". Portion size (~${portions} pcs) in clean packaging, appetizing crisp texture, warm commercial studio lighting, clean cafe wooden table background, 8k resolution, authentic and delicious advertising food photo.`;

  const models = [
    '@cf/black-forest-labs/flux-1-schnell',
    '@cf/bytedance/stable-diffusion-xl-lightning',
    '@cf/stabilityai/stable-diffusion-xl-base-1.0'
  ];

  let lastErr = null;
  for (const model of models) {
    try {
      const payload = JSON.stringify({
        prompt: prompt,
        num_steps: 4
      });

      const res = await makeHttpRequest({
        hostname: 'api.cloudflare.com',
        path: `/client/v4/accounts/${cfAccount}/ai/run/${model}`,
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${cfToken}`,
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload)
        }
      }, payload);

      if (res.status === 200) {
        const contentType = res.headers['content-type'] || '';
        if (contentType.startsWith('image/') || (res.buffer && (res.buffer[0] === 0xFF || res.buffer[0] === 0x89))) {
          return res.buffer;
        }

        const json = JSON.parse(res.body);
        if (json.result && json.result.image) {
          return Buffer.from(json.result.image, 'base64');
        }
      } else {
        lastErr = new Error(`Cloudflare (${model}) HTTP ${res.status}: ${res.body.slice(0, 100)}`);
      }
    } catch (err) {
      lastErr = err;
    }
  }

  throw lastErr || new Error('Gagal memproses gambar via Cloudflare Workers AI.');
}

async function main() {
  const isUseSource = process.argv.includes('--use-source');
  const isFallbackSource = process.argv.includes('--fallback-source');
  const cfConfig = getCloudflareConfig();
  const apiKey = getApiKey();
  const slugTarget = process.argv[2];

  console.log('====================================================');
  console.log('🎨 Jajan Cuan — AI Food Image Polisher (Anti-Halusinasi)');
  console.log('====================================================\n');

  if (!isUseSource && !apiKey && !cfConfig) {
    console.error('❌ Tidak ditemukan API Key / Provider AI!');
    console.log('\nPilihan Cara Menjalankan:');
    console.log('1. Cloudflare Workers AI (GRATIS 10.000 Neurons/hari — Model FLUX.1-schnell):');
    console.log('   node scripts/polish-image.js --all --cf-token <API_TOKEN> --cf-account <ACCOUNT_ID>\n');
    console.log('2. Google AI Studio (Paid Tier):');
    console.log('   node scripts/polish-image.js --all --key <GEMINI_API_KEY>\n');
    console.log('3. Gunakan Foto Asli Cookpad (100% GRATIS tanpa butuh API):');
    console.log('   node scripts/polish-image.js --all --use-source\n');
    process.exit(1);
  }

  const draftsPath = path.join(__dirname, '..', 'data', 'scraped-drafts.json');
  const webJsPath = path.join(__dirname, '..', 'web', 'js', 'scraped-drafts.js');

  if (!fs.existsSync(draftsPath)) {
    console.error(`❌ File draft ${draftsPath} tidak ditemukan. Jalankan scraper terlebih dahulu.`);
    process.exit(1);
  }

  const drafts = JSON.parse(fs.readFileSync(draftsPath, 'utf8'));

  const isBatchMode = process.argv.includes('--all') || process.argv.includes('--batch');
  const limitIdx = process.argv.indexOf('--limit');
  const limitCount = limitIdx !== -1 && process.argv[limitIdx + 1] ? parseInt(process.argv[limitIdx + 1], 10) : Infinity;

  let targets = [];
  if (isBatchMode) {
    targets = Object.values(drafts).filter(r => {
      const hasSource = Boolean(r.media?.source_photo);
      const notPolishedYet = !r.media?.ai_polished_photo;
      return hasSource && notPolishedYet;
    }).slice(0, limitCount);

    console.log(`🚀 MODE BATCH AKTIF: Ditemukan ${targets.length} resep yang siap diproses.\n`);
  } else if (slugTarget && !slugTarget.startsWith('--')) {
    const r = drafts[slugTarget];
    if (!r) {
      console.error(`❌ Resep dengan slug "${slugTarget}" tidak ditemukan di draft!`);
      process.exit(1);
    }
    targets = [r];
  } else {
    console.log('💡 CARA PAKAI:');
    console.log('1. Poles via Cloudflare Workers AI (FLUX.1-schnell GRATIS):');
    console.log('   node scripts/polish-image.js --batch --limit 5 --cf-token <TOKEN> --cf-account <ACCOUNT_ID>\n');
    console.log('2. Poles via Google AI Studio:');
    console.log('   node scripts/polish-image.js --batch --limit 5 --key <API_KEY>\n');
    console.log('3. Poles dengan otomatis Fallback ke foto asli Cookpad jika kuota habis:');
    console.log('   node scripts/polish-image.js --batch --limit 5 --fallback-source --key <API_KEY>\n');
    console.log('4. Gunakan foto asli Cookpad tanpa butuh API (GRATIS & CEPAT):');
    console.log('   node scripts/polish-image.js --all --use-source');
    console.log('   node scripts/polish-image.js --batch --limit 5 --use-source\n');
    return;
  }

  const outDir = path.join(__dirname, '..', 'web', 'img', 'recipes', 'polished');
  if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true });

  let successCount = 0;
  for (let i = 0; i < targets.length; i++) {
    const recipe = targets[i];
    const sourcePhoto = recipe.media?.source_photo;
    if (!sourcePhoto) continue;

    console.log(`[${i + 1}/${targets.length}] 📌 Memproses: "${recipe.title}"`);
    console.log(`   📷 Mengunduh foto asli Cookpad...`);

    try {
      const imgBuffer = await downloadImageBuffer(sourcePhoto);

      let finalBuffer = null;
      let usedMode = 'ai';

      if (isUseSource) {
        console.log(`   📸 Mode '--use-source' aktif: Menggunakan foto asli Cookpad berkualitas tinggi...`);
        finalBuffer = imgBuffer;
        usedMode = 'source';
      } else if (cfConfig) {
        console.log(`   ⚡ Memoles via Cloudflare Workers AI (FLUX.1-schnell)...`);
        try {
          finalBuffer = await callCloudflareImagePolish(cfConfig.token, cfConfig.accountId, recipe);
          usedMode = 'cloudflare_flux';
        } catch (cfErr) {
          if (isFallbackSource) {
            console.warn(`   ⚠️ Cloudflare AI gagal: ${cfErr.message}`);
            console.log(`   🔄 Fallback aktif (--fallback-source): Otomatis memakai foto asli Cookpad...`);
            finalBuffer = imgBuffer;
            usedMode = 'source_fallback';
          } else {
            throw cfErr;
          }
        }
      } else {
        console.log(`   ✨ Memoles via Google AI Studio (Grounding Retouch)...`);
        try {
          const base64Data = imgBuffer.toString('base64');
          finalBuffer = await callGeminiImagePolish(apiKey, base64Data, recipe);
          usedMode = 'gemini';
        } catch (aiErr) {
          if (isFallbackSource) {
            console.warn(`   ⚠️ AI Polish tidak tersedia: ${aiErr.message.split('\n')[0]}`);
            console.log(`   🔄 Fallback aktif (--fallback-source): Otomatis memakai foto asli Cookpad...`);
            finalBuffer = imgBuffer;
            usedMode = 'source_fallback';
          } else {
            throw aiErr;
          }
        }
      }

      const outFileName = `${recipe.slug}.jpg`;
      const outFilePath = path.join(outDir, outFileName);
      fs.writeFileSync(outFilePath, finalBuffer);

      const relativeWebPath = `img/recipes/polished/${outFileName}`;
      recipe.media.ai_polished_photo = relativeWebPath;
      recipe.media.status = usedMode === 'source' ? 'approved' : 'ready_for_review';
      recipe.media.mode = usedMode;
      recipe.media.polished_at = new Date().toISOString();

      drafts[recipe.slug] = recipe;
      successCount++;
      console.log(`   ✅ Selesai -> ${outFileName} [${usedMode}]`);

      fs.writeFileSync(draftsPath, JSON.stringify(drafts, null, 2), 'utf8');
      fs.writeFileSync(webJsPath, 'window.SCRAPED_DRAFTS = ' + JSON.stringify(drafts, null, 2) + ';', 'utf8');

      if (i < targets.length - 1) {
        console.log(`   ⏳ Jeda 1 detik sebelum resep berikutnya...\n`);
        await new Promise(r => setTimeout(r, 1000));
      }
    } catch(err) {
      console.error(`   ❌ Gagal memproses "${recipe.title}":`, err.message);
    }
  }

  console.log(`\n====================================================`);
  console.log(`🎉 PROSES SELESAI! Berhasil memproses: ${successCount} resep.`);
  console.log(`Buka web app Anda untuk review: http://localhost:3000/experience?admin=true`);
  console.log(`====================================================\n`);
}

main().catch(console.error);
