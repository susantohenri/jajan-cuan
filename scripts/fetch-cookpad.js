const fs = require('fs');
const path = require('path');
const https = require('https');

// Load ingredient dictionary
const dictPath = path.join(__dirname, 'ingredient-dictionary.json');
let ingredientDict = { keywords: {}, default_item_fallback: 2000 };
if (fs.existsSync(dictPath)) {
  ingredientDict = JSON.parse(fs.readFileSync(dictPath, 'utf8'));
}

function slugify(text) {
  return text
    .toString()
    .toLowerCase()
    .trim()
    .replace(/[^\w\s-]/g, '')
    .replace(/[\s_-]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

function detectPrice(name) {
  const lower = name.toLowerCase();
  for (const [kw, price] of Object.entries(ingredientDict.keywords)) {
    if (lower.includes(kw)) {
      return price;
    }
  }
  return ingredientDict.default_item_fallback || 2000;
}

function detectCategory(title, ingredients) {
  const combined = (title + ' ' + ingredients.join(' ')).toLowerCase();
  if (combined.includes('es ') || combined.includes('teh ') || combined.includes('minuman') || combined.includes('latte') || combined.includes('jelly') || combined.includes('thai tea') || combined.includes('jamu') || combined.includes('kunyit')) {
    return 'minuman';
  }
  if (combined.includes('kue') || combined.includes('bolu') || combined.includes('pie') || combined.includes('brownies') || combined.includes('cake') || combined.includes('roti') || combined.includes('bread') || combined.includes('nastar') || combined.includes('ongol') || combined.includes('bugis')) {
    return 'kue';
  }
  if (combined.includes('goreng') || combined.includes('sostel') || combined.includes('sosis') || combined.includes('pangsit') || combined.includes('risol') || combined.includes('batagor') || combined.includes('turon') || combined.includes('stik') || combined.includes('mustofa')) {
    return 'gorengan';
  }
  return 'jajanan-sekolah';
}

function fetchUrl(url) {
  return new Promise((resolve, reject) => {
    const options = {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Language': 'id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7'
      }
    };
    https.get(url, options, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        let redirectUrl = res.headers.location;
        if (!redirectUrl.startsWith('http')) {
          redirectUrl = 'https://cookpad.com' + redirectUrl;
        }
        return fetchUrl(redirectUrl).then(resolve).catch(reject);
      }
      if (res.statusCode !== 200) {
        return reject(new Error(`HTTP status ${res.statusCode}`));
      }
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => resolve(body));
    }).on('error', reject);
  });
}

function parseRecipeHtml(html, sourceUrl = '') {
  const jsonLdMatch = html.match(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/gi);
  if (!jsonLdMatch) return null;

  let recipeData = null;
  for (const block of jsonLdMatch) {
    const raw = block.replace(/<\/?script[^>]*>/gi, '');
    try {
      const data = JSON.parse(raw);
      if (data['@type'] === 'Recipe') {
        recipeData = data;
        break;
      } else if (Array.isArray(data)) {
        const found = data.find(d => d['@type'] === 'Recipe');
        if (found) { recipeData = found; break; }
      }
    } catch(e) {}
  }

  if (!recipeData) return null;

  const title = (recipeData.name || '').trim();
  const slug = slugify(title);
  
  // Extract Portions (Yield)
  const yieldText = recipeData.recipeYield ? String(recipeData.recipeYield) : '20 porsi';
  const matchNum = yieldText.match(/\d+/);
  const defaultPortions = matchNum ? parseInt(matchNum[0], 10) : 20;

  // Extract Ingredients with pricing
  const rawIngredients = recipeData.recipeIngredient || [];
  const ingredients = rawIngredients.map(item => {
    const text = item.replace(/\s+/g, ' ').trim();
    let name = text;
    let quantity = '';
    const parts = text.match(/^([\d\/\.,\s]+(?:gr|gram|kg|buah|butir|siung|sdm|sdt|centong|mika|bungkus|lembar|batang|ml|liter)?)(.*)$/i);
    if (parts && parts[1] && parts[2]) {
      quantity = parts[1].trim();
      name = parts[2].replace(/^[\s-]+/, '').trim();
    } else {
      quantity = 'secukupnya';
    }
    const default_price = detectPrice(name || text);
    return {
      name: name || text,
      raw_text: text,
      quantity: quantity || 'secukupnya',
      default_price: default_price
    };
  });

  const estimatedCapital = ingredients.reduce((sum, item) => sum + (item.default_price || 0), 0);

  // Extract Steps
  const rawSteps = recipeData.recipeInstructions || [];
  const cookingSteps = rawSteps.map(step => {
    if (typeof step === 'string') return step.trim();
    return (step.text || '').trim();
  }).filter(Boolean);

  const sourceImage = recipeData.image || '';
  const category = detectCategory(title, rawIngredients);

  const tags = [category, 'modal-kecil', 'viral'];
  if (title.toLowerCase().includes('2ribu') || title.toLowerCase().includes('1000') || title.toLowerCase().includes('ekonomis')) {
    tags.push('ekonomis');
  }

  return {
    id: slug,
    slug: slug,
    title: title,
    source_url: sourceUrl,
    excerpt: `Resep ide jualan modal ~Rp ${estimatedCapital.toLocaleString('id-ID')}, hasil ${defaultPortions} porsi (${yieldText}).`,
    category: category,
    tags: tags,
    published: true,
    created_at: new Date().toISOString(),
    yield_text: yieldText,
    default_portions: defaultPortions,
    business_analysis: {
      market_demand: `Tinggi — ${title} memiliki target pasar jelas di kalangan anak sekolah, mahasiswa, dan lingkungan perumahan.`,
      ease_of_making: 'Mudah — dapat diproduksi dalam skala rumahan dengan peralatan standar dapur.',
      ingredient_availability: 'Sangat mudah — seluruh bahan baku dapat ditemukan di toko kelontong atau pasar tradisional.',
      estimated_initial_capital: estimatedCapital
    },
    ingredients: ingredients,
    cooking_steps: cookingSteps,
    media: {
      source_photo: sourceImage,
      ai_polished_photo: "",
      status: "pending_review",
      notes: "Foto asli Cookpad sebagai acuan grounding wadah/porsi. Belum melalui polesan AI."
    },
    promo_images: []
  };
}

