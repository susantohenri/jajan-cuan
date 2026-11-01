# AGENTS.md — Jajan Cuan Android (aturan tetap untuk semua tahap)

## 1. IDENTITAS
Nama: Jajan Cuan. applicationId dan namespace: com.henrisusanto.vitmate. compileSdk 36, targetSdk 36, minSdk 26, versionName 1.0.0, versionCode 1.
Firebase project: jajan-cuan. RTDB: https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app. Hosting: https://jajan-cuan.web.app/. Privacy Policy: https://susantohenri.github.io/privacy-policy.
Aplikasi HANYA berbahasa Indonesia: tanpa values-en, tanpa pilihan bahasa, tanpa locales_config. Semua teks UI lewat string resources.

## 2. PROTOKOL TAHAP (WAJIB)
1. Kerjakan HANYA tahap yang diminta. Jangan mengerjakan tahap berikutnya walau mudah.
2. Dilarang git add/commit/push/reset/stash/checkout/rebase. Hanya boleh git status/diff/log. Saya yang review dan commit.
3. Awal tahap: baca AGENTS.md, PROGRESS.md, DATA_SCHEMA.md, lalu git status. Jika ada perubahan belum di-commit yang bukan bagian tahap ini, laporkan dan tanya sebelum lanjut.
4. Tanpa placeholder: dilarang TODO, stub, mock, atau fungsi kosong untuk fitur dalam lingkup tahap. Pengecualian hanya yang disebut eksplisit di prompt tahap. Yang tidak selesai ditulis jujur sebagai "belum selesai" beserta alasan. Jangan klaim selesai.
5. Jangan menebak skema atau logika; sumber kebenaran adalah data nyata dan kode web. Jika ragu pada hal berisiko: STOP dan tanya. Asumsi kecil dicatat di PROGRESS.md bagian "Keputusan & asumsi".
6. Verifikasi dengan menjalankan perintah yang diminta dan melaporkan hasil NYATA (perintah + hasil/exit status). Jika gagal, perbaiki; maksimal 3 percobaan per masalah, lalu STOP dan laporkan.
7. Diff kecil dan fokus: jangan refactor atau format ulang file di luar lingkup; jangan ubah versi dependensi di luar kebutuhan tahap.
8. LAPORAN BAKU di akhir tahap (setelah PROGRESS.md diperbarui):
   a. Ringkasan (maks 5 baris)
   b. File dibuat/diubah (ringkas)
   c. Hasil verifikasi (perintah + hasil)
   d. Asumsi dan penyimpangan dari prompt
   e. Belum selesai / risiko
   f. Fokus review: maksimal 5 file terpenting untuk saya baca
   g. Saran pesan commit (Conventional Commits, bahasa Inggris)
   h. Checklist manual untuk saya (jika ada)
   Lalu STOP dan tunggu prompt tahap berikutnya.

## 3. AREA & KEAMANAN
- Kerja Android di ./android. Di Windows pakai gradlew.bat.
- web/, scripts/, data/, database.rules.json: READ-ONLY, kecuali tahap S1 (file yang diizinkan disebut di prompt S1).
- Operasi TULIS ke production (RTDB/Hosting) HANYA di S2.
- Tanpa secret hardcode; jangan buat atau commit keystore. .gitignore wajib memuat: local.properties, *.jks, *.keystore, .env, build/, node_modules/, data/backup-*.json.

## 4. TEKNOLOGI & ATURAN BUILD
- Kotlin 2.x + Compose compiler plugin, KSP sesuai versi Kotlin, Jetpack Compose + Material 3. MVVM (ViewModel, Repository, Room). DI manual (AppContainer), tanpa Hilt, tanpa over-engineering.
- Versi library: stable terbaru yang saling kompatibel, diverifikasi lewat dokumentasi resmi/Maven. Dilarang alpha/beta/rc. Gradle/AGP harus mendukung compileSdk 36 (verifikasi tabel kompatibilitas resmi; kira-kira minimal AGP 8.9.1 + Gradle 8.11.1, atau lebih baru yang stable). Batas bawah: play-services-ads >= 23.6.0, user-messaging-platform >= 3.1.0, minSdk 26. Jangan menekan warning compileSdk.
- Android 16: edge-to-edge, predictive back (enableOnBackInvokedCallback=true), jangan kunci orientasi, layout adaptif (batasi lebar konten di tablet), SplashScreen API.
- Manifest: allowBackup=false. Izin yang boleh: INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, WRITE_EXTERNAL_STORAGE (maxSdkVersion=28). DILARANG: SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM. R8 aktif di release (minify + shrink) dengan keep rules seperlunya.
- Warna brand hangat (oranye/kuning); dynamic color nonaktif.

