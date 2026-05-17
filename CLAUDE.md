# Pet4You - Project Context for Claude

## Project Overview

Pet4You is a full-stack Android application for dog owners and service providers in the pet industry.
It provides a centralized platform for managing all aspects of a dog's life: authentication, dog profiles, reminders, social meetups, and service provider interactions.

## Architecture

Full **Client-Server** architecture:

* **Client**: Android app (Kotlin + Jetpack Compose) — handles UI and sends HTTP requests to the backend
* **Backend**: Python + Flask — handles business logic, security, and AI integration (deployed on Render)
* **Database**: Firebase Firestore — stores all core data
* **Auth**: Firebase Authentication

The client **never** communicates directly with OpenAI. All AI requests go through the backend.
All core features must work **independently** of AI availability.

## Tech Stack

| Layer           | Technology                                  |
| --------------- | ------------------------------------------- |
| Android App     | Kotlin, Jetpack Compose, Android Studio     |
| Backend         | Python, Flask, Visual Studio Code           |
| Deployment      | Render (cloud)                              |
| Database        | Firebase Firestore                          |
| Authentication  | Firebase Authentication                     |
| AI              | OpenAI API (API key stored on backend only) |
| Version Control | Git + GitHub                                |

## UI / Theme Notes

- Theme config: `ui/theme/Theme.kt` — `dynamicColor = false`, full `LightColorScheme` + `DarkColorScheme` wired
- TextField text color across the app comes from `colorScheme.onSurface` (Material3) — no per-field override needed
- Do NOT re-enable `dynamicColor` — it causes TextField text to appear white on Android 12+ devices

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
Always use these — do NOT duplicate loading/empty/error patterns:

| Component | Usage |
|-----------|-------|
| `Pet4YouTopBar(title, onBack?, actions?)` | Every screen's top bar |
| `LoadingBox(modifier?)` | Any loading state |
| `EmptyState(icon, title, subtitle, modifier?)` | Any empty list |
| `ErrorMessage(message, modifier?)` | Any error state — uses `colorScheme.error` |
| `StatusBadge(label, containerColor, contentColor)` | Colored pill for statuses |
| `Pet4YouCard(modifier?, onClick?, content)` | Standard ElevatedCard |
| `InfoRow(icon, text, modifier?, tint?)` | Icon + text row in detail screens |

### Card Style
- Use `ElevatedCard` with `elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)`
- Avatar circles: `Box` with `CircleShape` + `primaryContainer` background, first letter of name

### Navigation Transitions
- All routes in `NavGraph.kt` use `slideInHorizontally + fadeIn` / `slideOutHorizontally + fadeOut`
- Set globally at the `NavHost` level — no per-composable override needed

### Home Screen Pattern
- Gradient header: `Brush.verticalGradient(primary → primaryContainer)` in a `Box`
- Feature cards: `ElevatedCard` with icon + title + subtitle in a `Column`
- Logout: `IconButton` with `ExitToApp` icon in top-right

### Chat Bubble Colors
- User bubble: `MaterialTheme.colorScheme.primary` / text: `onPrimary`
- AI bubble: `MaterialTheme.colorScheme.surfaceVariant` / text: `onSurface`

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
* **dogs**: dogId, ownerId, name, breed, birthDate, notes
* **reminders**: reminderId, ownerId, dogId, type, dateTime, frequency, status (ACTIVE/DONE)
* **meetups**: meetupId, creatorId, title, location, dateTime, description, participants[], dogBreeds[], participantLimit (0=unlimited), createdAt — `recommendationScore` is transient (not in Firestore, populated from backend API)
* **serviceProviders**: serviceProviderId (=uid), providerType, fullName, email, description, location, isAvailable, createdAt
* **serviceRequests**: requestId, dogOwnerId, serviceProviderId, dogId, providerType, message, status (PENDING/APPROVED/REJECTED), createdAt, scheduledAt

## App Architecture Layers

```
UI (Compose Screens)
        ↓
ViewModel (state management)
        ↓
Repository (Firebase + backend API)
        ↓
Data Models
```

## Package Structure (current)

