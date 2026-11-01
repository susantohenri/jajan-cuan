# Panduan & Kumpulan Prompt Tahap S0 - S11 (Jajan Cuan Android)

Dokumen ini menyimpan seluruh prompt instruksi dari tahap S0 sampai S11 agar aman dan tidak hilang jika sesi percakapan terputus.

---

## S0 — Pre-flight & dokumentasi
```text
TAHAP S0 — PRE-FLIGHT & DOKUMENTASI. Kerjakan HANYA tahap ini. Jangan menjalankan git add/commit/push. Setelah laporan, STOP.

TUGAS
1. Buat folder android/ lalu tulis android/AGENTS.md PERSIS (tanpa mengubah isi) dari blok "BEGIN AGENTS.md" sampai "END AGENTS.md" di bawah.
2. Buat android/PROGRESS.md: checklist S0–S11 (judul tahap sesuai daftar di AGENTS.md §10), semua belum dicentang kecuali S0 setelah selesai. Sertakan bagian "Keputusan & asumsi" (kosong) dan "Checklist manual" (kosong).
3. Cek lingkungan (hanya baca, jangan install apa pun yang besar) dan laporkan sebagai tabel Siap/Tidak per kebutuhan tahap:
   - git status (apakah ada perubahan belum di-commit?)
   - Node, npm, apakah paket sharp bisa dipasang (npm view sharp version), Firebase CLI terpasang (firebase --version) dan login (firebase projects:list menampilkan jajan-cuan), isi firebase.json (ada konfigurasi database rules dan hosting?) -> kebutuhan S1/S2
   - JDK (java -version, minimal 17), ANDROID_HOME, Android SDK platform 36 + build-tools, Gradle di PATH atau wrapper, adb dan emulator/device -> kebutuhan S3+
   - File C:\Users\webhe\Downloads\google-services (5).json ada; project_id = jajan-cuan; package_name = com.henrisusanto.vitmate
4. Baca: data/seed-posts.json, scripts/release.js, scripts/seed-database.html, web/js/experience.js, database.rules.json. Tulis android/DATA_SCHEMA.md berisi:
   a. Skema NYATA /posts: tiap field, tipe, wajib/opsional, format harga/porsi/bahan/langkah, field gambar cover dan promo; 1 contoh item (dipangkas). Jumlah resep dan ukuran seed-posts.json.
   b. Aturan URL gambar di web (cover, foto polesan approve/tolak, promo), lengkap dengan contoh nilai nyata.
   c. Dari mana est_capital, portions, dan is_viral (jika ada) dihitung di kartu web.
   d. Rumus kalkulator HPP di web, langkah demi langkah, ditambah 1 contoh numerik nyata (input -> output) untuk dijadikan golden test.
   e. Mekanisme release.js menulis ke RTDB (auth/metode) dan men-deploy Hosting.
   f. Pertanyaan terbuka / ambiguitas.
5. Jangan membuat file lain di luar android/. Jangan mengubah web/, scripts/, data/, database.rules.json.

SELESAI JIKA: tiga file ada, tabel lingkungan terisi, DATA_SCHEMA.md berisi data nyata (bukan tebakan). Gunakan Laporan Baku (AGENTS.md §2.8).
```

---