## 5. KONTRAK DATA RTDB
- /posts/{id}: data penuh per resep (sudah ada; skema nyata di DATA_SCHEMA.md). JANGAN pernah mengunduh seluruh /posts.json dari Android.
- /search_index/{id} (dibuat S1), id = key yang sama dengan /posts/{id}: id, slug, title, category, tags (array), ingredients_text (nama bahan lowercase digabung spasi), cover (nilai PERSIS yang dipakai kartu web), thumb (path thumbnail 400x400 WebP, atau null), est_capital (angka), portions (angka), is_viral (boolean).
- /meta (dibuat S1): { "version": "<ISO timestamp rilis>", "count": <jumlah>, "schema": 1 }.
- Urutan tulis di release.js: /posts, lalu /search_index (PUT seluruh node agar resep yang dihapus ikut hilang), lalu /meta TERAKHIR (klien tidak boleh melihat versi baru sebelum datanya siap).
- Hanya resep published !== false yang masuk index. Rules: /search_index dan /meta public read; write tidak lebih longgar dari /posts.

## 6. ARSITEKTUR DATA ANDROID (online-first + cache lokal)
- Akses RTDB hanya lewat REST (OkHttp + kotlinx.serialization; ignoreUnknownKeys, coerceInputValues; field nullable atau berdefault). DILARANG memakai SDK firebase-database.
- Endpoint: /meta.json, /search_index.json, /posts/{id}.json.
- Room (exportSchema=true):
  - recipe_index: id, slug, title, category, tagsText, ingredientsText, cover, thumb, estCapital, portions, isViral, orderIdx.
  - recipe_detail: id, json, fetchedAt (cache detail non-favorit, LRU 30 item).
  - favorites: id, favoritedAt, title, category, tagsText, ingredientsText, estCapital, portions, thumbLocalPath, coverLocalPath, detailJson. Snapshot mandiri, TIDAK bergantung pada recipe_index.
  - unlocks: recipeId, feature (CALCULATOR | PROMO), unlockedAt; primary key (recipeId, feature). PERMANEN: tidak punya kedaluwarsa dan tidak terhapus oleh sync, pembuangan cache, atau penghapusan favorit.
  - calc_state: recipeId, pricesJson, portions, marginMode, marginValue.
  - active_timers.
- Sync: saat app dibuka, pull-to-refresh, dan kembali ke foreground (throttle metaCheckIntervalHours), ambil /meta. Jika version berbeda dari yang tersimpan: ganti isi recipe_index dari /search_index, kosongkan recipe_detail, lalu di background ambil ulang detail tiap favorit via /posts/{id}.json dan perbarui snapshot-nya. Jika server mengembalikan null/404 untuk sebuah favorit, pertahankan salinan lokal. Gagal jaringan: diam, pakai cache. Tabel unlocks tidak disentuh.
- Paginasi & Pengacakan Home: sepenuhnya lokal. Pada tab Home/Semua saat eksplorasi tanpa pencarian, urutan resep disajikan secara acak (shuffled) berbasis session seed yang dibuat setiap kali aplikasi dibuka (cold start) atau saat pull-to-refresh, sehingga katalog selalu terasa segar tiap kali dibuka tanpa lompat/duplikasi data saat paginasi scroll. Query Room dengan LIMIT :limit; limit awal = pageSize (default 10); saat scroll mendekati akhir (sisa <= 3 item) limit += pageSize; reset ke pageSize saat kategori atau kata kunci berubah. Chip kategori dari kategori distinct di index.
- Detail: urutan sumber = favorites.detailJson, lalu recipe_detail, lalu jaringan (spinner; error ramah + tombol "Coba Lagi"); hasil jaringan disimpan ke recipe_detail.
- Favorit: toggle hati menyimpan snapshot lengkap (ambil detail dulu bila belum ada; jika offline dan belum ada cache, Snackbar "Perlu internet untuk menyimpan resep ini pertama kali"), mengunduh thumbnail DAN foto cover penuh ke filesDir/covers/{id}. Hapus favorit = hapus baris + file. Favorit dan Detail-dari-Favorit HARUS jalan penuh saat offline (foto, bahan, langkah, kalkulator).
- Gambar: Coil (disk cache 100 MB). ImageUrlResolver meniru PERSIS logika URL gambar web (cover, thumb, promo) sesuai DATA_SCHEMA.md; fallback placeholder emoji kategori. Kartu list memakai thumb (fallback cover bila thumb null); foto penuh hanya di Detail dan Promo; kartu Favorit memakai file lokal.
- Timer yang aktif tidak dipulihkan setelah reboot perangkat (diterima).

