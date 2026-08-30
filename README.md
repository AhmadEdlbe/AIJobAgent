# AI Job Agent — Production-Ready Personal Job Search Assistant

> Single-user on-device AI job search. No registration, PIN + Biometric gate, encrypted local storage. Backend optional for heavy AI & multi-source aggregation.

## Tech Stack

| Layer | Tech |
|-------|------|
| **Android** | Kotlin, Jetpack Compose (Material3), MVVM, Hilt, Room, Navigation3, WorkManager, DataStore, Security Crypto |
| **Backend** | Spring Boot 3.2, Java 17, PostgreSQL, Spring Security (JWT), JPA, WebFlux (OpenAI), Jsoup (scraping), Scheduled scan |
| **AI** | OpenAI `gpt-4o-mini` (backend + direct client fallback) + heuristic fallback offline |
| **Security** | EncryptedSharedPreferences (AES256), EncryptedFile (resumes), SHA-256 PIN, BiometricPrompt |
| **Infra** | Docker & docker-compose, H2 for tests |

## Folder Structure

```
/
├── app/                           # Android module
│   └── src/main/java/com/example/aijobagent/
│       ├── AIJobAgentApp.kt
│       ├── MainActivity.kt
│       ├── core/
│       │   ├── config/BackendConfig.kt
│       │   ├── security/ PinManager, BiometricHelper, EncryptedPrefs, EncryptedFileManager
│       │   └── worker/ JobScanWorker, NotificationHelper
│       ├── data/
│       │   ├── local/ AppDatabase, Converters, dao/*, entity/*
│       │   ├── remote/ api/BackendApiService, OpenAiApiService, dto/*, mapper/*
│       │   └── repository/ *Impl.kt
│       ├── domain/ model/*, repository/*, usecase/*
│       ├── di/ DatabaseModule, AppModule, NetworkModule
│       ├── presentation/ navigation/*, security/*, dashboard/*, profile/*, jobs/*, tracker/*, coverletter/*, interview/*, settings/*, main/*
│       └── ui/theme/*
├── backend/                       # Spring Boot module
│   ├── src/main/java/com/aijobagent/
│   │   ├── AiJobAgentBackendApplication.java
│   │   ├── config/ SecurityConfig, WebConfig
│   │   ├── security/ JwtTokenProvider, JwtAuthFilter
│   │   ├── controller/ Auth, Profile, Job, Ai, Application, Dashboard
│   │   ├── service/ MappingService, OpenAiService, JobSearchService, ProfileService, ApplicationService, AiService, DailyScanScheduler, provider/*
│   │   ├── repository/ JobRepository, ApplicationRepository, ...
│   │   ├── entity/ JobEntity, UserProfileEntity, ...
│   │   ├── dto/ JobDto, ApplicationDto, ...
│   │   └── exception/ GlobalExceptionHandler
│   └── src/main/resources/application.yml
├── docker-compose.yml             # postgres + backend
├── docs/                          # architecture, db, api, plan
└── README.md
```

## Database Schema

- **Android Room v3** (`ai_job_agent_db`): `user_profile` (singleton id=1), `jobs`, `applications`, `cover_letters`, `interview_preps`. See `docs/DATABASE_SCHEMA.md`.
- **Backend PostgreSQL**: same entities plus `devices` for JWT. JPA `update` ddl, indexes on `matchPercentage`, `source`, `status`. Encrypted prefs complement.

## API Design

- **Local repositories** (Kotlin Flow/suspend): `ProfileRepository`, `JobRepository` (filter + scan), `ApplicationRepository`, `AiRepository`. See `docs/API_DESIGN.md`.
- **Backend REST** (`/api/v1`):
  - `POST /auth/register` `{deviceId, deviceName}` → JWT
  - `POST /auth/refresh`
  - `GET|PUT /profile`, `POST /profile/resume` (multipart)
  - `GET /jobs?q=&workMode=&seniority=&source=&minMatch=&page=&size=`, `GET /jobs/{id}`, `GET /jobs/high-match`, `POST /jobs/scan`, `PATCH /jobs/{id}/favorite`
  - `POST /ai/analyze/{jobId}`, `POST /ai/cover-letter/{jobId}`, `POST /ai/interview-prep/{jobId}`
  - `GET|POST /applications`, `PATCH /applications/{id}/status`, `DELETE /applications/{id}`
  - `GET /dashboard/stats`
  - `POST /chat/completions` direct fallback (client) when key set.