## S1 — Pipeline web (kode saja, tanpa menulis ke production)
```text
TAHAP S1 — PIPELINE WEB (KODE). Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap (§2). Kerjakan HANYA S1. Jangan git commit. Setelah laporan, STOP.

IZIN KHUSUS S1: boleh mengubah scripts/release.js, scripts/seed-database.html (opsional), database.rules.json, .gitignore, package.json/lock (hanya untuk sharp), dan membuat berkas baru di web/img/recipes/thumbs/. DILARANG mengubah web/experience.html, web/js/experience.js, atau perilaku /posts. DILARANG menulis ke RTDB/Hosting dan DILARANG deploy (hanya GET untuk membaca data publik).

TUGAS (kontrak data: AGENTS.md §5)
1. release.js: bangun /search_index dan /meta sesuai kontrak. Nilai cover/est_capital/portions/is_viral harus PERSIS sama dengan yang dipakai kartu web (lihat DATA_SCHEMA.md). Urutan tulis: /posts, lalu /search_index (PUT seluruh node), lalu /meta paling akhir. Mekanisme tulis/autentikasi sama dengan /posts.
2. Thumbnail: tiap resep -> web/img/recipes/thumbs/{slug}.webp dari foto cover yang sama dengan field cover (400x400, center-crop, WebP, target <= 40 KB). Pakai sharp (npm install --save-dev sharp). Sumber remote: unduh sementara. Gagal -> thumb = null untuk resep itu dan hitung jumlahnya. Lewati pembuatan ulang bila thumbnail sudah ada dan lebih baru dari sumbernya. Format path thumb sama gayanya dengan field cover.
3. Flag --dry-run: membangun index + meta + thumbnail lokal, mencetak jumlah item, ukuran JSON index (byte), jumlah thumbnail berhasil/gagal, total ukuran folder thumbs, dan 2 contoh item; TANPA menulis ke RTDB dan TANPA deploy. Tanpa flag, perilaku lama tetap ditambah index/meta.
4. database.rules.json: ".read": true untuk /search_index dan /meta; write tidak lebih longgar dari /posts; aturan /posts dan .indexOn tidak berubah. Validasi JSON.
5. seed-database.html: sertakan payload index/meta bila mudah; jika tidak, catat di laporan.
6. .gitignore sesuai AGENTS.md §3.

VERIFIKASI
- node scripts/release.js --dry-run -> tempel output nyata.
- Syarat: count index == jumlah resep published; ukuran index <= 150 KB (jika lebih, STOP dan usulkan pemangkasan, jangan terapkan diam-diam); thumbnail gagal <= 10%.
- git status: pastikan tidak ada perubahan di web/experience.html dan web/js/experience.js.
- Laporkan juga ukuran total thumbs (karena akan ikut commit).
```

---

## S2 — Deploy ke production (gerbang)
```text
TAHAP S2 — DEPLOY KE PRODUCTION. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S2. Jangan git commit. Setelah laporan, STOP. Tahap ini satu-satunya yang boleh menulis ke production.

LANGKAH (berurutan; STOP bila ada yang gagal, jangan lanjut)
1. Prasyarat: git status bersih kecuali perubahan S1 (yang sudah saya commit). Jalankan ulang `node scripts/release.js --dry-run`; syaratnya sama dengan S1.
2. Firebase CLI terpasang dan login (firebase projects:list menampilkan jajan-cuan), dan firebase.json punya konfigurasi database rules. Jika tidak: STOP dan beri tahu perintah manual yang harus saya jalankan.
3. Backup: GET <RTDB>/posts.json ke data/backup-posts-<timestamp>.json (harus tercakup .gitignore). Catat jumlah resep dan ukuran file.
4. `firebase deploy --only database`.
5. `node scripts/release.js` (menulis /posts, /search_index, /meta dan men-deploy Hosting).
6. Verifikasi live (tempel hasil nyata):
   - GET <RTDB>/meta.json non-null; count == jumlah resep published.
   - GET <RTDB>/search_index.json non-null; jumlah item sama dengan count; ukuran sesuai dry-run.
   - Query web yang sudah ada tetap jalan: GET <RTDB>/posts.json?orderBy="published"&equalTo=true&limitToFirst=1 mengembalikan data.
   - /posts di server sama jumlahnya dengan backup (atau jelaskan selisihnya).
   - Satu URL thumbnail via https://jajan-cuan.web.app/ mengembalikan HTTP 200 (image/webp), dan https://jajan-cuan.web.app/experience masih HTTP 200.
7. Jika ada kegagalan setelah langkah 4: JANGAN rollback otomatis. Laporkan keadaan dan usulkan rollback dari backup, tunggu persetujuan saya.

Di laporan: sebut jelas "tidak ada perubahan untuk di-commit" bila memang tidak ada. Jangan commit file backup.
```

---

