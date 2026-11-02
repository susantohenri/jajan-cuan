# Master Code Review — Jajan Cuan Android (S0–S11)
> Tanggal: 2026-11-02 | Lingkup: Seluruh arsitektur Kotlin, Jetpack Compose, Room, OkHttp, AdMob, Firebase, ProGuard & Play Store Readiness

---

## 1. Ringkasan Eksekutif & Statistik Temuan

Review mendalam lanjutan telah selesai dilakukan terhadap seluruh basis kode aplikasi Android (`com.henrisusanto.vitmate`). Total temuan kini berjumlah **40 temuan**, diklasifikasikan berdasarkan tingkat keparahan (*severity*) dan area risiko:

| Severity | Jumlah | Dampak Utama |
|---|:---:|---|
| 🔴 **High (Critical)** | **10** | Pelanggaran kebijakan AdMob, data stale permanen, memory leak, crash uncaught, ID collision. |
| 🟡 **Medium** | **18** | Bug lifecycle Compose, race conditions, inefisiensi query SQLite, OOM risk pada HP low-end. |
| 🟢 **Low / Improvement** | **12** | Redundansi ProGuard rules, standardisasi sanitasi input, optimasi alokasi objek. |
| **Total** | **40** | **100% kode sumber telah diaudit secara statis dan struktural.** |

---

## 2. Temuan Kritis Baru (Hasil Deep-Dive Lanjutan: F-031 s.d. F-040)

### F-031 · 🔴 High · AdMob Policy & Lifecycle
**File:** [`ads/NativeAdCard.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/ads/NativeAdCard.kt) (baris 50–56, 71–73)
- **Deskripsi Masalah:** `NativeAdCard` di dalam `LazyColumn` menggunakan `DisposableEffect(ad) { onDispose { adManager.releaseNativeAd(ad) } }`. Setiap kali sebuah kartu iklan tergeser keluar layar bahkan hanya 1 piksel saja, iklan langsung di-destroy dan memanggil `preloadNativeAds()` untuk meminta iklan baru dari server. Jika pengguna melakukan *fast scrolling* (flinging) naik-turun, AdMob akan mendeteksi puluhan panggilan iklan yang dimuat dan langsung dihancurkan dalam hitungan detik tanpa *viewability* yang valid. Hal ini dapat memicu penalti akun AdMob atas dugaan **"Rapid ad requesting / Invalid traffic"**.
- **Rekomendasi Perbaikan:** Jangan menghancurkan iklan saat keluar dari viewport sesaat. Ikat instans native ad pada level state list/ViewModel dengan key item yang stabil, atau gunakan *viewport idle debounce* sebelum memicu reload.

---

### F-032 · 🟡 Medium · Compose Recomposition Loop
**File:** [`ads/AdaptiveBannerAd.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/ads/AdaptiveBannerAd.kt) (baris 36–40)
- **Deskripsi Masalah:** `AdaptiveBannerAd` membungkus pembuatan `AdView` di dalam `key(widthDp)`. Jika lebar kontainer berubah hanya sebesar 1 dp (misalnya akibat animasi keyboard muncul/hilang, penyesuaian padding status/nav bar, atau transisi orientation), Compose akan me-recreate `AdView` dari nol dan memanggil ulang `adManager.loadBanner(...)`. Hal ini menyebabkan banner berkedip (*flicker*) dan mengirim request banner berulang-ulang tanpa perlu.
- **Rekomendasi Perbaikan:** Jangan menggunakan `key(widthDp)` mentah. Hitung orientasi dan lebar layar sekali saat inisialisasi atau kelompokkan per *bucket* ukuran banner adaptif resmi.

---

### F-033 · 🔴 High · Memory & Coroutine Leak di ViewModel
**File:** [`ui/screens/DetailViewModel.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/ui/screens/DetailViewModel.kt) & [`ads/RewardedController.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/ads/RewardedController.kt)
- **Deskripsi Masalah:** `DetailViewModel` menerima `rewardedSession: RewardedAdSession` (yang merupakan `RewardedController` dengan `CoroutineScope` mandiri). Namun, `DetailViewModel` **tidak meng-override `onCleared()`**. Akibatnya, saat pengguna meninggalkan layar Detail, `rewardedSession.destroy()` tidak pernah dipanggil. Coroutine `timeoutJob` atau `retryJob` yang sedang berjalan akan terus aktif di background, menahan referensi ke Activity/Context dan memicu kebocoran memori (*memory leak*).
- **Rekomendasi Perbaikan:** Tambahkan fungsi pembersihan pada `DetailViewModel`:
  ```kotlin
  override fun onCleared() {
      super.onCleared()
      rewardedSession.destroy()
  }
  ```

