# 🍳 CookItUp — Dokumentasi Lengkap Aplikasi

> **Tagline:** *"Got ingredients? Let's cook!"*

CookItUp adalah aplikasi Android yang membantu pengguna menemukan resep masakan berdasarkan bahan-bahan yang sudah tersedia di dapur atau kulkas mereka. Cukup input berbagai bahan yang ada, dan CookItUp akan merekomendasikan resep yang bisa langsung dibuat — tanpa perlu beli bahan tambahan.

---

## 📋 Daftar Isi

1. [Deskripsi Aplikasi](#1-deskripsi-aplikasi)
2. [Tujuan & Target Pengguna](#2-tujuan--target-pengguna)
3. [Fitur Utama](#3-fitur-utama)
4. [API yang Digunakan](#4-api-yang-digunakan)
5. [Arsitektur & Struktur Teknis](#5-arsitektur--struktur-teknis)
6. [Penjelasan Implementasi Teknis](#6-penjelasan-implementasi-teknis)
7. [Struktur Folder Project](#7-struktur-folder-project)
8. [Alur Penggunaan (User Flow)](#8-alur-penggunaan-user-flow)
9. [Kriteria Penilaian & Pemenuhannya](#9-kriteria-penilaian--pemenuhannya)
10. [Tech Stack](#10-tech-stack)
11. [Cara Menjalankan Aplikasi](#11-cara-menjalankan-aplikasi)
12. [Dependencies](#12-dependencies)

---

## 1. Deskripsi Aplikasi

**CookItUp** adalah aplikasi Android bertema **Food & Drink** yang dibuat sebagai tugas final mata kuliah Laboratorium Mobile 2026. Aplikasi ini memungkinkan pengguna untuk:

- Memasukkan banyak bahan makanan (Multi-Ingredient) yang tersedia di dapur/kulkas, yang akan ditampung sebagai komponen Chip.
- Mendapatkan rekomendasi resep masakan gabungan berdasarkan bahan-bahan tersebut.
- Melihat detail resep lengkap beserta foto, bahan, dan langkah memasak.
- Menyimpan resep favorit secara lokal agar bisa diakses tanpa koneksi internet.
- Menikmati antarmuka bertema hijau elegan (Primary: #3B6D11) yang sepenuhnya dilokalisasi ke dalam Bahasa Indonesia.
- Menggunakan tampilan mode gelap (Dark Mode) atau mode terang yang dapat diakses langsung melalui Custom TopBar di HomeFragment.

---

## 2. Tujuan & Target Pengguna

**Problem yang diselesaikan:**
- Mengurangi kebingungan memilih menu harian
- Mengurangi food waste dengan memanfaatkan bahan yang sudah ada
- Menghemat waktu dan biaya belanja bahan makanan

**Target pengguna:**
- Mahasiswa dan anak kost
- Ibu rumah tangga
- Siapa saja yang ingin memasak dari bahan yang tersedia

---

## 3. Fitur Utama

| Fitur | Deskripsi |
|---|---|
| 🥕 Multi-Ingredient Search | User dapat menginput banyak bahan sekaligus. Setiap bahan ditampung ke dalam komponen `Chip` di dalam `ChipGroup`. |
| 🔍 Cari Resep Gabungan | Sistem melakukan *fetch* secara paralel/berurutan ke API TheMealDB untuk setiap bahan, lalu menggabungkan hasilnya tanpa duplikasi. |
| 📋 Detail Resep | Menampilkan bahan lengkap, langkah memasak, dan foto masakan. |
| ❤️ Simpan Favorit | Menyimpan resep favorit ke database lokal SQLite. |
| 📴 Mode Offline | Resep favorit tetap dapat diakses saat tidak ada koneksi internet. |
| 🔄 Tombol Refresh | Muncul otomatis saat gagal mengambil data dari API. |
| 🌙 Dark / Light Theme | Toggle tema disimpan permanen via SharedPreferences dan telah dipindahkan ke dalam Custom TopBar di HomeFragment. |
| 🇮🇩 Lokalisasi Penuh | Seluruh antarmuka dan teks dalam aplikasi sekarang sepenuhnya dilokalisasi ke Bahasa Indonesia. |

---

## 4. API yang Digunakan

**TheMealDB API** — `https://www.themealdb.com/api/json/v1/1/`

> API ini **gratis**, **tidak membutuhkan API key**, dan memiliki dokumentasi yang lengkap.

| Endpoint | Method | Fungsi |
|---|---|---|
| `/filter.php?i={ingredient}` | GET | Mencari resep berdasarkan bahan (dipanggil berulang untuk setiap Chip) |
| `/lookup.php?i={mealId}` | GET | Mengambil detail resep berdasarkan ID |

---

## 5. Arsitektur & Struktur Teknis

Aplikasi ini mengusung desain UI baru dengan tema warna **Primary Green (#3B6D11)**. Seluruh komponen teks UI dilokalisasi ke dalam **Bahasa Indonesia**.

### Activity

| Activity | Launcher | Fungsi |
|---|---|---|
| `MainActivity` | ✅ Ya | Halaman utama dengan BottomNavigationView dan FrameLayout |
| `DetailActivity` | ❌ Tidak | Menampilkan detail resep lengkap (bahan + cara masak + foto) |

### Fragment

| Fragment | Fungsi |
|---|---|
| `HomeFragment` | Custom TopBar, input Multi-Ingredient (Chips), tombol cari, dan menampilkan hasil resep gabungan via RecyclerView |
| `FavoriteFragment` | Menampilkan daftar resep yang disimpan secara lokal dari SQLite |

### Navigasi

```
MainActivity
├── BottomNavigationView
│   ├── nav_home     → HomeFragment ("Cari Resep")
│   └── nav_favorite → FavoriteFragment ("Favorit")
└── FrameLayout (fragment_container)

Intent: MainActivity → DetailActivity (membawa mealId)
Bundle: HomeFragment/FavoriteFragment → MainActivity → DetailActivity
```

### RecyclerView (MaterialCardView)

Desain resep (Item resep) pada *list* diperbarui menggunakan `MaterialCardView` bersudut *rounded* 12dp.

| Lokasi | Data yang Ditampilkan | Layout Manager |
|---|---|---|
| `HomeFragment` | Hasil pencarian gabungan dari API | LinearLayoutManager |
| `FavoriteFragment` | Daftar resep favorit dari SQLite | LinearLayoutManager |

---

## 6. Penjelasan Implementasi Teknis

### 6.1 Networking & Logika Multi-Ingredient (Retrofit)

Sesuai materi Bab 6, Retrofit digunakan untuk mengambil data resep dari TheMealDB API. 
Pada pembaruan terbaru, pencarian resep sekarang mendukung banyak bahan sekaligus (**Multi-Ingredient Search**). Ketika user mengklik "Cari Resep", aplikasi akan melakukan *fetch* API TheMealDB (`/filter.php?i=`) untuk **setiap chip bahan** secara berurutan/paralel.

Untuk mencegah duplikasi resep yang muncul di lebih dari satu bahan, hasil respons dari berbagai bahan digabungkan ke dalam koleksi `LinkedHashMap<String, Meal>` (menggunakan `idMeal` sebagai key yang unik).

**Penerapan Fetch Gabungan:**
```java
HashMap<String, Meal> uniqueMeals = new HashMap<>();
// Iterasi setiap bahan yang ada di dalam ArrayList (dari ChipGroup)
for (String ingredient : ingredients) {
    Call<MealResponse> call = apiService.getMealsByIngredient(ingredient);
    call.enqueue(new Callback<MealResponse>() {
        @Override
        public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
            // Jika sukses, masukkan ke uniqueMeals menggunakan idMeal sebagai key
            uniqueMeals.put(meal.getIdMeal(), meal);
            // Lakukan pengecekan apakah semua fetch bahan sudah selesai
        }
        // ...
    });
}
```

### 6.2 Fragment & BottomNavigationView

Sesuai materi Bab 4, navigasi antar Fragment menggunakan `BottomNavigationView` dan `FrameLayout`. Data dikirim antar Fragment menggunakan `Bundle`.

### 6.3 Background Thread — Executor & Handler

Sesuai materi Bab 5, semua operasi berat (seperti query SQLite) dijalankan di background thread menggunakan `Executor`, kemudian hasilnya dikembalikan ke Main Thread via `Handler` untuk update UI.

**Pola yang digunakan (dari modul SQLite):**
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Handler handler = new Handler(Looper.getMainLooper());

executor.execute(() -> {
    // Berjalan di background thread (Contoh: load favorit)
    MealHelper mealHelper = MealHelper.getInstance(getContext());
    mealHelper.open();
    ArrayList<Meal> favorites = MappingHelper.mapCursorToArrayList(mealHelper.queryAll());

    handler.post(() -> {
        // Kembali ke Main Thread untuk update UI
        adapter.setData(favorites);
    });
});
```

### 6.4 SQLite — Penyimpanan Resep Favorit (Local Data Persistent)

Sesuai materi Bab 8, SQLite digunakan untuk menyimpan resep favorit secara lokal agar dapat diakses tanpa koneksi internet.

| File | Fungsi |
|---|---|
| `DatabaseContract.java` | Mendefinisikan nama tabel dan kolom |
| `DatabaseHelper.java` | Membuat dan upgrade tabel SQLite |
| `MealHelper.java` | getInstance, open, close, insert, queryAll, deleteById |
| `MappingHelper.java` | Mengkonversi Cursor menjadi ArrayList\<Meal\> |

### 6.5 SharedPreferences — Tema & Riwayat Bahan

Sesuai materi Bab 7, SharedPreferences digunakan untuk menyimpan preferensi pengguna:
1. **Preferensi Tema (Dark/Light Mode)**: Disetel melalui Switch berbasis SharedPreferences yang telah dipindahkan ke dalam Custom TopBar di `HomeFragment`.
2. **Riwayat Bahan**: Menyimpan bahan terakhir yang diinput otomatis.

---

## 7. Struktur Folder Project

```
CookItUp/
├── manifests/
│   └── AndroidManifest.xml           ← Permission INTERNET
│
├── java/com.example.cookitup/
│   ├── ui/
│   │   ├── MainActivity.java          ← Launcher, BottomNav
│   │   ├── DetailActivity.java        ← Detail resep + favorit
│   │   ├── home/
│   │   │   └── HomeFragment.java      ← Multi-Ingredient Chips, fetch gabungan
│   │   └── favorite/
│   │       └── FavoriteFragment.java  ← Load SQLite, offline mode
│   │
│   ├── adapter/
│   │   ├── MealAdapter.java           ← Adapter list resep
│   │   └── FavoriteAdapter.java       ← Adapter data SQLite
│   │
│   ├── network/
│   │   ├── RetrofitClient.java        ← Konfigurasi Retrofit + BASE_URL
│   │   └── ApiService.java            ← Interface endpoint TheMealDB
│   │
│   ├── model/
│   │   ├── Meal.java                  ← Model data list resep
│   │   ├── MealResponse.java          ← Wrapper response list
│   │   ├── MealDetail.java            ← Model data detail resep
│   │   └── MealDetailResponse.java    ← Wrapper response detail
│   │
│   └── database/
│       ├── DatabaseContract.java      ← Konstanta nama tabel & kolom
│       ├── DatabaseHelper.java        ← Create/upgrade tabel SQLite
│       ├── MealHelper.java            ← CRUD operations (singleton)
│       └── MappingHelper.java         ← Cursor → ArrayList<Meal>
│
└── res/
    ├── layout/
    │   ├── activity_main.xml          ← FrameLayout + BottomNavigationView
    │   ├── activity_detail.xml        ← Detail resep UI
    │   ├── fragment_home.xml          ← Custom TopBar, Multi-Ingredient + Chips
    │   ├── fragment_favorite.xml      ← RecyclerView favorit
    │   └── item_meal.xml              ← MaterialCardView 12dp rounded per item
    ├── menu/
    │   └── bottom_nav_menu.xml        ← Item menu bottom navigation
    └── values/
        ├── colors.xml                 ← Primary Green Theme (#3B6D11)
        ├── strings.xml                ← Terjemahan Bahasa Indonesia
        ├── themes.xml                 ← Light theme
        └── themes.xml (night)         ← Dark theme
```

---

## 8. Alur Penggunaan (User Flow)

```
┌─────────────────────────────────────────────────────────┐
│                    BUKA APLIKASI                        │
│         SharedPreferences load tema & bahan terakhir    │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                   HomeFragment                          │
│  1. User input bahan dan klik tombol '+'                │
│  2. Bahan ditambahkan sebagai Chip ke dalam ChipGroup   │
│  3. Klik tombol "Cari Resep"                            │
│  4. Retrofit fetch API TheMealDB paralel tiap chip      │
│  5. Hasil digabungkan di LinkedHashMap (tanpa duplikat) │
│  6. Ditampilkan di RecyclerView (CardView 12dp rounded) │
│  7. Jika gagal → tampilkan tombol Refresh               │
└────────────────────────┬────────────────────────────────┘
                         │ klik item resep
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  DetailActivity                         │
│  1. Retrofit fetch detail resep by ID                   │
│  2. Tampilkan foto, bahan, langkah memasak              │
│  3. Klik "Simpan ke Favorit" → Executor ke SQLite       │
│  4. Toast konfirmasi Bahasa Indonesia                   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                  FavoriteFragment                       │
│  1. Executor load data dari SQLite (background thread)  │
│  2. Handler update RecyclerView (main thread)           │
│  3. Klik item → Intent ke DetailActivity                │
│  4. Bisa diakses tanpa koneksi internet (offline)       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│               Settings (di HomeFragment)                │
│  1. Switch dark/light mode di Custom TopBar             │
│  2. Status disimpan ke SharedPreferences                │
│  3. Tema langsung berubah via AppCompatDelegate         │
└─────────────────────────────────────────────────────────┘
```

---

## 9. Kriteria Penilaian & Pemenuhannya

| Kriteria | Bobot | Pemenuhan |
|---|---|---|
| **Fungsi & Kegunaan** | 40% | Multi-ingredient search dengan Chip, mode offline via SQLite, tombol refresh saat gagal koneksi |
| **Kreativitas & Inovasi** | 10% | Solusi masak dari bahan yang ada, penggabungan hasil array API tanpa duplikat (HashMap). |
| **User Interface** | 20% | Tema warna hijau estetik, MaterialCardView 12dp, lokalisasi Bahasa Indonesia, Custom TopBar Material Design. |
| **Stabilitas & Performa** | 20% | Semua operasi berat di background thread (Executor), error handling lengkap, fetch paralel asinkron yang terkelola dengan baik. |
| **Dokumentasi** | 10% | README.md lengkap di GitHub mencakup seluruh fitur baru dan spesifikasi teknis arsitektur. |

### Checklist Spesifikasi Teknis

| No | Spesifikasi | Implementasi di CookItUp | Status |
|---|---|---|---|
| 1 | Minimal 2 Activity | MainActivity + DetailActivity | ✅ |
| 2 | Launcher Activity | MainActivity sebagai Launcher | ✅ |
| 3 | Intent | MainActivity → DetailActivity (membawa mealId) | ✅ |
| 4 | RecyclerView | HomeFragment (hasil API) + FavoriteFragment (SQLite) | ✅ |
| 5 | Minimal 2 Fragment | HomeFragment + FavoriteFragment | ✅ |
| 6 | Navigation Component | BottomNavigationView + loadFragment() | ✅ |
| 7 | Background Thread (Executor) | Semua operasi SQLite pakai Executor | ✅ |
| 8 | Background Thread (Handler) | Handler.post() untuk update UI dari background | ✅ |
| 9 | Retrofit | Fetch data dari TheMealDB API | ✅ |
| 10 | Data dari API ditampilkan | RecyclerView menampilkan hasil dari API | ✅ |
| 11 | Tombol Refresh | Muncul saat onFailure() dari Retrofit | ✅ |
| 12 | SQLite | Menyimpan resep favorit secara lokal | ✅ |
| 13 | Data tampil saat offline | FavoriteFragment load dari SQLite tanpa internet | ✅ |
| 14 | Dark / Light Theme | Toggle via SharedPreferences + AppCompatDelegate | ✅ |
| 15 | SharedPreferences | Simpan tema + riwayat bahan terakhir | ✅ |

---

## 10. Tech Stack

| Komponen | Teknologi | Versi |
|---|---|---|
| Language | Java | - |
| UI | XML Layout + Material Design (Chip, CardView) | - |
| Networking | Retrofit2 + Gson Converter | 2.9.0 |
| HTTP Client | OkHttp3 | 4.9.1 |
| Image Loading | Picasso | 2.71828 |
| Database Lokal | SQLite (android.database.sqlite) | Built-in |
| Preferences | SharedPreferences | Built-in |
| Navigation | BottomNavigationView + FrameLayout | Material |
| Background | ExecutorService + Handler | Built-in |
| Version Control | GitHub | - |

---

## 11. Cara Menjalankan Aplikasi

### Prasyarat
- Android Studio (versi terbaru)
- Android SDK minimal API 21 (Android 5.0)
- Koneksi internet (untuk fitur pencarian resep)

### Langkah-langkah

1. Clone repository ini:
   ```bash
   git clone https://github.com/username/CookItUp.git
   ```

2. Buka project di Android Studio

3. Tunggu Gradle sync selesai

4. Jalankan di emulator atau perangkat fisik:
    - Klik tombol **Run** ▶️ di Android Studio, atau
    - Jalankan via terminal: `./gradlew installDebug`

5. Pastikan perangkat terhubung ke internet untuk fitur pencarian resep

### Instalasi via APK
- Download file APK dari bagian [Releases](../../releases)
- Aktifkan "Install from Unknown Sources" di pengaturan perangkat
- Install file APK

---

## 12. Dependencies

Tambahkan di `build.gradle.kts` (Module :app):

```kotlin
dependencies {
    // Retrofit untuk Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp untuk HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // Picasso untuk loading gambar dari URL
    implementation("com.squareup.picasso:picasso:2.71828")

    // Material Design (BottomNavigationView, MaterialCardView, Chip, dll)
    implementation("com.google.android.material:material:1.9.0")
}
```

Tambahkan permission di `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 👤 Informasi Pengembang

| |                          |
|---|--------------------------|
| **Nama** | Akram Alfadli Tamir      |
| **NIM** | H071241076               |
| **Program Studi** | Sistem Informasi         |
| **Universitas** | Universitas Hasanuddin   |
| **Mata Kuliah** | Laboratorium Mobile 2026 |
| **Tema** | Food & Drink             |

---

*Dibuat sebagai Tugas Final Laboratorium Mobile 2026 — Universitas Hasanuddin*