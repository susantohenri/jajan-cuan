# RELEASE_CHECKLIST.md — Jajan Cuan Android

Dokumen ini adalah checklist rilis untuk `com.henrisusanto.vitmate` versi **1.0.0** (versionCode 1,
minSdk 26, compile/targetSdk 36). Jalankan seluruh butir di perangkat fisik atau emulator sebelum
mengunggah ke Play Console.

## 1. Build & verifikasi otomatis

- [ ] `cd android` lalu `.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug assembleRelease`
      selesai dengan `BUILD SUCCESSFUL`.
- [ ] `lintDebug` menghasilkan **0 error** (warning yang tersisa hanya saran, bukan blocker).
- [ ] Semua unit test hijau (154 test, 0 failure/error/skipped pada S11).
- [ ] APK release ada di `app/build/outputs/apk/release/` dan `mapping.txt` ada di
      `app/build/outputs/mapping/release/`.
- [ ] Build release memakai R8 (`isMinifyEnabled=true`, `isShrinkResources=true`).
- [ ] Manifest release tidak memuat `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`.
- [ ] Tidak ada ad unit ID produksi atau secret di repo; build debug memakai test ID.

## 2. Data, sync, dan cache

- [ ] Buka app pertama kali (atau setelah hapus data) -> katalog terisi dari `/search_index`;
      buka salah satu resep -> detail terunduh dari `/posts/{id}.json`.
- [ ] Pull-to-refresh memanggil sync paksa; indikator berhenti setelah selesai.
- [ ] Setelah `/meta.version` berubah di server: index diganti, cache detail lama dibersihkan,
      snapshot favorit dipertahankan/di-refresh; tabel `unlocks` tidak berubah.
- [ ] Mode pesawat saat app dibuka -> tidak crash, memakai cache Room yang ada.
- [ ] `/meta` gagal/404 -> app tetap memakai cache lama tanpa dialog error yang mengganggu.
- [ ] Tidak ada request ke `/posts.json` penuh (hanya per-id).

## 3. Home & pencarian

- [ ] Header brand, search expandable, chip kategori, dan kartu (thumb, judul, estimasi modal,
      porsi, hati) tampil benar.
- [ ] Scroll mendekati item ke-10 -> limit bertambah tanpa duplikasi/lompatan; urutan acak stabil
      selama sesi.
