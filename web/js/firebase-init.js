// Firebase SDK Initialization untuk Jajan Cuan Website
// Menggunakan Firebase JS SDK via CDN (compat mode untuk kesederhanaan)

const firebaseConfig = {
  apiKey: "AIzaSyDcNmkB3OC1xFqicMF3Mn0M__chbVAVDBk",
  authDomain: "jajan-cuan.firebaseapp.com",
  databaseURL: "https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "jajan-cuan",
  storageBucket: "jajan-cuan.firebasestorage.app",
  messagingSenderId: "1032979243442",
  appId: "1:1032979243442:web:67e7769d199f00a714c4ab",
  measurementId: "G-SKCSWCG5W1"
};

// Initialize Firebase
try {
  if (!firebase.apps || !firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
  }
} catch (e) {
  console.warn('Firebase init error:', e);
}

// Analytics dengan safe check (tidak crash jika offline / file:// / adblocker)
try {
  if (typeof firebase.analytics === 'function') {
    firebase.analytics();
  }
} catch (e) {
  console.warn('Firebase Analytics not supported in this environment:', e);
}

// Referensi global database
let db;
try {
  db = firebase.database();
} catch (e) {
  console.warn('Firebase Database init error:', e);
}
