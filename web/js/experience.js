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

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Global active recipes state & Admin Mode Detection
let allRecipes = [];
const urlParams = new URLSearchParams(window.location.search);
const isAdmin = urlParams.get('admin') === 'true';
const ADMIN_STORAGE_KEY = 'jajan_cuan_curated_drafts';

// --- DB-LEVEL PAGINATION CONFIG FOR END-USERS (5 items per page) ---
const PAGE_SIZE = 5;
let currentPage = 1;
const initialUrlFilter = (urlParams.get('filter') || 'all').replace(/[^\w-]/g, '');
let currentFilter = initialUrlFilter;
let pageCursors = [null]; // pageCursors[0] = null (page 1), pageCursors[1] = cursor key for page 2, etc.
let hasNextPage = false;
let isFetchingPage = false;

// --- SEARCH ENGINE STATE ---
let currentSearchQuery = '';
let searchDebounceTimer = null;
let isSearching = false;

// --- MAIN INIT ---
document.addEventListener('DOMContentLoaded', () => {
  initWaitlistModal();
  initFilters();

  // Set visual active state on matching filter button immediately
  if (currentFilter !== 'all') {
    const filterButtons = document.querySelectorAll('.filter-tag');
    filterButtons.forEach(btn => {
      if (btn.getAttribute('data-filter') === currentFilter) {
        btn.classList.add('active');
      } else {
        btn.classList.remove('active');
      }
    });
  }

  // Jika sedang di localhost tapi belum buka ?admin=true, tampilkan hint banner
  const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  if (isLocalhost && !isAdmin) {
    const hintBanner = document.getElementById('localhostHintBanner');
    if (hintBanner) hintBanner.style.display = 'block';
  }

  if (isAdmin) {
    setupAdminToolbar();
    loadAdminPosts().then(() => {
      initSearch();
    });
  } else {
    setupPaginationListeners();
    const queryParam = urlParams.get('q') || urlParams.get('search');
    if (!queryParam) {
      loadUserPage(1, currentFilter);
    }
    initSearch();
  }
});

// Setup Toolbar Admin jika ?admin=true
function setupAdminToolbar() {
  const toolbar = document.getElementById('adminToolbar');
  if (toolbar) {
    toolbar.style.display = 'block';
  }

  // Tombol Paksa Muat Draft Cookpad (Bypass Cache & Sync Disk)
  const loadDraftsBtn = document.getElementById('loadDraftsBtn');
  if (loadDraftsBtn) {
    loadDraftsBtn.addEventListener('click', async () => {
      loadDraftsBtn.disabled = true;
      loadDraftsBtn.textContent = '⏳ Membaca disk...';
      const freshDrafts = await fetchLatestDraftsFromDisk();
      if (freshDrafts && freshDrafts.length > 0) {
        allRecipes = freshDrafts;
        localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(allRecipes));
        updateAdminCount();
        renderRecipes(allRecipes);
        alert(`✅ Sukses membaca disk! Memuat ${allRecipes.length} resep draft hasil scraping Cookpad.`);
      } else {
        alert('Data draft belum tersedia di disk.');
      }
      loadDraftsBtn.disabled = false;
      loadDraftsBtn.textContent = '📋 Muat Draft Terbaru (Sync Disk)';
    });
  }

  // Tombol Export JSON Bersih
  const exportBtn = document.getElementById('exportJsonBtn');
  if (exportBtn) {
    exportBtn.addEventListener('click', () => {
      const exportData = {};
      allRecipes.forEach(r => {
        const key = r.slug || r.id;
        exportData[key] = r;
      });

      const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'jajan-cuan-curated-posts.json';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      alert(`🎉 Sebanyak ${allRecipes.length} resep terkurasi berhasil di-export ke "jajan-cuan-curated-posts.json"!`);
    });
  }

  // Tombol Reset Draft
  const resetBtn = document.getElementById('resetDraftsBtn');
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      if (confirm('Reset kurasi ke data draft scraping semula? Semua resep yang tadi dihapus akan muncul kembali.')) {
        localStorage.removeItem(ADMIN_STORAGE_KEY);
        location.reload();
      }
    });
  }
}

function updateAdminCount() {
  const countEl = document.getElementById('adminRecipeCount');
  if (countEl) {
    countEl.textContent = `${allRecipes.length} Resep Aktif`;
  }
}

