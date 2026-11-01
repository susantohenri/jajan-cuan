# DATA_SCHEMA.md — Skema Data & Kontrak Logika Bisnis Jajan Cuan

Dokumen ini memotret skema nyata dari `data/seed-posts.json`, perilaku penanganan gambar pada `scripts/release.js` dan `web/js/experience.js`, serta formula kalkulator HPP dan mekanisme rilis ke cloud.

---

## a. Skema Nyata `/posts`

### Metadata File Sumber
* **Path:** `data/seed-posts.json`
* **Jumlah Resep:** 209 resep (semua berstatus `published: true`).
* **Ukuran File:** 950.513 bytes (~928 KB).

### Kamus Field (Per Resep)
| Field | Tipe | Status | Keterangan & Format |
|---|---|---|---|
| `id` | String | Wajib | ID unik resep (sama dengan slug, misal `"mini-spaghetti-ekonomis-untuk-jualan-2ribuan"`). |
| `slug` | String | Wajib | Slug URL ramah SEO. |
| `title` | String | Wajib | Judul resep tampilan. |
| `source_url` | String | Opsional | URL sumber resep Cookpad asli. |
| `excerpt` | String | Wajib | Ringkasan singkat estimasi modal dan porsi (contoh: `"Resep ide jualan modal ~Rp 39.000, hasil 25 porsi (25 mika)."`). |
| `category` | String | Wajib | Kategori utama: `"gorengan"`, `"kue"`, atau `"minuman"`. |
| `tags` | Array of String | Wajib | Label pencarian & filter, misal `["gorengan", "modal-kecil", "viral"]`. |
| `published` | Boolean | Wajib | Status tayang (`true`/`false`). |
| `created_at` | String (ISO 8601) | Opsional | Timestamp pembuatan data. |
| `yield_text` | String | Opsional | Deskripsi porsi dari sumber asli (misal `"25 mika"`). |
| `default_portions` | Integer | Wajib | Angka porsi acuan (contoh: `25`). |
| `business_analysis` | Object | Wajib | Analisis bisnis & pasar: <br>• `market_demand` (String)<br>• `ease_of_making` (String)<br>• `ingredient_availability` (String)<br>• `estimated_initial_capital` (Integer, total modal acuan dalam Rupiah, misal `39000`). |
| `ingredients` | Array of Object | Wajib | Daftar bahan baku. Tiap elemen terdiri dari:<br>• `name` (String, nama bahan)<br>• `raw_text` (String, takaran + nama bahan)<br>• `quantity` (String, takaran, misal `"200 gr"`, `"secukupnya"`)<br>• `default_price` (Integer, estimasi harga pasar bahan dalam Rupiah). |
| `cooking_steps` | Array of String | Wajib | Urutan langkah memasak dari awal sampai siap saji. |
| `media` | Object | Wajib | Metadata aset gambar:<br>• `source_photo` (String, URL gambar asli Cookpad)<br>• `ai_polished_photo` (String, path relatif foto polesan AI di web, misal `"img/recipes/polished/{slug}.jpg"`)<br>• `status` (String, status kurasi: `"ready_for_review"`, `"approved"`, atau `"rejected"`)<br>• `notes` (String)<br>• `mode` (String)<br>• `polished_at` (String, ISO 8601). |
| `promo_images` | Array of String | Wajib | Daftar URL/path gambar promosi WhatsApp/IG (saat ini bernilai array kosong `[]`). |
| `original_title` | String | Opsional | Judul asli sebelum kurasi. |
| `title_polished` | String | Opsional | Judul hasil kurasi AI. |
| `title_polished_at` | String (ISO 8601) | Opsional | Timestamp kurasi judul. |
| `og_image` | String | Wajib | URL gambar cover saat ini (menunjuk ke `source_photo` atau `ai_polished_photo`). |
| `last_updated` | String (ISO 8601) | Wajib | Timestamp pembaruan terakhir. |

