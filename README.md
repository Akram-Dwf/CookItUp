# 🍳 CookItUp — Dokumentasi Lengkap Aplikasi

> **Tagline:** *"Got ingredients? Let's cook!"*

CookItUp adalah aplikasi Android yang membantu pengguna menemukan resep masakan berdasarkan bahan-bahan yang sudah tersedia di dapur atau kulkas mereka. Cukup input bahan yang ada, dan CookItUp akan merekomendasikan resep yang bisa langsung dibuat — tanpa perlu beli bahan tambahan.

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

- Memasukkan bahan-bahan makanan yang tersedia di dapur/kulkas
- Mendapatkan rekomendasi resep masakan berdasarkan bahan tersebut
- Melihat detail resep lengkap beserta foto, bahan, dan langkah memasak
- Menyimpan resep favorit secara lokal agar bisa diakses tanpa koneksi internet
- Menggunakan tampilan dark mode atau light mode sesuai preferensi

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
| 🥕 Input Bahan | User menambahkan bahan yang tersedia di dapur/kulkas |
| 🔍 Cari Resep | Sistem mencari resep berdasarkan bahan via API TheMealDB |
| 📋 Detail Resep | Menampilkan bahan lengkap, langkah memasak, dan foto masakan |
| ❤️ Simpan Favorit | Menyimpan resep favorit ke database lokal SQLite |
| 📴 Mode Offline | Resep favorit tetap dapat diakses saat tidak ada koneksi internet |
| 🔄 Tombol Refresh | Muncul otomatis saat gagal mengambil data dari API |
| 🌙 Dark / Light Theme | Toggle tema disimpan permanen via SharedPreferences |
| 🕓 Riwayat Bahan | Bahan terakhir yang diinput tersimpan otomatis via SharedPreferences |

---

## 4. API yang Digunakan

**TheMealDB API** — `https://www.themealdb.com/api/json/v1/1/`

> API ini **gratis**, **tidak membutuhkan API key**, dan memiliki dokumentasi yang lengkap.

| Endpoint | Method | Fungsi |
|---|---|---|
| `/filter.php?i={ingredient}` | GET | Mencari resep berdasarkan satu bahan |
| `/lookup.php?i={mealId}` | GET | Mengambil detail resep berdasarkan ID |
| `/random.php` | GET | Mengambil resep secara acak (fitur bonus) |

**Contoh Response `/filter.php?i=chicken`:**
```json
{
  "meals": [
    {
      "strMeal": "Chicken Handi",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/...",
      "idMeal": "52795"
    }
  ]
}
```

**Contoh Response `/lookup.php?i=52795`:**
```json
{
  "meals": [
    {
      "idMeal": "52795",
      "strMeal": "Chicken Handi",
      "strInstructions": "Heat oil in a pan...",
      "strMealThumb": "https://...",
      "strIngredient1": "Chicken",
      "strMeasure1": "1kg"
    }
  ]
}
```

---

## 5. Arsitektur & Struktur Teknis

### Activity

| Activity | Launcher | Fungsi |
|---|---|---|
| `MainActivity` | ✅ Ya | Halaman utama dengan BottomNavigationView dan FrameLayout |
| `DetailActivity` | ❌ Tidak | Menampilkan detail resep lengkap (bahan + cara masak + foto) |

### Fragment

| Fragment | Fungsi |
|---|---|
| `HomeFragment` | Input bahan, tombol cari, dan menampilkan hasil resep via RecyclerView |
| `FavoriteFragment` | Menampilkan daftar resep yang disimpan secara lokal dari SQLite |

### Navigasi

```
MainActivity
├── BottomNavigationView
│   ├── nav_home     → HomeFragment
│   └── nav_favorite → FavoriteFragment
└── FrameLayout (fragment_container)

Intent: MainActivity → DetailActivity (membawa mealId)
Bundle: HomeFragment/FavoriteFragment → MainActivity → DetailActivity
```

### RecyclerView

| Lokasi | Data yang Ditampilkan | Layout Manager |
|---|---|---|
| `HomeFragment` | Hasil pencarian resep dari API | LinearLayoutManager |
| `FavoriteFragment` | Daftar resep favorit dari SQLite | LinearLayoutManager |

---

## 6. Penjelasan Implementasi Teknis

### 6.1 Activity & Intent

Aplikasi memiliki dua Activity:

- **MainActivity** sebagai Launcher yang memuat BottomNavigationView dan Fragment
- **DetailActivity** yang menerima data `mealId` dari Intent untuk menampilkan detail resep

```java
// Pindah dari HomeFragment ke DetailActivity via MainActivity
Intent intent = new Intent(getActivity(), DetailActivity.class);
intent.putExtra("meal_id", meal.getIdMeal());
intent.putExtra("meal_name", meal.getStrMeal());
startActivity(intent);
```

---

### 6.2 Fragment & BottomNavigationView

Sesuai materi Bab 4, navigasi antar Fragment menggunakan `BottomNavigationView` dan `FrameLayout` di `activity_main.xml`. Data dikirim antar Fragment menggunakan `Bundle`.