```
com/example/pet4you/
├── data/model/
│   ├── User.kt            (+ UserRole: DOG_OWNER, SERVICE_PROVIDER, ADMIN)
│   ├── Dog.kt
│   ├── Reminder.kt        (+ ReminderType, ReminderFrequency, ReminderStatus)
│   ├── Meetup.kt          (title, location, dateTime, description, participants[], dogBreeds[], participantLimit, createdAt, recommendationScore?)
│   ├── ServiceProvider.kt (+ ProviderType: VET, DOG_SITTER, GROOMER)
│   ├── ServiceRequest.kt  (+ RequestStatus: PENDING, APPROVED, REJECTED)
│   └── ChatMessage.kt     (role: String, content: String)
├── network/
│   ├── ApiClient.kt       (Retrofit singleton + OkHttp X-API-Key interceptor)
│   └── ApiService.kt      (ChatRequest, ChatResponse, RecommendMeetupsRequest/Response, POST /chat + POST /recommend-meetups)
├── repository/
│   ├── AuthRepository.kt
│   ├── DogRepository.kt
│   ├── ReminderRepository.kt
│   ├── MeetupRepository.kt
│   ├── ServiceProviderRepository.kt
│   ├── ServiceRequestRepository.kt
│   └── AiChatRepository.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── DogViewModel.kt
│   ├── ReminderViewModel.kt
│   ├── MeetupViewModel.kt
│   ├── ServiceProviderViewModel.kt
│   ├── BrowseProvidersViewModel.kt
│   ├── ProviderDetailViewModel.kt
│   ├── IncomingRequestsViewModel.kt
│   ├── AiChatViewModel.kt (sealed ChatState + StateFlow messages)
│   └── MyScheduleViewModel.kt
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt  (role + providerType selection)
│   ├── home/
│   │   ├── DogOwnerHomeScreen.kt
│   │   └── ServiceProviderHomeScreen.kt
│   ├── dog/
│   │   ├── DogListScreen.kt
│   │   └── AddEditDogScreen.kt
│   ├── reminder/
│   │   ├── ReminderListScreen.kt
│   │   └── AddEditReminderScreen.kt
│   ├── meetup/
│   │   ├── MeetupListScreen.kt    (3 tabs: All / My Meetups / Recommended + search bar)
│   │   ├── MeetupDetailScreen.kt  (full detail: info cards, dog breeds chips, join/leave/delete)
│   │   └── CreateMeetupScreen.kt  (title + location required, participantLimit optional)
│   ├── serviceprovider/
│   │   ├── ServiceProviderProfileScreen.kt
│   │   ├── BrowseProvidersScreen.kt
│   │   ├── ProviderDetailScreen.kt
│   │   ├── IncomingRequestsScreen.kt
│   │   └── MyScheduleScreen.kt
│   ├── admin/
│   │   └── AdminScreen.kt
│   ├── chat/
│   │   └── AiChatScreen.kt    (WhatsApp-style bubbles)
│   ├── splash/
│   │   └── SplashScreen.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── theme/
└── MainActivity.kt
```

## Navigation Flow

```
App opens → SplashScreen → checks Firebase Auth
                ↓                     ↓
           not logged in          logged in → fetch role → home screen
                ↓
           LoginScreen ↔ RegisterScreen
                ↓
   DOG_OWNER → DogOwnerHomeScreen
                 ├── My Dogs → DogListScreen → AddEditDogScreen
                 ├── Reminders → ReminderListScreen → AddEditReminderScreen
                 ├── Meetups → MeetupListScreen → MeetupDetailScreen
                 │                            → CreateMeetupScreen
                 ├── Find Services → BrowseProvidersScreen → ProviderDetailScreen
                 └── AI Chat → AiChatScreen

   SERVICE_PROVIDER → ServiceProviderHomeScreen
                 ├── My Profile → ServiceProviderProfileScreen
                 ├── Service Requests → IncomingRequestsScreen
                 └── My Schedule → MyScheduleScreen
```

## Backend Notes (Flask)

- Entry point: `backend/app.py` — ~90 lines
- Env vars: `OPENAI_API_KEY` + `API_KEY` in `backend/.env` (gitignored); template in `.env.example`
- Model: `gpt-4o-mini`; security: `X-API-Key` header check
- API: `POST /chat` `{ message, history[] }` → `{ reply }` | `GET /health` → `{ status: "ok" }` | `POST /recommend-meetups` `{ dog_breeds, user_id, meetups[] }` → `{ recommendations[] }`
- Tests: `backend/test_recommend.py` — 8 pytest tests for `score_meetups()`; run with `py -m pytest test_recommend.py -v`
- Local run: `cd backend && source venv/Scripts/activate && python app.py`
- ⚠️ Windows gotcha: always run with `debug=False`. `debug=True` spawns Werkzeug child processes that survive pkill — stale processes accumulate on port 5000 with stale env vars. Kill all: PowerShell `Get-Process python | Stop-Process -Force`

