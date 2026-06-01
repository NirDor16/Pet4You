# Pet4You - Project Context for Claude

## Project Overview

Pet4You is a full-stack Android application for dog owners and service providers in the pet industry.
It provides a centralized platform for managing all aspects of a dog's life: authentication, dog profiles, reminders, social meetups, and service provider interactions.

## Architecture

Full **Client-Server** architecture:

* **Client**: Android app (Kotlin + Jetpack Compose) — handles UI and sends HTTP requests to the backend
* **Backend**: Python + Flask — handles business logic, security, AI integration, and third-party API proxying (deployed on Render)
* **Database**: Firebase Firestore — stores all core data
* **Auth**: Firebase Authentication
* **Storage**: Firebase Storage — dog photos

**Critical rule:** The client **never** communicates directly with any external API (OpenAI, OpenWeatherMap, SerpAPI, etc.). **ALL** third-party API requests go through the backend. API keys live only as Render environment variables — never in the APK or in `local.properties`.

All core features must work **independently** of AI availability.

## Tech Stack

| Layer           | Technology                                                      |
| --------------- | --------------------------------------------------------------- |
| Android App     | Kotlin, Jetpack Compose, Android Studio                         |
| Backend         | Python, Flask, Visual Studio Code                               |
| Deployment      | Render (cloud)                                                  |
| Database        | Firebase Firestore                                              |
| Authentication  | Firebase Authentication                                         |
| Storage         | Firebase Storage (dog photos)                                   |
| Image Loading   | Coil 2.7.0 (`coil-compose`) — `AsyncImage`, `SubcomposeAsyncImage` |
| Animations      | Lottie 6.4.0 (`lottie-compose`) — JSON assets in `app/src/main/assets/` |
| AI              | OpenAI API (key on backend only, via `/chat`)                   |
| Weather         | OpenWeatherMap API (key on backend only, via `/weather`)        |
| Dog Parks       | SerpAPI / Google Maps (key on backend only, via `/dog-parks`)   |
| GPS             | FusedLocationProviderClient (`play-services-location:21.3.0`)  |
| Dog Photos API  | Dog CEO API (`https://dog.ceo/api/`) — free, no key needed     |
| Version Control | Git + GitHub                                                    |

## API Key Security Policy

**Never put API keys in the Android app.** The pattern for any new third-party API:
1. Add the key as a Render environment variable on the backend
2. Add a backend route that calls the external API and returns the result
3. Android calls the backend route with `X-API-Key: pet4you-secret-123`

`local.properties` only needs `sdk.dir`. Collaborators need no API keys — just clone and run.
See `local.properties.example` at project root for the template.

## UI / Theme Notes

- Theme config: `ui/theme/Theme.kt` — `dynamicColor = false`, full `LightColorScheme` + `DarkColorScheme` wired
- TextField text color across the app comes from `colorScheme.onSurface` (Material3) — no per-field override needed
- Do NOT re-enable `dynamicColor` — it causes TextField text to appear white on Android 12+ devices
- App forces LTR globally: `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)` wraps NavGraph in `MainActivity` — do NOT remove, Hebrew-locale devices flip the entire layout without it
- Maps deep link pattern: `google.navigation:q=${Uri.encode(location)}` with fallback to `https://maps.google.com/maps?q=` — no API key needed

## Design System (established 2026-05-18)

The app uses a unified Material 3 design system. **All new screens must follow these rules.**

### Color Palette — Teal + Amber
- **Light:** primary `#006B5B` (teal), tertiary/amber `#7E5700`, background `#F4FBF8`
- **Dark:** primary `#5EDBC6`, tertiary/amber `#F9BC48`, background `#101512`
- Colors defined in `ui/theme/Color.kt` as `md_light_*` / `md_dark_*` constants
- Never use hardcoded `Color.Red`, `Color(0xFF...)` for UI — always use theme tokens

### Status Colors (use these everywhere)
| Status | containerColor | contentColor |
|--------|---------------|--------------|
| PENDING / neutral | `tertiaryContainer` | `onTertiaryContainer` |
| APPROVED / active / success | `primaryContainer` | `onPrimaryContainer` |
| REJECTED / error / blocked | `errorContainer` | `onErrorContainer` |

### Typography — Nunito
- Font: Nunito via `androidx.compose.ui:ui-text-google-fonts`
- Full M3 scale defined in `ui/theme/Type.kt` — use `MaterialTheme.typography.*` tokens
- Key weights: ExtraBold for headlines, SemiBold for titles, Regular for body
- Falls back to system Roboto if Google Fonts is unavailable (no crash)

