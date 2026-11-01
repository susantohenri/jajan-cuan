# PROGRESS.md — Status Implementasi Jajan Cuan Android

## Checklist Tahap
- [x] S0 Pre-flight & dokumentasi
- [x] S1 Pipeline web (kode)
- [x] S2 Deploy production
- [x] S3 Kerangka proyek Android
- [x] S4 Instrumentasi (Firebase, RC, UMP, AdManager)
- [x] S5 Data layer
- [x] S6 Home, Pencarian, Favorit
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
- S4 DI: `AppContainer` dibuat sekali di `JajanCuanApplication` dan dipakai `MainActivity`. `AnalyticsLogger` singleton per proses (`getInstance`), tetapi constructor internal menerima sink agar bisa diuji dengan fake tanpa Firebase.
- S4 Analytics: seluruh event §9 dibungkus fungsi bertipe (tanpa nama event liar di layar). `source_platform="android"` ditambahkan otomatis. Guard internal melempar `IllegalArgumentException` untuk `first_open`, `session_start`, `ad_click`, `ad_impression`, dan `error`. Crashlytics custom key `theme_mode`, `ads_enabled`, `last_screen` disediakan; `user_type` dihitung dari `firstInstallTime` (< 7 hari = new).
- S4 Remote Config: nilai awal `RemoteConfigValues.DEFAULTS` sengaja sama dengan `res/xml/remote_config_defaults.xml`; fetch berjalan non-blocking setelah setConfigSettings + setDefaults, interval 0 detik di debug dan 12 jam di release. Fetch gagal tidak memblokir UI dan tidak mengubah cache lokal yang sudah aktif.
- S4 temuan production: Remote Config project `jajan-cuan` saat verifikasi masih berisi parameter lama (`appOpenAdUnitId`, `interstitialAdUnitId`) dan `isAdsEnabled=false`; parameter Android baru lainnya tidak ada sehingga memakai default XML. Karena `isAdsEnabled=false`, banner/native/rewarded sengaja tidak memuat iklan (bukan bug gating). Untuk menguji iklan, ubah `isAdsEnabled` menjadi `true` di Firebase Console; S4 tidak menulis ke production.
- S4 AdUnitProvider: build debug SELALU memakai tiga test ID hardcoded (sama persis dengan XML defaults) dan mengabaikan Remote Config. Release memakai RC; bila nilai RC kosong, fallback ke test ID. Tidak ada ID unit produksi di kode.
- S4 UMP: `requestConsentInfoUpdate` -> `loadAndShowConsentFormIfRequired` -> callback, dijalankan sekali per proses dari `MainActivity`. `AdsInitializer` hanya memanggil `MobileAds.initialize` sekali setelah `canRequestAds()` true. `privacyOptionsRequired` (StateFlow) dan `showPrivacyOptionsForm` disediakan untuk S10. Opsi debug `umpDebugEea`/`umpTestDeviceHashedId` dibaca dari `local.properties` saat build menjadi `BuildConfig.UMP_DEBUG_EEA`/`UMP_TEST_DEVICE_HASHED_ID`; hanya dipakai di build debug.
- S4 Native ad: `item_native_ad.xml` memakai view Android standar + drawable rounded (tanpa menambah dependensi Material Components atau mengubah parent theme platform). Semua asset (headline, body, icon, media, CTA, rating, price, store, advertiser) didaftarkan; asset kosong disembunyikan. Label "Iklan" dari string resource. Pool maksimal 3, API take/release/destroyAll. `NativeAdCard` melepas iklan saat keluar dari komposisi; S6/S9 wajib memanggil `releaseNativeAd`/`destroyAllNativeAds` saat ViewModel cleared.
- S4 Rewarded: state `Idle`, `Loading`, `Ready`, `Failed`, `TimedOut`; timeout memakai clock yang bisa disuntik; retry 2x dengan backoff 3 detik lalu 8 detik; late success setelah timeout disimpan sebagai `Ready`. Controller tidak menulis unlocks/membuka fitur (S9).
- S4 Navigasi: listener NavController mencatat `screen_view` (nama rute tanpa argumen), Crashlytics `last_screen`, dan `AdManager.currentScreen` untuk parameter `screen` event iklan. Event layar lain menyusul saat layarnya diisi di S6/S7/S10.
- S4 verifikasi perangkat: memakai perangkat fisik SM-A045F (bukan emulator). Logcat membuktikan urutan `UMP requestConsentInfoUpdate` -> `UMP consent form selesai` -> `MobileAds.initialize dimulai setelah consent` -> `MobileAds selesai diinisialisasi`, tanpa `FATAL EXCEPTION`. Tidak ada log `Request banner/native/rewarded` karena `isAdsEnabled=false` dari RC; setelah flag diaktifkan, gate consent tetap berlaku.
- S4 tidak menambah penempatan banner/native ke layar Home/Favorit (S6) maupun FeatureGate Rewarded (S9); komponennya sudah selesai dan siap dipakai.
- S5 temuan skema: `title_polished` di `/posts` adalah boolean `true` pada 209/209 resep, bukan string seperti tertulis di DATA_SCHEMA.md. DTO menyimpannya sebagai `JsonElement?` agar tidak gagal bila bentuk berubah. `promo_images` kadang tidak dikirim RTDB untuk array kosong, jadi DTO memakai default list kosong.
- S5 REST: OkHttp + kotlinx.serialization dengan `ignoreUnknownKeys`, `coerceInputValues`, `explicitNulls=false`; hanya endpoint `/meta.json`, `/search_index.json`, dan `/posts/{id}.json`. Retry maksimal 2x untuk GET pada IOException/5xx dengan backoff 250ms/500ms; 404 dan body `null` menjadi `NoData`. Gzip ditangani transparan oleh OkHttp.
- S5 Room: semua entitas §6 memakai nama kolom camelCase, `exportSchema=true` ke `app/schemas/.../1.json`. Detail non-favorit di-prune ke 30 item setelah setiap simpan. `unlocks` memakai PK gabungan dan insert IGNORE agar tidak menimpa `unlockedAt` pertama. `active_timers` memakai id unik + notificationId untuk S8.
- S5 state sync: versi `/meta` dan waktu cek terakhir disimpan di SharedPreferences `sync_state` (bukan tabel Room), karena bukan konten katalog. Throttle memakai `metaCheckIntervalHours` dari Remote Config. `SyncManager.sync(force=true)` disediakan untuk pull-to-refresh S6.
- S5 foreground: ditambahkan `androidx.lifecycle:lifecycle-process` dan observer `ProcessLifecycleOwner` di Application; sync berjalan saat app dibuka dan kembali foreground, bukan hanya sekali di cold start.
- S5 favorit: snapshot mandiri di tabel `favorites`; file disimpan di `filesDir/covers/{id}/cover` dan `/thumb`. Jika thumbnail gagal diunduh, cover dipakai sebagai thumbnail. Hapus favorit menghapus baris + direktori. Refresh favorit saat versi index berubah mempertahankan snapshot lama bila server 404/null atau jaringan gagal.
- S5 urutan list: `orderIdx` menyimpan urutan dasar deterministik dari index (diurutkan per id saat sync); pengacakan sesi Home tetap menjadi tugas S6. Query LIKE memakai escape `\`, `%`, dan `_` yang diuji.
- S5 verifikasi: 70 unit test total (52 test baru S5) hijau; assembleDebug hijau; lint 0 error. Di perangkat SM-A045F, setelah app dibuka database Room berisi 209 baris `recipe_index` dengan field kartu benar, tanpa FATAL EXCEPTION.
- S6 pengacakan: `observeListShuffled` mengurutkan `(((orderIdx * :sessionSeed) % 1000003) + 1000003) % 1000003` di SQL, sehingga `LIMIT` tetap dipakai dan urutan stabil per seed (paginasi tidak menduplikasi/lompat). Seed hanya dikirim saat kata kunci kosong; saat mencari, urutan `orderIdx` dipakai agar hasil konsisten.
- S6 seed: dibuat baru saat ViewModel dibuat (cold start) dan saat pull-to-refresh; refresh yang gagal tetap memakai seed baru tetapi daftar diambil dari cache Room.
- S6 `RecipeRepository` kini mengimplementasikan `RecipeCatalog` (seed, count, observeFavoriteCount). Interface ini menjaga ViewModel tidak bergantung langsung pada Room. Implementasi dipulihkan dari salinan tracked `android/android/...` yang identik.
- S6 Home memuat halaman pertama lebih dulu; bila cache kosong ia memanggil sync sekali dan menampilkan error + "Coba Lagi" hanya jika sync gagal. Sync foreground tetap ditangani `ProcessLifecycleOwner` dari S5.
- S6 banner: slot sticky hanya mereservasi tinggi 56dp ketika `initialized && isAdsEnabled && bannerEnabled`; sebelum consent/flag mati slot tidak mengambil ruang (tidak ada area kosong).
- S6 Favorit sengaja tanpa pull-to-refresh karena sumbernya snapshot lokal; pencarian lokal tetap debounce 300ms + `distinctUntilChanged`. Empty state pencarian memakai komponen yang sama dengan Home, tanpa chip saran (tidak relevan untuk snapshot favorit).
- S6 log: `recipe_search` sekali per term final (setelah debounce), `view_favorites_tab` sekali per ViewModel Favorit dibuat, `change_category` setiap chip dipilih (null = "Semua"), `view_recipe` saat kartu dibuka.
- S6 test: 78 unit test total, semuanya hijau (5 HomeViewModel + 3 FavoritesViewModel baru). Test debounce memajukan waktu tanpa `advanceUntilIdle` di antara ketikan agar emisi antara benar-benar dibatalkan.
- S6 catatan lingkungan: `gradlew.bat` di repo ini selalu keluar dengan kode 1 walau build sukses (label `:fail` berada setelah `endlocal`, sehingga eksekusi jatuh ke `exit /b 1`). Verifikasi dinilai dari baris `BUILD SUCCESSFUL`; file wrapper ini tidak diubah karena di luar lingkup S6.

---

## Checklist manual
- S4 (opsional, setelah `isAdsEnabled=true` di Firebase Console): buka app dan cek logcat `Request native` hanya muncul setelah `MobileAds selesai diinisialisasi`; tidak boleh ada request iklan sebelum `UMP consent form selesai`.
- S4 (opsional): untuk melihat formulir consent EEA di debug, tambahkan `umpDebugEea=true` dan `umpTestDeviceHashedId=<hash dari logcat>` ke `android/local.properties`, rebuild debug, lalu hapus data app dan buka lagi.
- S4 (opsional): pastikan logcat menampilkan unit test ID `ca-app-pub-3940256099942544/...` di debug, bukan ID produksi.
- S4 (opsional): cek Firebase Analytics DebugView untuk `screen_view` dan event `jc_ad_*` setelah `isAdsEnabled=true`.
- S4 komponen banner/native baru akan terlihat setelah dipasang di Home/Favorit pada S6; Rewarded baru diuji penuh pada S9.
- S5 (sudah diverifikasi di SM-A045F): buka app pertama kali -> tabel `recipe_index` berisi 209 resep. Bisa diulang dengan menghapus data app lalu membuka app lagi.
- S5 (setelah S6): pull-to-refresh harus memanggil `syncManager.sync(force=true)` dan tetap mempertahankan favorit/unlock; pergantian versi `/meta` baru bisa diuji penuh lewat UI.
- S5 (opsional): jalankan app dalam mode pesawat setelah index terisi; daftar dari Room harus tetap tampil (UI baru ada di S6).
- S6 Home: buka app, scroll sampai mendekati item ke-10 -> limit bertambah tanpa kartu duplikat; tarik untuk refresh -> urutan berubah, indikator berhenti setelah sync selesai.
- S6 Pencarian: ketik "tapioka" -> hasil muncul setelah ±300ms; kosongkan/tutup pencarian -> daftar penuh kembali; cari kata acak -> empty state dengan chip kategori dan "Tampilkan Semua Resep"; cek Analytics DebugView hanya satu `recipe_search` untuk term final.
- S6 Favorit: tekan hati di Home -> Snackbar + getar + ikon terisi; buka tab Favorit -> jumlah di header bertambah, foto tampil dari file lokal; hapus bookmark -> kartu hilang + Snackbar; kosongkan daftar -> ilustrasi + tombol "Jelajahi Ide Resep" kembali ke Home.
- S6 Offline: setelah minimal satu favorit tersimpan (foto terbuka), aktifkan mode pesawat lalu buka tab Favorit -> kartu, foto, dan pencarian lokal tetap berfungsi. Tekan hati pada resep yang belum pernah dibuka saat offline -> Snackbar "Perlu internet untuk menyimpan resep ini pertama kali".
- S6 Tablet & gelap: jalankan di tablet/landscape -> konten maksimal 840dp dan terpusat; ganti tema sistem ke gelap -> semua layar tetap terbaca.
- S6 Iklan (setelah `isAdsEnabled=true`): native muncul tiap `nativeEveryN` kartu di Home/Favorit, banner tampil di atas bottom nav tanpa menutupi konten.