**activity_main.xml:**
```xml
<LinearLayout ...>
    <FrameLayout
        android:id="@+id/fragment_container"
        android:layout_weight="1"
        android:layout_width="match_parent"
        android:layout_height="0dp"/>

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:menu="@menu/bottom_nav_menu"/>
</LinearLayout>
```

**MainActivity.java — logika navigasi:**
```java
bottomNav.setOnItemSelectedListener(item -> {
    Fragment selectedFragment = null;
    int id = item.getItemId();
    if (id == R.id.nav_home) {
        selectedFragment = new HomeFragment();
    } else if (id == R.id.nav_favorite) {
        selectedFragment = new FavoriteFragment();
    }
    return loadFragment(selectedFragment);
});
```

---

### 6.3 RecyclerView

Sesuai materi Bab 3, RecyclerView digunakan di dua tempat:

**Komponen yang dibuat:**

| File | Fungsi |
|---|---|
| `MealAdapter.java` | Adapter untuk hasil pencarian dari API |
| `FavoriteAdapter.java` | Adapter untuk data favorit dari SQLite |
| `item_meal.xml` | Layout item untuk setiap resep di list |

**Struktur MealAdapter:**
```java
public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    private ArrayList<Meal> meals;

    public MealAdapter(ArrayList<Meal> meals) {
        this.meals = meals;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.tvName.setText(meal.getStrMeal());
        Picasso.get().load(meal.getStrMealThumb()).into(holder.ivThumbnail);
    }

    @Override
    public int getItemCount() { return meals.size(); }
}
```

---

### 6.4 Background Thread — Executor & Handler

Sesuai materi Bab 5, semua operasi berat (query SQLite, fetch API) dijalankan di background thread menggunakan `Executor`, kemudian hasilnya dikembalikan ke Main Thread via `Handler` untuk update UI.

**Pola yang digunakan (dari modul SQLite):**
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Handler handler = new Handler(Looper.getMainLooper());

executor.execute(() -> {
    // Berjalan di background thread
    MealHelper mealHelper = MealHelper.getInstance(getContext());
    mealHelper.open();
    ArrayList<Meal> favorites = MappingHelper.mapCursorToArrayList(
        mealHelper.queryAll()
    );

    handler.post(() -> {
        // Kembali ke Main Thread untuk update UI
        adapter.setData(favorites);
        if (favorites.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    });
});
```

---

### 6.5 Networking — Retrofit

Sesuai materi Bab 6, Retrofit digunakan untuk mengambil data resep dari TheMealDB API.

**RetrofitClient.java:**
```java
public class RetrofitClient {
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}
```

**ApiService.java:**
```java
public interface ApiService {
    @GET("filter.php")
    Call<MealResponse> getMealsByIngredient(@Query("i") String ingredient);

    @GET("lookup.php")
    Call<MealDetailResponse> getMealDetail(@Query("i") String mealId);

    @GET("random.php")
    Call<MealDetailResponse> getRandomMeal();
}
```

**Penggunaan di HomeFragment dengan error handling & tombol refresh:**
```java
private void searchMeals(String ingredient) {
    progressBar.setVisibility(View.VISIBLE);
    btnRefresh.setVisibility(View.GONE);

    ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
    Call<MealResponse> call = apiService.getMealsByIngredient(ingredient);

    call.enqueue(new Callback<MealResponse>() {
        @Override
        public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
            progressBar.setVisibility(View.GONE);
            if (response.isSuccessful() && response.body() != null) {
                List<Meal> meals = response.body().getMeals();
                adapter = new MealAdapter(new ArrayList<>(meals));
                recyclerView.setAdapter(adapter);
            }
        }

        @Override
        public void onFailure(Call<MealResponse> call, Throwable t) {
            progressBar.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.VISIBLE); // Tampilkan tombol refresh
            Toast.makeText(getContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
        }
    });
}
```

---

### 6.6 SQLite — Penyimpanan Resep Favorit

Sesuai materi Bab 8, SQLite digunakan untuk menyimpan resep favorit secara lokal.

**Struktur Tabel `favorite_meal`:**

| Kolom | Tipe | Keterangan |
|---|---|---|
| `_id` | INTEGER PRIMARY KEY AUTOINCREMENT | ID otomatis |
| `meal_id` | TEXT NOT NULL | ID resep dari API |
| `meal_name` | TEXT NOT NULL | Nama resep |
| `thumbnail` | TEXT NOT NULL | URL foto resep |
| `instructions` | TEXT | Langkah memasak |

**File-file yang dibuat:**

| File | Fungsi |
|---|---|
| `DatabaseContract.java` | Mendefinisikan nama tabel dan kolom |
| `DatabaseHelper.java` | Membuat dan upgrade tabel SQLite |
| `MealHelper.java` | getInstance, open, close, insert, queryAll, deleteById |
| `MappingHelper.java` | Mengkonversi Cursor menjadi ArrayList\<Meal\> |

**DatabaseContract.java:**
```java
public class DatabaseContract {
    public static final String TABLE_NAME = "favorite_meal";

