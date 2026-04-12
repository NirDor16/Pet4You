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

## User Roles

Stored as `role` field in Firestore per user:

* `DOG_OWNER` — browses providers, sends service requests, manages dogs/reminders/meetups
* `SERVICE_PROVIDER` — receives and manages incoming service requests, manages own profile
* `ADMIN` — manages users (block/unblock), future feature

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
* **reminders**: reminderId, dogId, type, dateTime, frequency, status (ACTIVE/DONE)
* **meetups**: meetupId, creatorId, location, dateTime, description, participants[], dogBreeds[]
* **serviceProviders**: serviceProviderId (=uid), providerType, fullName, email, description, location, isAvailable, createdAt
* **serviceRequests**: requestId, dogOwnerId, serviceProviderId, dogId, providerType, message, status (PENDING/APPROVED/REJECTED), createdAt

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
│   ├── User.kt            (+ UserRole: DOG_OWNER, SERVICE_PROVIDER, ADMIN)
│   ├── Dog.kt
│   ├── Reminder.kt        (+ ReminderStatus: ACTIVE, DONE)
│   ├── Meetup.kt          (includes dogBreeds[] for future matching)
│   ├── ServiceProvider.kt (+ ProviderType: VET, DOG_SITTER, GROOMER)
│   └── ServiceRequest.kt  (+ RequestStatus: PENDING, APPROVED, REJECTED)
├── repository/
│   └── AuthRepository.kt  (login, register[+providerType], logout, getUserRole)
├── viewmodel/
│   └── AuthViewModel.kt   (AuthState: Idle/Loading/Success(role)/Error)
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt  (role + providerType selection)
│   ├── home/
│   │   ├── DogOwnerHomeScreen.kt        (placeholder cards)
│   │   ├── ServiceProviderHomeScreen.kt (placeholder cards)
│   │   └── HomeScreen.kt               (unused)
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
   SERVICE_PROVIDER → ServiceProviderHomeScreen
```

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

## What's Done ✅

* Firebase Auth + Firestore + Navigation dependencies
* Full MVVM package structure
* Data models: User, Dog, Reminder, Meetup, ServiceProvider, ServiceRequest
* AuthRepository: login, register (with providerType), logout, getUserRole
* RegisterScreen: role selection + providerType selection for SERVICE_PROVIDER
* LoginScreen
* SplashScreen: auth guard + role-based routing
* NavGraph: routes splash/login/register/dog_owner_home/service_provider_home
* DogOwnerHomeScreen (placeholder)
* ServiceProviderHomeScreen (placeholder)
* PR #1 merged to master ✅
* Branch `feature/data-models-fix` in progress

## What's NOT Done Yet ❌ — Feature Roadmap

| Branch | Feature | Who |
|--------|---------|-----|
| `feature/dog-profiles` | Dog CRUD (list, add, edit, delete) | DOG_OWNER |
| `feature/reminders` | Reminder CRUD per dog | DOG_OWNER |
| `feature/meetups` | Create, browse, join meetups | DOG_OWNER |
| `feature/service-provider-profile` | Edit provider profile | SERVICE_PROVIDER |
| `feature/service-requests` | Browse providers + send request | DOG_OWNER |
| `feature/service-requests` | View + approve/reject requests | SERVICE_PROVIDER |
| future | Meetup recommendation algorithm | Backend |
| future | Admin: block/unblock users | ADMIN |
| future | AI Chat | Both (via backend) |

### Next to build: `feature/dog-profiles`
Files needed:
- `repository/DogRepository.kt`
- `viewmodel/DogViewModel.kt`
- `ui/dog/DogListScreen.kt`
- `ui/dog/AddEditDogScreen.kt`
- Wire "My Dogs" card in DogOwnerHomeScreen to DogListScreen

## Project History & Status

### 2026-04-11 — Initial Setup
* Created Android project, connected to GitHub

### 2026-04-11 — Foundation Complete (PR #1 → master)
* Dependencies, MVVM structure, Auth, role-based navigation

### 2026-04-12 — Data Models + Requirements Aligned (feature/data-models-fix)
* Added ServiceProvider.kt (+ ProviderType constants)
* Added ServiceRequest.kt (+ RequestStatus constants)
* Updated Meetup.kt with dogBreeds[]
* Updated RegisterScreen to capture providerType for SERVICE_PROVIDER
* Updated AuthRepository to create serviceProviders Firestore doc on register
* Corrected full system interaction model in CLAUDE.md

---

> Update this file after every milestone.
