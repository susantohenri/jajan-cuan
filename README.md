
# Catatan tambahan: landing page diindex, konten experience tidak diindex

## Tujuan keputusan
- Landing page: wajib bisa diindex oleh Google, karena fokusnya adalah validasi minat dan awareness.
- Halaman konten: dipakai untuk memberi pengalaman seperti aplikasi yang sedang dikembangkan, tapi tidak wajib diindex oleh mesin pencari.
- Dengan kata lain: halaman utama / landing page adalah media promosi, sementara konten di dalam website adalah experience layer yang memberi rasa produk yang lengkap.

## Arsitektur yang disarankan

### 1) Landing page statis (diindex)
- Gunakan halaman HTML statis yang memiliki meta tag SEO lengkap:
  - title
  - meta description
  - canonical
  - og:title
  - og:description
  - og:image
  - twitter:card
- Fokus landing page: menjelaskan manfaat, fitur utama, dan konteks aplikasi "Jajan Cuan".
- Gunakan GitHub Pages untuk hosting karena sesuai kebutuhan saat ini.

### 2) Halaman konten experience (tidak diindex)
- Konten di halaman content bisa diambil dari Firebase Firestore, misalnya collection `posts` atau `articles`.
- Tujuan bukan SEO, melainkan membuat website terasa nyata dan penuh isi seperti aplikasi yang akan datang.
- Untuk mencegah diindex, maka:
  - jangan sertakan halaman tersebut di sitemap.xml
  - bisa tambahkan meta noindex pada halaman tersebut
  - jika menggunakan routing SPA/JS, pastikan halaman tersebut tidak dibuka sebagai URL yang di-crawl secara berlebihan

### 3) SEO yang relevan
- Sitemap hanya mencantumkan landing page / halaman utama.
- robots.txt hanya mengizinkan halaman yang memang ingin diindex.
- Ini cukup untuk kebutuhan validasi awal: landing page maksudnya agar orang menemukan brand, baca value prop, lalu bisa melihat contoh content yang akan ada di app nanti.

## Skema data contoh (Firebase Firestore)

Collection: `posts`

```json
{
  "title": "Kue Bolu Keju",
  "slug": "kue-bolu-keju",
  "excerpt": "Resep cepat dan layak jual",
  "content": "<p>Isi konten yang ditampilkan ke user</p>",
  "published": true,
  "published_at": "2026-10-01T00:00:00Z",
  "tags": ["kue", "viral"],
  "og_image": "https://example.com/og.jpg",
  "last_updated": "2026-10-10T12:00:00Z"
}
```

### Tujuan data ini
- Menyimpan konten yang akan dikonsumsi user di website.
- Menyediakan referensi bahwa aplikasi nanti akan punya katalog yang lengkap.
- Tidak perlu content page diindex.

## Contoh policy SEO yang tepat untuk kebutuhan ini

### sitemap.xml
- Hanya memuat URL landing page utama, misalnya:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>https://your-domain.com/</loc>
    <lastmod>2026-10-17</lastmod>
    <priority>1.0</priority>
  </url>
</urlset>
```

### robots.txt

```txt
User-agent: *
Allow: /
Sitemap: https://your-domain.com/sitemap.xml
```

### Meta noindex (opsional, untuk halaman konten)

```html
<meta name="robots" content="noindex, nofollow" />
```

## Hosting & domain
- GitHub Pages dipakai untuk landing page.
- Domain nanti dibeli dan diarahkan ke GitHub Pages.
- Ini menyesuaikan kebutuhan validasi: landing page cepat, murah, dan dapat diukur dengan GA4.

## GA4
- Pasang di landing page untuk mengukur siapa yang datang, dari mana, dan seberapa lama mereka bertahan.
- Konten experience bisa tetap dipantau kalau perlu, tapi fokus pengukuran tetap di landing page karena itu yang akan diindex dan menjadi lead generation layer.

## Kesimpulan
- Jangan over-engineer dulu.
- Yang penting adalah landing page jelas, estetis, dan cepat dimengerti.
- Konten detail hanya sebagai proof-of-experience agar website terasa lengkap sebelum aplikasi benar-benar dibuat.
- SEO diprioritaskan hanya untuk landing page, bukan untuk seluruh konten.

-----

Catatan: bagian ini sengaja dibuat sebagai tambahan dokumentasi untuk kebutuhan validasi website. Ini bukan pengganti roadmap aplikasi, melainkan penjelasan strategis agar keputusan teknis lebih konsisten dengan targetmu.
