# Pet4You - Project Context for Claude

## Project Overview
Pet4You is an Android application for dog owners and service providers in the pet industry.
It provides a centralized platform for managing all aspects of a dog's life:
registration/login, dog profiles, reminders, social meetups, and service provider interaction.

## Architecture
Full **Client-Server** architecture:
- **Client**: Android app (Kotlin + Jetpack Compose) — handles UI and sends HTTP requests to the backend
- **Backend**: Python + Flask — handles business logic, security, and AI integration (deployed on Render)
- **Database**: Firebase Firestore — stores all core data
- **Auth**: Firebase Authentication

The client **never** communicates directly with OpenAI. All AI requests go through the backend.

## Tech Stack
| Layer | Technology |
|-------|-----------|
| Android App | Kotlin, Jetpack Compose, Android Studio |
| Backend | Python, Flask, Visual Studio Code |
| Deployment | Render (cloud) |
| Database | Firebase Firestore |
| Authentication | Firebase Authentication |
| AI | OpenAI API (API key stored on backend only) |
| Version Control | Git + GitHub |

## User Roles
Stored as `role` field in Firestore per user. Controls permissions and navigation flows:
- `DOG_OWNER` — standard dog owner user
- `SERVICE_PROVIDER` — vet, doggy sitter, groomer, etc.
- `ADMIN` — administrative access

## Features
- **Auth**: Registration and login via Firebase Authentication
- **Dog Profiles**: Create, update, and delete dog profiles
- **Reminders**: Medications, vaccines, and other recurring tasks
- **Social Meetups**: Create, search, and join meetups between dog owners
- **Service Providers**: Profiles for vets, doggy sitters, groomers; send service requests
- **AI Chat**: User types free text → request sent to backend → backend calls OpenAI API → response returned to app
- **Future**: Smart meetup recommendation algorithm (backend)

The core functionality (dog profiles, reminders, meetups) works independently even without AI/recommendations available.

## App Architecture Layers
```
UI (Activities / Fragments)
        ↓
ViewModel  — state management
        ↓
Repository — communicates with Firebase + backend via HTTP
        ↓
Data Models — shared structure between client and backend
```

## Git Workflow
- `main` branch = stable production version (source of truth)
- Every feature/task is done on a **separate branch** created from `main`
- Before starting: pull latest `main`, create a new branch
- Work on the branch, make commits
- When done: push branch to GitHub, open a **Pull Request**
- After review: **merge** into `main`
- After merge: everyone continues from the updated `main`

**Tools:**
- Android app → **Android Studio**
- Backend → **Visual Studio Code**
- Both connected to the same GitHub repository: `https://github.com/NirDor16/Pet4You.git`

## Project History & Status

### 2026-04-11 — Initial Setup
- Created Android project (Pet4You) in Android Studio
- Initialized git repository and connected to GitHub
- Pushed initial commit: basic Jetpack Compose starter project (49 files)
- Defined full project architecture and feature scope (described above)
- Current state: empty starter project — no features implemented yet, ready for development

---
> **Note to Claude:** Update the "Project History & Status" section after every GitHub push or significant milestone. Add a new dated entry describing what changed. This file is the primary context source for new sessions.
