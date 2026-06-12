<p align="center">
  <img src="assets/logo.png" alt="CookItUp Logo" width="120" />
</p>

<h1 align="center">🍳 CookItUp</h1>

<p align="center">
  <strong>Masak dari bahan yang kamu punya.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?logo=android" />
  <img src="https://img.shields.io/badge/Bahasa-Java-orange?logo=java" />
  <img src="https://img.shields.io/badge/API-TheMealDB-blue" />
  <img src="https://img.shields.io/badge/Min%20SDK-21-brightgreen" />
</p>

<p align="center">
  <a href="release/CookItUp-v1.0.apk"><strong>📥 Download APK</strong></a>
</p>

---

CookItUp adalah aplikasi Android yang membantu kamu menemukan resep masakan berdasarkan bahan-bahan yang sudah tersedia di dapur. Cukup tambahkan bahan yang ada, dan CookItUp akan merekomendasikan menu yang bisa langsung dimasak — tanpa perlu belanja tambahan.

---

## ✨ Fitur Utama

| Fitur | Deskripsi |
|---|---|
| 🥕 **Pencarian Multi-Bahan** | Tambahkan beberapa bahan sekaligus sebagai chip — aplikasi mencari resep yang cocok dengan SEMUA bahan |
| 📋 **Detail Resep Lengkap** | Lihat instruksi memasak, bahan beserta takarannya, dan foto masakan |
| ❤️ **Favorit + Mode Offline** | Simpan resep favorit ke SQLite — bisa dilihat kapan saja, bahkan tanpa internet |
| 🦴 **Skeleton Loading** | Placeholder shimmer yang halus saat data sedang dimuat (halaman beranda dan detail) |
| 🌗 **Tema Gelap / Terang** | Ganti tema kapan saja — preferensi tersimpan permanen via SharedPreferences |
| 🔄 **Penanganan Error** | Tombol Refresh muncul otomatis saat gagal mengambil data dari API |
| ↩️ **Undo Aksi** | Tidak sengaja menghapus favorit? Tekan UNDO di Snackbar untuk mengembalikannya |
| 📱 **Splash Screen** | Layar pembuka dengan animasi logo dan teks yang menarik |
| 👆 **Navigasi Geser** | Geser antar halaman Beranda dan Favorit, atau gunakan navigation bar di bawah |

---

## 📸 Tangkapan Layar

<!-- Tambahkan screenshot di sini -->
<!-- ![Beranda](screenshots/home.png) -->
<!-- ![Detail](screenshots/detail.png) -->
<!-- ![Favorit](screenshots/favorites.png) -->
<!-- ![Mode Gelap](screenshots/dark_mode.png) -->

---

## 🏗️ Arsitektur Proyek

```
CookItUp/
├── java/com.example.cookitup/
│   ├── ui/
│   │   ├── MainActivity.java          ← Launcher, ViewPager2 + BottomNav + Splash
│   │   ├── DetailActivity.java        ← Detail resep, skeleton loader, fallback offline
│   │   ├── home/
│   │   │   ├── HomeFragment.java      ← Multi-ingredient chips, pencarian API, skeleton
│   │   │   └── HomeViewModel.java     ← Menyimpan state pencarian saat config change
│   │   └── favorite/
│   │       └── FavoriteFragment.java  ← Favorit SQLite, mode offline
│   │
│   ├── adapter/
│   │   ├── MealAdapter.java           ← Adapter RecyclerView hasil pencarian
│   │   └── FavoriteAdapter.java       ← Adapter RecyclerView resep tersimpan
│   │
│   ├── network/
│   │   ├── RetrofitClient.java        ← Singleton Retrofit + GsonConverterFactory
│   │   └── ApiService.java            ← Endpoint API TheMealDB
│   │
│   ├── model/
│   │   ├── Meal.java                  ← Model data list resep
│   │   ├── MealResponse.java          ← Wrapper response API
│   │   ├── MealDetail.java            ← Model detail resep lengkap
│   │   └── MealDetailResponse.java    ← Wrapper response detail
│   │
│   └── database/
│       ├── DatabaseContract.java      ← Konstanta tabel & kolom
│       ├── DatabaseHelper.java        ← SQLiteOpenHelper (create/migrate)
│       ├── MealHelper.java            ← Operasi CRUD (singleton)
│       └── MappingHelper.java         ← Cursor → ArrayList<Meal>
│
└── res/
    ├── layout/                         ← Layout XML semua halaman
    ├── drawable/                       ← Ikon, background, animasi shimmer
    ├── values/                         ← Warna, string (English), tema terang
    └── values-night/                   ← Warna & override tema gelap
```

---

## 🔧 Cara Kerja

### Algoritma Pencarian

