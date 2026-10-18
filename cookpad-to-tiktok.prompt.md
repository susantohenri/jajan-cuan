Tolong ekstrak resep dari halaman yang sedang saya buka ini dan ubah menjadi pipeline aset siap pakai untuk pembuatan video pendek vertikal (TikTok/Reels/Shorts 9:16) mempromosikan website "Jajan Cuan" (domain: jajan-cuan.web.app).

Berikan output dalam 4 bagian terpisah tanpa basa-basi:

---

### ATURAN PARITAS MUTLAK (1:1 PARITY - SANGAT PENTING):
1. **Jumlah Scene di Bagian 1 (Naskah VO), Bagian 2 (Prompt Gambar), dan Bagian 4 (JSON Remotion) WAJIB SAMA PERSIS (N Scene = N Gambar = N Objek Scene di JSON)!**
   - Misal: Jika di Bagian 2 kamu menghasilkan 8 gambar (Scene 1 s/d 8), maka Naskah VO harus memiliki persis 8 scene, dan `remotion.json` harus memuat persis 8 elemen scene (`img/scene-1.jpg` s/d `img/scene-8.jpg`).
   - Jika kamu menghasilkan 9 gambar (Scene 1 s/d 9), maka Naskah VO memiliki persis 9 scene, dan `remotion.json` memiliki persis 9 elemen scene (`img/scene-1.jpg` s/d `img/scene-9.jpg`).
2. **DILARANG MELONCATKAN NAMA/NOMOR GAMBAR PADA JSON!**
   - File gambar di JSON harus runtut tanpa ada yang terlewat: `img/scene-1.jpg`, `img/scene-2.jpg`, ..., `img/scene-N.jpg`.
   - Dilarang membuat prompt untuk 9 gambar tetapi di JSON hanya ada 7 scene dan langsung meloncat dari `scene-6.jpg` ke `scene-9-cta.jpg`!
3. **DISTRIBUSI DURASI PROPORSIONAL (ANTI-FREEZE):**
   - Setiap scene rata-rata berdurasi 3 – 5.5 detik (90 – 165 frame pada 30 FPS).
   - Scene penutup (CTA) maksimal berdurasi 4 – 6 detik (120 – 180 frame).
   - **DILARANG membuat satu scene yang durasinya puluhan detik (misal 20-30 detik)** hanya untuk menghabiskan sisa audio. Pecah narasi dan visualnya menjadi scene-scene tersendiri.

---

### 1. NASKAH VOICE OVER (TTS) LENGKAP (N SCENES DENGAN SEAMLESS LOOP & COMMENT BAIT)
- Satukan dalam 1 blok teks utuh siap copas ke Google AI Studio / ElevenLabs / Edge-TTS.
- Durasi target: 38-50 detik (~1140 - 1500 frame pada 30 FPS).
- Gaya: Antusias, energik, kasual, ramah kreator lokal, menonjolkan estimasi modal/harga jual, potensi omzet ("1 Resep jadi [X] mika!"), tips masak praktis anti-gagal, dan diakhiri ajakan ke website Jajan Cuan.
- **Wajib Menerapkan 2 Fitur Retensi Viral:**
  1. **Seamless Loop (Sentence Bridge):**
     - Kalimat terakhir di scene penutup (CTA) **HARUS diakhiri dengan frasa/konjungsi menggantung** yang secara gramatikal langsung menyambung ke kata-kata pembuka di Scene 1!
     - *Contoh Loop:* 
       - Outro (Scene Terakhir): `"...karena semua rahasia jualan laris dan hitungan modalnya udah lengkap di website Jajan Cuan, apalagi buat resep..."`
       - Hook (Scene 1): `"Pie susu ekonomis dua ribuan ini! Modalnya tipis tapi cuannya selangit..."`
       - Penonton yang menonton sampai habis tidak akan sadar videonya sudah mengulang dari awal (memicu >100% completion rate!).
  2. **Natural Comment Bait (Pancingan Komentar Organik):**
     - Pada Scene sebelum CTA (biasanya Scene N-1), **selipkan 1 pancingan komentar alami** yang memicu jempol penonton berinteraksi (debat harga di daerah mereka, selera topping, atau ide nama baru).
     - *Contoh:* `"Menurut kalian kalau dijual 2 ribu di daerahmu, ini kemurahan apa pas nih? Coba komen di bawah!"` atau `"Kalian tim saus pedas manis apa tim keju lumer melimpah?"`