## Backend — Deployed (Render)

- **URL:** `https://pet4you-backend.onrender.com`
- **POST /chat** — request: `{ "message": "...", "history": [{"role":"user/assistant","content":"..."}] }` → response: `{ "reply": "..." }`
- **Header:** `X-API-Key: pet4you-secret-123`
- Free tier: first request after inactivity may take ~30s (cold start)

## Firestore Security Rules

Rules are configured in Firebase Console → Firestore → Rules tab.
⚠️ The original test-mode rules expired 2026-05-11 — replaced with production rules on 2026-05-12.

Current rules enforce:

| Collection | Read | Write |
|---|---|---|
| `users` | any signed-in user | owner creates own doc; owner or ADMIN updates; ADMIN deletes |
| `dogs` | any signed-in user | owner creates (ownerId == uid); owner updates/deletes |
| `reminders` | owner only (ownerId == uid) | owner only |
| `meetups` | any signed-in user | any signed-in creates/updates; creator deletes |
| `serviceProviders` | any signed-in user | provider updates own doc (uid == providerId); ADMIN deletes |
| `serviceRequests` | dogOwnerId or serviceProviderId or ADMIN | dogOwner creates; both sides update; dogOwner or ADMIN deletes |

Rules use helper functions `isSignedIn()`, `isUser(uid)`, `role()`, `isAdmin()` — `role()` does a `get()` on the user document to read the `role` field.

## Gradle Commands (Android)

- `./gradlew testDebugUnitTest` — run JVM unit tests
- `./gradlew assembleDebug` — compile check (full debug build)
- ⚠️ `local.properties` is gitignored — must be copied manually into git worktrees
- Pre-existing deprecation warnings (safe to ignore): `ProviderDetailScreen.kt`, `ServiceProviderProfileScreen.kt`, `CreateMeetupScreen.kt`, `MeetupDetailScreen.kt` — all about `Icons.Filled.Notes` / `menuAnchor()`; not breaking

## Git Workflow

* `master` = stable branch
* Each feature = separate branch from master
* Pull → branch → develop → commit → push → PR → merge to master

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
| #11 | feature/ai-chat | Android AI Chat — Retrofit + MVVM + bubble UI (ChatMessage, ApiClient, ApiService, AiChatRepository, AiChatViewModel, AiChatScreen) |
| #12 | feature/my-schedule | My Schedule — scheduledAt field on ServiceRequest, DatePicker+TimePicker on approve, MyScheduleViewModel + MyScheduleScreen (sorted by date) |
| #13 | feature/admin | Admin panel — block/unblock users, isBlocked login enforcement, AdminViewModel + AdminScreen, ADMIN routing in NavGraph |
| #14 | feature/meetup-recommendation | Meetup recommendation — For You tab with breed-based scoring, RecommendState + loadRecommendations() in MeetupViewModel |
| #15 | feature/ui-upgrade | Full Material 3 UI upgrade — Teal+Amber palette, Nunito font, dark mode, CommonComponents, all 17 screens redesigned, NavGraph slide+fade transitions |
| WIP | feature/meetup-upgrade | Meetup system upgrade — MeetupDetailScreen (new), 3-tab list (All/My/Recommended), search bar, title + participantLimit fields, shared ViewModel across list/detail/create |

## What's Done ✅ — Backend

| PR | Branch | Feature |
|----|--------|---------|
| #9 | feature/flask-backend | Flask app.py + POST /chat + OpenAI gpt-4o-mini + API key auth |
| #10 | feature/render-deploy | Render deploy — backend live at https://pet4you-backend.onrender.com |
| #14 | feature/meetup-recommendation | POST /recommend-meetups — score_meetups() algorithm + 8 pytest tests |

## What's Done ✅ — Firebase / Infrastructure

| Date | Item |
|------|------|
| 2026-05-12 | Firestore Security Rules — replaced expired test-mode rules with production rules scoped per collection and role |

## Future Work 🔮 — Meetup Recommendation Algorithm

The recommendation system is **partially implemented** — the infrastructure is ready, only the backend scoring logic needs expanding.