---

### F-034 · 🔴 High · Consent UMP Beku Permanen Saat Offline di Cold-Start
**File:** [`consent/ConsentManager.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/consent/ConsentManager.kt) (baris 83–88)
- **Deskripsi Masalah:** Jika pengguna membuka aplikasi pertama kali saat tidak ada koneksi internet (atau koneksi sangat buruk), `requestConsentInfoUpdate` gagal dan masuk ke blok error di mana `finish()` dipanggil dan menetapkan flag `finished.set(true)`. Karena `finished` bernilai `true`, setiap pemanggilan `gatherConsent()` selanjutnya di masa depan langsung melewati UMP flow via fast-path tanpa pernah mencoba lagi. Akibatnya, status `canRequestAds()` terkunci di nilai `false` selamanya selama masa hidup proses aplikasi, meskipun beberapa menit kemudian internet sudah aktif kembali.
- **Rekomendasi Perbaikan:** Pisahkan status `success` dan `error`. Jika gagal karena faktor jaringan, jangan set `finished = true`, sehingga alur consent dapat dicoba kembali saat aplikasi mendeteksi jaringan telah pulih.

---

### F-035 · 🟡 Medium · Risiko OutOfMemory (OOM) pada Unduhan Promo
**File:** [`promo/AndroidPromoActions.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/promo/AndroidPromoActions.kt) (baris 88–94)
- **Deskripsi Masalah:** Fungsi `fetch(url: String)` memanggil `response.body?.bytes()`. Pemanggilan ini mengalokasikan seluruh isi file gambar langsung ke dalam memori heap JVM sebagai `ByteArray`. Pada perangkat Android entry-level dengan alokasi heap terbatas (seperti Android Go atau RAM 2–3 GB), jika foto promosi beresolusi tinggi (misal 5–15 MB), proses ini dapat langsung memicu `OutOfMemoryError` dan membuat aplikasi crash.
- **Rekomendasi Perbaikan:** Gunakan teknik streaming buffer `InputStream.copyTo(OutputStream)` langsung ke file disk/MediaStore tanpa memuat seluruh array byte ke memori, serupa dengan implementasi di `OkHttpImageDownloader.kt`.

---