### 1 Contoh Item Nyata (Dipangkas)
```json
{
  "id": "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
  "slug": "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
  "title": "Spaghetti Mini Saus Sosis",
  "source_url": "https://cookpad.com/id/resep/26729440",
  "excerpt": "Resep ide jualan modal ~Rp 39.000, hasil 25 porsi (25 mika).",
  "category": "gorengan",
  "tags": [
    "gorengan",
    "modal-kecil",
    "viral"
  ],
  "published": true,
  "created_at": "2026-10-17T23:37:36.148Z",
  "yield_text": "25 mika",
  "default_portions": 25,
  "business_analysis": {
    "market_demand": "Tinggi — Spaghetti Mini Saus Sosis memiliki target pasar jelas di kalangan anak sekolah...",
    "ease_of_making": "Mudah — dapat diproduksi dalam skala rumahan...",
    "ingredient_availability": "Sangat mudah — seluruh bahan baku dapat ditemukan di toko kelontong...",
    "estimated_initial_capital": 39000
  },
  "ingredients": [
    {
      "name": "Pasta spaghetti merk Sedani",
      "raw_text": "200 gr Pasta spaghetti merk Sedani",
      "quantity": "200 gr",
      "default_price": 9000
    },
    {
      "name": "sosis merk Geboyku",
      "raw_text": "2 buah sosis merk Geboyku",
      "quantity": "2 buah",
      "default_price": 3000
    },
    {
      "name": "Secukupnya keju parut merk Wincheese",
      "raw_text": "Secukupnya keju parut merk Wincheese",
      "quantity": "secukupnya",
      "default_price": 10000
    }
  ],
  "cooking_steps": [
    "Didihkan air dan beri sedikit minyak dan garam, setelah air mendidih, masukkan pasta spaghetti dan rebus hingga matang ±7menit",
    "Setelah spaghetti matang, tiriskan ke dalam wadah dan beri sedikit minyak...",
    "Spaghetti ekonomis siap dijual✨"
  ],
  "media": {
    "source_photo": "https://img-global.cpcdn.com/recipes/cb921223baaca69f/1200x630cq80/photo.jpg",
    "ai_polished_photo": "img/recipes/polished/mini-spaghetti-ekonomis-untuk-jualan-2ribuan.jpg",
    "status": "ready_for_review",
    "notes": "Foto asli Cookpad sebagai acuan grounding wadah/porsi. Belum melalui polesan AI.",
    "mode": "cloudflare_flux",
    "polished_at": "2026-10-18T01:50:40.928Z"
  },
  "promo_images": [],
  "og_image": "https://img-global.cpcdn.com/recipes/cb921223baaca69f/1200x630cq80/photo.jpg",
  "last_updated": "2026-10-18T03:39:57.556Z"
}
```

---

## b. Aturan URL Gambar di Web

Sesuai kode nyata di `scripts/release.js` (baris 23–27) dan `web/js/experience.js` (baris 590, 596–598, 953–981):

1. **Cover Photo (`cover`):**
   * Logika penentuan di pipeline `release.js`:
     ```javascript
     const coverPhoto = (recipe.media?.status === 'approved' && recipe.media?.ai_polished_photo)
       ? recipe.media.ai_polished_photo
       : (recipe.media?.source_photo || recipe.og_image || '');
     ```
   * Jika `status === 'approved'` dan `ai_polished_photo` tersedia: memakai path foto polesan (misal `img/recipes/polished/{slug}.jpg`).
   * Jika belum di-approve atau ditolak: memakai foto asli remote `source_photo` (misal `https://img-global.cpcdn.com/recipes/cb921223baaca69f/1200x630cq80/photo.jpg`).
   * Resolusi di Android/Klien: Jika path diawali `img/`, URL absolutnya adalah `https://jajan-cuan.web.app/` + path tersebut. Jika sudah berawalan `http://` atau `https://`, gunakan langsung.
   * Fallback jika gagal muat (HTTP error/network offline): ditampilkan emoji kategori (`🥟` untuk gorengan, `🧀` untuk kue, `🧋` untuk minuman, `🍳` lainnya).

2. **Thumbnail (`thumb`):**
   * Di S1, thumbnail 400x400 WebP akan dibuat di `web/img/recipes/thumbs/{slug}.webp`.
   * URL publiknya: `https://jajan-cuan.web.app/img/recipes/thumbs/{slug}.webp`. Jika null/gagal, fallback ke cover photo.

3. **Konten Promosi (`promo_images`):**
   * Field `recipe.promo_images` menampung daftar string path/URL foto promosi. Saat ini di `seed-posts.json` berisi array kosong `[]`.

---

## c. Sumber Perhitungan Nilai Kartu Web

Sesuai `web/js/experience.js` (baris 586–590, 1400):
1. **`est_capital` (Estimasi Modal):**
   * Diambil dari: `recipe.business_analysis?.estimated_initial_capital || 35000`.
   * **Fakta Data:** Nilai `estimated_initial_capital` pada ke-209 resep terbukti 100% tepat sama dengan jumlah total `default_price` seluruh item di `ingredients`.
2. **`portions` (Hasil Porsi):**
   * Diambil dari: `recipe.default_portions || 20`.
3. **`is_viral`:**
   * Diambil dari tag: `(recipe.tags || []).includes('viral')`.

---

## d. Rumus Kalkulator HPP di Web & Golden Test

### Rumus Langkah demi Langkah (`web/js/experience.js` baris 1070–1096)
1. **Total Modal Bahan (`totalModal`):**
   $$\text{totalModal} = \sum (\text{harga bahan per baris})$$
   *(Default tiap baris adalah `ing.default_price`. Input kosong dianggap 0).*
2. **Target Porsi (`targetPortions`):**
   $$\text{targetPortions} = \max(1, \text{input porsi})$$
   *(Default `recipe.default_portions`).*
3. **HPP per Porsi (`hppPerPortion`):**
   $$\text{hppPerPortion} = \frac{\text{totalModal}}{\text{targetPortions}}$$
