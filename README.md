# 🍳 CookItUp

> **Cook with what you have.**

CookItUp is an Android app that helps you discover recipes based on ingredients you already have in your kitchen. Simply add your available ingredients, and CookItUp will recommend meals you can cook right away — no extra shopping needed.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🥕 **Multi-Ingredient Search** | Add multiple ingredients as chips — the app finds recipes matching ALL of them |
| 📋 **Full Recipe Details** | View complete instructions, ingredients with measurements, and photos |
| ❤️ **Favorites with Offline Access** | Save recipes locally via SQLite — view them anytime, even without internet |
| 🦴 **Skeleton Loading** | Smooth shimmer placeholders while data loads (both home and detail pages) |
| 🌗 **Dark / Light Theme** | Toggle between themes — your preference is saved and persisted across sessions |
| 🔄 **Smart Error Handling** | Refresh button appears automatically when network requests fail |
| ↩️ **Undo Actions** | Accidentally removed a favorite? Tap UNDO on the Snackbar to restore it |
| 📱 **Splash Screen** | Animated launch screen with logo scale-in and text fade-in |
| 👆 **Swipe Navigation** | Swipe between Home and Favorites, or use the bottom navigation bar |

---

## 📸 Screenshots

<!-- Add your screenshots here -->
<!-- ![Home Screen](screenshots/home.png) -->
<!-- ![Detail Screen](screenshots/detail.png) -->
<!-- ![Favorites](screenshots/favorites.png) -->
<!-- ![Dark Mode](screenshots/dark_mode.png) -->

---

## 🏗️ Architecture

```
CookItUp/
├── java/com.example.cookitup/
│   ├── ui/
│   │   ├── MainActivity.java          ← Launcher, ViewPager2 + BottomNav + Splash
│   │   ├── DetailActivity.java        ← Recipe details, skeleton loader, offline fallback
│   │   ├── home/
│   │   │   ├── HomeFragment.java      ← Multi-ingredient chips, API search, skeleton
│   │   │   └── HomeViewModel.java     ← Persists search state across config changes
│   │   └── favorite/
│   │       └── FavoriteFragment.java  ← SQLite favorites, offline mode
│   │
│   ├── adapter/
│   │   ├── MealAdapter.java           ← RecyclerView adapter for search results
│   │   └── FavoriteAdapter.java       ← RecyclerView adapter for saved recipes
│   │
│   ├── network/
│   │   ├── RetrofitClient.java        ← Retrofit singleton + GsonConverterFactory
│   │   └── ApiService.java            ← TheMealDB API endpoints
│   │
│   ├── model/
│   │   ├── Meal.java                  ← List item model
│   │   ├── MealResponse.java          ← API response wrapper
│   │   ├── MealDetail.java            ← Full recipe detail model
│   │   └── MealDetailResponse.java    ← Detail API response wrapper
│   │
│   └── database/
│       ├── DatabaseContract.java      ← Table & column constants
│       ├── DatabaseHelper.java        ← SQLiteOpenHelper (create/migrate)
│       ├── MealHelper.java            ← CRUD operations (singleton)
│       └── MappingHelper.java         ← Cursor → ArrayList<Meal>
│
└── res/
    ├── layout/
    │   ├── activity_main.xml          ← ViewPager2 + BottomNav + Splash overlay
    │   ├── activity_detail.xml        ← Detail with skeleton/content toggle
    │   ├── fragment_home.xml          ← TopBar, chips, skeleton, RecyclerView
    │   ├── fragment_favorite.xml      ← TopBar, RecyclerView, empty state
    │   ├── item_meal.xml              ← MaterialCardView recipe card
    │   ├── item_favorite.xml          ← Favorite recipe card with heart
    │   └── item_meal_skeleton.xml     ← Shimmer skeleton placeholder card
    ├── drawable/                       ← Icons, backgrounds, shimmer animations
    ├── values/                         ← Colors, strings (English), light theme
    └── values-night/                   ← Dark theme colors & overrides
```

