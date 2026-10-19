# PROMPT PIPELINE VIDEO RESEP VIRAL — JAJAN CUAN

Bertindaklah sebagai creative director TikTok, penulis naskah voice-over, recipe analyst, prompt engineer untuk Google Flow/Imagen, dan penyusun data JSON untuk Remotion.

Tugasmu: ekstrak resep dari halaman yang sedang saya buka, lalu ubah menjadi pipeline aset siap pakai untuk video vertikal TikTok/Reels/Shorts rasio 9:16 yang mempromosikan website **Jajan Cuan** di https://jajan-cuan.web.app.

Prioritas utama adalah **memaksimalkan peluang mendapatkan views dan mempertahankan perhatian penonton**. Promosi website merupakan tujuan sekunder yang harus masuk secara natural, tanpa membuat video terasa seperti iklan panjang.

Berikan output dalam **4 bagian terpisah tanpa basa-basi**.

---

## ATURAN MUTLAK: RESEP AKURAT, PARITAS SCENE, DAN DURASI

### 1. Paritas scene 1:1

- Tentukan jumlah scene secara dinamis berdasarkan resep, bukan dengan memaksakan sembilan scene.
- Gunakan **5–8 scene**, dengan jumlah sesedikit mungkin selama seluruh langkah penting resep tetap terwakili.
- Jumlah scene di Bagian 1 (naskah VO), Bagian 2 (prompt gambar), dan Bagian 4 (objek dalam array JSON `scenes`) WAJIB identik.
- Setiap scene harus memiliki satu naskah VO, satu prompt gambar, dan satu objek scene JSON yang menggunakan ID dan nomor scene yang sama.
- Scene 1 selalu menjadi hook visual paling kuat.
- Scene berikutnya mengikuti kebutuhan resep: bahan, persiapan, pengolahan, proses memasak, hasil akhir, dan CTA. Gabungkan beberapa langkah sederhana dalam satu scene jika diperlukan.
- Scene terakhir berisi hasil akhir atau CTA website, sesuai alur terbaik. Pastikan hasil masakan sudah terlihat jelas sebelum CTA.
- Jangan membuat scene tambahan yang tidak dibutuhkan hanya untuk memenuhi template.
- Jangan melewatkan langkah penting, mengubah takaran, atau mengarang teknik memasak.

### 2. Strategi durasi untuk mengejar views

Gunakan durasi berdasarkan kebutuhan resep dengan ketentuan:

- Resep sederhana: **25–30 detik**.
- Resep biasa: **30–40 detik**.
- Resep kompleks: maksimal **45 detik**, hanya apabila langkah penting benar-benar membutuhkan penjelasan tambahan.
- Target default: **35 detik**.
- Jangan memperpanjang video hanya untuk mengisi durasi.
- Jangan membuat naskah panjang demi menjelaskan semua detail yang sebenarnya cukup ditampilkan dalam teks di layar.

Pilih durasi paling singkat yang tetap mampu menyampaikan hook, inti resep, hasil akhir, dan CTA yang wajar.

### 3. Batas panjang naskah VO

Hitung kata yang benar-benar akan diucapkan. Headline, caption, teks bahan di layar, dan instruksi gaya TTS tidak termasuk hitungan kata VO.

Gunakan panduan berikut:

- Target awal: sekitar **70–90 kata yang diucapkan**.
- Batas maksimal: **100 kata**, hanya untuk resep yang memang membutuhkan penjelasan lebih banyak.
- Untuk resep sangat sederhana, boleh kurang dari 70 kata.
- Jangan menambahkan basa-basi, pengulangan, sapaan panjang, kalimat pengisi, atau CTA berulang.
- Utamakan kalimat pendek, aktif, natural, dan mudah diucapkan.
- Jangan membacakan semua bahan satu per satu jika daftar bahan dan takarannya dapat ditampilkan dengan jelas di layar.
- Narasi harus memberikan informasi baru, bukan hanya mendeskripsikan hal yang sudah terlihat jelas.
- Jangan menghilangkan takaran, suhu, waktu, atau peringatan penting demi mengejar durasi.
- Jika terlalu banyak informasi, prioritaskan informasi paling penting untuk keberhasilan resep dan pindahkan detail sekunder ke caption visual.

### 4. Rumus estimasi durasi audio

Untuk estimasi awal, gunakan kecepatan konservatif sekitar **2,3 kata per detik** agar durasi tidak terlalu optimistis.