## S3 — Kerangka proyek Android
```text
TAHAP S3 — KERANGKA PROYEK ANDROID. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S3. Jangan git commit. Setelah laporan, STOP.

TUGAS
1. Buat proyek Gradle di ./android: settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/libs.versions.toml, modul :app. Wrapper: jika belum ada dan Gradle ada di PATH, buat wrapper; jika tidak ada keduanya, STOP dan tanya (jangan mengunduh dari sumber tidak resmi). Buat local.properties dari ANDROID_HOME.
2. Version catalog LENGKAP dengan semua library final dan semua plugin: Compose BOM, Material 3, Navigation Compose, Lifecycle (ViewModel/Runtime Compose), DataStore, Room + KSP, kotlinx-serialization, OkHttp (+ mockwebserver untuk test), Coil, core-splashscreen, Firebase BoM (analytics, config, crashlytics), play-services-ads, UMP, plugin google-services dan crashlytics, Robolectric/JUnit untuk test. Pasang semua ke :app sekarang agar kompatibilitas toolchain tervalidasi sejak awal. Pilih versi sesuai AGENTS.md §4 dan tulis alasan pemilihan AGP/Gradle/Kotlin/KSP di PROGRESS.md.
3. Salin C:\Users\webhe\Downloads\google-services (5).json ke android/app/google-services.json; validasi project_id dan package_name. Catat di laporan bahwa file ini akan muncul di git status supaya saya yang memutuskan.
4. AndroidManifest sesuai AGENTS.md §4 (izin, allowBackup=false, predictive back, FileProvider terdaftar, AdMob App ID sample sebagai meta-data). Konfigurasi release: R8 aktif. BuildConfig: versionName dari Gradle.
5. Tema M3 (warna brand oranye/kuning, light + dark, dynamic color nonaktif), edge-to-edge, SplashScreen API, ikon launcher sementara bawaan (diganti di S10).
6. Shell navigasi: MainActivity + NavHost + bottom nav 3 tab (Home, Favorit, Settings) + rute Detail (bottom bar tersembunyi). ISI tab dan Detail boleh berupa teks sementara (diisi S6, S7, S10); ini satu-satunya placeholder yang diizinkan di tahap ini dan harus tercatat di PROGRESS.md.

VERIFIKASI (tempel hasil nyata)
- gradlew.bat assembleDebug -> BUILD SUCCESSFUL.
- gradlew.bat :app:dependencies (ringkas): tidak ada alpha/beta/rc.
- gradlew.bat lintDebug: laporkan jumlah error (harus 0) dan warning penting.
- Jika ada emulator/device via adb: install, jalankan, pastikan tidak crash dan bottom nav berpindah tab. Jika tidak ada, tulis di checklist manual.
```

---

## S4 — Instrumentasi (Firebase, Remote Config, UMP, Ads)
```text
TAHAP S4 — INSTRUMENTASI. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S4. Jangan git commit. Setelah laporan, STOP.

TUGAS (spesifikasi: AGENTS.md §8, §9)
1. JajanCuanApplication + AppContainer (DI manual).
2. AnalyticsLogger singleton: semua event di §9 sebagai fungsi bertipe (tanpa string liar), source_platform="android", user properties, dan guard yang menolak nama event reserved (first_open, session_start, ad_click, ad_impression, error). Crashlytics custom keys.
3. RemoteConfigManager: res/xml/remote_config_defaults.xml berisi semua parameter §8; fetch non-blocking; interval 12 jam release / 0 debug. AdUnitProvider: di debug SELALU mengembalikan test ID dan mengabaikan RC; di release memakai RC.
4. Consent + init: alur UMP sesuai §8 (requestConsentInfoUpdate -> loadAndShowConsentFormIfRequired -> init MobileAds sekali setelah canRequestAds()). Dukung opsi debug umpDebugEea/umpTestDeviceHashedId dari local.properties. Sediakan state "privacy options required" untuk S10.
5. AdManager untuk Banner, Native, Rewarded:
   - Banner: composable adaptive yang membaca flag RC dan consent.
   - Native: pool preload 3, API untuk mengambil/mengembalikan iklan, destroy() lengkap, komponen NativeAdView (XML M3) via AndroidView dengan label "Iklan" dan semua asset ter-register.
   - Rewarded: RewardedController dengan state (Idle, Loading, Ready, Failed, TimedOut), preload, retry 2x (backoff 3s, 8s), rewardedReadyTimeoutSec, callback onUserEarnedReward, dan jam yang bisa disuntik untuk test. Pembukaan fitur/unlocks BELUM di sini (S9).
   - OnPaidEventListener di ketiganya -> jc_ad_paid sesuai §8; plus jc_ad_request/jc_ad_click/jc_ad_failed.
6. Unit test: AdUnitProvider (debug vs release), logika state/timeout/retry Rewarded dengan jam palsu, perhitungan value micros -> value, guard event reserved.

VERIFIKASI (tempel hasil nyata)
- gradlew.bat testDebugUnitTest dan assembleDebug hijau.
- Jika emulator ada: jalankan, tunjukkan logcat urutan UMP lalu init MobileAds; pastikan tidak ada request iklan sebelum consent.
- Grep dan tunjukkan tidak ada string ad unit ID produksi di kode selain RC defaults.
```

