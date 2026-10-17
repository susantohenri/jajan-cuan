// Jajan Cuan - Experience Page Logic (RTDB Fetch, Filter, Accordion, Interactive HPP Calculator)

// --- SEED DATA FALLBACK (Persis seperti data/seed-posts.json) ---
const SEED_DATA = [
  {
    "id": "pangsit-goreng-viral",
    "title": "Pangsit Goreng Viral",
    "slug": "pangsit-goreng-viral",
    "excerpt": "Modal Rp 30.000, bisa jual 50 biji. Laris di depan sekolah & pasar!",
    "category": "gorengan",
    "tags": ["gorengan", "viral", "modal-kecil"],
    "published": true,
    "emoji": "🥟",
    "business_analysis": {
      "market_demand": "Tinggi — jajanan gorengan selalu dicari, terutama di area sekolah, kantor, dan pasar tradisional.",
      "ease_of_making": "Sangat mudah — tidak perlu oven, cukup wajan dan minyak goreng.",
      "ingredient_availability": "Semua bahan tersedia di warung/toko kelontong terdekat.",
      "estimated_initial_capital": 30000
    },
    "ingredients": [
      { "name": "Kulit pangsit", "quantity": "1 bungkus (50 lembar)", "default_price": 8000 },
      { "name": "Daging ayam giling", "quantity": "250 gram", "default_price": 12000 },
      { "name": "Bawang putih", "quantity": "3 siung", "default_price": 1000 },
      { "name": "Kecap asin", "quantity": "1 sdm", "default_price": 500 },
      { "name": "Merica bubuk", "quantity": "1/2 sdt", "default_price": 500 },
      { "name": "Minyak goreng", "quantity": "500 ml", "default_price": 7000 },
      { "name": "Garam", "quantity": "secukupnya", "default_price": 500 },
      { "name": "Daun bawang", "quantity": "2 batang", "default_price": 500 }
    ],
    "default_portions": 50,
    "cooking_steps": [
      "Campurkan daging ayam giling, bawang putih cincang halus, kecap asin, merica, garam, dan daun bawang iris.",
      "Aduk rata hingga bumbu meresap sempurna.",
      "Ambil selembar kulit pangsit, letakkan 1 sendok teh adonan di tengah.",
      "Lipat kulit pangsit membentuk segitiga, rekatkan pinggiran dengan air.",
      "Panaskan minyak goreng dengan api sedang.",
      "Goreng pangsit hingga kecokelatan dan renyah — sekitar 3-4 menit.",
      "Angkat dan tiriskan di atas kertas minyak."
    ]
  },
  {
    "id": "kue-bolu-keju",
    "title": "Kue Bolu Keju Lumer",
    "slug": "kue-bolu-keju",
    "excerpt": "Resep cepat dan layak jual — bolu lembut dengan keju yang meleleh di setiap gigitan.",
    "category": "kue",
    "tags": ["kue", "viral", "manis"],
    "published": true,
    "emoji": "🧀",
    "business_analysis": {
      "market_demand": "Tinggi — kue bolu keju viral di TikTok dan IG Reels, terutama untuk hampers dan oleh-oleh.",
      "ease_of_making": "Sedang — perlu mixer dan oven/panci kukus.",
      "ingredient_availability": "Mudah — semua bahan ada di supermarket atau toko bahan kue.",
      "estimated_initial_capital": 45000
    },
    "ingredients": [
      { "name": "Telur", "quantity": "4 butir", "default_price": 8000 },
      { "name": "Gula pasir", "quantity": "150 gram", "default_price": 3000 },
      { "name": "Tepung terigu", "quantity": "150 gram", "default_price": 3000 },
      { "name": "Margarin (lelehkan)", "quantity": "100 gram", "default_price": 4000 },
      { "name": "Susu bubuk", "quantity": "2 sdm", "default_price": 3000 },
      { "name": "Keju cheddar parut", "quantity": "100 gram", "default_price": 12000 },
      { "name": "Emulsifier/SP", "quantity": "1 sdt", "default_price": 2000 },
      { "name": "Vanili", "quantity": "1 sdt", "default_price": 1000 }
    ],
    "default_portions": 20,
    "cooking_steps": [
      "Kocok telur, gula, dan emulsifier (SP) dengan mixer kecepatan tinggi hingga kental berjejak — sekitar 10 menit.",
      "Masukkan tepung terigu dan susu bubuk yang sudah diayak. Aduk balik perlahan.",
      "Tuang margarin leleh, aduk rata dari bawah ke atas.",
      "Tambahkan sebagian keju parut, sisakan untuk topping.",
      "Tuang adonan ke dalam loyang yang sudah diolesi margarin dan ditaburi tepung.",
      "Taburi sisa keju parut di atas permukaan adonan.",
      "Panggang di oven suhu 170°C selama 30–35 menit atau kukus 40 menit hingga matang.",
      "Keluarkan dari oven, biarkan dingin, potong sesuai porsi."
    ]
  },
  {
    "id": "risol-mayo-lumer",
    "title": "Risol Mayo Lumer",
    "slug": "risol-mayo-lumer",
    "excerpt": "Jajanan hits anak muda — kulit renyah, isian lumer mayo + smoked beef. Modal kecil, untung besar!",
    "category": "gorengan",
    "tags": ["gorengan", "viral", "modal-kecil"],
    "published": true,
    "emoji": "🌯",
    "business_analysis": {
      "market_demand": "Sangat tinggi — risol mayo jadi jajanan No.1 di kantin sekolah & cafe kekinian.",
      "ease_of_making": "Mudah — kulit bisa beli jadi, isian tinggal campur.",
      "ingredient_availability": "Mudah — supermarket & minimarket.",
      "estimated_initial_capital": 40000
    },
    "ingredients": [
      { "name": "Kulit risol (beli jadi)", "quantity": "20 lembar", "default_price": 10000 },
      { "name": "Smoked beef", "quantity": "100 gram", "default_price": 10000 },
      { "name": "Mayonaise", "quantity": "100 gram", "default_price": 5000 },
      { "name": "Keju mozarella", "quantity": "100 gram", "default_price": 8000 },
      { "name": "Tepung panir", "quantity": "100 gram", "default_price": 3000 },
      { "name": "Telur (untuk coating)", "quantity": "2 butir", "default_price": 4000 },
      { "name": "Minyak goreng", "quantity": "500 ml", "default_price": 7000 }
    ],
    "default_portions": 20,
    "cooking_steps": [
      "Iris smoked beef tipis, tumis sebentar dengan sedikit minyak.",
      "Letakkan kulit risol di meja rata. Taruh smoked beef, keju mozarella, dan mayonaise secukupnya.",
      "Lipat kulit risol membentuk amplop rapat.",
      "Celupkan risol ke telur kocok, lalu gulingkan di tepung panir hingga rata.",
      "Goreng dalam minyak panas (deep fry) dengan api sedang hingga kuning keemasan — sekitar 3 menit.",
      "Angkat, tiriskan, dan sajikan selagi panas agar keju masih lumer."
    ]
  }
];