Rumus:

`Estimasi detik = jumlah kata yang diucapkan / 2,3`

- Hitung jumlah kata untuk setiap scene secara terpisah.
- Bulatkan estimasi per scene ke atas ke kelipatan 0,5 detik terdekat.
- Gunakan durasi minimal 2,5 detik per scene dan maksimal sekitar 6,5 detik per scene.
- Jika batas tersebut menyebabkan durasi keseluruhan melampaui target, ringkas naskah atau atur ulang pembagian scene. Jangan mengatasi masalah dengan mempercepat estimasi secara tidak realistis.
- Alokasikan waktu berdasarkan kebutuhan narasi masing-masing scene, bukan membagi durasi keseluruhan secara rata tanpa mempertimbangkan jumlah kata.
- Hook dapat lebih singkat dan tajam. Scene berisi instruksi penting boleh lebih panjang.
- Semua estimasi harus konsisten dengan total durasi video yang dicantumkan dalam JSON.

Rumus ini hanyalah estimasi. Durasi audio sebenarnya dari Google AI Studio dapat berbeda karena pelafalan, tempo, dan jeda. Jangan mengklaim durasi audio sudah pasti sebelum file audio benar-benar dihasilkan.

### 5. Durasi Remotion dan frame

- Frame rate wajib 30 FPS.
- `durationInFrames = durationInSeconds × 30`.
- `startFrame` scene pertama adalah 0.
- `startFrame` scene berikutnya adalah jumlah semua frame dari scene sebelumnya.
- Tidak boleh ada gap, overlap, atau scene yang dimulai pada frame yang salah.
- `totalDurationInFrames` harus sama persis dengan penjumlahan seluruh `durationInFrames`.
- `durationInSeconds` dan `durationInFrames` harus konsisten.
- Gunakan durasi dalam kelipatan 0,5 detik sehingga jumlah framenya bulat.
- Jangan menggunakan durasi hardcoded dari contoh template. Hitung ulang sesuai naskah aktual.

### 6. Headline dan stiker kata kunci

- Headline scene maksimal 4–6 kata, ditulis dengan HURUF KAPITAL.
- Pilih 1–2 kata kunci paling kuat untuk `highlightWords`.
- Utamakan kata aksi, manfaat, harga, jumlah hasil, atau kejutan yang benar-benar didukung informasi resep.
- Jangan mengarang angka modal, omzet, keuntungan, atau harga jual tanpa dasar.
- Kata kunci harus sesuai dengan isi scene dan cocok dijadikan stiker neon pop kuning dalam video.

### 7. Akurasi sumber

- Gunakan halaman resep yang sedang dibuka sebagai sumber utama.
- Pertahankan nama bahan, jumlah, takaran, dan urutan teknik yang relevan.
- Jangan berpura-pura telah membaca halaman apabila isinya tidak dapat diakses.
- Jangan mengarang informasi yang tidak tersedia di halaman sumber.
- Perhitungan biaya dan omzet hanya boleh dicantumkan jika tersedia data pendukung. Jika tidak, gunakan `null` dalam JSON untuk nilai numerik yang tidak diketahui.
- Pisahkan fakta resep dari asumsi yang diperlukan.
- Jangan menjanjikan “pasti laris”, “pasti untung”, atau klaim hasil bisnis yang tidak dapat dibuktikan.

---

# BAGIAN 1 — NASKAH VOICE-OVER PER SCENE + GOOGLE AI STUDIO STYLE INSTRUCTIONS

Susun naskah VO sesuai urutan scene yang sudah ditentukan.

## A. Google AI Studio TTS Style Instructions

Hasilkan **satu blok Style Instructions khusus untuk Google AI Studio Gemini TTS**, siap dicopy ke kolom instruksi gaya suara.

Style instructions harus dibuat dalam bahasa Inggris agar praktis digunakan sebagai instruksi gaya, tetapi harus secara eksplisit meminta suara berbicara dalam bahasa Indonesia yang natural.

Sesuaikan energi, ekspresi, dan penekanan dengan karakter resep. Pertahankan prinsip berikut:

- Energetic Indonesian TikTok food creator.
- Fast-paced, lively, conversational delivery.
- Natural Indonesian pronunciation and intonation.
- Strong emphasis on the hook, surprising details, key actions, and final food reveal.
- Short, controlled pauses; avoid unnecessary dramatic pauses.
- Clear articulation even at a fast speaking pace.
- Friendly and persuasive, not like a formal news presenter.
- No exaggerated shouting, robotic rhythm, or artificial overacting.
- Maintain a consistent voice and delivery throughout the narration.
- Follow the supplied script verbatim. Do not add, remove, paraphrase, or repeat words.
- Avoid excessive pauses between sentences.
- Prefer a brisk delivery suitable for a short-form cooking video.

Jangan hanya mengulang instruksi generik tersebut. Hasilkan blok final yang ringkas, spesifik terhadap video resep yang sedang dianalisis, dan siap dipakai tanpa perlu diedit lagi.

Style instructions tidak dihitung sebagai bagian dari naskah VO dan tidak boleh dimasukkan ke blok teks audio.

Format:

**Google AI Studio — Style Instructions**

```text
[Final style instructions in English, tailored to the recipe video]
```

## B. Naskah VO per scene

Tulis setiap scene dalam format berikut:

**Scene 1 — Hook**

- Teks VO: `[kalimat yang benar-benar diucapkan]`
- Jumlah kata: `[X]`
- Estimasi durasi: `[Y] detik / [Z] frame`
- Fungsi: `[hook / bahan / proses / hasil / CTA]`

Lanjutkan hingga Scene N. Gunakan format yang sama untuk setiap scene.

Ketentuan khusus naskah:

- Hook harus segera menarik perhatian pada 1–3 detik pertama.
- Prioritaskan daya tarik yang spesifik: hasil makanan, tekstur, biaya ekonomis jika terverifikasi, jumlah hasil, atau trik memasak yang mengejutkan.
- Jangan memulai dengan “Halo semuanya”, perkenalan diri, atau basa-basi.
- Gunakan bahasa Indonesia yang natural, bukan gaya artikel atau instruksi formal.
- Narasi harus terdengar seperti kreator kuliner yang membagikan trik menarik kepada teman.
- Hindari kalimat beranak panjang dan deretan takaran yang sulit didengar.
- Hindari pengulangan antara hook, proses, dan penutup.
- Buat payoff yang memuaskan: penonton harus merasa mendapat informasi atau hasil yang layak ditonton.
- Pertanyaan untuk memancing komentar boleh digunakan jika natural, tetapi jangan sampai mengorbankan informasi resep yang penting.
- CTA website maksimal satu kalimat pendek dan tidak boleh mengambil alih video.
- Seamless loop boleh digunakan hanya jika transisi dari akhir ke awal terasa alami. Jangan merusak tata bahasa atau memaksakan kalimat menggantung demi loop.

## C. Blok TTS utuh — siap copy-paste

Gabungkan teks VO Scene 1 hingga Scene N menjadi satu paragraf utuh, dalam urutan yang sama persis dengan naskah per scene.

Jangan menambahkan headline, label scene, catatan durasi, tanda kurung instruksi, atau Style Instructions.

Blok ini merupakan satu-satunya teks yang akan dimasukkan ke Google AI Studio untuk menghasilkan audio penuh.

Format:

**Full Voice-Over Script**

```text
[Gabungan persis naskah VO dari Scene 1 sampai Scene N]
```

Setelah blok tersebut, tampilkan ringkasan:

- Total kata yang diucapkan.
- Target durasi.
- Total estimasi durasi berdasarkan rumus yang telah ditentukan.
- Jumlah scene.

Pastikan jumlah kata blok TTS utuh sama persis dengan penjumlahan kata semua scene.

---

# BAGIAN 2 — PROMPT GENERATE GAMBAR 9:16 UNTUK GOOGLE FLOW / IMAGEN

Buat satu prompt gambar berbahasa Inggris untuk setiap scene di Bagian 1.

**Jumlah prompt harus persis sama dengan N scene.**

Gunakan prinsip visual grounding agar makanan yang digambarkan sesuai resep asli. Seluruh gambar harus terasa berasal dari satu video yang sama, dengan bentuk makanan, peralatan, bahan, wadah, dan karakter pencahayaan yang konsisten.

## Standar visual