## Key Features

1. **Security**: No login, PIN (SHA-256 DataStore) + Biometric, EncryptedSharedPreferences, EncryptedFile for resume PDF, Room file encryptable via SQLCipher hook.
2. **Profile**: Single row `id=1`, fields: fullName/email/phone/LinkedIn/GitHub, resume PDF (encrypted), countries/titles/skills (pipe/JSON). Picker via `GetContent` + EncryptedFile.
3. **Job Search**: Aggregates LinkedIn/Indeed/Glassdoor/Wellfound/RemoteOK/WWR/Company pages. `JobSearchService` fans out to `JobProvider`s (RemoteOK live API, WWR jsoup, Wellfound mock, MockProvider). Deduplicates, analyzes, persists.
4. **AI Matching**: `OpenAiService` tries `gpt-4o-mini` → `Json` parse → heuristic fallback (skill overlap + title/country bonus). Returns `matchPercentage`, `matching/missing`, `experienceFit/salaryFit`, `whyMatches/whyNot`.
5. **Smart Filters**: Chip rows for `TechStack` (FULL_STACK/BACKEND/FRONTEND/ANDROID/JAVA/SPRING_BOOT/REACT/NODE_JS), `WorkMode`, `Seniority`, countries/sources, minMatch, search query. `JobListViewModel` combines Flow + filter.
6. **Cover Letter**: `POST /ai/cover-letter` → template or GPT, editable in `CoverLetterScreen`, `isEdited` flag, never auto-submit.
7. **Workflow**: JobFound → Analyze → MatchScore → CoverLetter → UserApproval (`PENDING_APPROVAL` → `APPLIED` manual) → Tracker.
8. **Tracker**: Statuses `SAVED, PENDING_APPROVAL, APPLIED, INTERVIEW_SCHEDULED/COMPLETED, REJECTED, OFFER_RECEIVED, WITHDRAWN`. Cards with dropdown Change Status, delete.
9. **Notifications**: `JobScanWorker` (HiltWorker, 24h periodic) → `NotificationHelper` channel `ai_job_agent_channel`. High-match count + pending approvals reminder (added). Scheduled also on backend `DailyScanScheduler` cron 9am.
10. **Dashboard**: Stats cards: total/highMatch/applied/interviews/offers + high-match list + Scan button. Combines `ApplicationRepository.getDashboardStats` + `JobRepository.getHighMatchJobs`.
11. **Interview**: 7 questions (TECHNICAL/SYSTEM_DESIGN/HR/BEHAVIORAL) tailored to missing skills, suggested answers, GPT or heuristic.

## Architecture — Clean + MVVM

```
Compose UI (Screen + ViewModel @HiltViewModel)
  ↕ StateFlow / Events
Domain (Models + Repository interfaces + UseCase)
  ↕
Data (RepositoryImpl + Room + EncryptedPrefs/File + Retrofit BackendApi/OpenAi + Workers)
```

- DI: `DatabaseModule` (Room + DAOs), `AppModule` binds repos, `NetworkModule` (Moshi + OkHttp + Retrofit backend/openai + auth interceptor).
- Navigation: `Routes` sealed `NavKey`, `NavigationState`/`Navigator`, `AppNavigation` (Lock → Main → JobDetail→CoverLetter/Interview), `MainScaffold` bottom nav.
- Workers: `AIJobAgentApp` implements `Configuration.Provider` with `HiltWorkerFactory`, schedules `PeriodicWorkRequestBuilder<JobScanWorker>(24h)`.

## Backend Highlights

- **Auth**: Single-device `DeviceEntity` (`deviceId`, `apiKey`), JWT access+refresh (`JwtTokenProvider` JJWT 0.12.5), `JwtAuthFilter` adds `ROLE_USER`. `SecurityConfig` permits all (single-user) but validates token if present, CORS `*`.
- **Job Providers**: Interface `JobProvider.fetch(ScanRequest)`. `RemoteOkProvider` calls `https://remoteok.com/api`, `WeWorkRemotelyProvider` scrapes with Jsoup, `WellfoundProvider` mock (TODO GraphQL), `MockProvider` generates 8 varied jobs. `JobSearchService` runs providers in parallel (`FixedThreadPool`), dedup, `OpenAiService.analyzeJob` per job, saves via `JobRepository` (JPA).
- **OpenAI**: `OpenAiService` uses `WebClient`, builds prompts, calls `/chat/completions`, parses JSON, fallback heuristic identical to Android. `app.openai.enabled` flag + `api-key` from env `OPENAI_API_KEY`.
- **Controllers**: Thin, delegate to services, return DTOs, `GlobalExceptionHandler` → `ApiErrorResponse {timestamp,status,error,message,path}`.
- **Scheduler**: `DailyScanScheduler` `@Scheduled(cron="0 0 9 * * *")` triggers scan for singleton profile.

