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

* `DOG_OWNER`
* `SERVICE_PROVIDER`
* `ADMIN`

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

UI (Compose Screens)
↓
ViewModel (state management)
↓
Repository (Firebase + backend API)
↓
Data Models

## Git Workflow

* `main` = stable branch
* Each feature = separate branch
* Pull → branch → develop → commit → push → PR → merge

##  Important Development Rules

* Always follow the current development context and focus on the active layer (Android, Backend, or Database)
* Write clean, modular, and scalable code that fits the existing architecture
* Follow MVVM architecture for Android components
* Use proper data models and avoid hardcoded values
* Keep separation of concerns (UI / ViewModel / Repository / Data)
* Prefer reusable and maintainable solutions over quick fixes
* Do not assume missing requirements — ask for clarification if needed
* When working on a specific layer, do not implement other layers unless explicitly requested
* Ensure all code aligns with the overall system architecture described above
calable code

##  Current Development Focus

We are currently at the **initial development stage**.

The Android project has been created and connected to GitHub, but no features are implemented yet.


## Project History & Status

### 2026-04-11 — Initial Setup

* Created Android project (Pet4You) in Android Studio
* Initialized git repository and connected to GitHub
* Pushed initial commit (Compose starter project)
* Defined architecture and features

---

> Update this file after every milestone.