### Shapes
- Defined in `ui/theme/Shape.kt`: extraSmall=8dp, small=12dp, medium=16dp, large=24dp, extraLarge=32dp
- Applied automatically via `MaterialTheme.shapes.*`

### Reusable Components (`ui/components/CommonComponents.kt`)
Always use these — do NOT duplicate loading/empty/error/success patterns:

| Component | Usage |
|-----------|-------|
| `Pet4YouTopBar(title, onBack?, actions?)` | Every screen's top bar |
| `LoadingBox(modifier?)` | Any loading state — shows Lottie rotating arc, falls back to CircularProgressIndicator |
| `EmptyState(icon, title, subtitle, modifier?)` | Any empty list — Lottie pulse ring + icon overlay |
| `ErrorMessage(message, modifier?)` | Any error state — uses `colorScheme.error` |
| `StatusBadge(label, containerColor, contentColor)` | Colored pill for statuses |
| `Pet4YouCard(modifier?, onClick?, content)` | Standard ElevatedCard |
| `InfoRow(icon, text, modifier?, tint?)` | Icon + text row in detail screens |
| `SuccessOverlay(message?)` | Full-screen Lottie checkmark + message, shown after save/create; call with `return@Screen` to block Scaffold from rendering behind it |
| `BreedSelector(value, onValueChange, label?, onBreedSelected)` | Autocomplete dropdown for dog breeds — filters `DOG_BREEDS` list as user types, shows up to 8 matches, calls `onBreedSelected` on tap; used in AddEditDogScreen and CreateMeetupScreen |
| `PawBackground(modifier?, content)` | Canvas backdrop drawing 8 rotated paw prints at 5% alpha — used in form/detail screens (wraps Scaffold content) and list screens (wraps Scaffold content below TopBar) |

### Lottie Animation Assets (`app/src/main/assets/`)
| File | Used in | Description |
|------|---------|-------------|
| `lottie_loading.json` | `LoadingBox` | Rotating teal arc |
| `lottie_success.json` | `SuccessOverlay` | Circle pop + white checkmark |
| `lottie_empty.json` | `EmptyState` | Pulsing ring |
| `lottie_splash.json` | `SplashScreen` (replaced) | Bouncing teal circle (no longer active) |
| `lottie_dog.json` | `SplashScreen` + `DogListScreen` (empty state) | Blinking dog animation (220dp, IterateForever) |

Standard Lottie pattern:
```kotlin
val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_X.json"))
val progress    by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
LottieAnimation(composition, { progress }, modifier = Modifier.size(100.dp))
```
Use `iterations = 1` for one-shot animations (success). Always provide a non-Lottie fallback (`CircularProgressIndicator`) in case composition is null.

### Dog Grid (DogListScreen)
`DogListScreen` displays dogs as a 2-column `LazyVerticalGrid(GridCells.Fixed(2))`. Each `DogGridItem` shows:
- Square photo area (`fillMaxWidth().aspectRatio(1f)`) with three-tier fallback:
  1. Custom uploaded photo (Firebase Storage URL) → `AsyncImage` filling the square
  2. No custom photo + breed mapped in `DogCeoRepository` → Dog CEO API breed photo → `AsyncImage`
  3. No match → letter on `SoftBlue` background
- Dog name + breed below the photo
- Compact edit/delete icon buttons (36dp) at bottom-right

Use `produceState<String?>(null, dog.breed)` inside `DogGridItem` to fetch the Dog CEO URL. `DogCeoRepository` is a Kotlin `object` with `ConcurrentHashMap` cache — same breed fetched once per app session.

### Inner Screen Pattern (form + detail screens)
All form and detail screens use `PawBackground` wrapping the Scaffold content area (below TopBar):
```
Scaffold(topBar = ...) { padding ->
    PawBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(...)) {
            // form fields or detail cards
        }
    }
}
```

List screens (DogList, ReminderList, MeetupList, BrowseProviders, AiChat) also use `PawBackground` in the same position — no section banner or hero text of any kind.

Section accent colors (used in card icon containers, not in headers):
| Section | containerColor | iconTint |
|---------|---------------|---------|
| Dogs | `SoftBlue` | `DeepBlue` |
| Reminders | `SoftOrange` | `DeepOrange` |
| Meetups | `SoftGreen` | `DeepGreen` |
| Services / SP | `SoftBeige` | `DeepAmber` |

