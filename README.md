# Product Requirement Document (PRD) & Blueprint Konsep Aplikasi

## Aplikasi Android: Ide Usaha Jajanan Viral

-----

## 1\. Ringkasan Eksekutif & Identitas Produk

  * **Nama Aplikasi:**
      * *Jajan Cuan*
  * **Target Pengguna:** Pemula usaha kuliner rumahan, ibu rumah tangga, dan pelaku UMKM yang mencari ide jualan jajanan viral dengan modal terjangkau serta panduan bisnis praktis.
  * **Value Proposition:** Bukan sekadar buku resep biasa, melainkan *end-to-end business toolkit* yang memandu pengguna mulai dari pemilihan ide jajanan prospektif, panduan masak anti-gagal, kalkulasi modal dan harga jual otomatis, hingga penyediaan aset foto promosi siap pakai.

-----

## 2\. Arsitektur Sistem & Backend (Firebase)

Aplikasi memanfaatkan infrastruktur cloud Firebase untuk kemudahan operasional dan skalabilitas:

| Layanan Firebase                  | Fungsi Spesifik                                                                                                            |
| :-------------------------------- | :------------------------------------------------------------------------------------------------------------------------- |
| **Firebase Remote Config**        | Mengelola parameter aplikasi secara dinamis tanpa update APK (misal: ID unit iklan, resep unggulan mingguan, pengumuman).  |
| **Cloud Firestore / Realtime DB** | Menyimpan database terstruktur: katalog resep, kategori bahan, takaran default, dan data acuan HPP.                        |
| **Firebase Storage**              | Media hosting aset visual resolusi tinggi: foto utama resep, gambar per tahap cara memasak, dan materi promosi siap unduh. |
| **Firebase Hosting**              | Hosting berbasis CDN global untuk Landing Page & Experience Layer, mendukung SPA rewrite native, multi-site, dan SSL gratis.|
| **Google Analytics for Firebase** | Melacak perilaku pengguna: resep paling sering dibuka, interaksi kalkulator, konversi rewarded ads, dan retensi sesi.      |

-----

## 3\. Fitur Utama & Spesifikasi Fungsional

### A. Katalog Resep Jajan Viral (Main Feature)

  * Menyajikan kurasi resep jajanan yang sedang tren dengan perputaran modal cepat dan potensi pasar tinggi.
  * Setiap resep memiliki data terstruktur: Foto Utama, Analisis Bisnis (Pros), Komposisi Bahan, Cara Memasak, Kalkulator Biaya, dan Konten Promosi.

### B. Kalkulator HPP & Harga Jual Dinamis (On-The-Fly)

  * **Input Fleksibel:**
      * Daftar bahan baku beserta harganya dapat diedit langsung oleh pengguna secara *real-time* sesuai harga pasar di daerah masing-masing.
      * Jumlah porsi/biji (*pieces*) yang dihasilkan dari satu resep (nilai acuan dapat diubah).
      * Target laba yang diharapkan (berupa persentase % atau nominal rupiah per porsi).
  * **Output Otomatis:**
      * Total modal belanja bahan per resep.
      * Harga Pokok Penjualan (HPP) per porsi/biji.
      * Rekomendasi harga jual ideal per porsi/biji agar terhindar dari kerugian.

### C. Timer Notifikasi Memasak

  * Tombol timer tertanam langsung di setiap instruksi memasak yang membutuhkan penanda waktu (misal: "Kukus adonan selama 15 menit").
  * Pengguna dapat mengatur label dan durasi waktu memasak.
  * Aplikasi mengirimkan alarm notifikasi lokal (*local push notification*) saat waktu habis sehingga masakan matang sempurna tanpa risiko gosong.

### D. Konten Promosi Siap Pakai (Shareable Assets)

  * Galeri foto jajanan berkualitas tinggi yang sudah ditata menarik (*food styling*) untuk kebutuhan promosi.
  * Fitur interaksi:
      * Tampilan *pop-up viewer* untuk melihat detail gambar.
      * Tombol unduh (*download*) langsung ke galeri ponsel.
      * Tombol berbagi langsung (*one-click share*) ke WhatsApp Status, Instagram Story, Facebook Group, dan media sosial lainnya.