---

## S5 — Data layer
```text
TAHAP S5 — DATA LAYER. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S5. Jangan git commit. Setelah laporan, STOP.

PRASYARAT: GET <RTDB>/meta.json dan /search_index.json non-null. Jika null: STOP ("jalankan S1 lalu S2 dulu").

TUGAS (spesifikasi: AGENTS.md §5, §6)
1. DTO kotlinx.serialization untuk /meta, /search_index, /posts/{id} sesuai skema NYATA di DATA_SCHEMA.md.
2. REST client OkHttp (timeout wajar, gzip, retry sederhana untuk GET) dengan hasil bertipe Result (sukses/error jaringan/tidak ada data).
3. Room: semua entitas, DAO, dan database di §6 (exportSchema=true, schema JSON ke folder schemas). Query pencarian (LIKE dengan escape), query list dengan LIMIT :limit + filter kategori, query Favorit, DAO unlocks (insert idempoten, observe per recipeId), DAO calc_state, active_timers.
4. SyncManager: logika /meta persis di §6 (throttle, ganti index, kosongkan recipe_detail, refresh detail favorit di background, pertahankan favorit bila server 404/null, diam saat gagal jaringan, unlocks tidak disentuh).
5. RecipeRepository: Flow list/pencarian/kategori dengan limit; getDetail dengan urutan sumber §6; toggleFavorite lengkap (snapshot + unduh thumb dan cover ke filesDir/covers/{id}, cleanup saat dihapus, pesan saat offline tanpa cache).
6. ImageUrlResolver sesuai aturan di DATA_SCHEMA.md.
7. Salin sampel kecil (3 item /posts + salinan /search_index live yang ada saat ini) ke src/test/resources sebagai fixture.

TEST (JUnit + MockWebServer + Robolectric untuk Room in-memory)
Parser dengan fixture nyata; ImageUrlResolver (semua variasi di data nyata); pencarian (judul, bahan, tag, kategori, escape %/_); limit/kategori; sync: meta sama -> tanpa unduh index, meta beda -> index diganti, favorit 404 -> tetap ada, jaringan gagal -> cache dipakai; unlock permanen: tetap ada setelah sync, setelah detail cache dikosongkan, dan setelah favorit dihapus.

VERIFIKASI (tempel hasil nyata): gradlew.bat testDebugUnitTest dan assembleDebug hijau; sebutkan jumlah test. Tidak ada UI baru di tahap ini.
```

---

## S6 — Home, Pencarian, Favorit
```text
TAHAP S6 — HOME, PENCARIAN, FAVORIT. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S6. Jangan git commit. Setelah laporan, STOP.

TUGAS (spesifikasi: AGENTS.md §7 butir 1–3, §8 Native/Banner)
1. HomeViewModel/HomeScreen: header brand, search bar expandable, chip kategori dari index, LazyColumn kartu (thumb, judul, estimasi modal Rupiah id-ID, porsi, ikon hati), pengacakan resep eksplorasi berbasis session seed (diperbarui saat cold start / pull-to-refresh) agar selalu terasa segar, paginasi limit bertambah, pull-to-refresh yang memicu SyncManager, state loading/error ("Coba Lagi")/kosong, native ad tiap nativeEveryN item, banner sticky di atas bottom nav.
2. Pencarian sesuai butir 2 (debounce 300, distinctUntilChanged, digabung kategori, empty state dengan chip saran dan "Tampilkan Semua Resep", log recipe_search sekali per term final).
3. FavoritesViewModel/FavoritesScreen sesuai butir 3 (header dengan jumlah, kartu dari file lokal, hapus bookmark, pencarian lokal, native ad, banner, empty state dengan ilustrasi vektor dan tombol menuju Home). Log view_favorites_tab.
4. Toggle hati di kartu: Snackbar + haptic, status konsisten antar layar, log recipe_add_favorite/remove_favorite. Pesan offline pertama kali sesuai §6.
5. Tap kartu membuka rute Detail (isinya masih sementara; S7 mengisi). Log view_recipe dan change_category.
6. Layout adaptif (batasi lebar konten di tablet), dukung dark mode, semua teks di strings.xml, aksesibilitas dasar (contentDescription, ukuran target sentuh).

TEST: unit test ViewModel (kategori + pencarian + limit, reset limit saat filter berubah, status favorit konsisten) dengan repository palsu atau Room in-memory.
VERIFIKASI (tempel hasil nyata): testDebugUnitTest, assembleDebug, lintDebug (0 error). Jika emulator ada: jalankan skenario scroll, cari "tapioka", favorit lalu mode pesawat -> tab Favorit tetap tampil lengkap dengan foto; jika tidak, tulis sebagai checklist manual untuk saya.
```

