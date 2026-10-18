const fs = require('fs');
const path = require('path');

const draftsPath = path.join(__dirname, '..', 'data', 'scraped-drafts.json');
const seedPath = path.join(__dirname, '..', 'data', 'seed-posts.json');
const webDraftsPath = path.join(__dirname, '..', 'web', 'js', 'scraped-drafts.js');

const drafts = JSON.parse(fs.readFileSync(draftsPath, 'utf8'));

function classifyCategory(title) {
  const t = title.toLowerCase();

  // 1. KUE, ROTI, COOKIES & DESSERT
  const isKue = /\b(kue|bolu|cake|brownies|brownie|roti|bread|donat|doughnut|pie|pai|nastar|semprit|cookies|cookie|biskuit|puding|pudding|churros|ongol|bugis|talam|lemet|dadar gulung|mochi|bomboloni|sourdough|cinnamon|wingko|dodol|proll|poffertjes|salju|putu|cenil|bika|lapis|klepon|genit|muffin|cupcake|tart|pastry|kurma|kurcok)\b/i.test(t);
  if (isKue) return 'kue';

  // 2. GORENGAN & CAMILAN GURIH / MAKANAN
  const isGorengan = /\b(goreng|gorengan|risol|risoles|pangsit|pastel|cireng|cilok|batagor|siomay|dimsum|bakso|basreng|tahu|tempe|bakwan|lumpia|martabak|sosis|sostel|kroket|karipap|stik|keripik|mustofa|gulung|congkel|takoyaki|gohyong|naga|nugget|otak|pempek|turon|crispy|krispi|geprek|seblak|tteokbokki|topokki|pizza|spaghetti|spageti|mie|kimbab|sushi|ricebowl|rice bowl|nasi|bumbu|asinan|soto|ceker|abon|lele|ayam|daging|bistik|salad|jagung|popia)\b/i.test(t);
  if (isGorengan) return 'gorengan';

  // 3. MINUMAN SEGAR
  const isMinuman = (
    /\b(es|teh|tea|latte|matcha|kopi|coffee|jus|juice|smoothie|boba|jamu|kunyit|kencur|susu|sule|kuwut|cendol|dawet|kolak|sirup|syrup|ice|drink)\b/i.test(t) ||
    t.startsWith('es ') || t.startsWith('es-') || t.includes(' es ') || t.includes('es lilin') || t.includes('es lumut') || t.includes('es kopyor') || t.includes('es cokelat')
  );
  if (isMinuman) return 'minuman';

  return 'gorengan';
}

const viralKeywords = [
  'viral', 'tiktok', 'lumer', 'kekinian', 'hits', 'mochi', 'dimsum', 
  'gacoan', 'lumut', 'gohyong', 'mayo', 'korea', 'lava', 'creamy', 
  'churros', 'bomboloni', 'takoyaki', 'tteokbokki', 'topokki', 'mustofa', 
  'pangsit', 'spaghetti', 'kroket', 'sostel'
];

function isViralRecipe(title) {
  const t = title.toLowerCase();
  return viralKeywords.some(k => t.includes(k));
}

let catStats = { gorengan: 0, kue: 0, minuman: 0 };
let viralCount = 0;
let modalKecilCount = 0;

for (const [slug, item] of Object.entries(drafts)) {
  const newCat = classifyCategory(item.title);
  item.category = newCat;
  catStats[newCat] = (catStats[newCat] || 0) + 1;

  const cap = item.business_analysis?.estimated_initial_capital || 35000;
  const isModalKecil = cap <= 40000; // Modal kecil sesungguhnya (<= 40rb)
  const isViral = isViralRecipe(item.title) || item.title.toLowerCase().includes('viral');

  const tags = [newCat];
  if (isModalKecil) {
    tags.push('modal-kecil');
    modalKecilCount++;
  }
  if (isViral) {
    tags.push('viral');
    viralCount++;
  }
  if (item.title.toLowerCase().includes('ekonomis') || item.title.toLowerCase().includes('1000') || item.title.toLowerCase().includes('2ribu')) {
    tags.push('ekonomis');
  }

  item.tags = tags;
}

// 1. Tulis ke data/scraped-drafts.json
fs.writeFileSync(draftsPath, JSON.stringify(drafts, null, 2), 'utf8');

// 2. Tulis ke data/seed-posts.json
fs.writeFileSync(seedPath, JSON.stringify(drafts, null, 2), 'utf8');

// 3. Tulis ke web/js/scraped-drafts.js
const jsContent = `window.SCRAPED_DRAFTS = ${JSON.stringify(drafts, null, 2)};`;
fs.writeFileSync(webDraftsPath, jsContent, 'utf8');

console.log('✅ Re-klasifikasi selesai!');
console.log('Statistik Kategori Baru:', catStats);
console.log(`- Modal Kecil (<= 40rb): ${modalKecilCount} resep`);
console.log(`- Viral TikTok: ${viralCount} resep`);
console.log(`- Total Resep: ${Object.keys(drafts).length} resep`);