- **Pembagian N Scene (Pacing Cepat 3.0 - 5.5 Detik per Scene):**
  - **Scene 1 (Hook AI Video):** Hook pembuka rasa penasaran harga/modal receh & omzet ("Modal 2 ribuan per mika...").
  - **Scene 2 (Bahan Baku & Takaran):** Tampilan bahan mentah utama & takaran ekonomis yang bikin untung berlipat.
  - **Scene 3 (Olah Dasar / Persiapan):** Langkah awal (misal: aduk adonan / rebus / potong) + tips waktu/tekstur.
  - **Scene 4 (Proses Masak 2):** Masak isian / tumis bumbu / kocok vla.
  - **Scene 5 (Pencampuran / Finishing Masak):** Tuang isian / oven panggang / campur saus.
  - **Scene 6 (Hasil Matang Sempurna):** Tampilan produk matang mengkilap & aroma harum.
  - **Scene 7 (Kemas ke Mika / Wadah):** Proses porsi rapi ke wadah jualan ("1 resep bisa jadi [X] mika!").
  - **Scene 8 (Comment Bait & Estimasi Omzet):** Tampilan mika siap jual + lempar pertanyaan komentar penonton.
  - **Scene 9 (CTA Website + Loop Bridge):** Ajakan cek 50+ resep viral & kalkulator HPP otomatis di website Jajan Cuan + jembatan kalimat loop.
  *(Catatan: Jika resep lebih ringkas dan kamu membaginya menjadi 7 atau 8 scene, pastikan Bagian 2 dan Bagian 4 persis 7 atau 8 scene juga!)*

---

### 2. PROMPT GENERATE GAMBAR (N SCENES 9:16 UNTUK GOOGLE FLOW / IMAGEN)
Buat prompt bahasa Inggris format 9:16 vertikal dengan prinsip visual grounding (mempertahankan bentuk asli makanan, porsi, dan wadah kemasan agar realistis & konsisten).
**JUMLAH PROMPT HARUS PERSIS SAMA DENGAN JUMLAH SCENE DI BAGIAN 1 (N GAMBAR):**
- **Scene 1 (Hook Cover):** Foto komersial produk jadi dalam wadah mika berjajar rapi, taburan topping melimpah, lighting studio hangat menggugah selera, sudut pandang eye-level vertikal 9:16.
- **Scene 2 (Bahan Mentah/Flatlay):** Susunan rapi bahan-bahan mentah dengan takaran ekonomis di atas talenan kayu bersih / meja dapur estetik, pencahayaan natural cerah.
- **Scene 3 (Proses Masak 1):** Proses pengolahan adonan dasar / kulit / rebusan bahan utama dengan uap mengepul lembut.
- **Scene 4 (Proses Masak 2):** Mengolah isian / bumbu / vla di mangkuk atau wajan dengan tekstur kental berkilau.
- **Scene 5 (Proses Masak 3):** Menuang isian ke adonan / memanggang dalam oven / mencampur saus merata.
- **Scene 6 (Hasil Matang Sempurna):** Produk matang baru keluar dari oven/wajan, warna keemasan menggoda, tekstur renyah dan lembut.
- **Scene 7 (Kemas Mika):** Tangan memakai sarung tangan higienis sedang membagi porsi makanan secara rapi ke dalam jejeran mika plastik bening.
- **Scene 8 (Showcase Hasil Jadi & Topping):** Deretan penuh mika siap jual dengan stiker brand atau taburan melimpah, menggugah selera.
- **Scene 9 (Mockup CTA Website):** Mockup 3D floating smartphone vertikal menampilkan layar aplikasi "Jajan Cuan" (katalog 50+ resep viral & slider kalkulator modal otomatis), latar belakang dapur modern bernuansa pastel hangat.

---

### 3. PROMPT VIDEO HOOK (GOOGLE FLOW / VEO - IMAGE TO VIDEO)
Pilih **SALAH SATU dari 5 Archetype Kreatif** di bawah ini yang paling sesuai dengan karakter resep, agar thumbnail dan 3 detik pertama tiap video selalu segar, unik, dan tidak monoton:

#### *Menu 5 Archetype Hook Kreatif:*
- **Archetype A: Viral Comic Pop-Art & Bouncy Doodles** *(Cocok untuk jajanan kantin anak sekolah yang ceria & playful)*
  - Elemen: Stop-motion bouncy pop-up, potongan sosis/bahan melompat ceria, ripped paper border (kuning-merah), flying money, stiker komik 3D "IDE JUALAN 2 RIBUAN!".
- **Archetype B: Extreme Macro ASMR Sizzle & Sauce Pour** *(Cocok untuk makanan gurih/lumer, saus medok, keju meleleh, pie vla lumer)*
  - Elemen: Slow-motion macro push-in, saus kental berkilau disiramkan meleleh di atas makanan, uap panas mendesis mengepul tebal, cinematic warm lighting, efek lumer memuaskan.
- **Archetype C: Street Food Vendor POV Action Cam** *(Cocok untuk menu street food / kaki lima otentik)*
  - Elemen: First-person perspective tangan penjual yang lincah menata mika di atas gerobak jualan yang ramai, camera movement cepat energik, suasana antrean pembeli di latar belakang.