### E. Kategori Berdasarkan Bahan Dasar

  * Pengelompokan resep berdasarkan bahan utama (misal: Tepung Tapioka, Kulit Lumpia, Sosis, Keju, Cokelat).
  * Memudahkan pengguna memanfaatkan stok sisa bahan yang sudah ada di rumah untuk dijadikan produk jualan baru.

-----

## 4\. Struktur Halaman Aplikasi (Sitemap)

### A. Halaman Home

  * **Komponen Iklan:** Banner Ads di sisi layar dan Native Ads yang disisipkan di sela-sela kartu galeri.
  * **Konten:** Galeri *thumbnail* resep jajan viral terbaru dan terpopuler.

### B. Halaman Detail Resep

  * **Foto Utama:** Tampilan visual produk jadi berkualitas tinggi.
  * **Pros (Analisis Bisnis):** Ulasan peluang pasar, tingkat kemudahan bahan dan pembuatan, serta perkiraan modal awal.
  * **Komposisi:** Rincian takaran dan daftar bahan baku.
  * **Cara Memasak (Unlocked via Rewarded Ads):** Panduan bertahap menggunakan *slideshow* foto dan teks petunjuk, dilengkapi tombol pintas ke *Timer Notifikasi*.
  * **Kalkulator (Unlocked via Rewarded Ads):** Alat hitung modal bahan dan penetapan harga jual dinamis.
  * **Konten Promosi (Unlocked via Rewarded Ads):** Galeri foto promosi dengan fungsi *pop-up*, unduh, dan *share*.

### C. Halaman Bahan

  * **Komponen Iklan:** Banner Ads di sisi layar.
  * **Konten:** Galeri *thumbnail* aneka bahan dasar.
  * **Hasil Filter:** Menampilkan daftar resep yang relevan dengan bahan yang dipilih, diselingi Native Ads.

### D. Halaman Settings

  * **Privacy Policy:** Tautan kebijakan privasi yang wajib ada untuk kepatuhan Google Play Store dan jaringan iklan AdMob.
  * **Info Versi:** Keterangan versi build aplikasi dan pembaruan sistem.

-----

## 5\. Alur Pengguna Lengkap (End-to-End User Journey)

### Tahap 1: Eksplorasi Ide Usaha

  * Pengguna membuka aplikasi dan mendarat di halaman **Home** untuk melihat galeri jajanan viral yang sedang tren.
  * Alternatif alur: Jika pengguna memiliki sisa bahan baku di dapur, pengguna membuka tab **Bahan**, memilih bahan yang dimiliki (misal: kulit lumpia), dan melihat daftar resep yang bisa dibuat dari bahan tersebut.
  * Pengguna memilih salah satu resep yang paling menarik minatnya.

### Tahap 2: Validasi Peluang Bisnis

  * Pengguna masuk ke halaman **Detail Resep** dan membaca bagian **Pros (Analisis Bisnis)** untuk memahami potensi keuntungan, daya tarik pembeli, dan ketersediaan bahan di pasaran.
  * Pengguna memeriksa daftar **Komposisi** untuk memastikan bahan-bahannya mudah dijangkau.
  * Keputusan pengguna tervalidasi: *"Jajanan ini mudah dibuat, modalnya terjangkau, dan pasarnya jelas."*

### Tahap 3: Proses Memasak & Produksi

  * Pengguna menekan menu **Cara Memasak**.
  * Aplikasi memunculkan penawaran **Rewarded Video Ads**; pengguna menonton video iklan singkat untuk membuka panduan langkah demi langkah.
  * Pengguna mengikuti instruksi bertahap melalui tampilan *slideshow* foto dan teks.
  * Pada langkah yang memerlukan durasi tertentu (misal: menggoreng atau mengukus), pengguna menekan tombol **Timer**, memasukkan durasi waktu, dan menerima notifikasi alarm tepat waktu saat masakan siap diangkat.