## Setup

### Android

```bash
# requirements: JDK 17/25, Android Studio
./gradlew :app:assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
# optional: set OpenAI key in Profile → Advanced Settings → OpenAI API Key (encrypted)
# optional: enable backend and set Backend URL (default 10.0.2.2:8080 for emulator)
```

`local.properties`:
```
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
# optional
OPENAI_API_KEY=sk-...
```

### Backend

```bash
# env
export DATABASE_URL=jdbc:postgresql://localhost:5432/ai_job_agent
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=5367566B... # 256-bit hex
export OPENAI_API_KEY=sk-...
export OPENAI_ENABLED=true

# docker (postgres + backend)
docker compose up --build

# or run locally (needs postgres running)
cd backend
./gradlew bootRun
# API at http://localhost:8080/api/v1/auth/health
```

Test with H2:
```bash
cd backend
./gradlew test
```

## Notifications & Workers

- Android: `POST_NOTIFICATIONS` permission, `NotificationHelper.ensureChannel` creates `ai_job_agent_channel`. Worker shows high-match (`1001`) + pending reminder (`1002`). Reminder style `BigText`.
- Backend: `DailyScanScheduler` logs `{} jobs, {} high match`. TODO FCM.

## PIN + Biometric

- `PinManager` DataStore `pin_store` `{pin_hash: SHA-256, pin_enabled}`. `LockViewModel` checks `isPinSet`+`isPinEnabled` + `BiometricHelper.isBiometricAvailable()`. Auto-unlock if none. `LockScreen` PIN field + biometric button uses `BiometricPrompt` (FragmentActivity).
- `PinSetupScreen` 4-digit confirm.

## Encrypted Storage

- `EncryptedPrefs` (`MasterKey.AES256_GCM`, `EncryptedSharedPreferences.AES256_SIV/GCM`, fallback plain on emulator).
- `EncryptedFileManager` (`EncryptedFile.AES256_GCM_HKDF_4KB`) for `files/resumes/`.

## Smart Filters Implementation

- `JobFilter` data class, `JobListViewModel` holds `MutableStateFlow<JobFilter>`, `jobsFiltered` = `dao.getAllJobs().map { applyFilter }`. Toggles `toggleTech/WorkMode/Seniority`, `onSearch` debounce, `minMatchScore`.

## Application Workflow Enforcement

- `JobDetailViewModel.requestApproval()` → `ApplicationTrack(PENDING_APPROVAL)`
- `approveAndApply()` → `APPLIED` + `appliedAt=now`
- UI shows `Request Approval to Apply` / `Approve & Mark Applied` / `Save Job` based on `application.status`. No auto-submit anywhere.

## Interview Prep Example

- Technical: "Explain your experience with Kotlin..."
- System Design: "How would you design scalable system for X?"
- HR/Behavioral: "Why do you want to work at X?" + STAR.
- Tailored: if `missingSkills` not empty, adds ramp-up question.

## Docs

- `docs/ARCHITECTURE.md` — high-level + folder + security flow
- `docs/DATABASE_SCHEMA.md` — Room v3 + backend PostgreSQL
- `docs/API_DESIGN.md` — local + REST + DTOs
- `docs/IMPLEMENTATION_PLAN.md` — phase checklist + build commands

## Remaining Polish (Prod)

- Add SQLCipher: `SupportFactory(SQLiteDatabase.getBytes(passphrase))`
- PDF text extraction: `PdfRenderer` / `PDFBox` / `Apache Tika`
- Deep link `Intent.ACTION_VIEW` for job `url`
- Adaptive layout: `adaptive-navigation3` + `NavigationSuiteScaffold`
- Unit tests: `AiRepositoryMatchCalculationTest`, `DashboardStatsTest`
- Proguard for Moshi/Room/Serialization

## License

Personal single-user project.