### Card Style
- Use `ElevatedCard` with `elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)`
- Avatar circles: `Box` with `CircleShape` + `primaryContainer` background, first letter of name (see Dog Avatar Pattern for dogs)

### Navigation Transitions
- All routes in `NavGraph.kt` use `slideInHorizontally + fadeIn` / `slideOutHorizontally + fadeOut`
- Set globally at the `NavHost` level — no per-composable override needed

### Home Screen Pattern
- **No Scaffold, no PawBackground** — plain white system background
- Gradient hero header: `Brush.verticalGradient(primary → primaryContainer)` in a full-width `Box` with `statusBarsPadding()`; contains "Pet4You" title, Lottie dog (100dp), logout `IconButton`
- Feature cards: `ElevatedCard(containerColor = Color.White, contentColor = Color.Black)` — white card, black title, dark-gray subtitle (60% alpha)
- Colored icon container per section: 52dp `Box` with `item.containerColor` background + `item.iconTint` icon

### Chat Bubble Colors
- User bubble: `MaterialTheme.colorScheme.primary` / text: `onPrimary`
- AI bubble: `MaterialTheme.colorScheme.surfaceVariant` / text: `onSurface`

### SuccessOverlay + Navigation Pattern
After a successful save/create action, show `SuccessOverlay` then navigate back:
```kotlin
// In composable (before Scaffold):
if (actionState is XActionState.Success) {
    SuccessOverlay(message = "Done!")
    return@ScreenComposable
}
// In LaunchedEffect:
if (actionState is XActionState.Success) {
    delay(1400)
    viewModel.resetActionState()
    onNavigateBack()
}
```

## User Roles

Stored as `role` field in Firestore per user:

* `DOG_OWNER` — browses providers, sends service requests, manages dogs/reminders/meetups
* `SERVICE_PROVIDER` — receives and manages incoming service requests, manages own profile
* `ADMIN` — manages users (block/unblock) via AdminScreen

## Interaction Model (Asymmetric)

**DOG_OWNER initiates, SERVICE_PROVIDER responds — never the other way around.**

```
DOG_OWNER:
  Browse providers → filter by type (VET/DOG_SITTER/GROOMER) → view profile → send request

SERVICE_PROVIDER:
  View incoming requests → approve or reject
```

## Service Provider Types

SERVICE_PROVIDER has a `providerType` field stored in both `users` and `serviceProviders` collections:
* `VET` — Veterinarian
* `DOG_SITTER` — Dog Sitter
* `GROOMER` — Groomer

All provider types share the same UI, but providerType is used for filtering and display.

## Core Data Models (Firestore)

* **users**: uid, fullName, email, role, isBlocked, createdAt
* **dogs**: dogId, ownerId, name, breed, birthDate, notes, photoUrl (Firebase Storage URL; empty string = no photo)
* **reminders**: reminderId, ownerId, dogId, type, dateTime, frequency, status (ACTIVE/DONE)
* **meetups**: meetupId, creatorId, title, location, dateTime, description, participants[], dogBreeds[], participantLimit (0=unlimited), createdAt — `recommendationScore` is transient (not in Firestore, `@get:Exclude`, populated from `/recommend-meetups` response)
* **serviceProviders**: serviceProviderId (=uid), providerType, fullName, email, description, location, isAvailable, createdAt
* **serviceRequests**: requestId, dogOwnerId, serviceProviderId, dogId, providerType, message, status (PENDING/APPROVED/REJECTED), createdAt, scheduledAt

## App Architecture Layers

```
UI (Compose Screens)
        ↓
ViewModel (state management)
        ↓
Repository (Firebase + backend API + Dog CEO API)
        ↓
Data Models
```

## Package Structure (current)