---

## S7 — Detail & Kalkulator HPP
```text
TAHAP S7 — DETAIL RESEP & KALKULATOR HPP. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S7. Jangan git commit. Setelah laporan, STOP.

TUGAS (spesifikasi: AGENTS.md §7 butir 4–5)
1. DetailViewModel/DetailScreen dengan urutan sumber data §6 (offline dari Favorit/cache jalan penuh), state loading/error + "Coba Lagi". Action bar: favorit (sinkron dengan Home/Favorit) dan share teks sesuai spesifikasi. Bagian: foto utama, Pros/Analisis Bisnis, Komposisi, Cara Memasak (selalu terbuka), Kalkulator HPP, serta slot untuk Konten Promosi (belum diisi; dikerjakan S9). Bottom bar tersembunyi, predictive back berfungsi.
2. HppCalculator: kelas Kotlin murni yang mengikuti rumus web (DATA_SCHEMA.md). Hasil identik dengan web untuk input yang sama. Termasuk pembulatan ke atas kelipatan Rp 500, laba/porsi, laba total, guard (porsi >= 1, input kosong = 0, tanpa NaN/Infinity), format Rupiah id-ID.
3. UI Kalkulator: baris bahan editable (default harga acuan), porsi, toggle % atau Rp per porsi, hasil realtime. Tombol "Simpan" menyimpan ke calc_state dan memuat ulang nilai saat resep dibuka lagi. Log calculate_hpp (sekali per sesi edit, jangan per ketukan) dan save_calculation. Kalkulator di tahap ini TERBUKA (penguncian Rewarded dipasang di S9).
4. Parser durasi langkah memasak (menit/jam/detik, rentang pakai nilai terbesar) sebagai fungsi murni dan tampilkan tombol "Nyalakan Timer" pada langkah yang punya durasi (tombol belum berfungsi penuh; aksinya dikerjakan S8; ini satu-satunya placeholder yang diizinkan dan harus tercatat).

TEST: golden test HppCalculator memakai contoh numerik dari DATA_SCHEMA.md + kasus tepi (porsi 0, input kosong, nilai besar); parser durasi (contoh nyata dari data resep + "10-15 menit", "1 jam 30 menit", teks tanpa durasi); DetailViewModel (urutan sumber, favorit offline).
VERIFIKASI (tempel hasil nyata): testDebugUnitTest, assembleDebug, lintDebug (0 error). Checklist manual: bandingkan 1 resep di web dan Android dengan input sama, hasil harus sama.
```

---

## S8 — Timer memasak
```text
TAHAP S8 — TIMER MEMASAK (GRATIS, TANPA IKLAN). Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S8. Jangan git commit. Setelah laporan, STOP.

TUGAS (spesifikasi: AGENTS.md §7 butir 7)
1. Hubungkan tombol "Nyalakan Timer" (dari S7) ke dialog: label dan durasi prefilled dari langkah, keduanya bisa diedit; validasi durasi minimal 1 detik.
2. TimerScheduler: AlarmManager.setAlarmClock() (tanpa izin exact alarm; pastikan manifest tetap tanpa SCHEDULE_EXACT_ALARM/USE_EXACT_ALARM). PendingIntent unik per timer dengan flag immutable yang benar.
3. TimerReceiver: notifikasi channel cooking_timer (IMPORTANCE_HIGH, suara + getar) berisi label timer dan nama resep; tap membuka resep terkait (deep link ke rute Detail). Hapus baris active_timers setelah selesai.
4. Izin POST_NOTIFICATIONS (API 33+) diminta saat timer pertama dinyalakan. Jika ditolak: timer tetap berjalan sebagai countdown in-app dengan pesan jelas bahwa notifikasi tidak aktif (dan cara mengaktifkannya).
5. Tabel active_timers: beberapa timer aktif, daftar timer aktif + countdown di Detail (dan bisa dibatalkan). Countdown dihitung dari waktu selesai absolut, bukan dari hitung mundur lokal, supaya benar setelah app di-background.
6. Log start_cooking_timer (durasi, recipe_id).

TEST: scheduler dengan jam palsu / AlarmManager shadow (Robolectric): jadwal dibuat, dibatalkan, tidak ada exact-alarm permission; perhitungan sisa waktu; pembuatan notifikasi.
VERIFIKASI (tempel hasil nyata): testDebugUnitTest, assembleDebug, lintDebug (0 error). Tunjukkan izin hasil merge manifest (gradlew.bat :app:processDebugMainManifest atau baca merged manifest) tanpa exact alarm. Checklist manual untuk saya: timer 1 menit saat app ditutup, izin notifikasi ditolak, dua timer sekaligus.
```

