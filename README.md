# Pet4You

Pet4You is a full-stack Android application for dog owners and service providers in the pet
industry — dog profiles, care reminders, social meetups, and service-provider booking, all in
one place.

## Architecture

Client–server architecture:

- **Client** — Android app (Kotlin + Jetpack Compose)
- **Backend** — Python + Flask, deployed on [Render](https://render.com)
- **Database / Auth / Storage** — Firebase (Firestore, Authentication, Storage)

The Android app never calls third-party APIs (OpenAI, OpenWeatherMap, SerpAPI) directly — all
such requests go through the Flask backend, which holds the API keys as environment variables.

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Android Studio | current stable | includes JDK 11 and the Android SDK manager |
| JDK | 11 | bundled with recent Android Studio versions |
| Android SDK | compileSdk 36, minSdk 26, targetSdk 36 | installed via Android Studio's SDK Manager |
| Gradle | 8.11.1 | downloaded automatically by the Gradle wrapper (`gradlew`) — no manual install |
| Python | 3.10+ | only needed if you want to run the backend locally |

A Firebase project is **not** required to build/run for grading — `app/google-services.json`
(Firebase client config) is already included in the repo.

## Running the Android app

The app talks to the already-deployed backend at `https://pet4you-backend.onrender.com` by
default, so **no local backend setup is required just to run the app**.

1. Clone the repository and open it in Android Studio.
2. Copy `local.properties.example` to `local.properties`. Only the `sdk.dir` entry is required
   (Android Studio usually fills this in automatically on first open).
3. Build and run:
   - From Android Studio: click **Run ▶**, or
   - From the command line: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)

Other useful Gradle tasks:
- `./gradlew testDebugUnitTest` — run JVM unit tests
- `./gradlew clean assembleDebug` — clean build, useful if an incremental build fails

## Running the backend locally (optional)

Only needed if you want to modify or test the backend. The deployed backend is used by default,
as noted above.

```bash
cd backend
python -m venv venv

# Windows
venv\Scripts\activate
# macOS / Linux
source venv/bin/activate

pip install -r requirements.txt

# copy the template and fill in real keys
cp .env.example .env
```

Fill in `backend/.env` with:
- `OPENAI_API_KEY` — for the `/chat` AI assistant route
- `API_KEY` — shared secret the Android app sends as `X-API-Key` on every request
- `OPENWEATHER_API_KEY` — for the `/weather` and `/weather-forecast` routes
- `SERP_API_KEY` — for the `/dog-parks` route

Run the backend:

```bash
python app.py          # local dev server
# or
gunicorn app:app        # production-style server
```

Run backend tests:

```bash
python -m pytest test_recommend.py -v
```

## Deployment

The backend is deployed on Render, configured via `render.yaml` at the repo root
(`rootDir: backend`, `pip install -r requirements.txt`, `gunicorn app:app`). Deployment is not
required for grading — the app already points at the live instance.

## Project structure

```
app/       Android app source (Kotlin, Jetpack Compose, MVVM)
backend/   Flask backend source
gradle/    Gradle wrapper
```
