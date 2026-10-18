Tolong ekstrak resep dari halaman yang sedang saya buka ini dan ubah menjadi pipeline aset siap pakai untuk pembuatan video pendek vertikal (TikTok/Reels/Shorts 9:16) mempromosikan website "Jajan Cuan".

Berikan output dalam 3 bagian terpisah tanpa basa-basi:

### 1. NASKAH VOICE OVER (TTS) LENGKAP
- Satukan dalam 1 blok teks utuh siap copas ke Google AI Studio.
- Durasi target: 35-40 detik.
- Gaya: Antusias, kasual, ramah kreator lokal, menonjolkan estimasi modal/harga jual, tips masak praktis, dan diakhiri CTA mengunjungi website Jajan Cuan untuk cek kalkulator HPP.

### 2. PROMPT GENERATE GAMBAR (GOOGLE FLOW / IMAGEN)
Buat prompt bahasa Inggris format 9:16 vertikal dengan prinsip visual grounding (mempertahankan bentuk asli makanan, porsi, dan wadah/kemasannya agar tidak halusinasi):
- Scene 1 (Hook): Foto komersial produk jadi dalam kemasannya (close-up/top-down, pencahayaan studio hangat, menggugah selera).
- Scene 2 (Step Awal): Proses persiapan atau perebusan/pengolahan bahan utama.
- Scene 3 (Step Bumbu/Saus): Proses menumis bumbu, mencampur saus, atau memasak isian.
- Scene 4 (Finishing & Packing): Proses mencampur, menata ke dalam wadah/kemasan, dan memberi topping.
- Scene 5 (CTA): Mockup layar smartphone vertikal menampilkan UI website Jajan Cuan (katalog resep & kalkulator HPP).

### 3. SKEMA JSON LENGKAP UNTUK REMOTION
Susun JSON dengan format persis seperti ini (30 FPS, total duration ~1140 frames):
{
  "project": {
    "title": "[Nama Resep Singkat] - Jajan Cuan",
    "fps": 30,
    "width": 1080,
    "height": 1920,
    "totalDurationInFrames": 1140,
    "audio": {
      "voiceoverSrc": "audio/tts-full.mp3"
    }
  },
  "recipeData": {
    "title": "[Judul Resep]",
    "yield": "[Jumlah Porsi / Mika]",
    "ingredients": [
      "[Daftar bahan lengkap beserta takaran dari halaman]"
    ],
    "steps": [
      "[Daftar langkah memasak lengkap dari halaman]"
    ]
  },
  "scenes": [
    {
      "id": "scene-1-hook",
      "name": "Hook Cover Resep",
      "startFrame": 0,
      "durationInFrames": 150,
      "imageSrc": "img/scene-1-hook.jpg",
      "caption": "[Teks Caption Singkat & Menarik]",
      "voiceoverScript": "[Potongan naskah VO untuk scene 1]"
    },
    {
      "id": "scene-2-prep",
      "name": "[Nama Step]",
      "startFrame": 150,
      "durationInFrames": 300,
      "imageSrc": "img/scene-2.jpg",
      "caption": "[Teks Caption]",
      "voiceoverScript": "[Potongan naskah VO untuk scene 2]"
    },
    {
      "id": "scene-3-cook",
      "name": "[Nama Step]",
      "startFrame": 450,
      "durationInFrames": 300,
      "imageSrc": "img/scene-3.jpg",
      "caption": "[Teks Caption]",
      "voiceoverScript": "[Potongan naskah VO untuk scene 3]"
    },
    {
      "id": "scene-4-finish",
      "name": "[Nama Step]",
      "startFrame": 750,
      "durationInFrames": 210,
      "imageSrc": "img/scene-4.jpg",
      "caption": "[Teks Caption]",
      "voiceoverScript": "[Potongan naskah VO untuk scene 4]"
    },
    {
      "id": "scene-5-cta",
      "name": "CTA Website Jajan Cuan",
      "startFrame": 960,
      "durationInFrames": 180,
      "imageSrc": "img/scene-5-cta.jpg",
      "caption": "CEK KALKULATOR HPP",
      "voiceoverScript": "[Potongan naskah VO CTA]"
    }
  ]
}