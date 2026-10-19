Tolong ekstrak resep dari halaman yang sedang saya buka ini dan ubah menjadi pipeline aset siap pakai untuk pembuatan video pendek vertikal (TikTok/Reels/Shorts 9:16) mempromosikan website "Jajan Cuan" (domain: jajan-cuan.web.app).

Berikan output dalam 4 bagian terpisah tanpa basa-basi:

---

### ATURAN PARITAS MUTLAK & FORMULA DURASI (1:1 PARITY):
1. **Jumlah Scene di Bagian 1 (Naskah VO), Bagian 2 (Prompt Gambar), dan Bagian 4 (JSON Remotion) WAJIB SAMA PERSIS (N Scene = N Gambar = N Objek Scene di JSON)!**
   - Misal: Jika di Bagian 2 kamu menghasilkan 8 gambar (Scene 1 s/d 8), maka Naskah VO harus memiliki persis 8 scene, dan `remotion.json` harus memuat persis 8 elemen scene (`img/scene-1.jpg` s/d `img/scene-8.jpg`).
   - Jika kamu menghasilkan 9 gambar (Scene 1 s/d 9), maka Naskah VO memiliki persis 9 scene, dan `remotion.json` memiliki persis 9 elemen scene (`img/scene-1.jpg` s/d `img/scene-9.jpg`).
2. **FORMULA DURASI NASKAH PER SLIDE (ANTI-TELAT / SINKRON OTOMATIS):**
   - Standar bicara bahasa Indonesia video pendek: **~3 kata per detik** (atau ~90 frame per 10 kata pada 30 FPS).
   - Tulis naskah untuk tiap slide secara terpisah dan hitung durasinya berdasarkan jumlah kata:
     `Estimasi Detik = Jumlah Kata / 3` (dibulatkan, minimal 3.0 detik, maksimal 6.5 detik per slide).
   - Durasi di JSON (`durationInFrames`) dihitung dari: `Estimasi Detik * 30`.
3. **HEADLINE BESAR & STIKER KATA KUNCI (UI/UX MODERN):**
   - Headline tiap slide dibuat singkat & padat (maksimal 4–6 kata huruf kapital).
   - Pilih 1–2 kata aksi/kata kunci paling menarik untuk `highlightWords` (kata ini akan otomatis berubah menjadi stiker neon pop kuning menyala di video).

---

### 1. NASKAH VOICE OVER (TTS) PER SLIDE + BLOK GABUNGAN
Tuliskan naskah VO terbagi **per slide** secara rinci agar transisi gambar berganti tepat saat kalimatnya selesai dibacakan:

- **Slide 1 (Hook Video AI):**
  - Teks: `"[Naskah pancingan modal murah & omzet - misal: Pie susu ekonomis 2 ribuan! Modalnya tipis tapi cuannya selangit.]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 2 (Bahan Baku & Takaran):**
  - Teks: `"[Naskah takaran bahan utama ekonomis]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 3 (Langkah Olah Dasar 1):**
  - Teks: `"[Naskah aksi olah dasar - misal: campur adonan / rebus]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 4 (Langkah Olah Dasar 2):**
  - Teks: `"[Naskah aksi cetak / potong / tusuk garpu]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 5 (Langkah Olah Bumbu/Isian):**
  - Teks: `"[Naskah racik bumbu / kocok vla]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 6 (Finishing Masak / Saring):**
  - Teks: `"[Naskah saring bumbu / tuang isian ke kulit]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 7 (Proses Masak Panas / Oven / Goreng):**
  - Teks: `"[Naskah oven / goreng di minyak panas / kukus]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 8 (Kemas Mika & Natural Comment Bait):**
  - Teks: `"[Naskah kemas mika rapi + 1 kalimat pancingan komentar penonton: 'Kira-kira kalau di daerahmu pasnya dijual berapa? Coba komen di bawah!']"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)
- **Slide 9 (CTA Website Jajan Cuan & Seamless Loop Bridge):**
  - Teks: `"[Ajakan cek 50+ resep viral & kalkulator modal di website Jajan Cuan + kalimat penutup menggantung yang langsung menyambung ke kata pertama Slide 1!]"`
  - Estimasi: [X] kata (~[Y] detik / [Z] frame)

#### *Blok Teks Utuh TTS (Siap Copas ke Edge-TTS / ElevenLabs / Google AI Studio):*
```
[Gabungkan seluruh teks naskah Slide 1 s/d Slide 9 di sini menjadi 1 paragraf utuh untuk di-generate audionya dalam sekali klik]
```

---

