# PROGRESS.md — Status Implementasi Jajan Cuan Android

## Checklist Tahap
- [x] S0 Pre-flight & dokumentasi
- [ ] S1 Pipeline web (kode)
- [ ] S2 Deploy production
- [ ] S3 Kerangka proyek Android
- [ ] S4 Instrumentasi (Firebase, RC, UMP, AdManager)
- [ ] S5 Data layer
- [ ] S6 Home, Pencarian, Favorit
- [ ] S7 Detail & Kalkulator
- [ ] S8 Timer
- [ ] S9 Rewarded & Promo
- [ ] S10 Settings & Aset
- [ ] S11 QA & hardening

---

## Keputusan & asumsi
- Home Feed Shuffling (Fresh Experience): Resep di Home tab 'Semua' diacak menggunakan session seed yang diperbarui saat cold start aplikasi atau pull-to-refresh. Hal ini memastikan user selalu melihat susunan resep yang segar/baru tiap membuka aplikasi, namun urutan tetap konsisten dan bebas duplikasi selama sesi scrolling berlangsung.

---

## Checklist manual