### What's Already in Place
| Layer | What exists |
|-------|------------|
| Backend | `score_meetups()` in `backend/app.py` — currently scores: breed match=1.0, open meetup=0.5, no match=excluded |
| Backend | `POST /recommend-meetups` endpoint — stateless, receives meetups + breeds from Android |
| Android ViewModel | `RecommendState` sealed class + `loadRecommendations()` in `MeetupViewModel` |
| Android Model | `recommendationScore: Float?` field on `Meetup` — annotated `@get:Exclude` (not stored in Firestore; populated from API response when backend sends it) |
| Android UI | "Recommended" tab in `MeetupListScreen` — already wired and displayed |

### How to Extend the Algorithm (backend only)
The Android side is ready — only `score_meetups()` in `backend/app.py` needs changes:
- **Location proximity**: score by distance if meetup location matches user's city/area
- **Date preference**: boost upcoming meetups within the user's preferred window
- **Dog size/age**: match meetup's `dogBreeds` list to similar-sized breeds
- **Past history**: boost meetups in locations the user has attended before

When the backend starts returning a `score` field per meetup in the JSON response, add `@SerializedName("score")` to `recommendationScore` in `Meetup.kt` to surface it in the UI (e.g., "Match 87%" badge on Recommended cards).

## What's NOT Done Yet ❌ — Remaining Roadmap

No remaining core features. See "Future Work" above for the recommendation algorithm next step.

## Project History & Status

### 2026-04-11 — Initial Setup
* Created Android project, connected to GitHub

### 2026-04-11 — Foundation Complete (PR #1 → master)
* Dependencies, MVVM structure, Auth, role-based navigation

### 2026-04-12 — Data Models + Requirements Aligned (PR #2 → master)
* ServiceProvider.kt, ServiceRequest.kt, updated Meetup.kt, RegisterScreen providerType

### 2026-04-12 — Dog Profiles (PR #3 → master)
* DogRepository + DogViewModel + DogListScreen + AddEditDogScreen

### 2026-04-22 — Reminders (PR #4 → master)
* ReminderRepository + ReminderViewModel (dogMap) + ReminderListScreen + AddEditReminderScreen

### 2026-04-22 — Meetups (PR #5 → master)
* MeetupRepository + MeetupViewModel + MeetupListScreen + CreateMeetupScreen

### 2026-04-27 — Service Provider Profile (PR #6 → master)
* ServiceProviderRepository + ServiceProviderViewModel + ServiceProviderProfileScreen

### 2026-04-27 — Service Requests (PR #7 → master)
* ServiceRequestRepository + 3 ViewModels + BrowseProvidersScreen + ProviderDetailScreen + IncomingRequestsScreen

### 2026-04-27 — Flask Backend (PR #9 → master)
* backend/app.py — POST /chat + GET /health, gpt-4o-mini, X-API-Key auth, client-side history

### 2026-05-04 — Render Deploy (PR #10 → master)
* render.yaml added, backend deployed to https://pet4you-backend.onrender.com ✅

### 2026-05-04 — Android AI Chat (PR #11 → master)
* ChatMessage + ApiClient + ApiService + AiChatRepository + AiChatViewModel + AiChatScreen
* Retrofit 2.9.0 + Gson, OkHttp interceptor for X-API-Key header
* WhatsApp-style bubble UI, conversation history in ViewModel only (no Firestore persistence)

### 2026-05-08 — Admin Panel (PR #13 → master)
* isUserBlocked() check injected into AuthViewModel.login() — blocked users get error + auto-logout
* AuthRepository: getAllUsers(), setUserBlocked(), isUserBlocked()
* AdminViewModel + AdminScreen (ui/admin/) — list all users with Block/Unblock buttons + status badges
* homeRouteForRole() updated: ADMIN → admin_home route
* Admin accounts created manually via Firebase Console (no self-registration)

### 2026-05-08 — My Schedule (PR #12 → master)
* scheduledAt: Long field added to ServiceRequest (default 0L, backward-compatible with Firestore)
* Approve flow: DatePickerDialog (Material3) → TimePickerDialog (android.app) → saves scheduledAt to Firestore
* MyScheduleViewModel + MyScheduleScreen — APPROVED requests sorted by scheduledAt, LazyColumn of ScheduleCard
* My Schedule card wired in ServiceProviderHomeScreen + NavGraph route added