---

## S9 — Rewarded per resep & Konten Promosi
```text
TAHAP S9 — REWARDED PER RESEP & KONTEN PROMOSI. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S9. Jangan git commit. Setelah laporan, STOP.

TUGAS (spesifikasi: AGENTS.md §7 butir 6, §8 Rewarded)
1. UnlockRepository di atas tabel unlocks + SessionUnlocks (memori) untuk fallback. isUnlocked(recipeId, feature) = isAdsEnabled==false || rewardedEnabled==false || ada baris unlocks || ada di SessionUnlocks. Fitur yang bisa dikunci: CALCULATOR dan PROMO saja (Timer dan Cara Memasak tidak pernah dikunci).
2. FeatureGate composable untuk Kalkulator dan Promo di Detail: status Terkunci-Menyiapkan ("Menyiapkan iklan…", nonaktif + indikator), Terkunci-Siap ("Tonton iklan untuk membuka {fitur}", aktif), Terbuka. Tap -> dialog konfirmasi (Batal/Tonton). Pakai RewardedController dari S4: preload hanya saat Detail dibuka dan ada fitur terkunci; hanya satu iklan dimuat sekaligus; preload berikutnya bila masih ada fitur terkunci.
3. Penulisan baris unlocks HANYA di callback reward. Fallback (timeout atau gagal setelah retry, bila rewardedFallbackUnlockOnFailure=true): buka HANYA di SessionUnlocks + pesan "Iklan belum tersedia, fitur dibuka sementara"; tidak menulis ke unlocks. Log rewarded_unlock (feature, recipe_id, via_fallback).
4. Konten Promosi: grid foto promo (field nyata di data, via ImageUrlResolver), popup viewer, tombol Unduh (MediaStore, Pictures/JajanCuan, tanpa izin di API 29+; API 26–28 pakai WRITE_EXTERNAL_STORAGE sesuai manifest) dan Share (salin ke cache + FileProvider + ACTION_SEND dengan izin baca sementara). Butuh internet; offline tampilkan pesan jelas. Log promo_download dan promo_share. Resep tanpa foto promo -> bagian disembunyikan, tidak ada tombol terkunci kosong.
5. Pasang FeatureGate pada Kalkulator (dari S7).

TEST: UnlockRepository (kombinasi flag RC; unlock permanen antar instance database; fallback tidak tersimpan; unlock resep A tidak membuka resep B; unlock CALCULATOR tidak membuka PROMO); state machine FeatureGate dengan RewardedController palsu (siap, timeout, gagal 2x, reward diterima, dialog dibatalkan).
VERIFIKASI (tempel hasil nyata): testDebugUnitTest, assembleDebug, lintDebug (0 error). Checklist manual untuk saya: buka kalkulator martabak -> tonton iklan -> tutup paksa app -> buka lagi: tetap terbuka; resep lain tetap terkunci; mode pesawat saat iklan -> fallback sesi; cek isAdsEnabled=false.
```

---

