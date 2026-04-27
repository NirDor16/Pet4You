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
* **reminders**: reminderId, ownerId, dogId, type, dateTime, frequency, status (ACTIVE/DONE)
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

## Package Structure (current)

```
com/example/pet4you/
├── data/model/
│   ├── User.kt            (+ UserRole: DOG_OWNER, SERVICE_PROVIDER, ADMIN)
│   ├── Dog.kt
│   ├── Reminder.kt        (+ ReminderType, ReminderFrequency, ReminderStatus)
│   ├── Meetup.kt          (includes dogBreeds[] for future matching)
│   ├── ServiceProvider.kt (+ ProviderType: VET, DOG_SITTER, GROOMER)
│   └── ServiceRequest.kt  (+ RequestStatus: PENDING, APPROVED, REJECTED)
├── repository/
│   ├── AuthRepository.kt
│   ├── DogRepository.kt
│   ├── ReminderRepository.kt
│   ├── MeetupRepository.kt
│   ├── ServiceProviderRepository.kt
│   └── ServiceRequestRepository.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── DogViewModel.kt
│   ├── ReminderViewModel.kt
│   ├── MeetupViewModel.kt
│   ├── ServiceProviderViewModel.kt
│   ├── BrowseProvidersViewModel.kt
│   ├── ProviderDetailViewModel.kt
│   └── IncomingRequestsViewModel.kt
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
│   │   ├── MeetupListScreen.kt
│   │   └── CreateMeetupScreen.kt
│   ├── serviceprovider/
│   │   ├── ServiceProviderProfileScreen.kt
│   │   ├── BrowseProvidersScreen.kt
│   │   ├── ProviderDetailScreen.kt
│   │   └── IncomingRequestsScreen.kt
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
                 ├── Meetups → MeetupListScreen → CreateMeetupScreen
                 ├── Find Services → BrowseProvidersScreen → ProviderDetailScreen
                 └── AI Chat → (not yet wired — awaiting backend)

   SERVICE_PROVIDER → ServiceProviderHomeScreen
                 ├── My Profile → ServiceProviderProfileScreen
                 ├── Service Requests → IncomingRequestsScreen
                 └── My Schedule → (placeholder)
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

## What's NOT Done Yet ❌ — Remaining Roadmap

### Backend (Python + Flask — Visual Studio Code)

| Step | Description |
|------|-------------|
| Flask project setup | `app.py` with `POST /chat` endpoint |
| OpenAI integration | Forward chat messages to OpenAI API (key stored server-side only) |
| Deploy to Render | Public HTTPS URL for Android to call |
| Android Retrofit | Add Retrofit dependency, ApiClient, ApiService, wire AI Chat card |

### Future / Optional

| Feature | Who |
|---------|-----|
| Meetup recommendation algorithm | Backend |
| Admin: block/unblock users | ADMIN role |
| My Schedule (SERVICE_PROVIDER) | SERVICE_PROVIDER |

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

### Next: Flask Backend
* Build in VS Code, deploy to Render, connect Android AI Chat card via Retrofit

---

> Update this file after every milestone.