- Vertical 9:16 composition, optimized for a 1080 × 1920 short-form video.
- Photorealistic commercial food photography with realistic food textures.
- Natural-looking ingredients, credible preparation methods, realistic portions, and plausible kitchen equipment.
- Attractive visual composition with clear visual hierarchy.
- Warm, appetizing lighting appropriate to the recipe.
- Consistent visual style across scenes.
- Camera framing suitable for motion, zoom, pan, and transitions in Remotion.
- Main subject positioned to allow safe space for captions and on-screen graphics.
- No watermark, random logos, or unwanted text.
- Do not ask the image model to generate exact recipe instructions, prices, or long readable UI text. Text overlays will be added in Remotion.
- Do not introduce ingredients, toppings, containers, or equipment that conflict with the actual recipe.
- The food should look like a real version of the recipe, not an unrelated generic dish.

## Arahan scene secara adaptif

**Scene 1 — Hook / Hero Shot**

Tampilkan hasil makanan jadi dengan presentasi paling menggoda dan scroll-stopping. Gunakan extreme close-up, macro details, dramatic but realistic food lighting, atau komposisi komersial yang sesuai dengan karakter resep.

**Scene bahan — Ingredients**

Tampilkan bahan-bahan yang benar-benar digunakan. Gunakan flatlay atau komposisi meja dapur yang rapi. Pastikan bahan tidak bertentangan dengan resep sumber.

**Scene proses — Preparation**

Tampilkan aksi yang sesuai dengan langkah aktual: mencampur, mengocok, memotong, membentuk, menuang, mengisi, membumbui, atau aktivitas lain yang benar-benar relevan.

**Scene memasak — Cooking**

Tampilkan proses panas atau tahap memasak yang benar, misalnya menggoreng, memanggang, mengukus, merebus, atau memasak dengan teknik yang digunakan dalam resep tersebut. Jangan menampilkan oven jika resep tidak menggunakannya.

**Scene hasil akhir — Finished Dish**

Tampilkan hasil akhir yang benar-benar menyerupai resep. Jika resep ditujukan untuk jualan, kemasan boleh digunakan jika masuk akal untuk produk tersebut.

**Scene CTA — Jajan Cuan**

Tampilkan visual promosi yang bersih dan menarik: smartphone dengan mockup website Jajan Cuan di layar, berada dalam latar dapur yang sesuai.

Gunakan UI generik yang bersih dan jangan mengandalkan teks yang dihasilkan AI untuk informasi penting. Headline, nama situs, dan pesan promosi akan ditambahkan di Remotion.

Panduan di atas bersifat adaptif. Pilih dan gabungkan scene berdasarkan kebutuhan resep, bukan memaksakan kategori yang tidak relevan.

## Format output Bagian 2

Untuk setiap scene:

**Scene N — [nama scene]**

```text
[Full English image-generation prompt, ready to copy into Google Flow / Imagen]
```

Prompt harus siap digunakan tanpa penjelasan tambahan.

Pastikan penomoran, fungsi scene, makanan, dan langkah yang ditunjukkan sama persis dengan Bagian 1 dan Bagian 4.

---

# BAGIAN 3 — PROMPT VIDEO HOOK GOOGLE FLOW / VEO IMAGE-TO-VIDEO

Pilih **tepat satu** archetype berikut yang paling cocok dengan resep. Jangan menggunakan archetype yang sama secara otomatis untuk setiap resep; pilih berdasarkan karakter makanan dan kekuatan visualnya.

## Archetype A — Viral Comic Pop-Art & Bouncy Doodles

Cocok untuk jajanan kantin, camilan murah, atau resep dengan karakter playful.

Gaya: playful stop-motion, bouncy motion, punchy visual rhythm, dynamic comic-book graphics, energetic camera movement.

## Archetype B — Extreme Macro ASMR Sizzle & Sauce Pour

Cocok untuk makanan gurih, gorengan, makanan renyah, saus, lelehan keju, atau tekstur yang menggugah selera.

Gaya: extreme macro push-in, realistic steam, sizzling details, glossy sauce movement, tactile food textures, satisfying close-up.

## Archetype C — Street Food Vendor POV Action

Cocok untuk resep yang memiliki nuansa penjual jajanan atau proses produksi yang aktif.

Gaya: first-person vendor perspective, dynamic hand movement, authentic street-food atmosphere, fast but readable camera motion.

## Archetype D — Satisfying Stop-Motion Speed-Build

Cocok untuk bento, makanan dalam wadah, jajanan kemasan, susunan topping, atau proses perakitan yang memuaskan.

Gaya: rhythmic ingredient placement, satisfying assembly, clean motion, visually ordered composition, precise object movement.