### Tahap 4: Kalkulasi Modal & Harga Jual

  * Pengguna beralih ke fitur **Kalkulator** di halaman detail resep (menonton **Rewarded Video Ads** jika belum terbuka).
  * Pengguna menyesuaikan harga beli bahan baku sesuai nota belanja riil di pasar lokal.
  * Pengguna menentukan target porsi yang dihasilkan dan persentase laba yang diinginkan.
  * Kalkulator secara instan menampilkan HPP per biji dan rekomendasi harga jual pasti cuan.

### Tahap 5: Promosi & Penjualan Pertama

  * Pengguna membuka tab **Konten Promosi** (menonton **Rewarded Video Ads** untuk membuka akses unduh).
  * Pengguna melihat galeri foto promosi yang menggugah selera, lalu menekan tombol *download* atau langsung membagikannya ke WhatsApp Status dan media sosial untuk membuka sistem pemesanan (*Pre-Order*).
  * Calon pembeli merespons, dan transaksi pertama berhasil didapatkan.

### Tahap 6: Retensi & Loyalitas

  * Setelah pesanan pertama selesai, pengguna kembali membuka aplikasi untuk mencari menu jajanan viral berikutnya guna memperbanyak variasi produk jualan.
  * Pengguna juga dapat mengunjungi landing page **Website** atau menonton video panduan di **YouTube** melalui tautan resmi yang tersedia.

-----

## 6\. Strategi Monetisasi (Ad Placement)

  * **Banner Ads:** Tampil statis pada area atas atau bawah di halaman *Home* dan *Bahan*.
  * **Native Ads:** Tampil terintegrasi secara visual di antara kartu thumbnail pada daftar resep di halaman *Home* dan halaman *Filter Bahan*.
  * **Rewarded Video Ads:** Monetisasi dengan nilai eCPM tinggi menggunakan sistem pertukaran manfaat (*value exchange*). Pengguna menonton video iklan berdurasi singkat secara sukarela untuk membuka 3 fitur bernilai tinggi:
    1.  Akses panduan lengkap *Cara Memasak* beserta integrasi *Timer*.
    2.  Akses *Kalkulator HPP & Harga Jual Dinamis*.
    3.  Akses unduh dan berbagi aset *Konten Promosi*.

-----

## 7\. Ekosistem Pemasaran & Listing Google Play Store

### A. Saluran YouTube

  * **Peran:** Wadah hosting video tutorial resmi mengenai cara penggunaan aplikasi (panduan fitur kalkulator HPP, timer masak, dan navigasi resep).
  * **Integrasi:** Tautan video disematkan pada kolom *Promo Video* di listing Google Play Store guna meningkatkan tingkat konversi unduhan (*store conversion rate*).

### B. Website Resmi (Landing Page)

  * **Peran:** Halaman promosi web satu halaman (*single-page landing page*) yang responsif, estetis, dan informatif.
  * **Struktur Halaman Web:**
      * *Hero Section:* Judul/tagline pemikat, mock-up tangkapan layar ponsel, ringkasan manfaat utama, dan badge interaktif *"Download di Google Play (Segera Hadir) / Ingatkan Saya Saat Rilis"* yang memicu pop-up modal pendaftaran prioritas.
      * *Fitur Unggulan:* Ringkasan kelebihan aplikasi (resep viral, hitung modal otomatis, timer masak anti-gagal, foto promosi gratis).
      * *Kepatuhan Kebijakan:* Memuat halaman *Privacy Policy* dan *Terms of Service* yang siap ditautkan pada Google Play Console.

### C. Aset Visual Toko Aplikasi

  * **App Icon:** Ikon aplikasi berukuran 512x512 piksel dengan visual kontras, modern, dan mudah dikenali di layar ponsel pengguna.
  * **Feature Graphic / Banner:** Banner visual berukuran 1024x500 piksel untuk bagian atas listing Google Play Store yang menonjolkan pesan utama: ide usaha jajan viral modal kecil untung maksimal.

-----

## 8. Strategi Website: Landing Page (Indexed) & Experience Layer (No-Index)

