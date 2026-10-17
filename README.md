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

  * **Peran:** Halaman promosi web satu halaman (*single-page landing page*) yang responsif dan elegan.
  * **Struktur Halaman Web:**
      * *Hero Section:* Judul/tagline pemikat, mock-up tangkapan layar ponsel, dan tombol *Call to Action (CTA)* utama yang mengarahkan pengunjung langsung ke listing Google Play Store.
      * *Fitur Unggulan:* Ringkasan kelebihan aplikasi (resep viral, hitung modal otomatis, timer masak anti-gagal, foto promosi gratis).
      * *Kepatuhan Kebijakan:* Memuat halaman *Privacy Policy* dan *Terms of Service* yang wajib ditautkan pada Google Play Console.

### C. Aset Visual Toko Aplikasi

  * **App Icon:** Ikon aplikasi berukuran 512x512 piksel dengan visual kontras, modern, dan mudah dikenali di layar ponsel pengguna.
  * **Feature Graphic / Banner:** Banner visual berukuran 1024x500 piksel untuk bagian atas listing Google Play Store yang menonjolkan pesan utama: ide usaha jajan viral modal kecil untung maksimal.