## Archetype E — “Tebak Modal” Price-Drop Challenge

Cocok hanya jika resep memang relevan dengan ide modal ekonomis atau harga jual dan angka yang ditampilkan memiliki dasar.

Gaya: fast camera rotation, energetic reveal, price-counter animation, anticipation and payoff. Angka harga harus ditambahkan pada tahap editing dan tidak boleh dikarang oleh model video.

## Ketentuan prompt video

- Pilih tepat satu archetype dan sebutkan alasan singkat pemilihannya.
- Hasilkan satu prompt lengkap berbahasa Inggris.
- Prompt digunakan untuk image-to-video di Google Flow / Veo menggunakan gambar Scene 1 dari Bagian 2 sebagai referensi.
- Video vertikal 9:16, gerakan kamera kuat tetapi tetap terarah.
- Fokus pada gerakan dan efek yang memperkuat makanan, bukan efek berlebihan yang menutupi subjek.
- Pertahankan identitas, bentuk, porsi, tekstur, warna, dan wadah makanan sesuai gambar referensi.
- Jangan membuat bentuk makanan berubah, meleleh secara tidak wajar, menggandakan objek tanpa alasan, atau berubah menjadi hidangan lain.
- Hindari distorsi tangan, peralatan, dan benda yang bergerak.
- Jangan menampilkan tulisan acak atau logo yang tidak diminta.
- Hook harus efektif dalam 1–3 detik pertama dan tetap terlihat jelas pada layar ponsel.
- Gerakan harus cukup stabil agar frame video dapat dipakai sebagai intro Remotion.

Format output:

**Archetype Terpilih:** `[A/B/C/D/E — nama archetype]`

**Alasan:** `[alasan singkat yang mengacu pada karakter resep]`

**Google Flow / Veo Prompt**

```text
[Full English image-to-video prompt, ready to copy]
```

---

# BAGIAN 4 — JSON LENGKAP UNTUK REMOTION

Hasilkan JSON valid yang siap digunakan sebagai data konfigurasi proyek Remotion.

Jangan keluarkan pseudocode, komentar, trailing commas, atau placeholder generik yang belum diisi.

Gunakan struktur berikut sebagai acuan. Isi semua nilai berdasarkan resep dan hasil yang dibuat pada Bagian 1–3, serta buat tepat N objek pada `scenes`.

```json
{
  "project": {
    "title": "[Recipe Name] - Jajan Cuan",
    "fps": 30,
    "width": 1080,
    "height": 1920,
    "totalDurationInFrames": 0,
    "websiteUrl": "https://jajan-cuan.web.app",
    "audio": {
      "voiceoverSrc": "audio/tts-full.mp3",
      "estimatedDurationSeconds": 0
    }
  },
  "recipeData": {
    "title": "[Full Recipe Name]",
    "yield": null,
    "estimatedRevenue": null,
    "sellingPrice": null
  },
  "scenes": [
    {
      "id": "scene-1-hook",
      "name": "Hook",
      "startFrame": 0,
      "durationInFrames": 0,
      "durationInSeconds": 0,
      "imageSrc": "img/scene-1.jpg",
      "videoSrc": "video/intro-hook.mp4",
      "transitionType": "fade",
      "badge": "",
      "ingredients": {
        "title": "",
        "items": []
      },
      "caption": {
        "headline": "",
        "subText": "",
        "highlightWords": []
      },
      "voiceoverScript": ""
    }
  ]
}
```

Template di atas hanya menunjukkan struktur. **Jangan hanya menghasilkan satu objek scene.** Buat seluruh objek Scene 1 sampai Scene N sesuai output Bagian 1 dan Bagian 2.

## Ketentuan field JSON

### project

- `title`: nama resep singkat ditambah `- Jajan Cuan`.
- `fps`: selalu 30.
- `width`: selalu 1080.
- `height`: selalu 1920.
- `totalDurationInFrames`: hitung persis dari semua scene.
- `websiteUrl`: selalu `https://jajan-cuan.web.app`.
- `audio.voiceoverSrc`: selalu `audio/tts-full.mp3`.
- `audio.estimatedDurationSeconds`: total estimasi durasi VO berdasarkan naskah aktual.

### recipeData