### F-036 · 🟡 Medium · Race Condition pada Fitur Share Foto Promo
**File:** [`promo/AndroidPromoActions.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/promo/AndroidPromoActions.kt) (baris 71–73)
- **Deskripsi Masalah:** Pada baris 72 terdapat kode `directory.listFiles()?.forEach { it.delete() }` sebelum file share baru ditulis. Jika pengguna menekan tombol share pada foto A (membuka WhatsApp), lalu kembali ke aplikasi dan segera menekan share pada foto B, file foto A di folder cache akan langsung dihapus oleh aplikasi. Jika WhatsApp belum selesai membaca konten dari `FileProvider`, WhatsApp akan melempar `FileNotFoundException` atau gambar gagal terkirim.
- **Rekomendasi Perbaikan:** Berikan nama file unik berbasis timestamp/UUID atau bersihkan hanya file share lama yang umurnya sudah melewati batas wajar (misal lebih dari 1 jam).

---

### F-037 · 🟡 Medium · Kehilangan State Form Kalkulator Saat Process Death
**File:** [`ui/screens/DetailViewModel.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/ui/screens/DetailViewModel.kt) & [`MainActivity.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/MainActivity.kt)
- **Deskripsi Masalah:** Seluruh input form kalkulator yang sedang diketik pengguna (`IngredientInput`, `portionsText`, `marginText`) disimpan secara murni di memori RAM melalui `MutableStateFlow` di `DetailViewModel`, tanpa terhubung ke `SavedStateHandle`. Ketika aplikasi ditinggalkan di latar belakang atau ketika memutar iklan rewarded video (yang menghabiskan alokasi memori grafik besar), sistem operasi Android dapat membunuh proses di background (*process death*). Saat pengguna kembali, seluruh angka yang baru saja diketik tetapi belum sempat ditekan "Simpan" akan hilang dan kembali ke default.
- **Rekomendasi Perbaikan:** Simpan state draf kalkulator ke `SavedStateHandle` agar form tetap utuh setelah proses dipulihkan.

---

### F-038 · 🟢 Low · Potensi Obfuscation Issue pada DTO Serialization
**File:** [`data/remote/dto/RecipeDtos.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/data/remote/dto/RecipeDtos.kt) & [`proguard-rules.pro`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/proguard-rules.pro)
- **Deskripsi Masalah:** Field seperti `slug`, `title`, `category`, `cover`, `thumb`, dan `portions` pada `SearchIndexEntryDto` dan `PostDto` tidak memiliki anotasi `@SerialName(...)` eksplisit, melainkan mengandalkan nama field Kotlin bawaan. Jika R8 pada build release mengaburkan nama field tersebut dan konfigurasi serializer gagal membaca metadata class asli, deserialisasi data RTDB akan menghasilkan default kosong/nol.
- **Rekomendasi Perbaikan:** Tambahkan anotasi `@SerialName` eksplisit pada seluruh properti DTO, atau tambahkan aturan di `proguard-rules.pro`:
  ```proguard
  -keepclassmembers class com.henrisusanto.vitmate.data.remote.dto.** {
      <fields>;
  }
  ```

---