4. **Harga Jual Rekomendasi (`sellingPrice`):**
   * **Mode % (Web default):**
     $$\text{sellingPrice} = \text{hppPerPortion} \times \left(1 + \frac{\text{marginPercent}}{100}\right)$$
     *(Web default margin = 100%, rentang slider 20% – 250%).*
   * **Mode Rp Nominal (Spesifikasi Android AGENTS.md §7.5):**
     $$\text{sellingPrice} = \text{hppPerPortion} + \text{labaNominalPerPorsi}$$
5. **Estimasi Laba Total (`totalProfit`):**
   $$\text{totalProfit} = (\text{sellingPrice} - \text{hppPerPortion}) \times \text{targetPortions}$$
6. **Pembulatan ke Atas Kelipatan Rp 500 (Aturan Android AGENTS.md §7.5):**
   $$\text{sellingPriceRounded} = \lceil \text{sellingPrice} / 500 \rceil \times 500$$
7. **Format Angka Rupiah:**
   `'Rp ' + Math.round(num).toLocaleString('id-ID')` (contoh: `Rp 3.120`).

---

### Golden Test Numerik Nyata (Resep: `mini-spaghetti-ekonomis-untuk-jualan-2ribuan`)

#### Data Input Acuan
* **10 Bahan Baku:**
  1. Pasta spaghetti: Rp 9.000
  2. Sosis: Rp 3.000
  3. Saus tomat: Rp 3.000
  4. Saus cabe: Rp 2.000
  5. Garam: Rp 500
  6. Oregano: Rp 2.000
  7. Kecap manis: Rp 1.500
  8. Bawang putih: Rp 1.000
  9. Minyak goreng: Rp 7.000
  10. Keju parut: Rp 10.000
* **Porsi Default:** 25 pcs
* **Target Margin:** 100% (Mode Persentase)

#### Hasil Perhitungan (Golden Output)
* **Total Modal Bahan:** $\text{Rp } 39.000$
* **HPP per Porsi:** $\frac{39000}{25} = \text{Rp } 1.560$
* **Harga Jual (Mode % 100%, murni):** $1560 \times (1 + 1.0) = \text{Rp } 3.120$
* **Harga Jual Dibulatkan (Kelipatan Rp 500 ke atas):** $\lceil 3120 / 500 \rceil \times 500 = \text{Rp } 3.500$
* **Laba per Porsi (berdasarkan harga jual murni):** $3120 - 1560 = \text{Rp } 1.560$
* **Laba Total (berdasarkan harga jual murni):** $1560 \times 25 = \text{Rp } 39.000$
* **Laba per Porsi (berdasarkan harga bulat Rp 3.500):** $3500 - 1560 = \text{Rp } 1.940$
* **Laba Total (berdasarkan harga bulat Rp 3.500):** $1940 \times 25 = \text{Rp } 48.500$

*(Catatan Mode Nominal: Jika target laba Rp 1.000/porsi $\rightarrow$ harga jual murni = Rp 2.560, harga bulat = Rp 3.000, laba total = Rp 25.000).*

---

## e. Mekanisme `release.js` Menulis ke RTDB & Deploy Hosting

Sesuai `scripts/release.js`:
1. **Sumber Data:** Membaca `data/scraped-drafts.json`, menyaring `recipe.published !== false`.
2. **File Lokal:** Menyimpan ke `data/seed-posts.json` dan menyuntikkan payload ke variabel `const SEED_PAYLOAD` di `scripts/seed-database.html`.
3. **Metode Tulis RTDB:**
   * Melakukan request HTTPS `PUT` ke `https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app/posts.json`.
   * **Autentikasi:** Saat ini dilakukan secara *unauthenticated* (tanpa header Authorization atau parameter `auth`).
4. **Deploy Hosting:**
   * Menjalankan perintah terminal: `firebase deploy --only hosting` (dengan fallback `npx -p firebase-tools firebase deploy --only hosting`).

---

## f. Pertanyaan Terbuka & Ambiguitas

1. **Izin Tulis RTDB vs `.write: false`:**
   Pada `database.rules.json`, node `posts` memiliki aturan `".write": false`. Akibatnya, request `PUT` unauthenticated dari `release.js` akan ditolak (HTTP 401). Selama ini fallback-nya diarahkan membuka `seed-database.html`.
   *Untuk tahap S2, penulisan ke RTDB `/posts`, `/search_index`, dan `/meta` perlu mekanisme terautentikasi (misal token Firebase CLI atau aturan admin).*
2. **Konten `promo_images` Kosong:**
   Saat ini ke-209 resep memiliki `promo_images: []`. Di tahap S9, fitur Promo disembunyikan otomatis jika array kosong.
3. **Perubahan Belum Di-commit di Git:**
   Terdapat file `execute-content-pipeline.prompt.md` yang sedang dalam status *modified* di root workspace sebelum tahap S0 dimulai.