// Helper: Format Rupiah
function formatRupiah(num) {
  return 'Rp ' + Math.round(num).toLocaleString('id-ID');
}

// Global active recipes state
let allRecipes = [];

// --- MAIN INIT ---
document.addEventListener('DOMContentLoaded', () => {
  initWaitlistModal();
  initFilters();
  loadPosts();
});

// Helper: Fetch dengan timeout fallback agar UI tidak pernah stuck loading
function fetchWithTimeout(promise, ms = 3000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      reject(new Error('Firebase fetch timeout'));
    }, ms);
    promise
      .then(res => {
        clearTimeout(timer);
        resolve(res);
      })
      .catch(err => {
        clearTimeout(timer);
        reject(err);
      });
  });
}

// --- DATA FETCHING (RTDB + Fallback) ---
async function loadPosts() {
  try {
    if (typeof db !== 'undefined' && db) {
      // Baca langsung node posts (sama seperti tester seed-database.html) dengan timeout 3 detik
      const snapshot = await fetchWithTimeout(db.ref('posts').once('value'), 3000);
      const data = snapshot.val();
      if (data && Object.keys(data).length > 0) {
        allRecipes = Object.keys(data)
          .map(key => ({
            id: key,
            slug: data[key].slug || key,
            emoji: data[key].emoji || getEmojiForCategory(data[key].category),
            ...data[key]
          }))
          .filter(recipe => recipe.published !== false);
      } else {
        allRecipes = SEED_DATA;
      }
    } else {
      allRecipes = SEED_DATA;
    }
  } catch (error) {
    console.warn('RTDB fetch failed or timed out, rendering SEED_DATA fallback:', error);
    allRecipes = SEED_DATA;
  }

  // Render recipes to DOM
  renderRecipes(allRecipes);
}