- **Archetype D: Satisfying Stop-Motion Speed-Build** *(Cocok untuk menu bento/mika kekinian yang tertata aesthetic)*
  - Elemen: Gerakan ritmis stop-motion bahan satu per satu melompat masuk ke wadah mika secara presisi, topping berhamburan rapi otomatis, visual bersih memuaskan (oddly satisfying).
- **Archetype E: The "Tebak Modal" Price-Drop Challenge** *(Cocok untuk menu viral dengan modal sangat receh/mengejutkan)*
  - Elemen: Kamera berputar dinamis mendekati produk jadi, animasi counter angka digital berputar cepat lalu terkunci di angka "Rp 2.000 / Mika" dengan efek stempel merah "UNTUNG MELIMPAH!".

**Format Output Bagian 3:** Tuliskan pilihan Archetype yang dipilih beserta prompt bahasa Inggris lengkapnya yang siap di-copas ke Google Flow / Veo Image-to-Video menggunakan gambar Scene 1 sebagai referensi.

---

### 4. SKEMA JSON LENGKAP UNTUK REMOTION (N SCENES 1:1 MAPPING)
Susun data JSON terstruktur untuk Remotion (30 FPS, 1080x1920) dengan durasi total mengikuti naskah voiceover.
**Array `scenes` WAJIB memuat tepat N elemen sesuai jumlah gambar di Bagian 2 (tanpa ada nomor gambar yang dilewati):**