### A. Tujuan Keputusan & Pemisahan Dua Lapisan
* **Landing Page (Wajib Diindex):**
  * Fokus pada *brand awareness*, edukasi manfaat produk, dan penemuan organik via Google Search.
  * **Mekanisme Konversi: Soft Opt-in (Opsi C - Pop-Up Modal Interaktif):**
    * Pengunjung bebas menjelajah dan mencoba simulasi tanpa hambatan (*zero gatekeeping*).
    * Modal hanya muncul secara sukarela ketika pengunjung menekan tombol badge *"Download di Google Play (Segera Hadir)"* atau *"Ingatkan Saya Saat Rilis"*.
    * Form mikro (1 kolom: No. WhatsApp / Email) untuk menangkap basis kontak calon pengguna pertama (*early adopters*) yang langsung tersimpan ke Firebase RTDB.
  * Halaman HTML statis yang dioptimasi penuh untuk mesin pencari (SEO lengkap).
* **Halaman Konten / Experience (Tidak Diindex):**
  * Berfungsi sebagai *experience layer* untuk memberikan pengalaman bahwa produk sudah memiliki ekosistem konten yang hidup dan lengkap.
  * Murni sebagai simulasi aplikasi (*proof-of-experience*), bukan untuk mendatangkan traffic organik lewat konten artikel.

### B. Arsitektur Teknis
1. **Landing Page Statis (Diindex):**
   * Tag SEO wajib lengkap: `<title>`, `<meta name="description">`, `<link rel="canonical">`, Open Graph (`og:title`, `og:description`, `og:image`), dan Twitter Card (`twitter:card`).
   * Menjelaskan manfaat, fitur utama (resep viral, kalkulator HPP, foto promosi), dan konteks aplikasi **Jajan Cuan**.
   * Hosting menggunakan **Firebase Hosting** (cepat, stabil, native dalam ekosistem Firebase) dan siap diarahkan ke custom domain dengan SSL otomatis.
2. **Halaman Konten Experience (Tidak Diindex):**
   * Data konten diambil secara dinamis dari **Firebase Realtime Database (RTDB)** (instance `jajan-cuan-default-rtdb` dalam *Test Mode*) atau Firestore (koleksi `posts` / `recipes`).
   * Menampilkan katalog/detail agar website terasa nyata seperti aplikasi fungsional.
   * **Dukungan Routing SPA (Anti-404):** Menggunakan fitur *rewrites* native `firebase.json` mengarah ke `index.html` sehingga navigasi URL dinamis (misal: `/resep/kue-bolu-keju`) dapat di-refresh tanpa error 404.
   * **Pencegahan Indexing:**
     * Disematkan `<meta name="robots" content="noindex, nofollow" />` pada halaman konten/experience.
     * Tidak dicantumkan di `sitemap.xml`.
     * Menghindari pola URL yang mudah dirayapi crawler secara massal.