### 2026-05-08 — Meetup Recommendation (PR #14 → master)
* Backend: score_meetups() — breed match=1.0, open meetup=0.5, no match=excluded; sorted score DESC / dateTime ASC
* Backend: POST /recommend-meetups endpoint — same X-API-Key auth, stateless (Android sends meetups + breeds)
* Backend: test_recommend.py — 8 pytest tests, all passing
* Android: RecommendMeetupsRequest/Response + Retrofit endpoint in ApiService.kt
* Android: RecommendState sealed class + loadRecommendations() in MeetupViewModel
* Android: TabRow ("All Meetups" / "For You") in MeetupListScreen — lazy-loads on tab switch

### 2026-05-12 — Firestore Security Rules (Firebase Console)
* Test-mode rules expired (were set to allow all until 2026-05-11)
* Replaced with production security rules scoped per collection and role
* Rules enforce: authenticated-only access, owners manage own data, ADMIN has elevated permissions, serviceRequests visible only to the two parties involved

### 2026-05-18 — UI Upgrade (on master, no separate PR)
* **Design System**: `ui/theme/Color.kt` — full M3 Teal+Amber tonal palette (light + dark); `ui/theme/Type.kt` — Nunito Google Font, full type scale; `ui/theme/Shape.kt` — rounded shape tokens (8–32dp); `ui/theme/Theme.kt` — wires colors/type/shapes, responds to dark mode
* **CommonComponents**: `ui/components/CommonComponents.kt` — `Pet4YouTopBar`, `LoadingBox`, `EmptyState`, `ErrorMessage`, `StatusBadge`, `Pet4YouCard`, `InfoRow`
* **All 17 screens upgraded**: hardcoded colors → theme tokens, `Card` → `ElevatedCard`, raw TopAppBar → `Pet4YouTopBar`, inline loading/error/empty → CommonComponents
* **Home screens**: gradient header (primary→primaryContainer), icon-led feature cards
* **Auth screens**: Pet4You branding (Pets icon + title), leading icons in TextFields
* **Chat**: theme colors for user/AI bubbles, `OutlinedTextField` + `FilledIconButton` for input
* **Splash**: animated Pets icon + "Pet4You" title (fade + scale)
* **MyScheduleScreen**: CalendarMonth icon, timeline-style left-accent card
* **AdminScreen**: theme status badges (primaryContainer/errorContainer) replacing hardcoded green/red
* **ProviderDetailScreen**: header ElevatedCard with type badge, `HorizontalDivider`, `InfoRow` for details
* **NavGraph**: global slide+fade transitions on all routes (300ms)
* Google Fonts dependency added: `androidx.compose.ui:ui-text-google-fonts` (falls back to Roboto if GMS unavailable)

### 2026-05-18 — Meetup System Upgrade (feature/meetup-upgrade, WIP)
* **Data model**: `Meetup.kt` — added `title` (required), `participantLimit` (0=unlimited), `createdAt`, `recommendationScore: Float?` (`@get:Exclude` — not stored in Firestore, infrastructure for future scoring UI)
* **Repository**: `MeetupRepository.kt` — updated `createMeetup` signature, added `getMeetupById`
* **ViewModel**: `MeetupViewModel.kt` — added `MeetupDetailState`, `_detailState`, `loadMeetupById`; added `joinMeetupFromDetail` / `leaveMeetupFromDetail` / `deleteMeetupFromDetail` (set `MeetupActionState.Success` for detail screen; also refresh list in background); refactored to `refreshMeetupList()` private suspend fun
* **MeetupListScreen**: 3 tabs (All / My Meetups / Recommended); search bar on All tab (client-side filter by title/location/description); cards are clickable (navigate to detail); `StatusBadge` for "Your meetup" / "Joined"
* **CreateMeetupScreen**: added Title field (required), Participant Limit field (optional, numeric)
* **MeetupDetailScreen** (new): header ElevatedCard (title + location + date + participant count), description card, dog breeds `FlowRow` with `AssistChip`; bottom action button adapts to role (Join / Leave / Delete); join/leave → reload detail; delete → navigate back
* **NavGraph**: `MEETUP_DETAIL = "meetup_detail/{meetupId}"` route added; all 3 meetup composables share the same `MeetupViewModel` instance via `viewModel(meetupListEntry)` so list refreshes automatically when returning from detail/create

---

> Update this file after every milestone.