```json
{
  "project": {
    "title": "[Nama Resep Singkat] - Jajan Cuan",
    "fps": 30,
    "width": 1080,
    "height": 1920,
    "totalDurationInFrames": 1500,
    "websiteUrl": "https://jajan-cuan.web.app",
    "audio": {
      "voiceoverSrc": "audio/tts-full.mp3"
    }
  },
  "recipeData": {
    "title": "[Judul Resep Lengkap]",
    "yield": "[Jumlah Hasil, misal: 18 Pcs]",
    "estimatedRevenue": "[Estimasi Omzet, misal: Rp 36.000]",
    "sellingPrice": "[Harga Jual per Mika, misal: Rp 2.000]"
  },
  "scenes": [
    {
      "id": "scene-1-hook",
      "name": "Hook Video AI (Google Flow)",
      "startFrame": 0,
      "durationInFrames": 135,
      "durationInSeconds": 4.5,
      "imageSrc": "img/scene-1.jpg",
      "videoSrc": "video/intro-iframe.mp4",
      "transitionType": "fade",
      "badge": "🔥 1 RESEP = [JUMLAH HASIL]",
      "caption": {
        "headline": "",
        "subText": "",
        "highlightWords": []
      },
      "voiceoverScript": "[Naskah VO Scene 1 - Hook pembuka]"
    },
    {
      "id": "scene-2-prep",
      "name": "Bahan Pokok & Takaran",
      "startFrame": 135,
      "durationInFrames": 180,
      "durationInSeconds": 6.0,
      "imageSrc": "img/scene-2.jpg",
      "transitionType": "slide-left",
      "badge": "📋 BAHAN & TAKARAN",
      "ingredients": {
        "title": "Bahan Baku Utama",
        "items": [
          "[Bahan 1 + takaran gram/sdm]",
          "[Bahan 2 + takaran gram/sdm]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE BAHAN POKOK]",
        "subText": "[Takaran hemat modal receh]",
        "highlightWords": ["[KATA KUNCI 1]", "[KATA KUNCI 2]"]
      },
      "voiceoverScript": "[Naskah VO Scene 2]"
    },
    {
      "id": "scene-3-step1",
      "name": "Langkah 1: Olah Dasar",
      "startFrame": 315,
      "durationInFrames": 180,
      "durationInSeconds": 6.0,
      "imageSrc": "img/scene-3.jpg",
      "transitionType": "slide-left",
      "badge": "🍳 LANGKAH 1: [JUDUL LANGKAH]",
      "ingredients": {
        "title": "Cara Olah 1",
        "items": [
          "[Waktu rebus/aduk]",
          "[Tips agar tekstur pas]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE BESAR STEP 1]",
        "subText": "[Petunjuk langkah 1 ringkas]",
        "highlightWords": ["[KATA KUNCI 1]", "[KATA KUNCI 2]"]
      },
      "voiceoverScript": "[Naskah VO Scene 3]"
    },
    {
      "id": "scene-4-step2",
      "name": "Langkah 2: Olah Isian/Bumbu",
      "startFrame": 495,
      "durationInFrames": 180,
      "durationInSeconds": 6.0,
      "imageSrc": "img/scene-4.jpg",
      "transitionType": "slide-left",
      "badge": "🥛 LANGKAH 2: [JUDUL LANGKAH]",
      "ingredients": {
        "title": "Racikan Bahan",
        "items": [
          "[Bahan isian 1 + takaran]",
          "[Tips saring/aduk]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE STEP 2]",
        "subText": "[Petunjuk bumbu/isian]",
        "highlightWords": ["[KATA KUNCI 1]", "[KATA KUNCI 2]"]
      },
      "voiceoverScript": "[Naskah VO Scene 4]"
    },
    {
      "id": "scene-5-step3",
      "name": "Langkah 3: Finishing Masak",
      "startFrame": 675,
      "durationInFrames": 180,
      "durationInSeconds": 6.0,
      "imageSrc": "img/scene-5.jpg",
      "transitionType": "slide-left",
      "badge": "🔥 LANGKAH 3: [JUDUL LANGKAH]",
      "ingredients": {
        "title": "Suhu & Waktu",
        "items": [
          "[Suhu panggang/api]",
          "[Waktu matang]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE STEP 3]",
        "subText": "[Petunjuk masak finishing]",
        "highlightWords": ["[KATA KUNCI 1]", "[KATA KUNCI 2]"]
      },
      "voiceoverScript": "[Naskah VO Scene 5]"
    },
    {
      "id": "scene-6-step4",
      "name": "Langkah 4: Hasil Matang",
      "startFrame": 855,
      "durationInFrames": 165,
      "durationInSeconds": 5.5,
      "imageSrc": "img/scene-6.jpg",
      "transitionType": "slide-left",
      "badge": "✨ HASIL MATANG SEMPURNA",
      "ingredients": {
        "title": "Tekstur & Aroma",
        "items": [
          "[Karakter produk matang]",
          "[Aroma menggoda selera]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE HASIL MATANG]",
        "subText": "[Kualitas visual produk]",
        "highlightWords": ["[KATA KUNCI 1]", "[KATA KUNCI 2]"]
      },
      "voiceoverScript": "[Naskah VO Scene 6]"
    },
    {
      "id": "scene-7-pack",
      "name": "Kemas Jadi [X] Mika",
      "startFrame": 1020,
      "durationInFrames": 165,
      "durationInSeconds": 5.5,
      "imageSrc": "img/scene-7.jpg",
      "transitionType": "slide-left",
      "badge": "📦 KEMAS MIKA JUALAN",
      "ingredients": {
        "title": "Kemas & Porsi",
        "items": [
          "Bagi rata jadi [X] mika kecil",
          "Harga jual Rp [X] / mika"
        ]
      },
      "caption": {
        "headline": "KEMAS JADI [JUMLAH HASIL] MIKA!",
        "subText": "[Teknik porsi higienis & cepat]",
        "highlightWords": ["[JUMLAH HASIL] MIKA", "UNTUNG MAKSIMAL"]
      },
      "voiceoverScript": "[Naskah VO Scene 7 - Membahas porsi omzet]"
    },
    {
      "id": "scene-8-comment",
      "name": "Comment Bait Harga Daerah",
      "startFrame": 1185,
      "durationInFrames": 150,
      "durationInSeconds": 5.0,
      "imageSrc": "img/scene-8.jpg",
      "transitionType": "slide-left",
      "badge": "💬 TANYA PENDAPAT PENONTON",
      "caption": {
        "headline": "HARGA DI DAERAHMU BERAPA?",
        "subText": "Coba tulis pendapatmu di kolom komentar!",
        "highlightWords": ["HARGA", "KOMEN DI BAWAH"]
      },
      "voiceoverScript": "[Naskah VO Scene 8 - Mengandung Natural Comment Bait]"
    },
    {
      "id": "scene-9-cta",
      "name": "CTA Website Jajan Cuan & Loop",
      "startFrame": 1335,
      "durationInFrames": 165,
      "durationInSeconds": 5.5,
      "imageSrc": "img/scene-9.jpg",
      "transitionType": "zoom-in",
      "badge": "📚 50+ RESEP VIRAL LAINNYA",
      "caption": {
        "headline": "50+ RESEP VIRAL LAINNYA",
        "subText": "Cek resep viral & kalkulator HPP otomatis di website Jajan Cuan!",
        "highlightWords": ["50+ RESEP VIRAL", "KALKULATOR HPP", "JAJAN CUAN"]
      },
      "voiceoverScript": "[Naskah VO Scene 9 - CTA Website + Sentence Bridge Loop di akhir kalimat]"
    }
  ]
}
```
*(Ingat: Jika ada 8 gambar, maka scenes[] berisi 8 elemen dengan imageSrc img/scene-1.jpg s/d img/scene-8.jpg. Jika ada 9 gambar, scenes[] berisi 9 elemen img/scene-1.jpg s/d img/scene-9.jpg. Setiap gambar wajib memiliki scene tersendiri!).*
