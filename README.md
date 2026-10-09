# CourseFlow

Learning dashboard app: **Login → Course Dashboard → Course Details**, with offline support.
**Stack:** Kotlin · XML views + ViewBinding · MVVM · Coroutines/StateFlow · Room · EncryptedSharedPreferences · JUnit

## Run
Open in Android Studio (JDK 17) → Sync → Run. Tests: `./gradlew testDebugUnitTest`. Min SDK 24.
- **Login:** any valid email + password of 6+ characters. Password `wrong123` shows the error state.
- **Session:** the token is saved, so later launches open the dashboard directly (also offline). Menu (⋮) → *Log out*.
- **Offline demo:** log in and let the courses load → airplane mode → relaunch → courses and lessons still show, and lessons can still be completed.

## 1. Architecture
MVVM + Repository: `Activity → ViewModel (StateFlow) → Repository → Room + remote data source`. Each screen renders one
immutable state (Loading / Success / Empty / Error), so rotation and process death are safe and logic is testable without Android.
`domain/` holds pure logic (`ProgressCalculator`, `InputValidator`). The mock API sits behind an interface, so Retrofit can replace it
without touching upper layers. Manual DI via `ServiceLocator` keeps the 3-hour scope small (Hilt in production).

## 2. Offline Support
Room is the **single source of truth**; the UI only observes database `Flow`s. `refresh()` syncs remote → local in one transaction and
caches courses with their lessons, so list and details both work offline. If a refresh fails while the cache has data, the list stays visible
with an "Offline" banner; the Error state appears only when nothing is cached. Refresh never overwrites the user's local completions.
Marking a lesson done updates the lesson and course progress in one DB transaction.

## 3. Security
In production the token belongs in the Android Keystore-backed encrypted storage (this app uses `EncryptedSharedPreferences`), never plain
SharedPreferences. Use short-lived access tokens plus a refresh token, attach via an OkHttp interceptor, clear on logout/401, disable
backups, and use TLS with certificate pinning.

## 4. Scale (1M users, hundreds of courses)
1. Paginated, incremental sync (Paging 3 + `RemoteMediator`, ETags / `updatedSince`) instead of fetching everything.
2. Sync lesson progress to the backend through a WorkManager outbox (retry/backoff, idempotent writes, conflict rules).
3. CDN and HTTP caching for the catalog and images; server-side pagination and rate limiting.
4. Observability: Crashlytics, performance traces, API error/latency metrics, feature flags, staged rollouts.
5. Hilt, feature modules, CI with UI tests, and proper Room migrations (`exportSchema` is off in this demo).

## 5. Second Platform (iOS)
Same layers in Swift: SwiftUI views → `@Observable` view models (≈ ViewModel + StateFlow) → repository protocol → `URLSession` +
SwiftData/Core Data as the offline source of truth. Same state enum, `async/await`, `NWPathMonitor` for connectivity, token in the
**Keychain**, and `ProgressCalculator` ported 1:1 with XCTest. Kotlin Multiplatform could share the domain/data layer.

## Testing
`ProgressCalculatorTest` covers percentage rounding, empty courses, clamping and seeding from server progress; `InputValidatorTest` covers login rules.

## Known simplifications
Mock API and login (fake token, no refresh/expiry); no DI framework; lessons can only be marked completed (not undone); logout keeps cached courses.