## 7. NAVIGASI & LAYAR
Bottom nav 3 tab: Home, Favorit, Settings. Bottom bar disembunyikan di Detail. Tidak ada layar kalkulator terpisah.
1. Home: header brand + search bar expandable; chip kategori (Semua, dst.); LazyColumn kartu (thumb, judul, estimasi modal, porsi, ikon hati); native ad setiap nativeEveryN item; banner adaptif sticky tepat di atas bottom nav (tinggi dicadangkan agar layout tidak melompat).
2. Pencarian: StateFlow + debounce(300) + distinctUntilChanged; Room LIKE (escape % dan _) pada title, ingredientsText, tagsText, category di recipe_index; digabung chip kategori. Empty state: "Resep dengan kata kunci '{query}' tidak ditemukan", chip saran, tombol "Tampilkan Semua Resep". Log recipe_search.
3. Favorit: header "Resep Favorit Saya (N)"; kartu dengan tombol hapus bookmark; pencarian lokal; native ad di sela kartu; banner. Empty state: ilustrasi vektor, "Belum Ada Resep Favorit", penjelasan singkat, tombol "Jelajahi Ide Resep" menuju Home. Toggle hati: Snackbar + haptic; status konsisten di Home, Favorit, Detail.
4. Detail: action bar (favorit; share teks: judul + excerpt + "Dapatkan Jajan Cuan: https://jajan-cuan.web.app/"). Urutan: foto utama; Pros/Analisis Bisnis; Komposisi (bahan, takaran, harga acuan); Cara Memasak (SELALU terbuka); Kalkulator HPP (terkunci Rewarded per resep); Konten Promosi (terkunci Rewarded per resep). Langkah yang menyebut durasi (regex menit/jam/detik; rentang "10-15 menit" pakai nilai terbesar) mendapat tombol "Nyalakan Timer" — GRATIS.
5. Kalkulator HPP: IKUTI logika di web/js/experience.js (hasil identik dengan web untuk input sama; rumusnya ada di DATA_SCHEMA.md). Biaya bahan editable per baris (default harga acuan), porsi editable (default dari data), target laba toggle % atau Rp per porsi. Jika web tidak mendefinisikan: totalModal = jumlah biaya baris; HPP/porsi = totalModal / porsi; mode %: hargaJual = HPP x (1 + %/100); mode Rp: hargaJual = HPP + laba. Tampilkan juga harga jual dibulatkan ke atas kelipatan Rp 500, laba/porsi, laba total. Guard: porsi >= 1, input kosong = 0, tanpa NaN/Infinity. Format Rupiah id-ID. Tombol "Simpan" menyimpan ke calc_state (tanpa iklan), log save_calculation.
6. Konten Promosi: grid foto promo (field nyata di data), popup viewer, tombol Unduh dan Share. Unduh via MediaStore (tanpa izin di API 29+). Share via FileProvider + ACTION_SEND. Butuh internet; offline tampilkan pesan jelas.
7. Timer Memasak (gratis): dialog label + durasi (prefilled dari langkah, bisa diedit). AlarmManager.setAlarmClock(). BroadcastReceiver menampilkan notifikasi (channel cooking_timer, IMPORTANCE_HIGH, suara+getar) yang membuka resep terkait. Minta POST_NOTIFICATIONS (API 33+) saat timer pertama dinyalakan; jika ditolak, tetap tampilkan countdown in-app dengan pesan jelas. Beberapa timer aktif (tabel active_timers). Log start_cooking_timer.
8. Settings: Tema (Ikuti Sistem/Light/Dark, DataStore); versi (BuildConfig); tombol Kebijakan Privasi (buka browser); "Pengaturan privasi iklan" (UMP privacy options) hanya tampil bila status == REQUIRED.