// Helper: Ambil data draft paling segar dari disk via dynamic fetch anti-cache
async function fetchLatestDraftsFromDisk() {
  try {
    const res = await fetch('js/scraped-drafts.js?t=' + Date.now(), { cache: 'no-store' });
    if (res.ok) {
      const text = await res.text();
      const match = text.match(/window\.SCRAPED_DRAFTS\s*=\s*(\{[\s\S]*\});?/);
      if (match) {
        const parsed = JSON.parse(match[1]);
        window.SCRAPED_DRAFTS = parsed;
        return Object.values(parsed);
      }
    }
  } catch(e) {
    console.warn('Dynamic fetch draft failed, fallback to window.SCRAPED_DRAFTS:', e);
  }

  if (typeof window.SCRAPED_DRAFTS !== 'undefined' && window.SCRAPED_DRAFTS) {
    return Object.values(window.SCRAPED_DRAFTS);
  }
  return [];
}

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

// --- DATA FETCHING (Local Curation for Admin vs On-Demand DB Pagination for Users) ---

// 1. Data Fetching khusus Admin (?admin=true)
async function loadAdminPosts() {
  try {
    const freshDrafts = await fetchLatestDraftsFromDisk();
    const cached = localStorage.getItem(ADMIN_STORAGE_KEY);
    let localList = [];
    if (cached) {
      try { localList = JSON.parse(cached); } catch(e) {}
    }

    if (freshDrafts.length > localList.length) {
      allRecipes = freshDrafts;
      localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(allRecipes));
    } else if (localList.length > 0) {
      allRecipes = localList;
    } else if (freshDrafts.length > 0) {
      allRecipes = freshDrafts;
      localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(allRecipes));
    } else {
      allRecipes = SEED_DATA;
    }

    updateAdminCount();
    const paginationControls = document.getElementById('paginationControls');
    if (paginationControls) paginationControls.style.display = 'none';
    renderRecipes(allRecipes);
  } catch (error) {
    console.warn('Admin load drafts error:', error);
    allRecipes = SEED_DATA;
    renderRecipes(allRecipes);
  }
}