### C. Kebijakan SEO & Crawler Guardrails
* **`sitemap.xml`:**
  * Hanya mencantumkan 1 URL, yaitu URL landing page utama:
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <url>
        <loc>https://jajan-cuan.web.app/</loc>
        <lastmod>2026-10-18</lastmod>
        <priority>1.0</priority>
      </url>
    </urlset>
    ```
* **`robots.txt`:**
  * Mengizinkan perayapan umum dan mengarahkan ke sitemap:
    ```txt
    User-agent: *
    Allow: /
    Sitemap: https://jajan-cuan.web.app/sitemap.xml
    ```

### D. Skema Data Database (Firebase Realtime Database)

#### 1. Node `/posts/{slug}` (Katalog Resep / Konten Experience)
```json
{
  "title": "Kue Bolu Keju",
  "slug": "kue-bolu-keju",
  "excerpt": "Resep cepat dan layak jual",
  "content": "<p>Isi konten yang ditampilkan ke user</p>",
  "published": true,
  "published_at": "2026-10-01T00:00:00Z",
  "tags": ["kue", "viral"],
  "og_image": "https://example.com/og.jpg",
  "last_updated": "2026-10-10T12:00:00Z"
}
```

#### 2. Node `/waitlist/{pushId}` (Penyimpanan Soft Opt-in Modal)
```json
{
  "contact": "081234567890",
  "type": "whatsapp",
  "source": "hero_playstore_badge",
  "created_at": "2026-10-18T04:18:00Z"
}
```

### E. Analytics & Infrastruktur Hosting (Firebase Ecosystem)
* **Firebase Hosting & Custom Domain:**
  * Folder publik dikonfigurasi ke direktori `web/`.
  * Terintegrasi penuh dalam satu Firebase Console bersama Realtime Database, Storage, dan GA4.
  * Dukungan custom domain dengan sertifikat SSL/TLS otomatis gratis dari Google.
* **Arsitektur Multi-Site Gratis (1 Project Firebase):**
  * Site 1 (`jajan-cuan.web.app` / custom domain): Landing Page & Experience Layer untuk publik.
  * Site 2 (`admin-jajancuan.web.app`): Dashboard internal admin untuk kurasi resep, input data database, dan kelola HPP acuan tanpa biaya tambahan.
* **Konfigurasi SPA & Routing (`firebase.json`):**
  * Rewrite rule terpusat untuk routing SPA pada folder `web/`:
    ```json
    {
      "hosting": {
        "public": "web",
        "ignore": ["firebase.json", "**/.*", "**/node_modules/**"],
        "rewrites": [
          {
            "source": "**",
            "destination": "/index.html"
          }
        ]
      }
    }
    ```
* **Google Analytics 4 (GA4):**
  * Terhubung secara native dengan Firebase Analytics (`G-SKCSWCG5W1`) untuk melacak metrik pengunjung, asal traffic (*acquisition*), durasi sesi, eksplorasi katalog resep, serta konversi pendaftaran modal (`lead_captured`).
* **Kesiapan Masa Depan (Serverless & SSR Ready):**
  * Mendukung integrasi instan ke Firebase Cloud Functions atau Cloud Run jika di masa depan diperlukan fitur Dynamic Open Graph (OG) Images atau Server-Side Rendering (SSR).

### F. Keputusan Arsitektur: Firebase Hosting vs GitHub Pages

| Kebutuhan & Fitur PRD | GitHub Pages | Firebase Hosting | Keunggulan untuk Jajan Cuan |
| :--- | :---: | :---: | :--- |
| **Landing Page Statis (SEO)** | ✅ Sangat Baik | ✅ Sangat Baik | Keduanya cepat berbasis CDN global. |
| **Koneksi Database & Storage** | ⚠️ Terpisah (CORS / Rule setup manual) | 🚀 Native & Terintegrasi | Satu konsol & konfigurasi keamanan terpusat tanpa isu cross-origin. |
| **Dynamic SPA Routing (Anti-404)** | ❌ Perlu *hack* (`404.html`) | ✅ Native lewat `firebase.json` | Navigasi `/resep/:slug` aman saat di-refresh oleh pengunjung. |
| **Multi-Site dalam 1 Project** | ❌ 1 repo = 1 site | ✅ Multi-site gratis | Memisahkan landing page publik dan admin dashboard dalam 1 project. |
| **Preview Channel / PR Branch** | ✅ Via GitHub Actions | ✅ Native via Firebase CLI / Actions | Memudahkan preview branch fitur baru sebelum merge ke production. |
| **Ekstensibilitas Backend / Cloud Functions** | ❌ Tidak Ada | ✅ Didukung Sepenuhnya | Siap untuk dynamic OG image generator & SSR tanpa migrasi platform. |

### G. Prinsip Pelaksanaan: Lean & Anti Over-Engineering
* Landing page harus jelas, estetis, responsif, dan mudah dipahami dalam hitungan detik.
* Konten detail hanya sebagai *proof-of-concept/experience*, bukan sasaran SEO programmatic massal pada tahap awal ini.
* Penggunaan Firebase Hosting dan Realtime Database menyederhanakan stack (*operational simplicity*) karena seluruh komponen teknis berada di bawah satu atap Firebase.