---

## 🔧 How It Works

### Search Algorithm

When the user taps "FIND RECIPES", the app fires parallel Retrofit `enqueue()` calls to TheMealDB for **each** ingredient. Results are collected into a `HashMap` and only meals that appear in **ALL** ingredient responses are shown (intersection logic):

```java
// Only show meals matching ALL ingredients
for (String id : uniqueMeals.keySet()) {
    if (mealOccurrences.get(id) == ingredients.size()) {
        intersectionList.add(uniqueMeals.get(id));
    }
}
```

### Offline Mode

When a recipe is favorited, its full data (name, image URL, instructions, ingredients JSON) is saved to SQLite. If the API fails when viewing a favorited recipe, `DetailActivity` falls back to the local database automatically.

### Threading Model

All SQLite operations use `ExecutorService` + `Handler(Looper.getMainLooper())` to keep the UI thread free. API calls use Retrofit's built-in `enqueue()` which runs on OkHttp's background dispatcher.

---

## 🔌 API

**[TheMealDB](https://www.themealdb.com/api.php)** — Free, no API key required.

| Endpoint | Purpose |
|---|---|
| `GET /filter.php?i={ingredient}` | Search recipes by ingredient |
| `GET /lookup.php?i={mealId}` | Get full recipe details by ID |

---

## ✅ Technical Checklist

| # | Specification | Implementation | Status |
|---|---|---|---|
| 1 | Min. 2 Activities | `MainActivity` + `DetailActivity` | ✅ |
| 2 | Launcher Activity | `MainActivity` is the launcher | ✅ |
| 3 | Intent with data | Passes `meal_id` and `meal_name` to `DetailActivity` | ✅ |
| 4 | RecyclerView | Home (search results) + Favorites (SQLite data) | ✅ |
| 5 | Min. 2 Fragments | `HomeFragment` + `FavoriteFragment` | ✅ |
| 6 | Navigation | `BottomNavigationView` + `ViewPager2` with swipe | ✅ |
| 7 | Background Thread | `ExecutorService` for all SQLite operations | ✅ |
| 8 | Handler | `Handler(Looper.getMainLooper())` for UI updates | ✅ |
| 9 | Retrofit | `RetrofitClient` + `GsonConverterFactory` | ✅ |
| 10 | API data displayed | Search results in `RecyclerView` via `MealAdapter` | ✅ |
| 11 | Refresh on failure | Button shown on `onFailure()` callback | ✅ |
| 12 | SQLite | Favorites stored with full ingredient data | ✅ |
| 13 | Offline display | Favorites + detail pages work without internet | ✅ |
| 14 | Dark / Light Theme | `AppCompatDelegate.setDefaultNightMode()` | ✅ |
| 15 | SharedPreferences | Persists theme preference (`dark_mode` key) | ✅ |

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| UI | XML Layouts + Material Design 3 |
| Networking | Retrofit 2.9.0 + Gson Converter |
| HTTP Client | OkHttp 4.9.1 |
| Image Loading | Picasso 2.71828 |
| Local Database | SQLite (android.database.sqlite) |
| State Management | ViewModel + LiveData |
| Preferences | SharedPreferences |
| Navigation | ViewPager2 + BottomNavigationView |
| Threading | ExecutorService + Handler |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Android SDK API 21+ (Android 5.0 Lollipop)
- Internet connection (for recipe search)

### Run

```bash
# Clone the repository
git clone https://github.com/username/CookItUp.git

# Open in Android Studio, let Gradle sync, then:
./gradlew installDebug
```

### Dependencies

Add to `build.gradle.kts` (Module :app):

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

## 👤 Author

**Akram Alfadli Tamir**

| | |
|---|---|
| NIM | H071241076 |
| Program | Sistem Informasi |
| University | Universitas Hasanuddin |

---

*Built with ☕ and a lot of ingredients.*