## S10 — Settings & Aset
```text
TAHAP S10 — SETTINGS & ASET VISUAL. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S10. Jangan git commit. Setelah laporan, STOP.

TUGAS
1. SettingsScreen (AGENTS.md §7 butir 8): Tema Ikuti Sistem/Light/Dark disimpan di DataStore dan diterapkan seketika (user property theme_mode + event change_theme); versi dari BuildConfig; tombol Kebijakan Privasi membuka https://susantohenri.github.io/privacy-policy di browser; "Pengaturan privasi iklan" memanggil UMP showPrivacyOptionsForm dan HANYA tampil bila status == REQUIRED.
2. Aset: adaptive icon vektor (foreground, background, monochrome untuk themed icon) menggantikan ikon sementara; satu set saja, tanpa PNG per density. Buat di ./assets: icon-512.png (512x512, 32-bit) dan feature-graphic-1024x500.png (tanpa alpha), tema jajanan cerah, bernuansa "Jajan Cuan". Sumber SVG ikut disimpan di ./assets/src. Render dengan tool yang tersedia (mis. sharp dari S1). Jangan memakai gambar, font berlisensi, atau merek pihak lain; teks di SVG dikonversi ke path atau pakai font yang aman dan jelaskan pilihan di laporan.
3. Pastikan nama app di launcher = "Jajan Cuan" dan splash memakai ikon baru.

TEST: unit test untuk logika penyimpanan tema (DataStore) dan kondisi tampil tombol privasi.
VERIFIKASI (tempel hasil nyata): testDebugUnitTest, assembleDebug, lintDebug (0 error); verifikasi dimensi dan ada/tidaknya alpha pada dua PNG (tempel hasilnya). Checklist manual: ganti tema (aplikasi menyesuaikan tanpa restart), ikon tampil di launcher dan mode themed icon.
```

---

## S11 — QA & hardening
```text
TAHAP S11 — QA & HARDENING. Baca android/AGENTS.md, PROGRESS.md, DATA_SCHEMA.md lebih dulu dan ikuti Protokol Tahap. Kerjakan HANYA S11: perbaikan harus kecil dan terarah; bug besar dilaporkan, bukan ditulis ulang. Jangan git commit. Setelah laporan, STOP.

TUGAS
1. Jalankan penuh dan tempel hasil nyata: gradlew.bat clean testDebugUnitTest lintDebug assembleDebug assembleRelease. Release harus lolos R8; periksa mapping/keep rules untuk kotlinx.serialization, Room, dan Firebase (perbaiki keep rules bila perlu). Jika ada device/emulator: install APK release dan jalankan alur utama untuk membuktikan tidak crash karena R8.
2. Audit otomatis (tunjukkan perintah + hasil):
   - Merged manifest release: daftar izin; pastikan tanpa SCHEDULE_EXACT_ALARM/USE_EXACT_ALARM; catat izin yang ditambahkan SDK (mis. AD_ID).
   - Tidak ada event reserved yang dicatat manual (grep).
   - Tidak ada ad unit ID produksi atau secret di repo; debug hanya memakai test ID.
   - Tidak ada panggilan jaringan ke /posts.json penuh (grep), dan tidak ada dependensi firebase-database.
   - Tidak ada TODO/stub tersisa selain yang tercatat sengaja (cari TODO, FIXME, "not implemented").
   - Tidak ada alpha/beta/rc di dependensi.
3. Tinjau PROGRESS.md: pastikan setiap butir spesifikasi AGENTS.md §6–§9 ada implementasinya; daftar yang terlewat atau menyimpang.
4. Tulis android/RELEASE_CHECKLIST.md berisi: checklist uji manual lengkap (favorit offline termasuk foto, empty state, UMP, rewarded + unlock permanen setelah restart, fallback sesi, notifikasi timer, tema, banner tidak menutupi bottom nav, perilaku saat /meta berubah, kalkulator vs web), serta "Butuh tindakan manual":
   - Ganti ad unit ID produksi di Remote Config dan App ID produksi di manifest; uji unit produksi dengan test device (ID dari logcat).
   - Siapkan keystore atau Play App Signing (saya generate AAB sendiri).
   - Isi Data Safety, Target Audience, dan Ads declaration di Play Console.
   - Pastikan Privacy Policy menyebut AdMob, Firebase Analytics/Crashlytics, dan Advertising ID.
   - Tautkan AdMob ke Firebase bila ingin revenue masuk GA4 (jc_ad_paid tetap aman dari double count).
   - Sesuaikan PRD: timer kini gratis; unlock rewarded permanen per resep dan fitur; App Open/Interstitial tidak dipakai; Favorit memakai snapshot offline.
   - Pantau kuota Hosting Spark (360 MB/hari) setelah rilis.
   - Unggah aset dari ./assets ke listing.
5. Laporkan ukuran APK/AAB-perkiraan (ukuran APK release) dan 3 risiko teratas yang masih tersisa.
```