function getEmojiForCategory(cat) {
  if (cat === 'gorengan') return '🥟';
  if (cat === 'kue') return '🧀';
  if (cat === 'minuman') return '🧋';
  return '🍳';
}

// --- RENDER RECIPES ---
function renderRecipes(recipes) {
  const container = document.getElementById('recipesContainer');
  if (!container) return;

  if (recipes.length === 0) {
    container.innerHTML = `
      <div class="card text-center" style="padding: 3rem 1rem;">
        <h3>Belum Ada Resep di Kategori Ini</h3>
        <p class="text-secondary" style="margin-top: 0.5rem;">Coba pilih kategori filter yang lain.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = recipes.map((recipe) => {
    const slug = recipe.slug || recipe.id;
    const initialCapital = recipe.business_analysis?.estimated_initial_capital || 35000;
    const portions = recipe.default_portions || 20;
    const hppEstimate = Math.round(initialCapital / portions);
    const emoji = recipe.emoji || getEmojiForCategory(recipe.category);

    return `
      <article class="recipe-card" id="card-${slug}" data-category="${recipe.category}" data-tags='${JSON.stringify(recipe.tags || [])}'>
        <div class="recipe-card-header">
          <div class="recipe-card-banner">
            <span>${emoji}</span>
          </div>
          <div class="recipe-card-info">
            <div class="recipe-card-meta">
              <span class="mini-card-tag">${recipe.category}</span>
              ${(recipe.tags || []).map(t => `<span class="section-tag" style="font-size: 0.7rem; padding: 0.15rem 0.5rem; margin-bottom: 0;">#${t}</span>`).join('')}
            </div>
            <h3 class="recipe-card-title">${recipe.title}</h3>
            <p class="recipe-card-excerpt text-secondary">${recipe.excerpt}</p>
            
            <div class="recipe-card-stats">
              <div class="stat-item">
                <span class="stat-label">Estimasi Modal</span>
                <span class="stat-value">${formatRupiah(initialCapital)}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Hasil Porsi</span>
                <span class="stat-value">${portions} pcs</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">HPP / Pcs</span>
                <span class="stat-value cuan">~${formatRupiah(hppEstimate)}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Toggle Accordion Button -->
        <button class="btn-toggle-detail" data-target="detail-${slug}" aria-expanded="false">
          <span>Lihat Analisis, Resep & Kalkulator HPP</span>
          <span class="icon-chevron">▼</span>
        </button>

        <!-- Accordion Detail Section -->
        <div class="recipe-detail" id="detail-${slug}">
          <!-- Tabs Nav -->
          <div class="tabs-nav">
            <button class="tab-btn active" data-recipe="${slug}" data-tab="analysis">📊 Analisis Bisnis</button>
            <button class="tab-btn" data-recipe="${slug}" data-tab="ingredients">🥣 Bahan & Masak</button>
            <button class="tab-btn" data-recipe="${slug}" data-tab="calculator">🧮 Kalkulator HPP</button>
            <button class="tab-btn" data-recipe="${slug}" data-tab="promo">📸 Promo Siap Share</button>
          </div>

          <!-- Tab Content A: Analisis Bisnis -->
          <div class="tab-content active" id="tab-${slug}-analysis">
            <div class="grid-2col">
              <div class="card" style="padding: 1.25rem;">
                <h4>📈 Potensi Pasar & Permintaan</h4>
                <p style="margin-top: 0.5rem;">${recipe.business_analysis?.market_demand || 'Permintaan stabil di pasar lokal.'}</p>
              </div>
              <div class="card" style="padding: 1.25rem;">
                <h4>🍳 Kemudahan Pembuatan</h4>
                <p style="margin-top: 0.5rem;">${recipe.business_analysis?.ease_of_making || 'Bisa dibuat di dapur rumah tanpa peralatan khusus.'}</p>
              </div>
            </div>
            <div class="card" style="padding: 1.25rem; margin-top: 1rem; border-left: 4px solid var(--color-primary);">
              <h4>💡 Ketersediaan Bahan</h4>
              <p style="margin-top: 0.25rem;">${recipe.business_analysis?.ingredient_availability || 'Bahan mudah dicari di pasar atau toko terdekat.'}</p>
            </div>
          </div>

          <!-- Tab Content B: Bahan & Langkah Masak -->
          <div class="tab-content" id="tab-${slug}-ingredients">
            <h4 style="margin-bottom: 0.75rem;">Daftar Bahan (${portions} Porsi Acuan)</h4>
            <div class="calc-table-wrapper">
              <table class="calc-table">
                <thead>
                  <tr>
                    <th>Nama Bahan</th>
                    <th>Takaran</th>
                    <th>Estimasi Harga</th>
                  </tr>
                </thead>
                <tbody>
                  ${(recipe.ingredients || []).map(ing => `
                    <tr>
                      <td><strong>${ing.name}</strong></td>
                      <td>${ing.quantity}</td>
                      <td>${formatRupiah(ing.default_price)}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>

            <h4 style="margin: 1.5rem 0 0.75rem 0;">Langkah Memasak Anti-Gagal</h4>
            <ol class="step-list">
              ${(recipe.cooking_steps || []).map(step => `
                <li class="step-list-item">${step}</li>
              `).join('')}
            </ol>
          </div>

          <!-- Tab Content C: Kalkulator HPP Interaktif -->
          <div class="tab-content" id="tab-${slug}-calculator">
            <p style="margin-bottom: 1rem; font-size: 0.95rem;">
              Sesuaikan harga bahan di pasarmu untuk menghitung HPP dan harga jual secara realtime:
            </p>

            <div class="calc-table-wrapper">
              <table class="calc-table">
                <thead>
                  <tr>
                    <th>Bahan</th>
                    <th>Takaran</th>
                    <th>Harga Pasar Kamu (Rp)</th>
                  </tr>
                </thead>
                <tbody id="calc-rows-${slug}">
                  ${(recipe.ingredients || []).map((ing, idx) => `
                    <tr>
                      <td>${ing.name}</td>
                      <td><small class="text-secondary">${ing.quantity}</small></td>
                      <td>
                        <input 
                          type="number" 
                          class="calc-input-price" 
                          data-recipe="${slug}" 
                          data-idx="${idx}" 
                          value="${ing.default_price}" 
                          min="0"
                          step="500"
                        >
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>

            <!-- Kalkulator Settings Controls -->
            <div class="calc-controls">
              <div class="calc-control-group">
                <label for="portions-${slug}">Jumlah Porsi yang Dihasilkan (biji/bungkus):</label>
                <input 
                  type="number" 
                  id="portions-${slug}" 
                  data-recipe="${slug}" 
                  class="calc-input-portions" 
                  value="${portions}" 
                  min="1" 
                  max="1000"
                >
              </div>
              <div class="calc-control-group">
                <label for="margin-${slug}">
                  Target Margin Keuntungan: <span id="margin-val-${slug}" style="color: var(--color-primary); font-weight: 800;">100%</span>
                </label>
                <input 
                  type="range" 
                  id="margin-${slug}" 
                  data-recipe="${slug}" 
                  class="calc-input-margin" 
                  value="100" 
                  min="20" 
                  max="250" 
                  step="5"
                >
              </div>
            </div>

            <!-- Output Realtime Summary -->
            <div class="calc-summary-card" id="summary-${slug}">
              <div class="calc-summary-item">
                <span class="calc-summary-label">Total Modal Bahan</span>
                <span class="calc-summary-val" id="sum-modal-${slug}">${formatRupiah(initialCapital)}</span>
              </div>
              <div class="calc-summary-item">
                <span class="calc-summary-label">HPP per Porsi</span>
                <span class="calc-summary-val" id="sum-hpp-${slug}">${formatRupiah(hppEstimate)}</span>
              </div>
              <div class="calc-summary-item">
                <span class="calc-summary-label">Rekomendasi Jual</span>
                <span class="calc-summary-val cuan" id="sum-jual-${slug}">${formatRupiah(hppEstimate * 2)}</span>
              </div>
              <div class="calc-summary-item">
                <span class="calc-summary-label">Est. Laba Bersih</span>
                <span class="calc-summary-val cuan" id="sum-laba-${slug}">${formatRupiah(initialCapital)}</span>
              </div>
            </div>
          </div>

          <!-- Tab Content D: Promo Siap Share -->
          <div class="tab-content" id="tab-${slug}-promo">
            <h4 style="margin-bottom: 0.75rem;">Foto & Template Promosi Siap Share</h4>
            <p style="margin-bottom: 1.25rem;">Download gambar estetik jajanan ini untuk promosi di WhatsApp Status & Instagram Story.</p>
            
            <div class="promo-grid">
              <div class="promo-placeholder">
                <span style="font-size: 3rem;">📸</span>
                <span>Poster Promo WhatsApp</span>
                <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
                  <button class="btn btn-sm btn-cta" onclick="alert('Fitur download HD tersedia di aplikasi Jajan Cuan!')">📥 Download Foto</button>
                  <button class="btn btn-sm btn-outline" style="color: #fff; border-color: #fff;" onclick="alert('Fitur share langsung WhatsApp tersedia di aplikasi Jajan Cuan!')">📤 Share WA</button>
                </div>
              </div>
              <div class="promo-placeholder" style="background: linear-gradient(135deg, #2EC4B6 0%, #1A1A2E 100%);">
                <span style="font-size: 3rem;">📱</span>
                <span>Story IG & Reels Banner</span>
                <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
                  <button class="btn btn-sm btn-cta" onclick="alert('Fitur download HD tersedia di aplikasi Jajan Cuan!')">📥 Download Foto</button>
                  <button class="btn btn-sm btn-outline" style="color: #fff; border-color: #fff;" onclick="alert('Fitur share langsung Instagram tersedia di aplikasi Jajan Cuan!')">📤 Share IG</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </article>
    `;
  }).join('');

  // Attach dynamic event listeners after rendering
  setupAccordionListeners();
  setupTabListeners();
  setupCalculatorListeners();
}

// --- ACCORDION LOGIC ---
function setupAccordionListeners() {
  document.querySelectorAll('.btn-toggle-detail').forEach((btn) => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-target');
      const targetEl = document.getElementById(targetId);
      if (!targetEl) return;

      const isExpanded = btn.classList.contains('expanded');
      if (isExpanded) {
        btn.classList.remove('expanded');
        targetEl.classList.remove('active');
        btn.setAttribute('aria-expanded', 'false');
        btn.querySelector('span:first-child').textContent = 'Lihat Analisis, Resep & Kalkulator HPP';
      } else {
        btn.classList.add('expanded');
        targetEl.classList.add('active');
        btn.setAttribute('aria-expanded', 'true');
        btn.querySelector('span:first-child').textContent = 'Tutup Detail Resep';
      }
    });
  });
}

// --- TAB SWITCHING LOGIC ---
function setupTabListeners() {
  document.querySelectorAll('.tab-btn').forEach((tabBtn) => {
    tabBtn.addEventListener('click', () => {
      const slug = tabBtn.getAttribute('data-recipe');
      const tabName = tabBtn.getAttribute('data-tab');

      // Deactivate all sibling tabs for this recipe
      const recipeDetail = document.getElementById(`detail-${slug}`);
      if (!recipeDetail) return;

      recipeDetail.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      recipeDetail.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

      // Activate current tab
      tabBtn.classList.add('active');
      const targetContent = document.getElementById(`tab-${slug}-${tabName}`);
      if (targetContent) {
        targetContent.classList.add('active');
      }
    });
  });
}

// --- CALCULATOR REALTIME ENGINE ---
function setupCalculatorListeners() {
  // Listen on price input change
  document.querySelectorAll('.calc-input-price').forEach((input) => {
    input.addEventListener('input', () => {
      const slug = input.getAttribute('data-recipe');
      recalculate(slug);
    });
  });

  // Listen on portions change
  document.querySelectorAll('.calc-input-portions').forEach((input) => {
    input.addEventListener('input', () => {
      const slug = input.getAttribute('data-recipe');
      recalculate(slug);
    });
  });

  // Listen on margin range change
  document.querySelectorAll('.calc-input-margin').forEach((range) => {
    range.addEventListener('input', () => {
      const slug = range.getAttribute('data-recipe');
      const marginLabel = document.getElementById(`margin-val-${slug}`);
      if (marginLabel) {
        marginLabel.textContent = range.value + '%';
      }
      recalculate(slug);
    });
  });

  // Initial calculation run on all recipes
  allRecipes.forEach((r) => {
    recalculate(r.slug || r.id);
  });
}

function recalculate(slug) {
  const priceInputs = document.querySelectorAll(`.calc-input-price[data-recipe="${slug}"]`);
  let totalModal = 0;
  priceInputs.forEach((inp) => {
    const val = parseFloat(inp.value) || 0;
    totalModal += val;
  });

  const portionsInput = document.getElementById(`portions-${slug}`);
  const marginInput = document.getElementById(`margin-${slug}`);

  const targetPortions = Math.max(1, parseInt(portionsInput?.value || 20, 10));
  const marginPercent = parseFloat(marginInput?.value || 100);

  const hppPerPortion = targetPortions > 0 ? (totalModal / targetPortions) : 0;
  const sellingPrice = hppPerPortion * (1 + marginPercent / 100);
  const totalProfit = (sellingPrice - hppPerPortion) * targetPortions;

  // Update DOM Elements
  const modalEl = document.getElementById(`sum-modal-${slug}`);
  const hppEl = document.getElementById(`sum-hpp-${slug}`);
  const jualEl = document.getElementById(`sum-jual-${slug}`);
  const labaEl = document.getElementById(`sum-laba-${slug}`);

  if (modalEl) modalEl.textContent = formatRupiah(totalModal);
  if (hppEl) hppEl.textContent = formatRupiah(hppPerPortion);
  if (jualEl) jualEl.textContent = formatRupiah(sellingPrice);
  if (labaEl) labaEl.textContent = formatRupiah(totalProfit);
}

// --- FILTER SYSTEM ---
function initFilters() {
  const filterButtons = document.querySelectorAll('.filter-tag');
  filterButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      filterButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      const filter = btn.getAttribute('data-filter');
      filterRecipes(filter);
    });
  });
}

function filterRecipes(filter) {
  if (filter === 'all') {
    renderRecipes(allRecipes);
    return;
  }

  const filtered = allRecipes.filter((r) => {
    const tags = r.tags || [];
    const category = r.category || '';
    if (filter === 'modal-kecil') {
      return tags.includes('modal-kecil') || (r.business_analysis?.estimated_initial_capital <= 50000);
    }
    if (filter === 'viral') {
      return tags.includes('viral');
    }
    return category === filter || tags.includes(filter);
  });

  renderRecipes(filtered);
}

// --- WAITLIST MODAL FOR EXPERIENCE PAGE ---
function initWaitlistModal() {
  const modal = document.getElementById('waitlistModal');
  const closeModalBtn = document.getElementById('closeModalBtn');
  const waitlistForm = document.getElementById('waitlistForm');
  const contactInput = document.getElementById('contactInput');
  const formContainer = document.getElementById('modalFormContainer');
  const successContainer = document.getElementById('modalSuccessContainer');
  const submitBtn = document.getElementById('submitWaitlistBtn');
  const radioButtons = document.querySelectorAll('input[name="contactType"]');
  const triggers = document.querySelectorAll('.trigger-waitlist');

  let currentSource = 'experience_sticky';

  function openModal(source = 'experience_sticky') {
    currentSource = source;
    if (modal) {
      modal.classList.add('active');
      modal.setAttribute('aria-hidden', 'false');
      if (formContainer && successContainer) {
        formContainer.style.display = 'block';
        successContainer.classList.remove('active');
      }
      if (contactInput) {
        setTimeout(() => contactInput.focus(), 100);
      }
    }
  }

  function closeModal() {
    if (modal) {
      modal.classList.remove('active');
      modal.setAttribute('aria-hidden', 'true');
      if (waitlistForm) {
        waitlistForm.reset();
      }
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Kabari Saya Saat Rilis →';
      }
    }
  }

  triggers.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const source = btn.getAttribute('data-source') || 'experience_sticky';
      openModal(source);
    });
  });

  if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);

  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) closeModal();
    });
  }

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal && modal.classList.contains('active')) {
      closeModal();
    }
  });

  radioButtons.forEach((radio) => {
    radio.addEventListener('change', (e) => {
      if (e.target.value === 'email') {
        contactInput.placeholder = 'Alamat Email (contoh: nama@gmail.com)';
        contactInput.type = 'email';
      } else {
        contactInput.placeholder = 'Nomor WhatsApp (contoh: 0812xxxx)';
        contactInput.type = 'text';
      }
    });
  });

  if (waitlistForm) {
    waitlistForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const contactVal = contactInput.value.trim();
      const selectedType = document.querySelector('input[name="contactType"]:checked')?.value || 'whatsapp';

      if (contactVal.length < 5) {
        alert('Mohon masukkan nomor WhatsApp atau email yang valid.');
        contactInput.focus();
        return;
      }

      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Menyimpan...';
      }

      const payload = {
        contact: contactVal,
        type: selectedType,
        source: currentSource,
        created_at: new Date().toISOString()
      };

      try {
        if (typeof db !== 'undefined') {
          await db.ref('waitlist').push(payload);
        }

        if (typeof gtag === 'function') {
          gtag('event', 'lead_captured', {
            contact_type: selectedType,
            source: currentSource
          });
        }

        if (formContainer && successContainer) {
          formContainer.style.display = 'none';
          successContainer.classList.add('active');
        }

        setTimeout(closeModal, 2500);
      } catch (err) {
        console.error('Error submitting waitlist from experience:', err);
        alert('Terjadi kesalahan saat menyimpan kontak. Silakan coba lagi.');
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.textContent = 'Kabari Saya Saat Rilis →';
        }
      }
    });
  }
}