### 2. PROMPT GENERATE GAMBAR (N SCENES 9:16 UNTUK GOOGLE FLOW / IMAGEN)
Buat prompt bahasa Inggris format 9:16 vertikal dengan prinsip visual grounding (mempertahankan bentuk asli makanan, porsi, dan wadah kemasan agar realistis & konsisten).
**JUMLAH PROMPT HARUS PERSIS SAMA DENGAN JUMLAH SLIDE DI BAGIAN 1 (N GAMBAR):**
- **Scene 1 (Hook Cover):** Foto komersial produk jadi dalam wadah mika berjajar rapi, taburan topping melimpah, lighting studio hangat menggugah selera, sudut pandang eye-level vertikal 9:16.
- **Scene 2 (Bahan Mentah/Flatlay):** Susunan rapi bahan-bahan mentah dengan takaran ekonomis di atas talenan kayu bersih / meja dapur estetik, pencahayaan natural cerah.
- **Scene 3 (Proses Masak 1):** Proses pengolahan adonan dasar / kulit / rebusan bahan utama dengan uap mengepul lembut.
- **Scene 4 (Proses Masak 2):** Mencetak adonan / menusuk dengan garpu / memotong bahan rapi di atas meja dapur.
- **Scene 5 (Proses Masak 3):** Mengolah isian / bumbu / vla di mangkuk atau gelas takar dengan whisk / spatula.
- **Scene 6 (Proses Masak 4):** Menuang isian lewat saringan halus ke atas kulit pie / mencampur saus merata.
- **Scene 7 (Proses Masak 5 - Panas):** Produk sedang dipanggang di dalam oven keemasan / digoreng mendesis di wajan panas.
- **Scene 8 (Hasil Jadi & Kemas Mika):** Deretan penuh wadah mika bening berisi makanan jadi yang tertata rapi siap jual di meja booth.
- **Scene 9 (Mockup CTA Website):** Mockup 3D floating smartphone vertikal menampilkan layar aplikasi "Jajan Cuan" (katalog 50+ resep viral & slider kalkulator modal otomatis), latar belakang dapur modern bernuansa pastel hangat.

---

### 3. PROMPT VIDEO HOOK (GOOGLE FLOW / VEO - IMAGE TO VIDEO)
Pilih **SALAH SATU dari 5 Archetype Kreatif** di bawah ini yang paling sesuai dengan karakter resep, agar thumbnail dan 3 detik pertama tiap video selalu segar, unik, dan tidak monoton:

#### *Menu 5 Archetype Hook Kreatif:*
- **Archetype A: Viral Comic Pop-Art & Bouncy Doodles** *(Jajanan kantin anak sekolah: bouncy stop-motion, sosis melompat, border kertas robek)*
- **Archetype B: Extreme Macro ASMR Sizzle & Sauce Pour** *(Makanan gurih/panas/lumer: macro push-in, saus mengalir berkilau, keju mulur, uap mendesis)*
- **Archetype C: Street Food Vendor POV Action Cam** *(Street food otentik: first-person tangan penjual lincah di gerobak jualan ramai)*
- **Archetype D: Satisfying Stop-Motion Speed-Build** *(Bento/mika kekinian: bahan melompat masuk wadah mika secara ritmis rapi & aesthetic)*
- **Archetype E: The "Tebak Modal" Price-Drop Challenge** *(Menu viral modal receh: kamera memutar cepat, counter harga digital berputar lalu terkunci di Rp 2.000)*

**Format Output Bagian 3:** Tuliskan pilihan Archetype yang dipilih beserta prompt bahasa Inggris lengkapnya yang siap di-copas ke Google Flow / Veo Image-to-Video menggunakan gambar Scene 1 sebagai referensi.

---

### 4. SKEMA JSON LENGKAP UNTUK REMOTION (N SCENES 1:1 MAPPING)
Susun data JSON terstruktur untuk Remotion (30 FPS, 1080x1920).
**Array `scenes` WAJIB memuat tepat N elemen sesuai jumlah gambar di Bagian 2 (tanpa ada nomor gambar yang dilewati), dengan durasi yang sudah dihitung proporsional dari naskah per slide di Bagian 1:**

