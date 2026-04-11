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

## User Roles

Stored as `role` field in Firestore per user. Controls permissions and navigation flows:

* `DOG_OWNER` — sees: My Dogs, Reminders, Meetups, AI Chat
* `SERVICE_PROVIDER` — sees: My Profile, Service Requests, My Schedule
* `ADMIN` — future

## Core Data Models (Firestore)

* **users**: uid, fullName, email, role, isBlocked, createdAt
* **dogs**: dogId, ownerId, name, breed, birthDate, notes
* **reminders**: reminderId, dogId, type, dateTime, frequency, status
* **meetups**: meetupId, creatorId, location, dateTime, description, participants[]
* **serviceProviders** (future)
* **serviceRequests** (future)

## Features

* Auth (Firebase Authentication)
* Dog profile management (CRUD)
* Reminders (CRUD)
* Social meetups (create, search, join)
* Service provider system (future)
* AI Chat (via backend → OpenAI)
* Recommendation system (future)

Core features must work **independently** of AI availability.

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

## Package Structure

```
com/example/pet4you/
├── data/model/
│   ├── User.kt          (+ UserRole constants: DOG_OWNER, SERVICE_PROVIDER, ADMIN)
│   ├── Dog.kt
│   ├── Reminder.kt      (+ ReminderStatus constants)
│   └── Meetup.kt
├── repository/
│   └── AuthRepository.kt  (login, register, logout, getUserRole)
├── viewmodel/
│   └── AuthViewModel.kt   (AuthState: Idle/Loading/Success(role)/Error)
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   ├── home/
│   │   ├── DogOwnerHomeScreen.kt       (placeholder cards)
│   │   ├── ServiceProviderHomeScreen.kt (placeholder cards)
│   │   └── HomeScreen.kt               (unused — can be deleted)
│   ├── splash/
│   │   └── SplashScreen.kt  (checks auth + fetches role → navigates)
│   ├── navigation/
│   │   └── NavGraph.kt      (Routes: splash/login/register/dog_owner_home/service_provider_home)
│   └── theme/
│       ├── Color.kt, Theme.kt, Type.kt
└── MainActivity.kt  (always starts at Routes.SPLASH)
```

## Navigation Flow

```
App opens → SplashScreen → checks Firebase Auth
                ↓                     ↓
           not logged in          logged in
                ↓                     ↓
           LoginScreen     fetch role from Firestore
                ↓                     ↓
           RegisterScreen    DOG_OWNER → DogOwnerHomeScreen
           (role selection)  SERVICE_PROVIDER → ServiceProviderHomeScreen
```

## Git Workflow

* `main` = stable branch
* Each feature = separate branch from main
* Pull → branch → develop → commit → push → PR → merge to main

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
* Prefer reusable and maintainable solutions over quick fixes
* Do not assume missing requirements — ask for clarification if needed
* When working on a specific layer, do not implement other layers unless explicitly requested
* Ensure all code aligns with the overall system architecture described above

## Current Development Focus

**Android — Feature implementation stage**

The foundation (auth, navigation, role-based routing) is complete.
The next step is implementing actual features, starting with **Dog Profile Management (CRUD)** for DOG_OWNER.

## What's Done ✅

* Project created in Android Studio (Kotlin + Jetpack Compose)
* Connected to GitHub, working on branch `feature/project-setup`
* Firebase configured (google-services.json in app/)
* Firebase Auth + Firestore dependencies added
* Navigation Compose + ViewModel dependencies added
* Data models: User, Dog, Reminder, Meetup
* AuthRepository: login, register, logout, getUserRole
* AuthViewModel: login(), register(), AuthState with role
* LoginScreen + RegisterScreen (with role selection chips)
* SplashScreen: checks auth and fetches role → routes correctly
* NavGraph: role-based routing (DOG_OWNER / SERVICE_PROVIDER)
* DogOwnerHomeScreen (placeholder — cards with no functionality yet)
* ServiceProviderHomeScreen (placeholder — cards with no functionality yet)

## What's NOT Done Yet ❌ (Next Steps)

### Next features to implement (in order):

**For DOG_OWNER:**
1. **Dog Profile CRUD** — add/edit/delete dogs, list dogs screen
   - Needs: `DogRepository.kt`, `DogViewModel.kt`, `DogListScreen.kt`, `AddEditDogScreen.kt`
   - Branch to create: `feature/dog-profiles`
2. **Reminders CRUD** — add/edit/delete reminders per dog
   - Needs: `ReminderRepository.kt`, `ReminderViewModel.kt`, screens
   - Branch to create: `feature/reminders`
3. **Meetups** — create, search, join meetups
   - Needs: `MeetupRepository.kt`, `MeetupViewModel.kt`, screens
   - Branch to create: `feature/meetups`

**For SERVICE_PROVIDER:**
5. **Service Provider Profile** — create/edit provider profile
6. **Service Requests** — view and manage incoming requests

**Shared:**
7. **AI Chat** — text input → HTTP to backend → response displayed
   - Backend must be running first (Python/Flask on Render)
8. **Backend setup** (separate — Visual Studio Code)

## Project History & Status

### 2026-04-11 — Initial Setup
* Created Android project in Android Studio
* Connected to GitHub
* Pushed initial commit (Compose starter project)
* Defined full project architecture

### 2026-04-11 — Foundation Complete ✅ (merged to main)
* Added all dependencies (Firebase, Navigation, ViewModel)
* Created full package structure (MVVM)
* Implemented data models (User, Dog, Reminder, Meetup)
* Implemented Auth (login, register, logout, role-based navigation)
* SplashScreen with auth guard
* Home screen placeholders per role (DogOwnerHomeScreen, ServiceProviderHomeScreen)
* PR #1 merged to master — master is stable and up to date

---

> Update this file after every milestone. Add to "What's Done", remove from "What's NOT Done", and add a new entry to Project History.