- [ ] Pull-to-refresh -> urutan berubah (seed baru) dan tidak ada duplikasi.
- [ ] Ketik kata kunci -> hasil muncul setelah ±300 ms; pencarian mencakup judul, bahan, tag,
      dan kategori; karakter `%`, `_`, `\` dicari sebagai literal.
- [ ] Kata kunci tanpa hasil -> empty state "Resep dengan kata kunci … tidak ditemukan", chip
      saran, dan tombol "Tampilkan Semua Resep".
- [ ] Ganti kategori -> daftar dan paginasi reset ke halaman pertama.
- [ ] DebugView: `recipe_search` tercatat sekali per term final, `change_category` saat chip
      dipilih, `screen_view` saat berpindah tab.

## 4. Favorit & offline

- [ ] Tekan hati di Home/Detail -> Snackbar + haptic + ikon terisi; tab Favorit ikut bertambah.
- [ ] Tab Favorit menampilkan header "Resep Favorit Saya (N)" dan foto dari file lokal.
- [ ] Hapus favorit -> kartu hilang, Snackbar muncul, file cover/thumb terhapus.
- [ ] Pencarian lokal di Favorit berfungsi (debounce, tanpa chip kategori).
- [ ] Daftar kosong -> ilustrasi, "Belum Ada Resep Favorit", penjelasan, tombol
      "Jelajahi Ide Resep" menuju Home.
- [ ] Setelah minimal satu favorit tersimpan: aktifkan mode pesawat -> kartu, foto, pencarian
      lokal, detail favorit, bahan, langkah, dan kalkulator tetap berfungsi.
- [ ] Tekan hati resep yang belum pernah dibuka saat offline -> Snackbar
      "Perlu internet untuk menyimpan resep ini pertama kali".

## 5. Detail, kalkulator, dan promo

- [ ] Buka resep dari Home -> foto utama, judul, kategori/tag, excerpt, estimasi modal, dan porsi
      tampil; bottom bar hilang; back sistem dan panah kembali berfungsi.
- [ ] Urutan bagian: foto -> Pros/Analisis Bisnis -> Komposisi -> Cara Memasak (selalu terbuka)
      -> Kalkulator HPP -> Konten Promosi.
- [ ] Kalkulator: ubah harga bahan/porsi -> hasil berubah realtime; bandingkan satu resep dengan
      web memakai input identik (harga, porsi, margin) -> Total Modal, HPP/porsi, Harga Jual,
      dan Laba Total sama (harga bulat Rp 500 hanya tambahan tampilan).
- [ ] Mode % dan mode Rp berfungsi; input kosong dianggap 0, porsi minimal 1, tidak ada
      NaN/Infinity/format rusak.
- [ ] Tombol Simpan -> Snackbar "Perhitungan disimpan"; buka lagi resep yang sama -> harga, porsi,
      mode margin, dan hasil tetap.
- [ ] Share resep -> chooser berisi judul + excerpt + "Dapatkan Jajan Cuan: https://jajan-cuan.web.app/".
- [ ] Promo (butuh resep dengan `promo_images` terisi): grid 2 kolom, viewer dengan panah
      sebelumnya/berikutnya, Unduh menyimpan ke `Pictures/JajanCuan`, Share membuka chooser foto.
- [ ] Promo offline -> "Konten promosi membutuhkan internet"; Unduh/Share tidak mencatat event.
- [ ] API 26–28: Unduh pertama kali meminta `WRITE_EXTERNAL_STORAGE`; API 29+ tidak meminta izin.

## 6. Timer memasak

- [ ] Langkah berdurasi menampilkan tombol "Nyalakan Timer (…)"; dialog label + durasi terisi
      otomatis dan bisa diedit.
- [ ] Timer 1 menit, tutup app (bukan force stop) -> notifikasi "Waktu Memasak Selesai" muncul
      berisi label + nama resep; tap membuka resep terkait.
- [ ] Izin `POST_NOTIFICATIONS` diminta sekali saat timer pertama; jika ditolak, countdown in-app
      tetap berjalan dan Snackbar menjelaskan cara mengaktifkan izin.
- [ ] Dua timer aktif tampil dengan countdown masing-masing; batalkan salah satu -> hanya timer
      itu yang hilang dan alarmnya tidak berbunyi.
- [ ] Android 12+ memakai alarm inexact (tanpa izin exact alarm) sehingga notifikasi bisa
      meleset beberapa detik/menit; Android 11 ke bawah memakai alarm clock tepat waktu.
- [ ] Timer tidak dijadwalkan ulang setelah reboot perangkat (batasan yang diterima).

## 7. Iklan, consent, dan rewarded

- [ ] Urutan log: UMP `requestConsentInfoUpdate` -> form selesai -> `MobileAds.initialize` ->
      request iklan; tidak ada iklan sebelum consent.
- [ ] `isAdsEnabled=false` atau `rewardedEnabled=false`: semua iklan hilang dan fitur terkunci
      terbuka tanpa menulis baris `unlocks`.
- [ ] Dengan iklan aktif: native muncul setiap `nativeEveryN` kartu di Home/Favorit, banner
      adaptif menempel tepat di atas bottom nav tanpa menutupi konten, dan tidak ada slot kosong.
- [ ] Rewarded: kalkulator dan promo terkunci per resep; "Menyiapkan iklan…" nonaktif, lalu
      "Tonton iklan untuk membuka {fitur}" aktif; dialog Batal/Tonton berfungsi.
- [ ] Setelah reward: fitur terbuka, baris `unlocks` tertulis, `rewarded_unlock` tercatat, dan
      setelah restart fitur tetap terbuka tanpa iklan lagi.
- [ ] Fallback: timeout/no-fill -> fitur terbuka sementara + Snackbar
      "Iklan belum tersedia, fitur dibuka sementara untuk sesi ini", `via_fallback=true`, tidak
      ada baris `unlocks`; force stop -> fitur terkunci lagi.
- [ ] `jc_ad_paid` (value/currency/ad_format/ad_unit_name/ad_source/screen) tercatat di DebugView
      untuk banner, native, dan rewarded.
- [ ] Debug build hanya memakai test ad unit `ca-app-pub-3940256099942544/...`.

## 8. Settings, tema, dan tampilan

- [ ] Pilih Terang/Gelap -> seluruh layar berubah seketika tanpa restart; Ikuti Sistem mengikuti
      tema perangkat; setelah force stop pilihan terakhir tetap.
- [ ] `change_theme` + user property `theme_mode` tercatat hanya saat nilai berubah.
- [ ] Versi menampilkan "1.0.0"; Kebijakan Privasi membuka
      `https://susantohenri.github.io/privacy-policy`.