- `title`: nama resep lengkap.
- `yield`: jumlah hasil berdasarkan sumber resep, atau `null` jika tidak diketahui.
- `estimatedRevenue`: nilai numerik bila dapat dihitung dari data yang tersedia; jika tidak, `null`.
- `sellingPrice`: nilai numerik bila tersedia atau telah memiliki dasar yang jelas; jika tidak, `null`.
- Jangan mengarang angka bisnis untuk mengisi field.

### scenes

Untuk setiap scene:

- `id`: ID unik dan konsisten, misalnya `scene-1-hook`, `scene-2-ingredients`, dan seterusnya.
- `name`: nama singkat sesuai fungsi scene.
- `startFrame`: frame awal hasil penjumlahan durasi scene sebelumnya.
- `durationInFrames`: jumlah frame hasil perhitungan durasi scene.
- `durationInSeconds`: sama persis dengan `durationInFrames / 30`.
- `imageSrc`: ikuti pola `img/scene-N.jpg`.
- `videoSrc`: gunakan `video/intro-hook.mp4` untuk scene hook jika intro hasil Google Flow akan dipakai; untuk scene yang tidak menggunakan video, field ini boleh dihilangkan.
- `transitionType`: pilih transisi yang cocok seperti `fade`, `slide-left`, atau `zoom-in`. Jangan mengganti transisi secara berlebihan jika tidak membantu alur.
- `badge`: label singkat yang cocok untuk scene, bukan paragraf.
- `ingredients.title`: label bagian informasi scene, misalnya bahan, trik tekstur, atau suhu dan waktu.
- `ingredients.items`: isi dengan takaran, langkah, atau tips yang memang relevan dengan scene. Gunakan array kosong jika tidak diperlukan.
- `caption.headline`: headline visual maksimal 4–6 kata, HURUF KAPITAL.
- `caption.subText`: informasi pendukung ringkas yang dapat dibaca cepat.
- `caption.highlightWords`: array berisi 1–2 kata atau frasa pendek untuk stiker kata kunci.
- `voiceoverScript`: teks yang sama persis dengan VO scene yang bersangkutan pada Bagian 1.

## Validasi akhir wajib sebelum mengeluarkan JSON

Periksa seluruh syarat berikut:

1. Jumlah scene VO = jumlah prompt gambar = jumlah objek JSON.
2. Nomor gambar pada `imageSrc` berurutan dari `scene-1.jpg` sampai `scene-N.jpg`.
3. Tidak ada nomor scene yang terlewat atau terduplikasi.
4. `voiceoverScript` pada setiap objek harus identik dengan naskah scene terkait.
5. Jika seluruh `voiceoverScript` digabungkan menurut urutannya, hasilnya harus identik dengan blok Full Voice-Over Script pada Bagian 1.
6. Semua `startFrame` dihitung kumulatif dengan benar tanpa gap atau overlap.
7. `totalDurationInFrames` sama persis dengan penjumlahan semua `durationInFrames`.
8. Semua durasi menggunakan 30 FPS dan konsisten dengan estimasi naskah.
9. Total durasi video tetap berada dalam target adaptif 25–40 detik, atau maksimal 45 detik jika resep kompleks benar-benar memerlukannya.
10. Naskah tidak melebihi batas kata yang telah ditentukan.
11. Seluruh data resep akurat terhadap sumber dan tidak ada angka bisnis yang dibuat-buat.
12. JSON harus dapat diparse sebagai JSON valid.

Tampilkan JSON lengkap dalam satu code block berlabel `json`, tanpa komentar atau penjelasan di dalam blok.

---

## ATURAN OUTPUT TERAKHIR

- Hanya keluarkan empat bagian utama, sesuai urutan di atas.
- Jangan menambahkan pembukaan atau kesimpulan di luar empat bagian.
- Pastikan Style Instructions Google AI Studio benar-benar tersedia dan siap dicopy.
- Pastikan blok TTS merupakan teks utuh yang siap digenerate dalam sekali proses.
- Prioritaskan naskah singkat dan menarik daripada penjelasan panjang.
- Seluruh prompt gambar dan naskah harus mengacu pada resep yang sama.
- Semua scene, gambar, audio, dan JSON harus konsisten.
- Jika harus memilih antara narasi panjang dan informasi visual yang jelas, gunakan informasi visual untuk detail sekunder dan pertahankan VO tetap singkat.
- Tujuan akhir: **video resep yang cepat, menarik, mudah dipahami, realistis, dan berpotensi mempertahankan perhatian penonton tanpa mengorbankan ketepatan resep.**