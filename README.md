# WizCoach (AEye-Systems)

Spring Boot app with a lightweight static frontend for workout tracking (“Motion”), nutrition logging (“Nutrition”), and an AI coach (“AI Coach”). Data is stored in Firebase/Firestore, and AI features can use Gemini (or Ollama for local testing).

## Tech stack

- **Backend**: Spring Boot 3.3.x (Java **21**), Maven
- **Frontend**: Static HTML/CSS/JS in `src/main/resources/static/`
- **Auth**: Firebase Auth (client-side) + backend token verification for protected endpoints
- **Data**: Firebase Admin SDK / Firestore
- **AI**:
  - Coach chat: Gemini SDK (`com.google.genai`)
  - Food photo analysis + nutrition suggestions: configurable (`api.server=gemini|ollama`)

## App pages

These are served as static pages by Spring Boot:

- `GET /` → redirects to `login.html`
- `GET /login.html` – sign-in
- `GET /dashboard.html`
- `GET /motion.html` – camera-based rep tracking UI
- `GET /nutrition.html` – meals + food image analysis
- `GET /ai-coach.html` – chat + optional image
- `GET /profile-setup.html`

## API overview (high-level)

- **Config**
  - `GET /api/config` – returns Firebase web config used by the frontend (`FIREBASE_*` variables)
- **AI coach**
  - `POST /api/coach/chat`
- **Nutrition logs**
  - `POST /api/nutrition-logs/analyze-image`
  - `GET/POST /api/nutrition-logs` and date/week helpers
- **Motion logs**
  - `GET/POST` endpoints under `/api/motion-logs` (see `MotionLogController`)

## Required environment variables (production)

### Firebase (server / Firestore)

Set **one** of the following:

- **`FIREBASE_CREDENTIALS_JSON`**: full Firebase service-account JSON (recommended)
- **`FIREBASE_CREDENTIALS_BASE64`**: same JSON but Base64-encoded (use if copy/paste breaks)
- **`GOOGLE_APPLICATION_CREDENTIALS`**: path to a JSON key file in the container

### Firebase (browser SDK config, used by `GET /api/config`)

Set these if the frontend uses Firebase Auth:

- `FIREBASE_API_KEY`
- `FIREBASE_AUTH_DOMAIN`
- `FIREBASE_PROJECT_ID`
- `FIREBASE_STORAGE_BUCKET`
- `FIREBASE_MESSAGING_SENDER_ID`
- `FIREBASE_APP_ID`

### Gemini / AI

- **`GEMINI_API_KEY`** (or `GOOGLE_API_KEY`) – required for Gemini features

Optional:

- `API_SERVER` – defaults to `gemini` (see `src/main/resources/application.yml`)
- `GEMINI_URL` – defaults to `https://generativelanguage.googleapis.com`
- `GEMINI_MODEL` – defaults to `gemini-3.1-flash-lite-preview`
- `OLLAMA_URL`, `OLLAMA_MODEL` – for local Ollama mode

## Run locally

Prereqs:
- Java **21**
- Maven

Run:

```bash
mvn spring-boot:run
```

Then open:

- `http://localhost:8080/`

Notes:
- For local Firestore access, put `firebase-key.json` in `src/main/resources/` (it’s gitignored).
- If you don’t set Firebase web config vars, `/api/config` will return empty strings and client auth may not work.

## Deploy to Railway

This repo includes a `Dockerfile` so Railway uses Docker builds (avoids Railpack/mise “secret api: not found” build failures).

Railway variables to set:
- `PORT` (Railway usually provides this automatically)
- `FIREBASE_CREDENTIALS_JSON` (or `FIREBASE_CREDENTIALS_BASE64`)
- `GEMINI_API_KEY`
- (optional) the `FIREBASE_*` web config vars for the frontend

## Build

Standard build:

```bash
mvn -B clean package
```

Production build (skips compiling/running tests):

```bash
mvn -B -Pproduction clean package
```