```
com/example/pet4you/
├── data/model/
│   ├── User.kt            (+ UserRole: DOG_OWNER, SERVICE_PROVIDER, ADMIN)
│   ├── Dog.kt             (photoUrl: String = "")
│   ├── DogBreeds.kt       (DOG_BREEDS list — 60+ breeds, used by BreedSelector)
│   ├── Reminder.kt        (+ ReminderType, ReminderFrequency, ReminderStatus)
│   ├── Meetup.kt          (title, location, dateTime, description, participants[], dogBreeds[], participantLimit, createdAt, recommendationScore? @get:Exclude)
│   ├── ServiceProvider.kt (+ ProviderType: VET, DOG_SITTER, GROOMER)
│   ├── ServiceRequest.kt  (+ RequestStatus: PENDING, APPROVED, REJECTED)
│   └── ChatMessage.kt     (role: String, content: String)
├── network/
│   ├── ApiClient.kt       (Retrofit singleton, baseUrl = pet4you-backend.onrender.com, OkHttp X-API-Key interceptor)
│   └── ApiService.kt      (ALL backend request/response models + ALL endpoints:
│                            POST /chat, POST /recommend-meetups,
│                            GET /weather?location=, POST /dog-parks {lat,lon})
├── repository/
│   ├── AuthRepository.kt
│   ├── DogRepository.kt        (+ uploadDogPhoto: Firebase Storage → returns download URL)
│   ├── ReminderRepository.kt
│   ├── MeetupRepository.kt     (+ getMeetupById)
│   ├── ServiceProviderRepository.kt
│   ├── ServiceRequestRepository.kt
│   ├── AiChatRepository.kt
│   ├── DogCeoRepository.kt     (object singleton; breedNameToDogCeoPath() maps 60+ breeds; ConcurrentHashMap cache; clearBreedCache(breed) for refresh)
│   ├── WeatherRepository.kt    (object singleton; calls ApiClient /weather; bestCityQuery() extracts city from "Park, City" strings; ConcurrentHashMap cache)
│   └── DogParkRepository.kt   (object singleton; calls ApiClient /dog-parks with lat/lon)
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── DogViewModel.kt         (addDog/updateDog accept photoUri: Uri? → uploads before saving)
│   ├── ReminderViewModel.kt
│   ├── MeetupViewModel.kt      (+ MeetupDetailState, loadMeetupById, join/leave/deleteMeetupFromDetail, RecommendState, loadRecommendations via Dijkstra backend)
│   ├── ServiceProviderViewModel.kt
│   ├── BrowseProvidersViewModel.kt
│   ├── ProviderDetailViewModel.kt
│   ├── IncomingRequestsViewModel.kt
│   ├── AiChatViewModel.kt      (sealed ChatState + StateFlow messages)
│   └── MyScheduleViewModel.kt
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt       (role + providerType selection)
│   ├── home/
│   │   ├── DogOwnerHomeScreen.kt
│   │   └── ServiceProviderHomeScreen.kt
│   ├── dog/
│   │   ├── DogListScreen.kt        (DogAvatar: custom photo → Dog CEO breed photo → letter fallback)
│   │   └── AddEditDogScreen.kt     (88dp photo circle picker; BreedInspirationCard from Dog CEO when breed set + no photo)
│   ├── reminder/
│   │   ├── ReminderListScreen.kt
│   │   └── AddEditReminderScreen.kt
│   ├── meetup/
│   │   ├── MeetupListScreen.kt     (3 tabs: All / My Meetups / Recommended + search bar on All tab)
│   │   ├── MeetupDetailScreen.kt   (info card + WeatherCard for meetup location + description + dog breeds + action button)
│   │   └── CreateMeetupScreen.kt   (title + location + "Find nearby dog parks" button → GPS → /dog-parks → ModalBottomSheet → auto-fill location)
│   ├── serviceprovider/
│   │   ├── ServiceProviderProfileScreen.kt
│   │   ├── BrowseProvidersScreen.kt
│   │   ├── ProviderDetailScreen.kt
│   │   ├── IncomingRequestsScreen.kt
│   │   └── MyScheduleScreen.kt
│   ├── admin/
│   │   └── AdminScreen.kt
│   ├── chat/
│   │   └── AiChatScreen.kt         (WhatsApp-style bubbles)
│   ├── splash/
│   │   └── SplashScreen.kt         (Lottie bouncing animation + "Pet4You" title)
│   ├── components/
│   │   └── CommonComponents.kt     (Pet4YouTopBar, LoadingBox, EmptyState, ErrorMessage, StatusBadge, Pet4YouCard, InfoRow, SuccessOverlay, BreedSelector)
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── theme/
│       ├── Color.kt    (md_light_* / md_dark_* M3 tonal palette)
│       ├── Type.kt     (Nunito Google Font, full M3 scale)
│       ├── Shape.kt    (8/12/16/24/32dp)
│       └── Theme.kt    (dynamicColor=false, responds to dark mode)
└── MainActivity.kt     (CompositionLocalProvider LTR wrap)

app/src/main/assets/
├── lottie_loading.json
├── lottie_success.json
├── lottie_empty.json
└── lottie_splash.json
```