- [ ] "Pengaturan Privasi Iklan" hanya tampil saat UMP REQUIRED; tap membuka formulir opsi privasi.
- [ ] Launcher: label "Jajan Cuan", ikon cone + jajanan + koin; themed icon Android 13+ memakai
      varian monochrome; splash memakai ikon baru; tidak ada PNG per density.
- [ ] Edge-to-edge: konten tidak tertutup status/navigation bar; bottom nav dan banner aman.
- [ ] Tablet/landscape: konten maksimal 840 dp dan terpusat di Home, Favorit, Detail, Settings.
- [ ] Tema gelap: semua layar, dialog, form kalkulator, dan Snackbar tetap terbaca.
- [ ] Predictive back (Android 13+) dan back sistem bekerja di semua layar.

## 9. Rilis & Play Console (butuh tindakan manual)

- [ ] **Ganti ad unit ID produksi** di Remote Config dan **App ID produksi** di
      `AndroidManifest.xml`; uji unit produksi memakai test device (ID dari logcat).
- [ ] **Siapkan keystore atau Play App Signing**; generate AAB sendiri dengan
      `.\gradlew.bat bundleRelease` (tidak ada keystore di repo).
- [ ] **Isi Play Console**: Data Safety, Target Audience, dan Ads declaration.
- [ ] **Perbarui Privacy Policy** agar menyebut AdMob, Firebase Analytics/Crashlytics, dan
      Advertising ID.
- [ ] **Tautkan AdMob ke Firebase** bila ingin revenue masuk GA4 (`jc_ad_paid` tetap aman dari
      double count).
- [ ] **Sesuaikan PRD**: timer gratis; unlock rewarded permanen per resep dan fitur;
      App Open/Interstitial tidak dipakai; Favorit memakai snapshot offline.
- [ ] **Pantau kuota Hosting Spark** (360 MB/hari) setelah rilis.
- [ ] **Unggah aset** dari `./assets` ke listing (icon 512×512, feature graphic 1024×500).
- [ ] Pastikan `google-services.json` tersedia di mesin build (tidak di-commit).
- [ ] Simpan `mapping.txt` release untuk deobfuscation Crashlytics.

## 10. Batasan yang diketahui

- Timer memakai alarm inexact di Android 12+ karena izin exact alarm dilarang; waktu bisa meleset.
- Timer aktif tidak dipulihkan setelah reboot perangkat (sesuai keputusan produk).
- Seluruh 209 resep seed saat ini memiliki `promo_images` kosong, sehingga bagian promo tidak
  tampil sampai data promo diisi.
- Remote Config produksi masih `isAdsEnabled=false`; iklan dan gate rewarded baru aktif setelah
  flag diubah di Firebase Console.
- Release APK saat ini unsigned; AAB/keystore disiapkan manual oleh pemilik proyek.
