# Firebase Setup — Jajan Cuan

## Project Info
- Project ID: `jajan-cuan`
- RTDB Instance: `jajan-cuan-default-rtdb`
- Database URL: `https://jajan-cuan-default-rtdb.firebaseio.com`
- Default Domain: `https://jajan-cuan.web.app`
- Status: Test Mode (sebelum go-live, terapkan rules dari `database.rules.json`)

## Cara Deploy
1. Install Firebase CLI (jika belum ada):
   ```bash
   npm install -g firebase-tools
   ```
2. Login ke Google Account:
   ```bash
   firebase login
   ```
3. Deploy Hosting:
   ```bash
   firebase deploy --only hosting
   ```
4. Deploy Realtime Database Rules:
   ```bash
   firebase deploy --only database
   ```

## Cara Seed Data
1. Buka file `scripts/seed-database.html` di browser lokal.
2. Klik tombol **"Push Seed Data ke RTDB"**.
3. Cek Firebase Console → Realtime Database → node `/posts`.