## Navigation Flow

```
App opens → SplashScreen (Lottie) → checks Firebase Auth
                ↓                          ↓
           not logged in            logged in → fetch role → home screen
                ↓
           LoginScreen ↔ RegisterScreen
                ↓
   DOG_OWNER → DogOwnerHomeScreen
                 ├── My Dogs → DogListScreen → AddEditDogScreen
                 ├── Reminders → ReminderListScreen → AddEditReminderScreen
                 ├── Meetups → MeetupListScreen → MeetupDetailScreen (weather card)
                 │                            → CreateMeetupScreen (dog park picker)
                 ├── Find Services → BrowseProvidersScreen → ProviderDetailScreen
                 └── AI Chat → AiChatScreen

   SERVICE_PROVIDER → ServiceProviderHomeScreen
                 ├── My Profile → ServiceProviderProfileScreen
                 ├── Service Requests → IncomingRequestsScreen
                 └── My Schedule → MyScheduleScreen
```

## Backend Notes (Flask)

- Entry point: `backend/app.py` — ~290 lines
- Dependencies: `flask`, `openai`, `python-dotenv`, `gunicorn`, `requests`
- Env vars on Render: `OPENAI_API_KEY`, `API_KEY`, `OPENWEATHER_API_KEY`, `SERP_API_KEY`
- Local `.env` (gitignored): same vars; template in `backend/.env.example`
- Security: `X-API-Key` header checked on every route via `check_api_key()`
- Algorithm: `dijkstra_recommend()` in `backend/app.py` — see section below
- Tests: `backend/test_recommend.py` — pytest; run with `py -m pytest test_recommend.py -v`
- Local run: `cd backend && source venv/Scripts/activate && python app.py`
- ⚠️ Windows: always `debug=False`. `debug=True` → Werkzeug spawns child processes that survive, accumulate on port 5000. Kill all: `Get-Process python | Stop-Process -Force`

## Backend API Reference

**Base URL (deployed):** `https://pet4you-backend.onrender.com`
**Header required:** `X-API-Key: pet4you-secret-123`
**Free tier:** first request after inactivity ~30s cold start

| Method | Route | Request | Response |
|--------|-------|---------|----------|
| GET | `/health` | — | `{ status: "ok" }` |
| POST | `/chat` | `{ message, history[] }` | `{ reply }` |
| POST | `/recommend-meetups` | `{ dog_breeds[], user_id, meetups[] }` | `{ recommendations[] }` — each meetup has `score` field |
| GET | `/weather` | `?location={city}` | OpenWeatherMap current weather (main, weather[], wind, name) |
| GET | `/weather-forecast` | `?location={city}&datetime_ms={ms}` | OWM current or forecast weather by date — ≤12h→current, 12h–5d→nearest 3h slot, >5d→404 |
| POST | `/dog-parks` | `{ lat, lon }` | `{ local_results[] }` — SerpAPI parks (title, address, rating, reviews) |

## Meetup Recommendation Algorithm (Dijkstra)

**Entry point:** `dijkstra_recommend(dog_breeds, user_id, meetups)` in `backend/app.py`
**Called by:** `POST /recommend-meetups` → Android `MeetupViewModel.loadRecommendations()`

### How it works

**Step 1 — Build a weighted directed graph:**
- Node `"user"` → meetup nodes (direct edges)
- Meetup node → meetup node (similarity edges between meetups that share breeds)

**Edge weights (lower = more relevant):**
| Condition | Base weight |
|-----------|------------|
| Exact breed match (user's dog breed ∈ meetup's dogBreeds) | 0.10 |
| Same size group match (small/medium/large) | 0.35 |
| Open meetup (no breed filter) | 0.65 |
| No connection | no edge — meetup excluded |
| Meetup within 7 days | −0.20 bonus |
| Meetup within 30 days | −0.10 bonus |
| Meetup → Meetup (shared breed) | 0.20 |

**Breed size groups** (`BREED_SIZES` dict in app.py):
- `small`: chihuahua, maltese, pomeranian, yorkshire terrier, shih tzu, pug, etc.
- `medium`: beagle, bulldog, cocker spaniel, french bulldog, siberian husky, etc.
- `large`: labrador, golden retriever, german shepherd, rottweiler, samoyed, etc.

**Step 2 — Dijkstra shortest path** from `"user"` to every reachable meetup.

