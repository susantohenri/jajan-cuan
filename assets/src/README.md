# Sumber Aset Visual Jajan Cuan

File di folder ini adalah sumber vektor untuk aset toko:

- `icon.svg` — ikon 512×512: latar gradien hangat, kertas cone, tiga jajanan, koin cuan, dan kilau.
- `feature-graphic.svg` — feature graphic 1024×500 dengan wordmark dan tagline.

Palet mengikuti brand hangat Jajan Cuan: `#FFC107`, `#FF8A00`, `#E64A19`, dengan aksen krem `#FFF8E1` dan `#FFF3E0`. Mark dibuat khusus untuk Jajan Cuan; tidak ada gambar, logo, atau merek pihak lain.

Teks pada `feature-graphic.svg` ("Jajan Cuan", tagline, dan badge) sudah dikonversi menjadi path/outline, sehingga SVG tidak bergantung pada font saat dirender. Huruf dibuat dengan **Baloo 2** (SIL Open Font License) hanya pada saat pembuatan outline; tidak ada file font, gambar, atau aset berlisensi lain yang disertakan di repositori.

Hasil render:

- `assets/icon-512.png` — 512×512, PNG RGBA (32-bit), alpha 100% opak.
- `assets/feature-graphic-1024x500.png` — 1024×500, PNG RGB tanpa alpha.

Render memakai `sharp` (sudah tersedia di `node_modules`). Contoh perintah:

```js
const sharp = require('sharp');
sharp('assets/src/icon.svg').resize(512, 512).ensureAlpha()
  .png().toFile('assets/icon-512.png');
sharp('assets/src/feature-graphic.svg').resize(1024, 500)
  .flatten({ background: '#E64A19' }).removeAlpha()
  .png().toFile('assets/feature-graphic-1024x500.png');
```