### F-039 · 🔴 High · Potensi Uncaught Exception pada URL Malformed
**File:** [`data/image/ImageDownloader.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/data/image/ImageDownloader.kt) (baris 31–33)
- **Deskripsi Masalah:** Di `OkHttpImageDownloader.kt`:
  ```kotlin
  try {
      val request = Request.Builder().url(url).get().build()
      ...
  } catch (exception: IOException) {
      false
  }
  ```
  Pemanggilan `Request.Builder().url(url)` pada OkHttp akan melempar `IllegalArgumentException` (yang merupakan turunan dari `RuntimeException`, bukan `IOException`) apabila string `url` memiliki format yang tidak valid atau karakter ilegal. Karena blok `catch` hanya menangkap `IOException`, exception ini akan lolos keluar dan menyebabkan crash aplikasi saat pengguna membuka resep yang URL fotonya cacat.
- **Rekomendasi Perbaikan:** Tangkap juga `IllegalArgumentException` pada blok try-catch atau gunakan `url.toHttpUrlOrNull()` sebelum membangun request.

---

### F-040 · 🟢 Low · Pruning State Kalkulator Yatim
**File:** [`data/local/Entities.kt`](file:///c:/Users/webhe/Downloads/MSI/Local%20Sites/jajan-cuan/android/app/src/main/kotlin/com/henrisusanto/vitmate/data/local/Entities.kt)
- **Deskripsi Masalah:** Tabel `calc_state` dan `favorites` dirancang tanpa relasi *Foreign Key cascade* ke `recipe_index` (hal ini memang disengaja agar data favorit pengguna tidak hilang saat katalog index server diperbarui). Namun, jika di masa depan ada resep yang dihapus permanen dari server dan tidak difavoritkan lagi, baris pada `calc_state` akan tetap tersimpan selamanya di SQLite tanpa pernah dibersihkan.
- **Rekomendasi Perbaikan:** Tambahkan fungsi pembersihan berkala (*housekeeping query*) untuk menghapus entri `calc_state` yang `recipeId`-nya tidak lagi ada di `recipe_index` maupun `favorites`.

---

## 3. Matriks Lengkap Seluruh 40 Temuan (F-001 s.d. F-040)

| No | ID | Kategori | Severity | File Sumber | Ringkasan Inti |
|:---:|:---:|:---:|:---:|---|---|
| 1 | **F-001** | Security | 🔴 High | `AndroidManifest.xml` | Test AdMob App ID hardcoded di manifest produksi. |
| 2 | **F-002** | Security | 🔴 High | `RemoteConfigValues.kt` | Test Ad Unit ID dikirim sebagai default Remote Config. |
| 3 | **F-003** | Bug | 🔴 High | `SyncManager.kt` | Timestamp sync disimpan *sebelum* fetch index berhasil (data macet). |
| 4 | **F-004** | Bug | 🔴 High | `HomeScreen.kt` | `LaunchedEffect` load-more ada di dalam `itemsIndexed` (multi-trigger). |
| 5 | **F-005** | Risk | 🔴 High | `TimerReceiver.kt` | Coroutine scope `goAsync()` tidak pernah di-cancel (leak di alarm). |
| 6 | **F-006** | Risk | 🔴 High | `TimerRepository.kt` | ID notifikasi dapat bentrok dan membatalkan alarm yang ada. |
| 7 | **F-007** | Architecture | 🟡 Medium | `MainActivity.kt` | `pageSize` tidak reaktif terhadap update Remote Config. |
| 8 | **F-008** | Bug | 🟡 Medium | `DetailScreen.kt` | Key `LaunchedEffect` countdown timer terlalu kasar. |
| 9 | **F-009** | Security | 🟡 Medium | `OkHttpRecipeRemoteDataSource.kt` | RTDB base URL hardcoded sebagai konstanta string. |
| 10 | **F-010** | Risk | 🟡 Medium | `RecipeRepository.kt` | Return value `deleteRecursively()` diabaikan (residu foto orphan). |
| 11 | **F-011** | Architecture | 🟡 Medium | `AppContainer.kt` | Application CoroutineScope tidak memiliki fungsi `destroy()` untuk testing. |
| 12 | **F-012** | Risk | 🟡 Medium | `AdManager.kt` | Manipulasi `nativePool` tanpa anotasi `@MainThread`. |
| 13 | **F-013** | Performance | 🟡 Medium | `Daos.kt` | `ORDER BY` computed math expression menonaktifkan index SQLite. |
| 14 | **F-014** | Performance | 🟡 Medium | `HomeViewModel.kt` | `_query` mentah masuk combine UI sebelum debounce selesai. |
| 15 | **F-015** | Bug | 🟡 Medium | `DetailScreen.kt` | Iklan rewarded gagal diam-diam jika Activity bernilai null. |
| 16 | **F-016** | Risk | 🟡 Medium | `ConsentManager.kt` | Status consent UMP tidak direset antar-sesi runtime. |
| 17 | **F-017** | Architecture | 🟡 Medium | `AppDatabase.kt` | Room Database v1 belum memiliki deklarasi migration/fallback eksplisit. |
| 18 | **F-018** | Bug | 🟡 Medium | `RecipeRepository.kt` | `FavoriteEntity.toSummary()` mengisi `slug` dengan `id`. |
| 19 | **F-019** | Risk | 🟡 Medium | `FeatureGateCoordinator.kt` | Fallback unlock membuka SEMUA fitur terkunci sekaligus. |
| 20 | **F-020** | Security | 🟡 Medium | `DetailScreen.kt` | `FLAG_GRANT_READ_URI_PERMISSION` ditaruh di Intent Chooser wrapper. |
| 21 | **F-021** | Risk | 🟢 Low | `AnalyticsLogger.kt` | `require()` reserved event melempar runtime exception. |
| 22 | **F-022** | Performance | 🟡 Medium | `OkHttpRecipeRemoteDataSource.kt` | `RetryInterceptor` menggunakan `Thread.sleep()` memblokir thread pool. |
| 23 | **F-023** | Risk | 🟢 Low | `AndroidPromoActions.kt` | Error unduhan promo tidak membedakan jenis kegagalan jaringan. |
| 24 | **F-024** | Architecture | 🟢 Low | `MainActivity.kt` | `recipeId` kosong tidak divalidasi sebelum inisialisasi Detail. |
| 25 | **F-025** | Risk | 🟢 Low | `Screen.kt` | Parameter navigasi `detail/$recipeId` tidak di-URL encode. |
| 26 | **F-026** | Performance | 🟢 Low | `RecipeRepository.kt` | Alokasi `HashSet` berulang pada setiap emisi list favorit. |
| 27 | **F-027** | Style | 🟢 Low | `proguard-rules.pro` | Keep rule `kotlinx.serialization.**` terlalu lebar (redundansi). |
| 28 | **F-028** | Risk | 🟢 Low | `AdManager.kt` | Race condition pada preload native ad saat config berubah cepat. |
| 29 | **F-029** | Architecture | 🟢 Low | `AppContainer.kt` | `adUnitProvider` dapat diakses publik sebelum inisialisasi ad selesai. |
| 30 | **F-030** | Incomplete | 🟢 Low | `AppDatabase.kt` | File Room schema JSON (`schemas/`) belum ter-commit di repository. |
| 31 | **F-031** | AdMob Policy | 🔴 High | `NativeAdCard.kt` | Destroy & reload instan saat native ad keluar viewport 1px (rapid requests). |
| 32 | **F-032** | Performance | 🟡 Medium | `AdaptiveBannerAd.kt` | `key(widthDp)` memicu re-create `AdView` saat insets bergeser 1dp. |
| 33 | **F-033** | Lifecycle | 🔴 High | `DetailViewModel.kt` | `DetailViewModel` tidak override `onCleared()`; `RewardedController` leak. |
| 34 | **F-034** | Connectivity | 🔴 High | `ConsentManager.kt` | Gagal consent di awal mengunci status iklan mati permanen. |
| 35 | **F-035** | Memory (OOM) | 🟡 Medium | `AndroidPromoActions.kt` | `bytes()` memuat seluruh gambar promo ke heap RAM (risiko crash di HP low-end). |
| 36 | **F-036** | Concurrency | 🟡 Medium | `AndroidPromoActions.kt` | Purge folder cache share menghapus file yang sedang dibaca aplikasi lain. |
| 37 | **F-037** | Architecture | 🟡 Medium | `DetailViewModel.kt` | Form HPP tidak disimpan di `SavedStateHandle` (hilang saat process death). |
| 38 | **F-038** | Serialization | 🟢 Low | `RecipeDtos.kt` | Field DTO tanpa `@SerialName` berpotensi salah nama saat obfuscation R8. |
| 39 | **F-039** | Robustness | 🔴 High | `ImageDownloader.kt` | `Request.Builder().url()` melempar uncaught `IllegalArgumentException`. |
| 40 | **F-040** | Database | 🟢 Low | `Entities.kt` | Tidak ada pembersihan entri `calc_state` yatim saat resep dihapus. |

---

## 4. Panduan Eksekusi Perbaikan (Action Plan)

### Tahap 1: Blocker Pra-Rilis Play Store (Wajib Diselesaikan)
1. **Ganti AdMob ID Produksi (`F-001`, `F-002`)**: Masukkan App ID dan Ad Unit ID rilismu di AdMob Console dan Firebase Remote Config.
2. **Perbaiki Lifecycle Sync (`F-003`)**: Pindahkan `saveLastMetaCheckAt` ke setelah database transaction selesai.
3. **Cegah Coroutine Leak di ViewModel (`F-033`)**: Override `onCleared()` di `DetailViewModel` untuk memanggil `rewardedSession.destroy()`.
4. **Tangani Malformed URL Exception (`F-039`)**: Tambahkan penanganan `IllegalArgumentException` pada downloader gambar.
5. **Perbaiki Loop Load-More (`F-004`)**: Pindahkan pemicu `onLoadMore` ke luar `itemsIndexed`.

### Tahap 2: Stabilitas & Kepatuhan Kebijakan Iklan
1. **Stabilkan Native Ad di Scroll (`F-031`)**: Hindari pemanggilan `destroy()` instan pada setiap scroll item.
2. **Perbaiki UMP Retry Saat Offline (`F-034`)**: Izinkan `ConsentManager` mencoba ulang ketika konektivitas internet telah aktif kembali.
3. **Gunakan Streaming untuk Unduhan Media (`F-035`)**: Hindari `response.body?.bytes()` untuk mencegah lonjakan memori.
4. **Simpan Slug Favorit (`F-018`)**: Tambahkan kolom `slug` pada `FavoriteEntity`.

---

*Laporan Master Code Review ini diperbarui secara otomatis di artifact: [android-review.md](file:///C:/Users/webhe/.gemini/antigravity/brain/606ba900-54a5-48c4-8262-6402a8242723/android-review.md).*