**Step 3 — Convert distance → score:**
```
score = max(1.0 - distance, 0.0)   # rounded to 2 decimal places
```
Meetups with `score <= 0` are excluded. Results sorted by (distance ASC, dateTime ASC).

### What gets filtered out (never recommended):
- Meetups the user created (`creatorId == user_id`)
- Meetups the user already joined (`user_id in participants`)
- Past meetups (`dateTime <= now_ms`)

### Android side (no changes needed to recommend new logic):
- `RecommendMeetupsRequest` in `ApiService.kt` — sends `dog_breeds`, `user_id`, `meetups[]`
- `RecommendMeetupsResponse` — receives `recommendations[]` with `score` field per meetup
- `Meetup.recommendationScore: Float?` — annotated `@get:Exclude` (not stored in Firestore)
- `MeetupViewModel.loadRecommendations()` — called lazily when "Recommended" tab is first opened
- "Recommended" tab in `MeetupListScreen` — shows scored meetups sorted by score DESC

### To extend the algorithm — backend only:
Edit `_build_graph()` or `dijkstra_recommend()` in `backend/app.py`. Android side is already wired.

---

**Adding a new backend route** — always follow this pattern:
```python
@app.route("/my-route", methods=["POST"])
def my_route():
    if not check_api_key():
        return jsonify({"error": "Unauthorized"}), 401
    data = request.get_json(silent=True) or {}
    # ... call external API with os.environ.get("MY_API_KEY") ...
    return jsonify(result)
```
Then add the corresponding method to `network/ApiService.kt` and a repository.

## Firestore Security Rules

Rules are configured in Firebase Console → Firestore → Rules tab.
Production rules active since 2026-05-12.

| Collection | Read | Write |
|---|---|---|
| `users` | any signed-in user | owner creates own doc; owner or ADMIN updates; ADMIN deletes |
| `dogs` | any signed-in user | owner creates (ownerId == uid); owner updates/deletes |
| `reminders` | owner only (ownerId == uid) | owner only |
| `meetups` | any signed-in user | any signed-in creates/updates; creator deletes |
| `serviceProviders` | any signed-in user | provider updates own doc (uid == providerId); ADMIN deletes |
| `serviceRequests` | dogOwnerId or serviceProviderId or ADMIN | dogOwner creates; both sides update; dogOwner or ADMIN deletes |

Rules use helper functions `isSignedIn()`, `isUser(uid)`, `role()`, `isAdmin()` — `role()` does a `get()` on the user document.

## Firebase Storage Rules

Configured in Firebase Console → Storage → Rules.
```
match /dog_photos/{userId}/{photo} {
    allow read:  if request.auth != null;
    allow write: if request.auth != null && request.auth.uid == userId;
}
```

## Gradle / Build Notes

- `./gradlew testDebugUnitTest` — run JVM unit tests
- `./gradlew assembleDebug` — compile check (full debug build)
- `./gradlew clean assembleDebug` — use when incremental build fails (stale cache error about `!intermediateDir.isDirectory()`)
- `local.properties` is gitignored — only needs `sdk.dir`, no API keys
- `local.properties.example` is committed — template for collaborators
- `buildConfig = true` in `build.gradle.kts` buildFeatures (required for `BuildConfig` class)
- Pre-existing deprecation warnings (safe to ignore): `Icons.Filled.Notes`, `Icons.Filled.ExitToApp`, `menuAnchor()` — not breaking

## Git Workflow

* `master` = stable branch
* Each feature = separate branch from master
* Pull → branch → develop → commit → push → PR → merge to master
* Always open a new branch before starting a feature

**Tools:**
- Android app → **Android Studio**
- Backend → **Visual Studio Code**
- GitHub: `https://github.com/NirDor16/Pet4You.git`

## Important Development Rules

* Always follow the current development context and focus on the active layer (Android, Backend, or Database)
* Write clean, modular, and scalable code that fits the existing architecture
* Follow MVVM architecture for Android components
* Use proper data models and avoid hardcoded values
* Keep separation of concerns (UI / ViewModel / Repository / Data)
* Do not assume missing requirements — ask for clarification if needed
* When working on a specific layer, do not implement other layers unless explicitly requested
* AI is supplementary — never make core features depend on AI availability
* **All third-party API keys go to Render environment variables only — never in the APK**

## What's Done ✅ — Android (all merged to master)