```json
{
  "project": {
    "title": "[Nama Resep Singkat] - Jajan Cuan",
    "fps": 30,
    "width": 1080,
    "height": 1920,
    "totalDurationInFrames": 1560,
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
      "name": "Hook Video AI",
      "startFrame": 0,
      "durationInFrames": 150,
      "durationInSeconds": 5.0,
      "imageSrc": "img/scene-1.jpg",
      "videoSrc": "video/intro-iframe.mp4",
      "transitionType": "fade",
      "badge": "🔥 1 RESEP = [JUMLAH HASIL]",
      "caption": {
        "headline": "",
        "subText": "",
        "highlightWords": []
      },
      "voiceoverScript": "[Naskah Slide 1]"
    },
    {
      "id": "scene-2-prep",
      "name": "Bahan Pokok & Takaran",
      "startFrame": 150,
      "durationInFrames": 195,
      "durationInSeconds": 6.5,
      "imageSrc": "img/scene-2.jpg",
      "transitionType": "slide-left",
      "badge": "📋 BAHAN & TAKARAN",
      "ingredients": {
        "title": "Bahan Baku",
        "items": [
          "[Bahan 1 + takaran]",
          "[Bahan 2 + takaran]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE SINGKAT STEP 2]",
        "subText": "[Petunjuk ringkas]",
        "highlightWords": ["[KATA KUNCI 1]"]
      },
      "voiceoverScript": "[Naskah Slide 2]"
    },
    {
      "id": "scene-3-step1",
      "name": "Langkah 1: Olah Dasar",
      "startFrame": 345,
      "durationInFrames": 105,
      "durationInSeconds": 3.5,
      "imageSrc": "img/scene-3.jpg",
      "transitionType": "slide-left",
      "badge": "🥣 LANGKAH 1",
      "ingredients": {
        "title": "Tips Olah",
        "items": [
          "[Tips penting]",
          "[Waktu/tekstur]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE PUNCHY STEP 3]",
        "subText": "[Petunjuk ringkas]",
        "highlightWords": ["[KATA KUNCI]"]
      },
      "voiceoverScript": "[Naskah Slide 3]"
    },
    {
      "id": "scene-4-step2",
      "name": "Langkah 2: Cetak / Bentuk",
      "startFrame": 450,
      "durationInFrames": 105,
      "durationInSeconds": 3.5,
      "imageSrc": "img/scene-4.jpg",
      "transitionType": "slide-left",
      "badge": "🥧 LANGKAH 2",
      "ingredients": {
        "title": "Trik Cetak",
        "items": [
          "[Petunjuk cetak/bentuk]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE PUNCHY STEP 4]",
        "subText": "[Petunjuk ringkas]",
        "highlightWords": ["[KATA KUNCI]"]
      },
      "voiceoverScript": "[Naskah Slide 4]"
    },
    {
      "id": "scene-5-step3",
      "name": "Langkah 3: Bumbu / Isian",
      "startFrame": 555,
      "durationInFrames": 120,
      "durationInSeconds": 4.0,
      "imageSrc": "img/scene-5.jpg",
      "transitionType": "slide-left",
      "badge": "🥛 LANGKAH 3",
      "ingredients": {
        "title": "Racik Isian",
        "items": [
          "[Bahan isian 1]",
          "[Bahan isian 2]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE PUNCHY STEP 5]",
        "subText": "[Petunjuk ringkas]",
        "highlightWords": ["[KATA KUNCI]"]
      },
      "voiceoverScript": "[Naskah Slide 5]"
    },
    {
      "id": "scene-6-step4",
      "name": "Langkah 4: Finishing Olah",
      "startFrame": 675,
      "durationInFrames": 120,
      "durationInSeconds": 4.0,
      "imageSrc": "img/scene-6.jpg",
      "transitionType": "slide-left",
      "badge": "✨ LANGKAH 4",
      "ingredients": {
        "title": "Trik Tekstur",
        "items": [
          "[Tips saring/campur]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE PUNCHY STEP 6]",
        "subText": "[Petunjuk ringkas]",
        "highlightWords": ["[KATA KUNCI]"]
      },
      "voiceoverScript": "[Naskah Slide 6]"
    },
    {
      "id": "scene-7-step5",
      "name": "Langkah 5: Masak Panas / Oven",
      "startFrame": 795,
      "durationInFrames": 240,
      "durationInSeconds": 8.0,
      "imageSrc": "img/scene-7.jpg",
      "transitionType": "slide-left",
      "badge": "🔥 LANGKAH 5",
      "ingredients": {
        "title": "Suhu & Waktu",
        "items": [
          "[Suhu oven/api]",
          "[Waktu masak]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE PUNCHY STEP 7]",
        "subText": "[Petunjuk suhu & waktu]",
        "highlightWords": ["[KATA KUNCI]"]
      },
      "voiceoverScript": "[Naskah Slide 7]"
    },
    {
      "id": "scene-8-pack",
      "name": "Kemas Mika & Comment Bait",
      "startFrame": 1035,
      "durationInFrames": 300,
      "durationInSeconds": 10.0,
      "imageSrc": "img/scene-8.jpg",
      "transitionType": "slide-left",
      "badge": "📦 KEMAS MIKA JUALAN",
      "ingredients": {
        "title": "Kemas & Cuan",
        "items": [
          "1 Resep = [X] pcs",
          "Estimasi Omzet: [Omzet]"
        ]
      },
      "caption": {
        "headline": "[HEADLINE COMMENT BAIT]",
        "subText": "[Pancingan pertanyaan harga penonton]",
        "highlightWords": ["KOMEN DI BAWAH"]
      },
      "voiceoverScript": "[Naskah Slide 8]"
    },
    {
      "id": "scene-9-cta",
      "name": "CTA Website Jajan Cuan & Loop",
      "startFrame": 1335,
      "durationInFrames": 225,
      "durationInSeconds": 7.5,
      "imageSrc": "img/scene-9.jpg",
      "transitionType": "zoom-in",
      "badge": "📚 50+ RESEP VIRAL LAINNYA",
      "caption": {
        "headline": "50+ RESEP VIRAL LAINNYA",
        "subText": "Kunjungi https://jajan-cuan.web.app sekarang!",
        "highlightWords": ["JAJAN CUAN", "KALKULATOR HPP"]
      },
      "voiceoverScript": "[Naskah Slide 9]"
    }
  ]
}
```
*(Catatan: Jika ada N gambar, pastikan scenes[] memiliki persis N objek scene dengan startFrame berurutan tanpa jeda bolong!).*