// MAIN CONTROLLER
async function main() {
  const outputPath = path.join(__dirname, '..', 'data', 'scraped-drafts.json');
  const webJsPath = path.join(__dirname, '..', 'web', 'js', 'scraped-drafts.js');

  // Load existing drafts to merge
  let existingDrafts = {};
  if (fs.existsSync(outputPath)) {
    try {
      existingDrafts = JSON.parse(fs.readFileSync(outputPath, 'utf8'));
    } catch(e) {}
  }

  const initialCount = Object.keys(existingDrafts).length;
  console.log(`📂 Draft saat ini memuat: ${initialCount} resep.\n`);

  // Target URLs: dari argumen CLI (bisa URL resep, URL pencarian, atau file urls.txt)
  let targetUrls = [];
  const cliArg = process.argv[2];

  if (cliArg && cliArg.startsWith('http')) {
    // KASUS A: Pengguna memasukkan URL PENCARIAN (misal: https://cookpad.com/id/cari/ide%20jualan)
    if (cliArg.includes('/cari/')) {
      console.log(`🔍 Terdeteksi URL Halaman Pencarian: ${cliArg}`);
      console.log(`⏳ Mengambil daftar link resep dari hasil pencarian Cookpad...`);
      try {
        const pagesIdx = process.argv.indexOf('--pages');
        let searchPagesToScrape = 5; // default 5 jika tidak dispesifikasikan
        if (pagesIdx !== -1 && process.argv[pagesIdx + 1]) {
          const parsedPages = parseInt(process.argv[pagesIdx + 1], 10);
          if (!isNaN(parsedPages) && parsedPages > 0) {
            searchPagesToScrape = parsedPages;
          }
        }
        console.log(`📑 Menargetkan ${searchPagesToScrape} halaman pencarian...`);

        const discovered = new Set();
        
        for (let p = 1; p <= searchPagesToScrape; p++) {
          let pageUrl = cliArg;
          try {
            const parsedUrl = new URL(cliArg);
            parsedUrl.searchParams.set('page', p);
            pageUrl = parsedUrl.toString();
          } catch(e) {
            pageUrl = cliArg.includes('?') ? `${cliArg}&page=${p}` : `${cliArg}?page=${p}`;
          }
          console.log(`   📄 Membaca halaman pencarian ke-${p}: ${pageUrl}`);
          const searchHtml = await fetchUrl(pageUrl);
          
          const regex = /href="\/id\/resep\/(\d+)"/g;
          let m;
          while ((m = regex.exec(searchHtml)) !== null) {
            const recipeUrl = `https://cookpad.com/id/resep/${m[1]}`;
            discovered.add(recipeUrl);
          }
          await new Promise(r => setTimeout(r, 1000));
        }

        targetUrls = Array.from(discovered);
        console.log(`🎯 Berhasil mengekstrak ${targetUrls.length} tautan resep unik dari hasil pencarian!\n`);
      } catch(err) {
        console.error(`❌ Gagal mengambil halaman pencarian:`, err.message);
        if (err.message.includes('ENOTFOUND')) {
          console.log('\n💡 CATATAN JARINGAN: Jika Anda menjalankan perintah ini di lingkungan sandbox, jalankan langsung di Terminal / PowerShell komputer Anda:');
          console.log(`   node scripts/fetch-cookpad.js "${cliArg}"\n`);
          return;
        }
      }
    } else {
      // KASUS B: Pengguna memasukkan 1 URL resep spesifik
      targetUrls = [cliArg.trim()];
      console.log(`🎯 Menargetkan 1 resep dari input: ${cliArg}`);
    }
  } else {
    // KASUS C: Membaca dari scripts/urls.txt
    const urlsFile = path.join(__dirname, 'urls.txt');
    if (fs.existsSync(urlsFile)) {
      targetUrls = fs.readFileSync(urlsFile, 'utf8')
        .split('\n')
        .map(u => u.trim())
        .filter(u => u.startsWith('http'));
      console.log(`📋 Membaca ${targetUrls.length} URL dari scripts/urls.txt`);
    }
  }

  if (targetUrls.length === 0) {
    console.log('💡 CARA PAKAI SCRAPER CEPAT:');
    console.log('1. Langsung dari URL Pencarian (OTOMATIS):');
    console.log('   node scripts/fetch-cookpad.js "https://cookpad.com/id/cari/ide%20jualan"');
    console.log('2. Scrape 1 resep:');
    console.log('   node scripts/fetch-cookpad.js https://cookpad.com/id/resep/xxxx');
    console.log('3. Scrape batch dari file: Masukkan list URL ke scripts/urls.txt, lalu jalankan:');
    console.log('   node scripts/fetch-cookpad.js\n');
    return;
  }

  let newlyAdded = 0;
  for (let i = 0; i < targetUrls.length; i++) {
    const url = targetUrls[i];
    
    // Cek apakah URL sudah pernah di-scrape
    const alreadyExists = Object.values(existingDrafts).some(r => r.source_url === url);
    if (alreadyExists) {
      console.log(`⏩ [${i+1}/${targetUrls.length}] Lewati (sudah ada): ${url}`);
      continue;
    }

    console.log(`⏳ [${i+1}/${targetUrls.length}] Mengambil: ${url} ...`);
    try {
      const html = await fetchUrl(url);
      const parsed = parseRecipeHtml(html, url);
      if (parsed) {
        existingDrafts[parsed.slug] = parsed;
        newlyAdded++;
        console.log(`   ✅ BERHASIL: "${parsed.title}" (Porsi: ${parsed.default_portions}, Modal: Rp ${parsed.business_analysis.estimated_initial_capital})`);
      } else {
        console.log(`   ⚠️ Tidak ditemukan data Recipe Schema di URL ini.`);
      }
      // Delay sopan agar tidak terkena rate limit
      await new Promise(r => setTimeout(r, 1500));
    } catch(err) {
      console.error(`   ❌ Gagal mengambil ${url}:`, err.message);
      if (err.message.includes('ENOTFOUND')) {
        console.log('\n💡 CATATAN JARINGAN: Jika Anda menjalankan perintah ini di lingkungan sandbox, jalankan langsung di Terminal / PowerShell komputer Anda:');
        console.log(`   node scripts/fetch-cookpad.js "${url}"\n`);
        break;
      }
    }
  }

  // Simpan hasil merge
  fs.writeFileSync(outputPath, JSON.stringify(existingDrafts, null, 2), 'utf8');
  fs.writeFileSync(webJsPath, 'window.SCRAPED_DRAFTS = ' + JSON.stringify(existingDrafts, null, 2) + ';', 'utf8');

  console.log(`\n🎉 SELESAI!`);
  console.log(`- Berhasil menambahkan: ${newlyAdded} resep baru.`);
  console.log(`- Total resep di draft: ${Object.keys(existingDrafts).length} resep.`);
  console.log(`- Tersimpan di:`);
  console.log(`  1. ${outputPath}`);
  console.log(`  2. ${webJsPath}`);
  console.log(`\nSilakan refresh web app di browser: http://localhost:3000/experience?admin=true`);
}

main().catch(console.error);