| PR | Branch | Feature |
|----|--------|---------|
| #1 | feature/project-setup | Firebase Auth + Firestore + MVVM structure + Navigation |
| #2 | feature/data-models-fix | Full data models + RegisterScreen providerType |
| #3 | feature/dog-profiles | Dog CRUD (list, add, edit, delete) |
| #4 | feature/reminders | Reminder CRUD + status toggle (ACTIVE/DONE) + dog picker |
| #5 | feature/meetups | Meetups: browse, create, join, leave, delete |
| #6 | feature/service-provider-profile | SERVICE_PROVIDER edits own profile |
| #7 | feature/service-requests | Browse providers, send request (dialog + dog picker), approve/reject |
| #8 | fix/textfield-text-color | TextField text visible — disable dynamicColor, explicit onSurface |
| #11 | feature/ai-chat | Android AI Chat — Retrofit + MVVM + bubble UI |
| #12 | feature/my-schedule | My Schedule — scheduledAt field, DatePicker+TimePicker on approve, MyScheduleScreen |
| #13 | feature/admin | Admin panel — block/unblock users, isBlocked login enforcement |
| #14 | feature/meetup-recommendation | For You tab with breed-based scoring via backend |
| #15 | feature/ui-upgrade | Full Material 3 upgrade — Teal+Amber, Nunito, dark mode, CommonComponents, 17 screens |
| #17 | feature/meetup-upgrade | MeetupDetailScreen, 3-tab list, search, title + participantLimit fields |
| #18 | feature/maps-navigation-ltr-fix | LTR layout fix + Navigate button → Google Maps |
| #19 | feature/breed-selector | BreedSelector autocomplete component in CommonComponents |
| #20 | feature/dijkstra-recommendation | Dijkstra algorithm upgrade for meetup recommendations |
| #21 | feature/dog-photos | Dog photo upload — Firebase Storage + Coil AsyncImage; `photoUrl` field on Dog model |
| #22 | feature/lottie-animations | Lottie animations — LoadingBox, EmptyState, SuccessOverlay, SplashScreen |
| #23 | feature/dog-ceo-api | Dog CEO API — breed photos in DogListScreen avatars + BreedInspirationCard in AddEditDog |
| #24 | feature/weather | OpenWeatherMap weather card in MeetupDetailScreen (via backend proxy) |
| #25 | feature/dog-park-picker | Nearby dog parks in CreateMeetupScreen — GPS + SerpAPI + ModalBottomSheet (via backend proxy) |
| #26 | feature/backend-proxy | Move all API keys to Render — Android calls backend only; deleted WeatherApiService + SerpApiService |
| #27 | feature/dog-lottie | Blinking dog Lottie animation (`lottie_dog.json`) — SplashScreen (220dp) + DogListScreen empty state |
| #28 | feature/weather-forecast | Weather by meetup date — `/weather-forecast` backend endpoint + WeatherPreview in CreateMeetupScreen + WeatherChip in MeetupListScreen + updated WeatherCard in MeetupDetailScreen |
| #30 | feature/ui-redesign | Premium inner screens — PawBackground + SectionBanner on all form/detail screens; accent colors per section; home screen hero + colored icon containers; card elevation 3dp across app |
| #31 | feature/ui-redesign | UI polish — DogListScreen 2-column photo grid; remove SectionHero text labels from all list screens; home screen plain white background + white cards with black text |

## What's Done ✅ — Backend

| PR | Branch | Feature |
|----|--------|---------|
| #9 | feature/flask-backend | Flask app.py + POST /chat + OpenAI gpt-4o-mini + API key auth |
| #10 | feature/render-deploy | Render deploy — backend live at https://pet4you-backend.onrender.com |
| #14 | feature/meetup-recommendation | POST /recommend-meetups — Dijkstra-based scoring algorithm |
| #26 | feature/backend-proxy | GET /weather (→ OpenWeatherMap) + POST /dog-parks (→ SerpAPI); `requests` library added |
| #28 | feature/weather-forecast | GET /weather-forecast — routes to OWM current or 5-day forecast by datetime_ms; finds nearest 3h slot |

## What's Done ✅ — Firebase / Infrastructure

| Date | Item |
|------|------|
| 2026-05-12 | Firestore Security Rules — production rules scoped per collection and role |
| 2026-05-24 | Firebase Storage Rules — dog_photos/{userId}/{photo} — owner write, any auth read |
| 2026-05-24 | Render env vars — OPENWEATHER_API_KEY + SERP_API_KEY added to backend service |