    public static final class MealColumns implements BaseColumns {
        public static final String MEAL_ID = "meal_id";
        public static final String MEAL_NAME = "meal_name";
        public static final String THUMBNAIL = "thumbnail";
        public static final String INSTRUCTIONS = "instructions";
    }
}
```

**Menyimpan favorit (di DetailActivity):**
```java
btnFavorite.setOnClickListener(v -> {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    executor.execute(() -> {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MealColumns.MEAL_ID, mealDetail.getIdMeal());
        values.put(DatabaseContract.MealColumns.MEAL_NAME, mealDetail.getStrMeal());
        values.put(DatabaseContract.MealColumns.THUMBNAIL, mealDetail.getStrMealThumb());
        values.put(DatabaseContract.MealColumns.INSTRUCTIONS, mealDetail.getStrInstructions());

        long result = mealHelper.insert(values);

        handler.post(() -> {
            if (result > 0) {
                Toast.makeText(DetailActivity.this, "Resep disimpan!", Toast.LENGTH_SHORT).show();
            }
        });
    });
});
```

---

### 6.7 SharedPreferences — Tema & Riwayat Bahan

Sesuai materi Bab 7, SharedPreferences digunakan untuk dua hal:

**a. Menyimpan preferensi tema (dark/light mode):**
```java
// Menyimpan
SharedPreferences.Editor editor = sharedPreferences.edit();
editor.putBoolean("dark_mode", isChecked);
editor.apply();

// Membaca dan menerapkan saat app dibuka
boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
if (isDarkMode) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
} else {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
}
```

**b. Menyimpan riwayat bahan terakhir:**
```java
// Menyimpan bahan terakhir setelah user klik "Find Recipes"
editor.putString("last_ingredients", etIngredient.getText().toString());
editor.apply();

// Memuat bahan terakhir saat HomeFragment dibuka
String lastIngredient = sharedPreferences.getString("last_ingredients", "");
if (!lastIngredient.isEmpty()) {
    etIngredient.setText(lastIngredient);
}
```

---

## 7. Struktur Folder Project

```
CookItUp/
├── manifests/
│   └── AndroidManifest.xml           ← Permission INTERNET
│
├── java/com.example.cookitup/
│   ├── ui/
│   │   ├── MainActivity.java          ← Launcher, BottomNav, loadFragment()
│   │   ├── DetailActivity.java        ← Detail resep + tombol favorit
│   │   ├── home/
│   │   │   └── HomeFragment.java      ← Input bahan, fetch API, RecyclerView
│   │   └── favorite/
│   │       └── FavoriteFragment.java  ← Load SQLite, RecyclerView favorit
│   │
│   ├── adapter/
│   │   ├── MealAdapter.java           ← Adapter hasil pencarian API
│   │   └── FavoriteAdapter.java       ← Adapter data favorit SQLite
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
    │   ├── fragment_home.xml          ← Input bahan + RecyclerView
    │   ├── fragment_favorite.xml      ← RecyclerView favorit
    │   └── item_meal.xml              ← Layout per item resep
    ├── menu/
    │   └── bottom_nav_menu.xml        ← Item menu bottom navigation
    └── values/
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
│  1. User input bahan (misal: "chicken, garlic")         │
│  2. Klik tombol "Find Recipes"                          │
│  3. Bahan disimpan ke SharedPreferences                 │
│  4. Retrofit fetch TheMealDB API (background thread)    │
│  5. Hasil ditampilkan di RecyclerView                   │
│  6. Jika gagal → tampilkan tombol Refresh               │
└────────────────────────┬────────────────────────────────┘
                         │ klik item resep
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  DetailActivity                         │
│  1. Retrofit fetch detail resep by ID                   │
│  2. Tampilkan foto, bahan, langkah memasak              │
│  3. Klik ❤️ → Executor simpan ke SQLite (background)   │
│  4. Toast konfirmasi "Resep disimpan!"                  │
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
│  1. Switch dark/light mode                              │
│  2. Status disimpan ke SharedPreferences                │
│  3. Tema langsung berubah via AppCompatDelegate         │
└─────────────────────────────────────────────────────────┘
```

---

## 9. Kriteria Penilaian & Pemenuhannya

| Kriteria | Bobot | Pemenuhan |
|---|---|---|
| **Fungsi & Kegunaan** | 40% | Pencarian resep by bahan berfungsi penuh, mode offline via SQLite, tombol refresh saat gagal koneksi |
| **Kreativitas & Inovasi** | 10% | Konsep "masak dari bahan yang ada" unik dan problem-solving, riwayat bahan tersimpan otomatis |
| **User Interface** | 20% | Dark/light theme, card design di RecyclerView, layout bersih menggunakan Material Design |
| **Stabilitas & Performa** | 20% | Semua operasi berat di background thread, error handling lengkap, tidak ada operasi di main thread |
| **Dokumentasi** | 10% | README.md lengkap di GitHub dengan deskripsi, cara penggunaan, dan penjelasan teknis |

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
| UI | XML Layout + Material Design | - |
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

    // Material Design (BottomNavigationView, CardView, dll)
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