## 8. MONETISASI (hanya Banner, Native, Rewarded; TIDAK ada App Open dan Interstitial)
- Test ID default di res/xml/remote_config_defaults.xml: Banner ca-app-pub-3940256099942544/6300978111; Native ca-app-pub-3940256099942544/2247696110; Rewarded ca-app-pub-3940256099942544/5224354917. App ID manifest (sample): ca-app-pub-3940256099942544~3347511713.
- Remote Config: isAdsEnabled=true, bannerEnabled=true, nativeEnabled=true, rewardedEnabled=true, bannerAdUnitId, nativeAdUnitId, rewardedAdUnitId, nativeEveryN=5, rewardedFallbackUnlockOnFailure=true, rewardedReadyTimeoutSec=8, metaCheckIntervalHours=6, pageSize=10. Min fetch interval 12 jam di release, 0 di debug; fetch tidak boleh memblokir UI.
- Debug build SELALU memakai test ID (abaikan RC). Test device ID tidak diperlukan untuk test ad unit. Untuk menguji formulir consent, sediakan opsi debug via local.properties (umpDebugEea=true dan umpTestDeviceHashedId=...) yang membaca ConsentDebugSettings; tanpa opsi itu perilaku normal.
- Urutan: UMP requestConsentInfoUpdate -> loadAndShowConsentFormIfRequired -> init MobileAds sekali setelah canRequestAds() -> load iklan. Tidak ada iklan sebelum consent.
- isAdsEnabled=false atau rewardedEnabled=false: semua iklan hilang dan semua fitur terkunci dianggap terbuka (TANPA menulis ke unlocks).
- Rewarded (satu iklan = satu fitur pada satu resep, permanen):
  - Terbuka = ada baris unlocks (recipeId, feature). Kembali ke resep yang sudah dibuka tidak meminta iklan lagi, termasuk setelah restart app.
  - Preload saat Detail dibuka, hanya jika ada fitur di resep itu yang masih terkunci.
  - Tombol terkunci: "Menyiapkan iklan…" (nonaktif, indikator) lalu "Tonton iklan untuk membuka {fitur}" (aktif). Tap menampilkan dialog konfirmasi (Batal/Tonton). Penulisan baris unlocks HANYA di OnUserEarnedRewardListener.
  - Jika iklan tidak siap dalam rewardedReadyTimeoutSec atau gagal setelah 2 retry (backoff 3s, 8s) dan rewardedFallbackUnlockOnFailure=true: buka fitur HANYA untuk sesi itu (state di memori, TIDAK menulis ke unlocks) dengan pesan "Iklan belum tersedia, fitur dibuka sementara". Setelah app dimatikan, fitur terkunci lagi.
  - Setelah satu iklan dipakai, preload berikutnya bila masih ada fitur terkunci.
- Native: preload pool 3 iklan; NativeAdView (XML gaya Material 3) via AndroidView, semua asset ter-register; label "Iklan" jelas; jangan render slot kosong; destroy() saat ViewModel clear.
- Banner: adaptive, HANYA di Home dan Favorit.
- OnPaidEventListener di ketiga format. Event custom jc_ad_paid dengan param: value (= valueMicros / 1_000_000.0), currency (fallback "USD" bila kosong), ad_format (BANNER/NATIVE/REWARDED), ad_unit_name, ad_source (dari responseInfo adapter), screen (layar aktif). Event lain: jc_ad_request, jc_ad_click, jc_ad_failed. DILARANG mencatat manual event reserved: first_open, session_start, ad_click, ad_impression, error.

## 9. ANALYTICS & CRASHLYTICS
- AnalyticsLogger singleton; semua event menyertakan source_platform="android".
- Event: screen_view (manual via nav listener), view_recipe, change_category, recipe_search (search_term, result_count; log sekali per term final), recipe_add_favorite, recipe_remove_favorite, view_favorites_tab (total_favorites_count), calculate_hpp, save_calculation, start_cooking_timer, rewarded_unlock (feature, recipe_id, via_fallback), promo_download, promo_share, share_recipe, change_theme.
- User properties: theme_mode (system/light/dark), user_type (new bila install < 7 hari, selain itu returning).
- Crashlytics: custom keys theme_mode, ads_enabled, last_screen. ANR reporting otomatis; jangan buat handler exception sendiri.

## 10. DAFTAR TAHAP
S0 Pre-flight & dokumentasi; S1 Pipeline web (kode); S2 Deploy production; S3 Kerangka proyek Android; S4 Instrumentasi (Firebase, RC, UMP, AdManager); S5 Data layer; S6 Home, Pencarian, Favorit; S7 Detail & Kalkulator; S8 Timer; S9 Rewarded & Promo; S10 Settings & Aset; S11 QA & hardening.