## Future Work 🔮

### Recommendation Algorithm — Extend Scoring
The Dijkstra-based algorithm is live. To improve it (backend only — `dijkstra_recommend()` in `app.py`):
- **Location proximity**: smaller edge weight if meetup city matches user's city
- **Past attendance**: lower weight for locations the user has visited before
- **Dog age matching**: boost meetups where breed sizes are compatible

When the backend returns a `score` field per meetup in `/recommend-meetups`, add `@SerializedName("score")` to `recommendationScore` in `Meetup.kt` to surface "Match 87%" badges in the Recommended tab UI.

### Dog CEO API — More Breeds
`DogCeoRepository.breedNameToDogCeoPath()` currently maps ~60 breeds. Unmapped breeds fall back to the letter avatar. Add more mappings as needed — the Dog CEO API breed list is at `https://dog.ceo/api/breeds/list/all`.

## Project History & Status

### 2026-04-11 — Foundation Complete (PRs #1–#2 → master)
* Firebase Auth, Firestore, MVVM structure, Navigation, full data models

### 2026-04-12 — Dog Profiles (PR #3 → master)
* DogRepository + DogViewModel + DogListScreen + AddEditDogScreen

### 2026-04-22 — Reminders + Meetups (PRs #4–#5 → master)
* Full CRUD for reminders (with dog picker) and meetups

### 2026-04-27 — Service Layer (PRs #6–#7, #9 → master)
* Service provider profile, service requests flow, Flask backend + /chat

### 2026-05-04 — AI Chat + Render Deploy (PRs #10–#11 → master)
* Backend deployed to Render; Android AI Chat with Retrofit + bubble UI

### 2026-05-08 — Admin + Schedule + Recommendations (PRs #12–#14 → master)
* Admin panel (block/unblock), My Schedule with date picker, meetup recommendations

### 2026-05-12 — Firestore Security Rules
* Production rules replacing expired test-mode rules

### 2026-05-18 — UI Upgrade + Meetup System (PRs #15, #17–#18 → master)
* Full Material 3 redesign (Teal+Amber, Nunito, dark mode, CommonComponents)
* MeetupDetailScreen, 3-tab list, search, Google Maps navigation, LTR fix

### 2026-05-24 — External APIs + Animations (PRs #19–#26 → master)
* **#19** BreedSelector autocomplete component
* **#20** Dijkstra recommendation algorithm upgrade
* **#21** Dog photo upload — Firebase Storage + Coil; `photoUrl` on Dog model
* **#22** Lottie animations — loading/success/empty/splash (4 custom JSON assets)
* **#23** Dog CEO API — breed photos as avatar fallback + BreedInspirationCard with refresh
* **#24** OpenWeatherMap — weather card in MeetupDetailScreen
* **#25** Dog park picker — GPS + SerpAPI + ModalBottomSheet → auto-fill meetup location
* **#26** Backend proxy refactor — ALL third-party keys moved to Render env vars; Android never calls external APIs directly; `local.properties.example` added for collaborators

### 2026-06-01 — Premium Inner Screens + UI Polish (PRs #30–#31 → feature/ui-redesign)
* **#30** PawBackground (Canvas paw prints at 5% alpha) applied to all form, detail, and list screens; home screen hero gradient + Lottie dog + colored icon containers per section; card elevation unified to 3dp; section accent colors (Dogs=blue, Reminders=orange, Meetups=green, Services=beige/amber)
* **#31** DogListScreen converted to 2-column photo grid (`LazyVerticalGrid(GridCells.Fixed(2))`) with square photo + name + edit/delete; SectionHero text banners removed from all list screens (no text headers on inner screens); home screen background plain white (no PawBackground), cards white with black text

### 2026-05-25 — Dog Animation + Date-Aware Weather (PRs #27–#28 → master)
* **#27** Blinking dog Lottie (`lottie_dog.json`) — replaces splash animation in SplashScreen; DogListScreen empty state shows animated dog instead of generic icon
* **#28** Weather forecast by meetup date — `/weather-forecast` backend endpoint (OWM current/forecast routing); `WeatherPreview` in CreateMeetupScreen (live preview when date changes); `WeatherChip` in MeetupListScreen cards (meetups within 5 days); `WeatherRepository.getWeatherForDate()` with hour-bucket cache; `WeatherCard` in MeetupDetailScreen updated to use forecast instead of current weather

---

> Update this file after every milestone.