Saat pengguna menekan "FIND RECIPES", aplikasi menjalankan request Retrofit `enqueue()` secara paralel ke TheMealDB untuk **setiap** bahan. Hasil dikumpulkan dalam `HashMap` dan hanya resep yang muncul di **SEMUA** respons bahan yang ditampilkan (logika intersection):

```java
// Hanya tampilkan resep yang cocok dengan SEMUA bahan
for (String id : uniqueMeals.keySet()) {
    if (mealOccurrences.get(id) == ingredients.size()) {
        intersectionList.add(uniqueMeals.get(id));
    }
}
```

### Mode Offline

Saat resep di-favorit-kan, seluruh datanya (nama, URL gambar, instruksi, JSON bahan) disimpan ke SQLite. Jika API gagal saat membuka resep favorit, `DetailActivity` otomatis fallback ke database lokal.

### Model Threading

Semua operasi SQLite menggunakan `ExecutorService` + `Handler(Looper.getMainLooper())` agar UI thread tetap responsif. Panggilan API menggunakan `enqueue()` bawaan Retrofit yang berjalan di background thread OkHttp.

---

## 🔌 API

**[TheMealDB](https://www.themealdb.com/api.php)** — Gratis, tidak memerlukan API key.

| Endpoint | Fungsi |
|---|---|
| `GET /filter.php?i={ingredient}` | Mencari resep berdasarkan bahan |
| `GET /lookup.php?i={mealId}` | Mengambil detail resep berdasarkan ID |

---

## ✅ Checklist Spesifikasi Teknis

| No | Spesifikasi | Implementasi | Status |
|---|---|---|---|
| 1 | Minimal 2 Activity | `MainActivity` + `DetailActivity` | ✅ |
| 2 | Launcher Activity | `MainActivity` sebagai launcher | ✅ |
| 3 | Intent dengan data | Mengirim `meal_id` dan `meal_name` ke `DetailActivity` | ✅ |
| 4 | RecyclerView | Beranda (hasil pencarian) + Favorit (data SQLite) | ✅ |
| 5 | Minimal 2 Fragment | `HomeFragment` + `FavoriteFragment` | ✅ |
| 6 | Komponen Navigasi | `BottomNavigationView` + `ViewPager2` dengan geser | ✅ |
| 7 | Background Thread | `ExecutorService` untuk semua operasi SQLite | ✅ |
| 8 | Handler | `Handler(Looper.getMainLooper())` untuk update UI | ✅ |
| 9 | Retrofit | `RetrofitClient` + `GsonConverterFactory` | ✅ |
| 10 | Data API ditampilkan | Hasil pencarian di `RecyclerView` via `MealAdapter` | ✅ |
| 11 | Tombol Refresh | Muncul saat callback `onFailure()` | ✅ |
| 12 | SQLite | Favorit disimpan dengan data bahan lengkap | ✅ |
| 13 | Tampil saat offline | Halaman favorit + detail berfungsi tanpa internet | ✅ |
| 14 | Tema Gelap / Terang | `AppCompatDelegate.setDefaultNightMode()` | ✅ |
| 15 | SharedPreferences | Menyimpan preferensi tema (key `dark_mode`) | ✅ |

---

## 🛠️ Tech Stack

| Komponen | Teknologi |
|---|---|
| Bahasa | Java |
| UI | XML Layout + Material Design 3 |
| Networking | Retrofit 2.9.0 + Gson Converter |
| HTTP Client | OkHttp 4.9.1 |
| Image Loading | Picasso 2.71828 |
| Database Lokal | SQLite (android.database.sqlite) |
| State Management | ViewModel + LiveData |
| Preferensi | SharedPreferences |
| Navigasi | ViewPager2 + BottomNavigationView |
| Threading | ExecutorService + Handler |

---

## 🚀 Cara Menjalankan

### Prasyarat
- Android Studio (versi terbaru)
- Android SDK API 21+ (Android 5.0 Lollipop)
- Koneksi internet (untuk fitur pencarian resep)

### Menjalankan dari Source Code

```bash
# Clone repository
git clone https://github.com/username/CookItUp.git

# Buka di Android Studio, tunggu Gradle sync, lalu:
./gradlew installDebug
```

### Instalasi via APK

1. Download file [CookItUp-v1.0.apk](release/CookItUp-v1.0.apk)
2. Aktifkan "Instal dari Sumber Tidak Dikenal" di pengaturan perangkat
3. Install file APK

### Dependencies

Tambahkan di `build.gradle.kts` (Module :app):

```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.71828")

    // Material Design
    implementation("com.google.android.material:material:1.9.0")
}
```

---

## 👤 Pengembang

**Akram Alfadli Tamir**

| | |
|---|---|
| NIM | H071241076 |
| Program Studi | Sistem Informasi |
| Universitas | Universitas Hasanuddin |

---

*Dibuat dengan ☕ dan banyak bahan masakan.*