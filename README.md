# GatusMobile — Gatus Android Client

GatusMobile is an Android client for [Gatus](https://github.com/TwiN/gatus), a developer-oriented health dashboard. It displays endpoint statuses, response time trends, and events from a Gatus server.

## Quick Start

```bash
# Build debug APK
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

First launch: enter a Gatus server URL → app validates via `GET /api/v1/config` → saves to DataStore → enters dashboard.

---

## Project Architecture

```
me.fengqi.gatusmobile/
├── MainActivity.kt            ─ App entry, DataStore init, NavHost
├── navigation/
│   └── NavGraph.kt            ─ Routes: dashboard / settings / endpoint_detail (with slide animations)
├── data/
│   ├── model/                 ─ Data classes matching Gatus API responses
│   │   ├── Config.kt          ─ AppConfig, UIConfig, UIButton, Announcement
│   │   ├── EndpointStatus.kt  ─ EndpointStatus, EndpointEvent
│   │   ├── HealthCheckResult.kt ─ HealthCheckResult, ConditionResult
│   │   └── SuiteStatus.kt     ─ SuiteStatus
│   ├── api/
│   │   ├── GatusApiService.kt ─ Retrofit interface (4 endpoints)
│   │   └── RetrofitClient.kt  ─ OkHttp client factory, baseUrl is dynamic
│   ├── repository/
│   │   └── GatusRepository.kt ─ Wraps API calls with Result<T>
│   └── util/
│       └── TimeUtils.kt       ─ formatTimeAgo, formatTimestamp, response time helpers
├── ui/
│   ├── theme/                 ─ Gatus dark theme (background #0F172A)
│   │   ├── Color.kt           ─ GatusHealthy (#22C55E), GatusUnhealthy (#EF4444) etc.
│   │   ├── Theme.kt           ─ darkColorScheme, GatusTheme composable
│   │   └── Type.kt            ─ Typography
│   ├── components/            ─ Reusable composables
│   │   ├── StatusBadge.kt     ─ Healthy/Unhealthy badge with colored dot
│   │   ├── EndpointCard.kt    ─ Card: name + status badge + result bar chart + response time
│   │   ├── ResultBarChart.kt  ─ Green/red bars for historical check results (inside EndpointCard)
│   │   ├── SearchBar.kt       ─ TextField + FilterChips (Failing/Recent/Group)
│   │   ├── Pagination.kt      ─ Page navigator with ellipsis
│   │   ├── ResponseTimeChart.kt ─ Canvas line chart for response time trend
│   │   └── LoadingIndicator.kt ─ Centered spinner with message
│   ├── screen/
│   │   ├── DashboardScreen.kt   ─ Main grid: search, filter, group view, pagination
│   │   ├── EndpointDetailScreen.kt ─ Summary cards + recent checks + trend chart + events
│   │   └── SettingsScreen.kt     ─ Server URL input with validation
│   └── viewmodel/
│       ├── DashboardViewModel.kt       ─ Endpoint/suite list, search/filter/sort/pagination state
│       ├── EndpointDetailViewModel.kt  ─ Single endpoint detail with history
│       └── SettingsViewModel.kt        ─ DataStore persistence + URL validation
```

---

## Gatus API Endpoints

| Method | Endpoint | Used By |
|--------|----------|---------|
| GET | `/api/v1/config` | Validation on setup, DashboardScreen header |
| GET | `/api/v1/endpoints/statuses?page=1&pageSize=50` | DashboardScreen |
| GET | `/api/v1/endpoints/{key}/statuses?page=1&pageSize=50` | EndpointDetailScreen |
| GET | `/api/v1/suites/statuses?page=1&pageSize=50` | DashboardScreen |

---

## Navigation & Routes

```
Settings ──(validation success)──> Dashboard ──(tap endpoint)──> EndpointDetail
                                      ^                              │
                                      └──────(pop back)──────────────┘
```

Animations: 200ms slide-in/slide-out horizontally (WeChat-style).
Defined in `NavGraph.kt` with `slideInHorizontally` / `slideOutHorizontally`.

Key navigation code:
```kotlin
// Going forward: slides in from right
enterTransition = { slideInHorizontally(tween(200)) { it } }

// Going back: slides out to right
popExitTransition = { slideOutHorizontally(tween(200)) { it } }
```

---

## Data Flow

1. **SettingsScreen**: user enters URL → `SettingsViewModel.validateAndSave()` → calls `GET /api/v1/config` → on success saves URL to DataStore → NavGraph observes `ValidationState.Success` → navigates to Dashboard
2. **DashboardScreen**: `DashboardViewModel.init(url)` → fetches endpoints + suites + config → `DashboardUiState` drives UI
3. **EndpointDetailScreen**: `EndpointDetailViewModel.init(url, key)` → fetches `GET /api/v1/endpoints/{key}/statuses` → displays results + events

---

## Key Patterns

### No Flash on Back Navigation
ViewModels check `endpoints.isEmpty()` before showing loading indicator on refresh.
```kotlin
fun fetchAll(showLoading: Boolean = _uiState.value.endpoints.isEmpty()) {
    // showLoading = false on subsequent calls (no loading spinner flash)
}
```

### Server URL Validation
Before saving, `RetrofitClient.getApiService(url).getConfig()` is called. Error messages are human-readable (DNS / timeout / 401 / 403 / 404). Validation errors clear when user starts editing.

### Dynamic Base URL
`RetrofitClient` recreates the Retrofit instance when `baseUrl` changes. The URL is normalized (adds `https://` prefix if missing, ensures trailing `/`).

---

## Dependencies (libs.versions.toml)

| Library | Purpose |
|---------|---------|
| Retrofit 2.9 + Gson | REST API calls |
| OkHttp 4.12 + logging | HTTP client |
| Navigation Compose 2.7 | Screen routing |
| Lifecycle ViewModel Compose | State management |
| DataStore Preferences | Persist server URL |
| Vico 2.0 (alpha) | Response time chart (currently using Canvas - Vico available for upgrade) |
| Material Icons Core | Arrow icons |
| Compose BOM 2026.02 | Compose versions |

---

## Known Issues & Gotchas

### 1. PowerShell Encoding (Windows)
`Set-Content` in PowerShell uses system default encoding (Windows-1252/ANSI), **not UTF-8**. This corrupts Chinese comments and Unicode symbols (• ▶ ▼ ✓). Always use the `write` tool (which handles UTF-8) to create/edit Kotlin files. If you must use PowerShell, pipe through `Out-File -Encoding utf8`.

Symptoms: build errors like `Syntax error: Expecting '"'` or `Unresolved reference 'onClick'` on lines with Chinese text or Unicode symbols.

### 2. Nested LazyVerticalGrid inside verticalScroll
The DashboardScreen uses `LazyVerticalGrid` inside a `Column` with `verticalScroll`. This is a Compose anti-pattern. Currently worked around by giving the grid a fixed `.height(200.dp * ((n + 1) / 2))`. If card sizes change, this needs recalculation. A proper fix would be to use a single `LazyColumn` with `stickyHeader`.

### 3. Dashboard Pagination
Pagination renders all suites + endpoints on the current page together, then paginates by total items. This is an approximation (not true server-side pagination), but sufficient for typical Gatus monitoring setups.

### 4. Vico Chart Library
Vico 2.0-alpha.19 is declared in dependencies but currently unused. The `ResponseTimeChart` composable uses a custom `Canvas` implementation. To switch to Vico, replace `ResponseTimeChart.kt` with a `LineChart` from `com.patrykandpatrick.vico.compose`.

### 5. Error Handling
Repository wraps all API calls in `runCatching {}`. Individual failures (e.g., config endpoint failing while endpoints succeed) are silently swallowed and logged. The DashboardViewmodel replaces failed calls with empty lists.

---

## Building

```bash
# Requires JDK 21+
# JAVA_HOME example: C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot

# Assemble debug APK
./gradlew assembleDebug

# Clean build
./gradlew clean assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`
