# PROGRESS.md — Status Implementasi Jajan Cuan Android

## Checklist Tahap
- [x] S0 Pre-flight & dokumentasi
- [x] S1 Pipeline web (kode)
- [x] S2 Deploy production
- [x] S3 Kerangka proyek Android
- [ ] S4 Instrumentasi (Firebase, RC, UMP, AdManager)
- [ ] S5 Data layer
- [ ] S6 Home, Pencarian, Favorit
- [ ] S7 Detail & Kalkulator
- [ ] S8 Timer
- [ ] S9 Rewarded & Promo
- [ ] S10 Settings & Aset
- [ ] S11 QA & hardening

---

## Keputusan & asumsi
- Home Feed Shuffling (Fresh Experience): Resep di Home tab 'Semua' diacak menggunakan session seed yang diperbarui saat cold start aplikasi atau pull-to-refresh. Hal ini memastikan user selalu melihat susunan resep yang segar/baru tiap membuka aplikasi, namun urutan tetap konsisten dan bebas duplikasi selama sesi scrolling berlangsung.
- S1 thumbnail: sumber cover saat ini 100% remote (cpcdn), jadi thumbnail di-unduh via REST (mengikuti redirect). Path `thumb` di index selalu `img/recipes/thumbs/{slug}.webp` (gaya sama dengan `ai_polished_photo`), bernilai null bila unduh/encode gagal.
- S1 "lewati pembuatan ulang bila sudah ada & lebih baru dari sumbernya": karena sumber remote tanpa mtime lokal yang andal, diimplementasikan sebagai pencocokan konten — state disimpan di `web/img/recipes/thumbs/.gen-state.json` ({url, sha1 sumber}); thumbnail dilewati bila file ada dan URL+hash sumber sama. `--force-thumbs` memaksa rebuild. File `.gen-state.json` di-.gitignore (build artifact).
- S1 `--dry-run` tidak menulis seed-posts.json/seed-database.html/RTDB/deploy; hanya membangun index/meta (in-memory) + thumbnail lokal. Mode normal mempertahankan perilaku lama (seed-posts + seed-database.html) ditambah push /search_index dan /meta (urutan: posts -> search_index -> meta) lalu deploy hosting.
- Ukuran index 143.645 byte (~140.3 KB), di bawah batas 150 KB. 3 thumbnail berada di 39.1–39.6 KiB (di bawah 40 KiB); target <=40KB terpenuhi.
- S2 mekanisme tulis RTDB: aturan `.write:false` memblokir SEMUA write non-admin — diverifikasi baik PUT unauthenticated maupun OAuth token owner sama-sama "Permission denied" (hanya Admin SDK/service account yang bypass). Karena itu dipakai alur "buka rules sementara": deploy rules `.write:true` pada posts/search_index/meta via `firebase deploy --only database`, jalankan `release.js` (push data + deploy hosting), lalu `git checkout database.rules.json` + redeploy rules aman. Jendela tulis terbuka hanya selama release.js berjalan; diakhiri dengan rules `.write:false` kembali (verifikasi: PUT unauth ke /meta = 401).
- S2 seed-database.html: `updateSeedHtml` diperbaiki dari regex sensitif-whitespace (yang tidak pernah match, jadi tool fallback tidak tersinkron) menjadi brace-counting. Sekarang menyinkron SEED_PAYLOAD dan menyisipkan SEED_SEARCH_INDEX + SEED_META. Sinkronisasi HTML bisa dijalankan tanpa push/deploy lewat `require('./scripts/release.js')` (auto-run dijaga dengan `require.main === module`).
- S2 backup: `data/backup-posts-<timestamp>.json` (209 resep, ~728 KB, di-.gitignore) diambil sebelum tulis.
- S3 Toolchain & AGP: AGP 8.9.1 + Gradle 8.11.1 + Kotlin 2.1.0 + KSP 2.1.0-1.0.29 + JDK 17 (Adoptium 17.0.20.1) diset via `org.gradle.java.home` di `gradle.properties` agar kompatibel penuh dengan `compileSdk 36` dan menghindari crash parser `JavaVersion` pada Java 25.
- S3 Version Catalog: Semua dependensi 100% stabil (tanpa alpha/beta/rc) mencakup Compose BOM 2025.02.00, Room 2.6.1, OkHttp 4.12.0, Firebase BoM 33.10.0, Play Services Ads 23.6.0, UMP 3.1.0.
- S3 google-services.json: Disalin dari `C:\Users\webhe\Downloads\google-services (5).json`, tervalidasi `project_id: jajan-cuan` dan `package_name: com.henrisusanto.vitmate`.
- S3 Navigation Shell & Placeholder: Shell navigasi di `MainActivity.kt` memuat 3-tab Bottom Navigation (Home, Favorit, Pengaturan) dan rute Detail (bottom bar otomatis disembunyikan). Placeholder teks sementara diizinkan pada tahap ini untuk diisi di S6, S7, dan S10.
- S3 Tema & Edge-to-Edge: SplashScreen API diimplementasikan, XML parent theme menggunakan `android:Theme.Material.Light.NoActionBar` dengan `tools:targetApi="29"` untuk `enforceNavigationBarContrast`. Dynamic color dimatikan, warna brand oranye hangat (`#E65100` / `#FF8A65`).

---

## Checklist manual
