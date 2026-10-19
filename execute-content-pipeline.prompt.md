# PROMPT EKSEKUSI KONTEN REMOTION (HYBRID WORKFLOW)
Gunakan prompt di bawah ini untuk memulai pembuatan video konten berikutnya di Antigravity / Gemini.

---

```markdown
Halo! Aku mau bikin konten video TikTok/Reels baru untuk aplikasi "Jajan Cuan" menggunakan pipeline Remotion kita.

### 1. Lokasi Folder Aset & Remotion:
- Folder Aset: `C:\Users\webhe\Downloads\MSI\Local Sites\jajan-cuan\assets\[NAMA_FOLDER_KONTEN]`
- Folder Proyek Remotion: `C:\Users\webhe\Downloads\MSI\Local Sites\remotion`

Di dalam folder aset tersebut sudah siap:
- Gambar hasil generate (berformat .jpg/.png berurutan nomor: 1_..., 2_..., dst.)
- Video intro AI dari Google Flow (intro.mp4)
- File Voice Over (voice-over.wav, Generated Audio ... .wav atau .mp3)
- JSON panduan remotion (remotion.json - sudah memuat naskah & durasi per slide)

---

### ATURAN & KEBIJAKAN EKSEKUSI (WAJIB DIIKUTI - JANGAN DIABAIKAN):
1. **SEMUA GAMBAR DI FOLDER ASET HARUS MASUK KE DALAM VIDEO (1 Gambar = 1 Scene):**
   - Hitung jumlah file gambar yang ada di folder aset (misal ada N gambar: `1_...` sampai `N_...`).
   - **SEMUA N GAMBAR WAJIB MEMILIKI SCENE TERSENDIRI DAN TERTAMPIL DI VIDEO!**
   - Jika `remotion.json` hanya berisi sedikit scene (misal gambar ada 9 tetapi di `remotion.json` hanya ada 7 scene), **JANGAN HANYA MENELAN JSON MENTAH-MENTAH!**
   - Asisten **WAJIB menyesuaikan array `scenes` di `src/data.ts` menjadi tepat N scene**, memetakan `scene-1.jpg` sampai `scene-N.jpg` secara urut agar tidak ada gambar yang dibuang!

2. **SINKRONISASI DURASI PROPORSIONAL (TANPA PERLU DETEKSI SUARA RUMIT):**
   - Periksa total durasi riil audio voiceover menggunakan `ffprobe`.
   - Total durasi video (`totalDurationInFrames` di `data.ts`) **disesuaikan mengikuti durasi riil audio** (`durasi_detik * 30 FPS` + buffer ~25 frame).
   - Karena `remotion.json` sudah memuat naskah dan durasi per slide, gunakan durasi tersebut langsung. Jika total durasi audio sedikit berbeda dengan total durasi di JSON, skalakan durasi tiap scene secara proporsional:
     `durationInFrames = Math.round((scene.durationInFrames / totalJsonFrames) * totalAudioFrames)`
   - Setiap gambar akan otomatis berganti tepat pada saat naskah kalimat untuk slide tersebut selesai dibacakan!

3. **PEMETAAN GAMBAR URUT & INTRO VIDEO:**
   - Salin dan ubah nama gambar secara berurutan: `1_...` -> `public/img/scene-1.jpg`, `2_...` -> `public/img/scene-2.jpg`, ..., `N_...` -> `public/img/scene-N.jpg`.
   - Konversi `intro.mp4` ke all-intraframe (`-c:v libx264 -pix_fmt yuv420p -g 1 -keyint_min 1 -r 30`) dan simpan ke `public/video/intro-iframe.mp4`.
   - Scene 1 memakai background video AI hook tanpa teks ganda.

4. **DESAIN UI/UX MODERN & SAFE ZONE 9:16:**
   - Komponen Remotion sudah terkonfigurasi dengan standar UI/UX modern (Headline 68px, sticker tag neon kuning untuk kata kunci, IngredientCard 30px, Revenue Pill 32px, dan posisi safe zone `bottom: 340px` agar tidak tertutup caption/tombol TikTok).
   - Pastikan HeaderBar dan CtaCard memuat domain `jajan-cuan.web.app` serta katalog 50+ resep viral.

---

### Instruksi Langkah Demi Langkah untuk Asisten:
1. **Analisis Folder Aset:**
   - Hitung jumlah file gambar ($N$).
   - Cek durasi audio voice-over dengan `ffprobe`.
2. **Proses & Salin Aset ke Remotion:**
   - Konversi `intro.mp4` menjadi all-intraframe ke `public/video/intro-iframe.mp4`.
   - Salin semua $N$ gambar ke `public/img/scene-1.jpg` sampai `public/img/scene-N.jpg`.
   - Salin file voiceover ke `public/audio/tts-full.mp3`.
3. **Penyusunan `src/data.ts` (Dynamic N-Scene Mapping):**
   - Susun array `scenes` berisi tepat $N$ scene (1 per gambar).
   - Terapkan `startFrame` dan `durationInFrames` dari `remotion.json` yang sudah proporsional.
   - Jalankan `npm run lint` untuk verifikasi.
4. **Render Video Final:**
   - Jalankan perintah: `npx remotion render src/index.ts JajanCuan out/[NamaResep].mp4`.
   - Salin hasil video render ke folder aset awal sebagai `[NamaResep]_Final.mp4`.
   - Berikan laporan hasil, konfirmasi bahwa semua $N$ gambar telah masuk, dan tampilkan link file video yang siap ditonton!
```
