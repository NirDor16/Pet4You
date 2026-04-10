# Pet4You - Project Context for Claude

## About the Project
Pet4You is an Android application project managed in Android Studio, written in Kotlin with Jetpack Compose.

## Tech Stack
- **Android App**: Android Studio, Kotlin, Jetpack Compose
- **Backend**: Visual Studio Code (backend code in a separate directory/repo)
- **Version Control**: Git + GitHub (`https://github.com/NirDor16/Pet4You.git`)

## Git Workflow
- `main` branch = stable production version (source of truth)
- Every feature or task is done on a **separate branch** created from `main`
- Before starting work: pull latest `main`, create a new branch
- Work on the branch, make commits
- When done: push branch to GitHub, open a Pull Request
- After review: merge into `main`
- After merge: everyone continues working from the updated `main`

**Tools used:**
- Android app code managed from **Android Studio**
- Backend code managed from **Visual Studio Code**
- Both connected to the same GitHub repository

## Project History & Status

### 2026-04-11 — Initial Setup
- Created the Android project (Pet4You) in Android Studio
- Initialized git repository locally
- Connected to GitHub: `https://github.com/NirDor16/Pet4You.git`
- Pushed initial commit with 49 files (basic Jetpack Compose project structure)
- Current state: empty starter project, no features implemented yet

---
> **Note to Claude:** Update this file after every GitHub push or significant milestone. Add a new entry under "Project History & Status" with the date and what changed. This keeps future sessions informed of where the project stands.
