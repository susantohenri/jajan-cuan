# PROMPT EKSEKUSI KONTEN REMOTION — JAJAN CUAN (HYBRID WORKFLOW)

Halo! Buat video konten TikTok/Reels/Shorts untuk website **Jajan Cuan** menggunakan pipeline Remotion yang sudah ada di komputer ini.

Tujuan: menghasilkan video vertikal 9:16 yang menarik, menjaga tempo sesuai voice-over, memasukkan seluruh gambar konten, dan mempromosikan website secara natural.

Jangan hanya memberikan tutorial atau kode. Kerjakan langsung pada folder lokal, jalankan validasi, render video, dan laporkan hasil sebenarnya.

## 1. LOKASI FOLDER

**Folder aset konten:**

`C:\Users\webhe\Downloads\MSI\Local Sites\jajan-cuan\assets\[NAMA_FOLDER_KONTEN]`

**Folder proyek Remotion:**

`C:\Users\webhe\Downloads\MSI\Local Sites\remotion\`

Ganti `[NAMA_FOLDER_KONTEN]` dengan nama folder konten yang sedang dikerjakan.

Di folder aset, cari berkas berikut:

- Gambar hasil generate dengan nomor urut, misalnya `1_...`, `2_...`, dan seterusnya, berformat JPG, JPEG, PNG, atau WebP.
- Video intro Google Flow, biasanya `intro.mp4`.
- File voice-over, misalnya `voice-over.wav`, `Generated Audio ... .wav`, atau MP3.
- `remotion.json` yang berisi naskah, urutan scene, caption, dan estimasi durasi.
- Aset pendukung lain jika memang diperlukan.

Jangan mengubah atau menghapus file sumber. Kerjakan menggunakan salinan di proyek Remotion.

---

## 2. ATURAN MUTLAK: INVENTARISASI DAN PEMETAAN GAMBAR

### A. Hitung seluruh gambar konten

- Inventarisasi semua gambar bernomor yang sesuai dengan pola `1_...`, `2_...`, dan seterusnya.
- Urutkan berdasarkan angka secara numerik, bukan urutan alfabetis. Contohnya, urutan `1, 2, 3, ... 10`, bukan `1, 10, 2, 3`.
- Tentukan jumlah gambar konten sebagai **N**.
- Pastikan nomor tidak duplikat atau terlewat. Jika ditemukan, lakukan best effort dengan menginventarisasi file aktual dan laporkan anomali tersebut.
- Gambar logo, thumbnail, gambar referensi, atau aset nonkonten yang tidak mengikuti urutan scene jangan otomatis dijadikan scene tambahan. Laporkan secara terpisah jika relevan.

### B. Satu gambar konten = satu scene

**Semua N gambar konten bernomor wajib tampil secara nyata di video final. Setiap gambar harus memiliki satu scene tersendiri.**

- Jumlah objek pada array `scenes` dalam `src/data.ts` harus tepat N.
- Petakan gambar sesuai urutan menjadi `scene-1`, `scene-2`, dan seterusnya sampai `scene-N`.
- Jangan mengabaikan gambar hanya karena `remotion.json` berisi jumlah scene yang berbeda.
- Jangan menambahkan gambar buatan atau menduplikasi gambar untuk memenuhi jumlah scene.
- Verifikasi bahwa setiap gambar benar-benar terlihat pada frame video, bukan hanya disalin ke folder proyek.

### C. Jika jumlah gambar berbeda dari jumlah scene JSON

Gunakan gambar bernomor sebagai sumber jumlah scene dan `remotion.json` sebagai sumber urutan konten, naskah, caption, serta bobot durasi.

Jika jumlahnya sama, petakan secara langsung.

Jika jumlah gambar lebih banyak daripada scene JSON, petakan gambar tambahan ke langkah atau bagian konten yang paling relevan. Bagi bobot durasi dari bagian terkait secara proporsional.

Jika jumlah gambar lebih sedikit daripada scene JSON, gabungkan informasi dari scene JSON yang berdekatan dan relevan ke scene gambar yang tersedia. Pertahankan urutan narasi dan jangan menghilangkan isi voice-over.

Dalam kedua kondisi tersebut:

- Jangan mengarang langkah memasak, bahan, atau klaim bisnis.
- Jangan membuat narasi baru yang tidak ada dalam audio hanya untuk mencocokkan gambar.
- Pertahankan seluruh durasi audio.
- Jika pemetaan tidak sempurna, gunakan pembagian durasi terbaik berdasarkan urutan naskah dan laporkan bahwa sinkronisasinya bersifat estimasi.

---

## 3. ANALISIS AUDIO DAN DURASI VIDEO

### A. Temukan audio final

Identifikasi file voice-over final dalam folder aset. Jika ada beberapa kandidat, periksa nama, format, dan metadata untuk menentukan file yang paling relevan.

Jangan memilih file secara sembarangan jika ada petunjuk yang lebih jelas mengenai audio final.

Gunakan `ffprobe` untuk mendapatkan durasi riil audio. Catat:

- Nama dan format file.
- Durasi dalam detik.
- Sample rate dan jumlah channel jika tersedia.
- Nama file yang akan digunakan dalam proyek.

Jangan memakai estimasi durasi di JSON sebagai pengganti pengukuran audio yang sebenarnya.

### B. Audio adalah sumber kebenaran durasi total

Hitung:

`audioFrames = ceil(durasiAudioDetik × 30)`

Tambahkan buffer visual 25 frame pada akhir video agar gambar terakhir tetap bertahan sesaat setelah audio selesai.

Dengan demikian:

`totalDurationInFrames = audioFrames + 25`

Buffer tersebut hanya berlaku pada durasi visual akhir, bukan pada setiap scene.

Jangan mempercepat, memotong, atau menghilangkan bagian audio hanya demi memenuhi durasi perkiraan JSON. Audio final harus tetap utuh.

### C. Skalakan durasi scene dari JSON

Baca durasi awal seluruh scene dalam `remotion.json`, kemudian hitung total bobot durasi JSON.

Untuk setiap scene hasil pemetaan:

`durasiSceneBaru = durasiBobotScene / totalBobotJSON × audioFrames`

- Bulatkan durasi ke bilangan frame yang valid.
- Sesuaikan selisih pembulatan agar jumlah durasi seluruh scene tepat sama dengan `audioFrames`.
- Tambahkan 25 frame buffer ke scene terakhir.
- Pastikan `startFrame` setiap scene dihitung secara kumulatif tanpa gap atau overlap.
- Pastikan jumlah seluruh `durationInFrames` sama dengan `totalDurationInFrames`.

Jika gambar dan scene JSON berjumlah berbeda, gunakan pembobotan yang sudah direkonsiliasi pada bagian sebelumnya sebelum melakukan penskalaan.

### D. Batasan sinkronisasi

Penskalaan proporsional menyelaraskan pergantian gambar dengan estimasi durasi narasi, tetapi **tidak menjamin gambar berganti tepat pada akhir kalimat yang diucapkan**.

Jangan mengklaim sinkronisasi per kalimat sudah sempurna tanpa data timestamp atau proses alignment audio-naskah.

Gunakan `remotion.json` sebagai panduan durasi per bagian dan audio aktual sebagai acuan durasi keseluruhan. Prioritaskan alur visual yang masuk akal dan hindari pemotongan pada momen penting.

Jika audio ternyata jauh lebih panjang daripada perkiraan naskah, tetap gunakan audio lengkap, skalakan durasi visual sesuai aturan, dan laporkan durasi aktualnya. Jangan menyamarkan masalah dengan mempercepat audio secara otomatis.

---

## 4. SALIN DAN SIAPKAN ASET

### A. Gambar

Salin seluruh N gambar konten ke:

`public/img/`

Gunakan nama output berurutan:

- `public/img/scene-1.jpg`
- `public/img/scene-2.png`
- dan seterusnya, menyesuaikan ekstensi file aktual.

Pertahankan ekstensi asli setiap gambar dan perbarui `imageSrc` dalam data scene sesuai ekstensi tersebut.

Jangan mengganti nama file PNG menjadi JPG tanpa melakukan konversi format yang sesungguhnya.

Pastikan tidak ada file sumber yang tertimpa atau hilang. Periksa dimensi gambar dan pastikan komposisinya sesuai rasio vertikal 9:16. Jangan mendistorsi gambar hanya demi memenuhi ukuran video.

### B. Audio

Siapkan audio final di:

`public/audio/tts-full.mp3`

Jika sumber sudah MP3, salin dan ubah nama file tanpa mengubah isi audionya.

Jika sumber WAV, lakukan konversi audio sungguhan menggunakan FFmpeg, misalnya:

`ffmpeg -i "voice-over.wav" -vn -codec:a libmp3lame -q:a 2 "public/audio/tts-full.mp3"`

Sesuaikan nama file input dengan file audio final yang ditemukan.

Periksa kembali durasi file hasil konversi. Jika proyek yang ada menggunakan format lain atau mempunyai mekanisme pemutaran audio khusus, ikuti implementasi proyek yang sebenarnya dan sesuaikan path-nya.

Jangan pernah sekadar mengubah ekstensi WAV menjadi MP3 tanpa encoding.

### C. Intro Google Flow

Jika `intro.mp4` tersedia, siapkan versi all-intra untuk Remotion:

`public/video/intro-iframe.mp4`

Gunakan FFmpeg dengan pengaturan yang sesuai, misalnya:

`ffmpeg -i "intro.mp4" -c:v libx264 -pix_fmt yuv420p -g 1 -keyint_min 1 -r 30 -an -movflags +faststart "public/video/intro-iframe.mp4"`

Periksa hasil proses untuk memastikan file valid, video dapat dibaca, dan frame rate sesuai target.

Perhatikan bahwa `-an` memang menghilangkan audio intro supaya tidak bertabrakan dengan voice-over utama.

Pertahankan rasio aspek video. Jangan melakukan stretching atau cropping agresif jika tidak diperlukan; sesuaikan framing di Remotion dengan cara yang benar.

Jika konversi gagal, periksa sumber dan error FFmpeg terlebih dahulu. Jangan melanjutkan dengan path video yang tidak valid.

### D. Scene 1 dan intro video

Intro video adalah animasi hook, tetapi gambar Scene 1 tetap wajib muncul.

- Gunakan gambar Scene 1 sebagai tampilan pembuka singkat di dalam scene pertama, kemudian lanjutkan ke video intro apabila diperlukan.
- Jika intro video sudah memperlihatkan gambar referensi yang sama dengan jelas pada awal video, jangan menambahkan tampilan diam kedua yang membuat hook terasa berulang.
- Pastikan intro tidak membuat headline atau stiker yang sama muncul ganda.
- Jangan membuat intro sebagai scene tambahan di luar N scene gambar.
- Durasi pemutaran intro harus mengikuti batas scene pertama. Jika klip lebih panjang, trim secara wajar; jika lebih pendek, tahan frame terakhir atau gunakan transisi yang sesuai.
- Jangan merusak kecepatan video secara berlebihan demi menyesuaikan durasi.

Jika intro tidak tersedia atau rusak, tetap render video menggunakan seluruh gambar konten dan transisi Remotion. Laporkan bahwa intro tidak dapat digunakan.

---

## 5. INTEGRASI DENGAN PROYEK REMOTION YANG SUDAH ADA

Sebelum mengubah kode, periksa:

- `package.json`
- `src/data.ts`
- `src/index.ts`
- File komponen utama yang merender scene.
- Tipe TypeScript atau schema data scene yang digunakan.
- Referensi path audio, gambar, dan video.
- ID composition yang benar.
- Script lint dan command render yang tersedia.

**Pertahankan arsitektur dan struktur data proyek yang sudah berjalan.**

Jangan mengasumsikan nama field atau tipe data hanya berdasarkan `remotion.json`. Cocokkan setiap field dengan implementasi aktual.

Jangan mengganti seluruh `src/data.ts` atau melakukan refactor besar jika hanya diperlukan pembaruan data dan pemetaan scene.

Pertahankan mekanisme yang sudah ada untuk:

- Headline besar.
- Sticker neon kuning untuk `highlightWords`.
- IngredientCard.
- Revenue Pill.
- HeaderBar.
- CtaCard.
- Pengaturan safe zone untuk antarmuka TikTok.

Gunakan implementasi dan ukuran aktual dalam proyek. Jangan mengubah styling global tanpa alasan teknis.

Pastikan HeaderBar dan CtaCard menggunakan domain `jajan-cuan.web.app` dan pesan katalog 50+ resep sesuai komponen yang memang sudah tersedia.

Jangan membuat komponen atau field baru yang tidak dibutuhkan oleh implementasi aktual.

### Teks dan safe zone

- Headline mengikuti `caption.headline` dari JSON.
- Stiker kata kunci mengikuti `caption.highlightWords`.
- Bahan, takaran, dan tips mengikuti data resep yang tersedia.
- Letakkan konten penting di dalam safe zone vertikal agar tidak tertutup caption, username, dan tombol TikTok.
- Jangan mengandalkan teks yang terbakar ke dalam gambar AI jika teks yang sama sudah dirender oleh Remotion.

### `voiceoverScript`

Pertahankan naskah asli dari JSON semaksimal mungkin.

Jika scene direkonsiliasi karena jumlah gambar berbeda, susun metadata scene sesuai urutan narasi. Jangan mengubah file audio atau menggandakan naskah menjadi suara tambahan.

Voice-over utama tetap satu file penuh yang digunakan oleh komposisi Remotion.

---

## 6. PEMBUATAN `src/data.ts`

Bangun ulang atau perbarui data proyek berdasarkan hasil inventarisasi dan mapping aset.

Persyaratan:

1. Array `scenes` harus berisi tepat N objek.
2. Setiap `imageSrc` mengarah ke file gambar yang benar-benar tersedia.
3. Semua N gambar digunakan dalam urutan yang benar.
4. Field dan tipe data cocok dengan komponen Remotion yang sudah ada.
5. `startFrame` harus berurutan, tanpa gap maupun overlap.
6. Semua scene harus memiliki durasi positif.
7. `totalDurationInFrames` sama dengan jumlah seluruh frame scene.
8. Total durasi mencakup durasi audio penuh dan buffer visual 25 frame.
9. File audio dan video menggunakan path yang benar.
10. Jangan meninggalkan placeholder atau path file yang tidak tersedia.

Pastikan komponen Remotion benar-benar membaca `totalDurationInFrames` dan daftar scene terbaru. Jika total durasi composition ditentukan pada file lain, perbarui sumber durasi yang sebenarnya juga.

Jangan hanya mengubah angka di `data.ts` jika komposisi utama menggunakan durasi hardcoded.

---

## 7. VALIDASI DAN RENDER VIDEO

Setelah seluruh aset dan data diperbarui:

### A. Validasi kode

Jalankan dari folder proyek Remotion:

`npm run lint`

Jika gagal, baca error yang sebenarnya dan perbaiki kesalahan yang berhubungan dengan perubahan ini. Jangan menutupi error atau menonaktifkan linting hanya demi membuat proses lolos.

Periksa juga path aset yang digunakan oleh scene.

### B. Render

Buat output di:

`out/[NamaResep].mp4`

Gunakan command berikut apabila ID composition yang ditemukan di proyek memang `JajanCuan`:

`npx remotion render src/index.ts JajanCuan out/[NamaResep].mp4`

Jika entry point, ID composition, atau konfigurasi proyek yang sebenarnya berbeda, gunakan nilai yang ditemukan pada proyek. Jangan memaksakan ID yang salah.

### C. Verifikasi hasil render

Setelah proses render selesai:

- Pastikan file MP4 benar-benar ada dan tidak kosong.
- Periksa durasi video final dengan `ffprobe`.
- Periksa durasi audio dan bandingkan dengan total durasi video.
- Periksa dimensi 1080 × 1920 dan frame rate target 30 FPS.
- Periksa bahwa audio terdengar dan tidak terpotong.
- Periksa frame representatif dari setiap scene untuk memastikan semua N gambar tampil.
- Pastikan intro hanya berada pada scene pertama dan tidak menyebabkan teks ganda.
- Pastikan scene terakhir tetap tampil selama buffer akhir.
- Pastikan tidak ada gambar hilang, layar hitam yang tidak disengaja, frame rusak, atau transisi yang salah.

Jika verifikasi menemukan masalah, perbaiki dan render ulang bila memungkinkan.

### D. Salin hasil akhir

Setelah render berhasil dan file lolos verifikasi, salin video ke folder aset konten asal dengan nama:

`[NamaResep]_Final.mp4`

Jangan menimpa video final yang sudah ada tanpa memastikan file tujuan memang boleh diganti. Jika nama sudah digunakan, buat nama baru yang tidak menimpa hasil sebelumnya.

---

## 8. LAPORAN AKHIR WAJIB

Setelah benar-benar menyelesaikan pekerjaan, berikan laporan ringkas berisi:

- Nama folder konten yang diproses.
- Jumlah gambar konten yang ditemukan dan jumlah scene final.
- Konfirmasi jumlah gambar yang benar-benar tampil di video.
- Nama dan durasi aktual file audio.
- Durasi video final, dimensi, dan frame rate.
- Status lint dan render.
- Lokasi file video final di folder aset.
- Lokasi video render di folder Remotion.
- Setiap masalah yang belum terselesaikan atau penyesuaian yang dilakukan karena jumlah gambar berbeda dengan JSON.

Berikan link atau preview video yang dapat dibuka jika lingkungan mendukungnya. Jika hanya tersedia penyimpanan lokal, berikan path lengkap file hasilnya.

**Jangan menyatakan berhasil jika render atau verifikasi belum benar-benar berhasil.**

## PRIORITAS AKHIR

1. Jangan kehilangan atau melewatkan gambar konten.
2. Pertahankan audio voice-over secara utuh.
3. Jadikan durasi audio aktual sebagai acuan durasi keseluruhan video.
4. Gunakan JSON sebagai panduan proporsi durasi scene, bukan klaim sinkronisasi kalimat yang presisi.
5. Pertahankan struktur dan perilaku proyek Remotion yang sudah ada.
6. Utamakan kebenaran resep, keterbacaan visual, dan hasil render yang benar-benar dapat diputar.