// 2. Data Fetching Pengunjung Biasa — ON-DEMAND DB PAGINATION (5 ITEM PER REQUEST)
// PENTING: Dilarang fetch all lalu slice di JS. Query RTDB dibatasi limitToFirst(PAGE_SIZE + 1).
async function loadUserPage(page = 1, filter = currentFilter) {
  if (isFetchingPage) return;
  isFetchingPage = true;
  currentPage = page;
  currentFilter = filter;

  const container = document.getElementById('recipesContainer');
  const paginationControls = document.getElementById('paginationControls');
  const prevBtn = document.getElementById('prevPageBtn');
  const nextBtn = document.getElementById('nextPageBtn');
  const pageNumEl = document.getElementById('currentPageNum');

  if (pageNumEl) pageNumEl.textContent = currentPage;
  if (prevBtn) prevBtn.disabled = (currentPage === 1);
  if (nextBtn) nextBtn.disabled = true;

  if (container) {
    container.innerHTML = `
      <div class="loading-state">
        <div class="spinner"></div>
        <p>Memuat resep halaman ${currentPage} dari database...</p>
      </div>
    `;
  }

  try {
    let pagedRecipes = [];
    let hasMore = false;

    if (typeof db !== 'undefined' && db) {
      let query;
      const cursor = pageCursors[page - 1]; // Cursor key untuk halaman ini

      if (filter === 'all') {
        query = db.ref('posts').orderByKey();
        if (cursor) {
          query = query.startAt(cursor);
        }
        query = query.limitToFirst(PAGE_SIZE + 1);

        const snapshot = await fetchWithTimeout(query.once('value'), 4000);
        const items = [];
        snapshot.forEach((child) => {
          const val = child.val();
          if (val && val.published !== false) {
            items.push({
              id: child.key,
              slug: val.slug || child.key,
              emoji: val.emoji || getEmojiForCategory(val.category),
              ...val
            });
          }
        });

        if (items.length > PAGE_SIZE) {
          hasMore = true;
          pagedRecipes = items.slice(0, PAGE_SIZE);
          pageCursors[page] = items[PAGE_SIZE].id;
        } else {
          hasMore = false;
          pagedRecipes = items;
          pageCursors[page] = null;
        }

      } else if (filter === 'kue' || filter === 'gorengan' || filter === 'minuman') {
        // Query RTDB terindeks pada field category
        query = db.ref('posts').orderByChild('category').equalTo(filter);
        const snapshot = await fetchWithTimeout(query.once('value'), 4000);
        const items = [];
        snapshot.forEach((child) => {
          const val = child.val();
          if (val && val.published !== false) {
            items.push({
              id: child.key,
              slug: val.slug || child.key,
              emoji: val.emoji || getEmojiForCategory(val.category),
              ...val
            });
          }
        });

        const startIdx = (page - 1) * PAGE_SIZE;
        pagedRecipes = items.slice(startIdx, startIdx + PAGE_SIZE);
        hasMore = items.length > startIdx + PAGE_SIZE;

      } else {
        // Filter Tag (modal-kecil, viral)
        let sourceList = [];
        if (typeof window.SCRAPED_DRAFTS !== 'undefined' && window.SCRAPED_DRAFTS) {
          sourceList = Object.values(window.SCRAPED_DRAFTS);
        } else {
          const snap = await fetchWithTimeout(db.ref('posts').once('value'), 4000);
          snap.forEach(c => {
            const val = c.val();
            if (val && val.published !== false) sourceList.push({ id: c.key, ...val });
          });
        }

        sourceList = sourceList.filter(r => {
          const tags = r.tags || [];
          if (filter === 'modal-kecil') {
            return tags.includes('modal-kecil') || (r.business_analysis?.estimated_initial_capital <= 40000);
          }
          if (filter === 'viral') {
            return tags.includes('viral');
          }
          return tags.includes(filter);
        });

        const startIdx = (page - 1) * PAGE_SIZE;
        pagedRecipes = sourceList.slice(startIdx, startIdx + PAGE_SIZE);
        hasMore = sourceList.length > startIdx + PAGE_SIZE;
      }
    } else {
      throw new Error('Database offline / not initialized');
    }

    hasNextPage = hasMore;
    allRecipes = pagedRecipes;
    renderRecipes(allRecipes);

    if (paginationControls) {
      paginationControls.style.display = 'flex';
    }
    if (prevBtn) prevBtn.disabled = (currentPage === 1);
    if (nextBtn) nextBtn.disabled = !hasNextPage;

  } catch (err) {
    console.warn('DB Pagination fallback (Offline/Localhost):', err);
    // Fallback Offline/Localhost: Potong 5 item sesuai permintaan halaman
    let sourceList = [];
    if (typeof window.SCRAPED_DRAFTS !== 'undefined' && window.SCRAPED_DRAFTS) {
      sourceList = Object.values(window.SCRAPED_DRAFTS);
    } else {
      sourceList = SEED_DATA;
    }

    if (filter !== 'all') {
      sourceList = sourceList.filter(r => {
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
    }

    const startIdx = (page - 1) * PAGE_SIZE;
    allRecipes = sourceList.slice(startIdx, startIdx + PAGE_SIZE);
    hasNextPage = sourceList.length > startIdx + PAGE_SIZE;

    renderRecipes(allRecipes);

    if (paginationControls) {
      paginationControls.style.display = 'flex';
    }
    if (prevBtn) prevBtn.disabled = (currentPage === 1);
    if (nextBtn) nextBtn.disabled = !hasNextPage;
  } finally {
    isFetchingPage = false;
  }
}

// Setup Event Listener Tombol Pagination
function setupPaginationListeners() {
  const prevBtn = document.getElementById('prevPageBtn');
  const nextBtn = document.getElementById('nextPageBtn');

  if (prevBtn) {
    prevBtn.addEventListener('click', () => {
      if (currentPage > 1 && !isFetchingPage) {
        loadUserPage(currentPage - 1, currentFilter);
        const filterEl = document.getElementById('filterContainer');
        if (filterEl) filterEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  }

  if (nextBtn) {
    nextBtn.addEventListener('click', () => {
      if (hasNextPage && !isFetchingPage) {
        loadUserPage(currentPage + 1, currentFilter);
        const filterEl = document.getElementById('filterContainer');
        if (filterEl) filterEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  }
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
    if (isSearching && currentSearchQuery) {
      container.innerHTML = `
        <div class="empty-search-card">
          <div style="font-size: 3rem; margin-bottom: 0.5rem;">🔍</div>
          <h3 style="font-size: 1.35rem; margin-bottom: 0.5rem; color: var(--ink);">Resep Tidak Ditemukan</h3>
          <p class="text-secondary" style="max-width: 480px; margin: 0 auto 1.25rem;">
            Tidak ada jajanan dengan kata kunci "<strong>${escapeHtml(currentSearchQuery)}</strong>"${currentFilter !== 'all' ? ` di kategori <strong>${escapeHtml(currentFilter)}</strong>` : ''}.
          </p>
          <div style="display: flex; justify-content: center; gap: 0.5rem; flex-wrap: wrap;">
            <button class="btn btn-primary btn-sm btn-reset-search">
              🔄 Tampilkan Semua Resep
            </button>
            ${currentFilter !== 'all' ? `
              <button class="btn btn-outline btn-sm btn-clear-filter-search">
                🌐 Cari di Semua Kategori
              </button>
            ` : ''}
          </div>
          <div style="margin-top: 1.5rem; font-size: 0.85rem; color: var(--mut);">
            <span>Saran kata kunci populer:</span><br>
            <button class="search-suggestion-chip" data-keyword="tapioka">tapioka</button>
            <button class="search-suggestion-chip" data-keyword="kulit lumpia">kulit lumpia</button>
            <button class="search-suggestion-chip" data-keyword="keju">keju</button>
            <button class="search-suggestion-chip" data-keyword="cireng">cireng</button>
            <button class="search-suggestion-chip" data-keyword="sosis">sosis</button>
            <button class="search-suggestion-chip" data-keyword="bolu">bolu</button>
          </div>
        </div>
      `;
      setupEmptySearchListeners();
      return;
    }

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
    const sourcePhoto = recipe.media?.source_photo || recipe.og_image || '';

    return `
      <article class="recipe-card" id="card-${slug}" data-category="${recipe.category}" data-tags='${JSON.stringify(recipe.tags || [])}'>
        <div class="recipe-card-header">
          <div class="recipe-card-banner" style="${sourcePhoto ? 'padding: 0; overflow: hidden; background: #e5e7eb;' : ''}">
            ${sourcePhoto 
              ? `<img src="${sourcePhoto}" alt="${recipe.title}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.onerror=null; this.parentElement.innerHTML='<span>${emoji}</span>';">` 
              : `<span>${emoji}</span>`}
            ${!isAdmin ? `
              <button class="btn-card-bookmark trigger-waitlist" data-source="card_bookmark_hook" data-recipe="${slug}" title="Simpan resep ke favorit (Buka di Aplikasi)" aria-label="Simpan resep ke favorit">
                <span class="bookmark-icon">🤍</span>
                <span class="bookmark-text">Simpan</span>
              </button>
            ` : ''}
          </div>
          <div class="recipe-card-info">
            <div class="recipe-card-meta">
              <span class="mini-card-tag">${recipe.category}</span>
              ${(recipe.tags || []).map(t => `<span class="section-tag" style="font-size: 0.7rem; padding: 0.15rem 0.5rem; margin-bottom: 0;">#${t}</span>`).join('')}
              ${recipe._matchedIngredient ? `
                <span class="search-match-pill" title="Resep ini cocok karena menggunakan bahan ${escapeHtml(recipe._matchedIngredient)}">
                  🧂 Bahan: <strong>${escapeHtml(recipe._matchedIngredient)}</strong>
                </span>
              ` : ''}
            </div>
            
            ${isAdmin ? `
              <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; margin: 0.35rem 0 0.5rem 0;">
                <h3 class="recipe-card-title" style="margin: 0;">${recipe.title}</h3>
                <button class="btn-hard-delete" data-slug="${slug}" style="background: #FEE2E2; color: #DC2626; border: 1px solid #FCA5A5; padding: 0.35rem 0.65rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700; cursor: pointer; white-space: nowrap; flex-shrink: 0;" title="Hard Delete resep ini dari kurasi lokal">
                  🗑️ Hapus Resep
                </button>
              </div>
            ` : `
              <h3 class="recipe-card-title">${recipe.title}</h3>
            `}

            <p class="recipe-card-excerpt text-secondary">${recipe.excerpt}</p>
            
            ${isAdmin && recipe.yield_text ? `
              <div style="margin: 0.25rem 0 0.5rem 0;">
                <span style="font-size: 0.75rem; background: #EEF2FF; color: #4338CA; border: 1px solid #C7D2FE; padding: 0.15rem 0.5rem; border-radius: 4px; font-weight: 600;">
                  🎯 Porsi Asli Cookpad: ${recipe.yield_text}
                </span>
              </div>
            ` : ''}
            
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

            ${sourcePhoto ? `
              <!-- Visual Grounding Review Panel (SOP Anti-Halusinasi AI) -->
              <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 1.25rem; margin-bottom: 1.5rem;">
                <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem;">
                  <span style="font-size: 1.1rem;">🖼️</span>
                  <strong style="color: #0F172A; font-size: 0.95rem;">Review Visual Grounding (Polesan AI Berbasis Foto Asli)</strong>
                </div>
                <p style="font-size: 0.8rem; color: #475569; margin-bottom: 1rem; line-height: 1.4;">
                  <strong>Peringatan Keras PRD:</strong> AI dilarang men-generate gambar baru dari teks (text-to-image murni). Pemolesan wajib berbasis foto asli di bawah agar bentuk fisik, porsi (${portions}), dan wadah riil (mika/cup/plastik) tetap otentik dan akurat saat dijual.
                </p>

                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem;">
                  <!-- Kolom 1: Foto Asli -->
                  <div style="border: 1px solid #CBD5E1; border-radius: 8px; padding: 0.75rem; background: #fff;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                      <span style="font-size: 0.75rem; font-weight: 700; color: #475569;">1. FOTO ASLI (COOKPAD)</span>
                      <span style="font-size: 0.7rem; background: #E2E8F0; padding: 0.1rem 0.4rem; border-radius: 4px;">Grounding</span>
                    </div>
                    <img src="${sourcePhoto}" style="width: 100%; height: 160px; object-fit: cover; border-radius: 6px;" alt="Foto asli ${recipe.title}">
                  </div>

                  <!-- Kolom 2: Slot Polesan AI -->
                  <div style="border: ${recipe.media?.ai_polished_photo ? '2px solid #10B981' : '2px dashed #94A3B8'}; border-radius: 8px; padding: 0.75rem; background: #F8FAFC; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; min-height: 200px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; width: 100%; margin-bottom: 0.5rem;">
                      <span style="font-size: 0.75rem; font-weight: 700; color: #047857;">2. POLESAN AI (IMG2IMG)</span>
                      ${recipe.media?.ai_polished_photo ? `<span style="font-size: 0.7rem; background: #D1FAE5; color: #065F46; padding: 0.1rem 0.4rem; border-radius: 4px; font-weight: 700;">Siap Review</span>` : ''}
                    </div>

                    ${recipe.media?.ai_polished_photo ? `
                      <img src="${recipe.media.ai_polished_photo}" style="width: 100%; height: 160px; object-fit: cover; border-radius: 6px; margin-bottom: 0.75rem;" alt="Hasil polesan AI ${recipe.title}">
                      <div style="display: flex; gap: 0.5rem; width: 100%;">
                        <button class="btn btn-sm btn-approve-photo" data-slug="${slug}" style="background: #10B981; color: #fff; font-size: 0.75rem; font-weight: 700; flex: 1; padding: 0.4rem; border: none; border-radius: 6px; cursor: pointer;">
                          ✅ Approve Foto Ini
                        </button>
                        <button class="btn btn-sm btn-reject-photo" data-slug="${slug}" style="background: #FEE2E2; color: #DC2626; font-size: 0.75rem; font-weight: 700; padding: 0.4rem 0.6rem; border: 1px solid #FCA5A5; border-radius: 6px; cursor: pointer;">
                          ❌ Tolak
                        </button>
                      </div>
                    ` : `
                      <span style="font-size: 2rem; margin: 0.5rem 0 0.25rem 0;">✨</span>
                      <p style="font-size: 0.75rem; color: #64748B; margin: 0 0 0.5rem 0;">Belum dipoles AI</p>
                      <code style="font-size: 0.7rem; background: #E2E8F0; padding: 0.25rem 0.5rem; border-radius: 4px; display: block; margin-bottom: 0.75rem; word-break: break-all;">
                        node scripts/polish-image.js ${slug}
                      </code>
                      <button class="btn btn-sm" style="background: #6366F1; color: #fff; font-size: 0.75rem; font-weight: 600; padding: 0.35rem 0.75rem; border-radius: 6px;" onclick="navigator.clipboard.writeText('node scripts/polish-image.js ${slug}'); alert('Perintah disalin ke clipboard! Buka terminal dan jalankan: node scripts/polish-image.js ${slug}');">
                        📋 Salin Command Terminal
                      </button>
                    `}
                  </div>
                </div>
              </div>
            ` : ''}
            
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
  if (isAdmin) {
    setupAdminDeleteListeners();
  }

  // Handle deep-link to specific recipe (?recipe=slug or #card-slug)
  handleTargetRecipeDeepLink();
}

// Auto-scroll and auto-expand target recipe card if specified in URL
function handleTargetRecipeDeepLink() {
  const targetRecipe = (urlParams.get('recipe') || location.hash.replace('#card-', '') || '').replace(/[^\w-]/g, '');
  if (!targetRecipe) return;

  setTimeout(() => {
    const card = document.getElementById(`card-${targetRecipe}`);
    if (card) {
      card.classList.add('highlight-target');
      card.scrollIntoView({ behavior: 'smooth', block: 'center' });
      const toggleBtn = card.querySelector('.btn-toggle-detail');
      if (toggleBtn && !toggleBtn.classList.contains('expanded')) {
        toggleBtn.click();
      }
    }
  }, 250);
}

// Event Listener khusus Hard Delete di Mode Admin
function setupAdminDeleteListeners() {
  document.querySelectorAll('.btn-hard-delete').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const slug = btn.getAttribute('data-slug');
      const target = allRecipes.find(r => (r.slug || r.id) === slug);
      const title = target ? target.title : slug;
      
      if (confirm(`⚠️ YAKIN HARD DELETE?\n\nResep "${title}" akan dihapus permanen dari kurasi lokal ini.`)) {
        allRecipes = allRecipes.filter(r => (r.slug || r.id) !== slug);
        localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(allRecipes));
        
        const card = document.getElementById(`card-${slug}`);
        if (card) {
          card.style.transition = 'all 0.3s ease';
          card.style.opacity = '0';
          card.style.transform = 'scale(0.95)';
          setTimeout(() => {
            card.remove();
            updateAdminCount();
            if (allRecipes.length === 0) {
              renderRecipes([]);
            }
          }, 300);
        }
      }
    });
  });

  // Listener Approve Foto Polesan AI Jadi Foto Utama Resep
  document.querySelectorAll('.btn-approve-photo').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const slug = btn.getAttribute('data-slug');
      const recipe = allRecipes.find(r => (r.slug || r.id) === slug);
      if (recipe && recipe.media?.ai_polished_photo) {
        recipe.og_image = recipe.media.ai_polished_photo;
        recipe.media.status = 'approved';
        localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(allRecipes));
        alert(`✅ Foto polesan AI disetujui sebagai foto utama untuk "${recipe.title}"!`);
        renderRecipes(allRecipes);
      }
    });
  });

  // Listener Tolak Foto Polesan AI
  document.querySelectorAll('.btn-reject-photo').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const slug = btn.getAttribute('data-slug');
      const recipe = allRecipes.find(r => (r.slug || r.id) === slug);
      if (recipe) {
        recipe.media.ai_polished_photo = '';
        recipe.media.status = 'rejected';
        localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(allRecipes));
        alert(`❌ Foto polesan AI ditolak. Resep "${recipe.title}" kembali menggunakan foto asli Cookpad.`);
        renderRecipes(allRecipes);
      }
    });
  });
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
      currentFilter = filter;

      if (isSearching && currentSearchQuery) {
        // Jika sedang mencari, jalankan ulang pencarian dalam lingkup filter kategori baru
        executeSearch(currentSearchQuery);
      } else {
        if (!isAdmin) {
          // Mode Pengunjung: Reset DB pagination ke halaman 1 untuk filter ini (load on-demand)
          currentPage = 1;
          pageCursors = [null];
          loadUserPage(1, filter);
        } else {
          filterRecipes(filter);
        }
      }
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

  let currentSource = 'experience_sticky';

  function openModal(source = 'experience_sticky') {
    currentSource = source;
    if (modal) {
      modal.classList.add('active');
      modal.setAttribute('aria-hidden', 'false');

      // Kustomisasi pesan jika dipicu oleh tombol bookmark di kartu resep (App-Install Hook)
      const modalExpTitle = document.getElementById('modalExpTitle');
      const modalSubtitle = modal.querySelector('.modal-subtitle');
      if (source === 'card_bookmark_hook') {
        if (modalExpTitle) modalExpTitle.textContent = 'Simpan Resep ke Favorit 🤍';
        if (modalSubtitle) modalSubtitle.textContent = 'Mau simpan resep favorit tanpa batas & buka offline di pasar? Dapatkan akses prioritas aplikasi Android Jajan Cuan di Google Play!';
      } else {
        if (modalExpTitle) modalExpTitle.textContent = 'Jadilah yang Pertama Tahu Saat Rilis!';
        if (modalSubtitle) modalSubtitle.textContent = 'Dapatkan akses prioritas ke 100+ resep jajanan viral, kalkulator HPP otomatis, dan bahan promosi siap pakai langsung di HP kamu.';
      }

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

  // Delegasi event listener untuk menangani elemen statis dan tombol dinamis (.btn-card-bookmark)
  document.body.addEventListener('click', (e) => {
    const trigger = e.target.closest('.trigger-waitlist');
    if (trigger) {
      e.preventDefault();
      const source = trigger.getAttribute('data-source') || 'experience_sticky';
      openModal(source);
    }
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

// --- SEARCH ENGINE IMPLEMENTATION ---
function getAllCatalogRecipes() {
  if (isAdmin && typeof adminDrafts !== 'undefined' && Object.keys(adminDrafts).length > 0) {
    return Object.values(adminDrafts).filter(r => r && r.published !== false);
  }
  if (typeof window.SCRAPED_DRAFTS !== 'undefined' && window.SCRAPED_DRAFTS) {
    return Object.values(window.SCRAPED_DRAFTS).filter(r => r && r.published !== false);
  }
  if (typeof SEED_DATA !== 'undefined' && Array.isArray(SEED_DATA)) {
    return SEED_DATA.filter(r => r && r.published !== false);
  }
  return allRecipes;
}

function initSearch() {
  const searchInput = document.getElementById('recipeSearchInput');
  const clearBtn = document.getElementById('clearSearchBtn');
  if (!searchInput) return;

  // Handle Input with Debounce (250 ms)
  searchInput.addEventListener('input', (e) => {
    const query = e.target.value;
    if (clearBtn) {
      clearBtn.style.display = query.trim().length > 0 ? 'inline-flex' : 'none';
    }
    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(() => {
      executeSearch(query);
    }, 250);
  });

  // Handle Clear Button Click
  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      searchInput.value = '';
      clearBtn.style.display = 'none';
      searchInput.focus();
      executeSearch('');
    });
  }

  // Keyboard shortcut: '/' to focus search, 'Escape' to clear
  document.addEventListener('keydown', (e) => {
    if (e.key === '/' && document.activeElement !== searchInput && !['INPUT', 'TEXTAREA'].includes(document.activeElement.tagName)) {
      e.preventDefault();
      searchInput.focus();
      searchInput.select();
    } else if (e.key === 'Escape' && document.activeElement === searchInput) {
      if (searchInput.value) {
        searchInput.value = '';
        if (clearBtn) clearBtn.style.display = 'none';
        executeSearch('');
      }
      searchInput.blur();
    }
  });

  // Check URL query param (?q= or ?search=)
  const queryParam = urlParams.get('q') || urlParams.get('search');
  if (queryParam) {
    searchInput.value = queryParam;
    if (clearBtn) clearBtn.style.display = 'inline-flex';
    executeSearch(queryParam);
  }
}

function executeSearch(rawQuery) {
  const query = (rawQuery || '').trim().toLowerCase();
  currentSearchQuery = query;

  const searchStats = document.getElementById('searchStats');
  const clearBtn = document.getElementById('clearSearchBtn');
  const paginationControls = document.getElementById('paginationControls');

  if (!query) {
    isSearching = false;
    if (searchStats) searchStats.style.display = 'none';
    if (clearBtn) clearBtn.style.display = 'none';

    if (isAdmin) {
      filterRecipes(currentFilter);
    } else {
      if (paginationControls) paginationControls.style.display = 'flex';
      loadUserPage(1, currentFilter);
    }
    return;
  }

  isSearching = true;
  if (clearBtn) clearBtn.style.display = 'inline-flex';
  if (paginationControls) paginationControls.style.display = 'none';

  const allAvailable = getAllCatalogRecipes();
  const searchTerms = query.split(/\s+/).filter(Boolean);

  // Score & filter
  const matches = [];

  allAvailable.forEach((recipe) => {
    if (recipe.published === false) return;

    // Filter kategori aktif jika bukan 'all'
    let matchesCategory = true;
    if (currentFilter !== 'all') {
      const tags = recipe.tags || [];
      const cat = recipe.category || '';
      if (currentFilter === 'modal-kecil') {
        matchesCategory = tags.includes('modal-kecil') || (recipe.business_analysis?.estimated_initial_capital <= 50000);
      } else if (currentFilter === 'viral') {
        matchesCategory = tags.includes('viral');
      } else {
        matchesCategory = (cat === currentFilter) || tags.includes(currentFilter);
      }
    }

    if (!matchesCategory) return;

    const titleLower = (recipe.title || '').toLowerCase();
    const excerptLower = (recipe.excerpt || '').toLowerCase();
    const catLower = (recipe.category || '').toLowerCase();
    const tagsLower = (recipe.tags || []).join(' ').toLowerCase();

    // Check ingredients
    let matchedIngredientName = null;
    let ingredientsText = '';
    if (Array.isArray(recipe.ingredients)) {
      recipe.ingredients.forEach((ing) => {
        const name = (ing.name || '').toLowerCase();
        const raw = (ing.raw_text || '').toLowerCase();
        ingredientsText += ' ' + name + ' ' + raw;

        searchTerms.forEach(term => {
          if ((name.includes(term) || raw.includes(term)) && !matchedIngredientName) {
            matchedIngredientName = ing.name;
          }
        });
      });
    }

    const fullSearchableText = `${titleLower} ${excerptLower} ${catLower} ${tagsLower} ${ingredientsText}`;

    // Setiap kata kunci harus ada di data resep
    const allTermsMatch = searchTerms.every(term => fullSearchableText.includes(term));

    if (allTermsMatch) {
      // Perhitungan skor relevansi
      let score = 0;
      if (titleLower === query) score += 100;
      else if (titleLower.startsWith(query)) score += 80;
      else if (titleLower.includes(query)) score += 60;

      if (matchedIngredientName) score += 45;
      if (tagsLower.includes(query) || catLower.includes(query)) score += 30;
      if (excerptLower.includes(query)) score += 10;

      matches.push({
        recipe: {
          ...recipe,
          _matchedIngredient: matchedIngredientName
        },
        score
      });
    }
  });

  // Urutkan dari skor tertinggi
  matches.sort((a, b) => b.score - a.score);
  const matchedRecipes = matches.map(m => m.recipe);

  // Render resep yang cocok
  renderRecipes(matchedRecipes);

  // Tampilkan badge statistik hasil pencarian
  if (searchStats) {
    searchStats.style.display = 'flex';
    const filterNote = currentFilter !== 'all' ? ` di kategori <strong>${escapeHtml(currentFilter)}</strong>` : '';
    searchStats.innerHTML = `
      <div class="search-stats-text">
        Ditemukan <strong>${matchedRecipes.length}</strong> resep untuk "<strong>${escapeHtml(query)}</strong>"${filterNote}
      </div>
      <button class="search-reset-link" id="statsResetBtn">✕ Reset</button>
    `;

    const statsResetBtn = document.getElementById('statsResetBtn');
    if (statsResetBtn) {
      statsResetBtn.addEventListener('click', () => {
        const searchInput = document.getElementById('recipeSearchInput');
        if (searchInput) {
          searchInput.value = '';
          searchInput.focus();
        }
        executeSearch('');
      });
    }
  }

  // Pelacakan GA4 Event
  if (typeof gtag === 'function') {
    gtag('event', 'recipe_search', {
      search_term: query,
      result_count: matchedRecipes.length,
      filter: currentFilter
    });
  }
}

function setupEmptySearchListeners() {
  const resetBtn = document.querySelector('.btn-reset-search');
  if (resetBtn) {
    resetBtn.addEventListener('click', () => {
      const searchInput = document.getElementById('recipeSearchInput');
      if (searchInput) {
        searchInput.value = '';
        searchInput.focus();
      }
      executeSearch('');
    });
  }

  const clearFilterBtn = document.querySelector('.btn-clear-filter-search');
  if (clearFilterBtn) {
    clearFilterBtn.addEventListener('click', () => {
      const filterButtons = document.querySelectorAll('.filter-tag');
      filterButtons.forEach(b => {
        if (b.getAttribute('data-filter') === 'all') b.classList.add('active');
        else b.classList.remove('active');
      });
      currentFilter = 'all';
      executeSearch(currentSearchQuery);
    });
  }

  document.querySelectorAll('.search-suggestion-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const keyword = chip.getAttribute('data-keyword') || chip.textContent;
      const searchInput = document.getElementById('recipeSearchInput');
      if (searchInput) {
        searchInput.value = keyword;
        searchInput.focus();
      }
      executeSearch(keyword);
    });
  });
}
