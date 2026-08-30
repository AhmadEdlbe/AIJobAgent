# AI Job Agent — Full Architecture

## 1. Project Goal
Single-user, on-device AI job search assistant. No registration/login, PIN + Biometric gate, encrypted local storage. All personal data stays on phone. Optional Spring Boot backend for heavy AI & multi-source scraping; Android works fully offline with heuristic fallback.

## 2. High-Level Architecture (Clean + MVVM)

```
Presentation (Compose + Navigation3 + ViewModel + Hilt)
     ↕ state / events
Domain (UseCase + Repository interfaces + Models + Enums)
     ↕
Data (Repository impl + Room + EncryptedPrefs/File + Retrofit Backend/OpenAI + Workers + Ai heuristics)
     ↕
Backend (Spring Boot + PostgreSQL + JWT + OpenAI + Jsoup providers)
```

### Modules

- **core:security**
  - `PinManager` — SHA-256 hash in DataStore (`pin_store`), `isPinEnabled` flow
  - `BiometricHelper` — wrapper around `BiometricManager` + `BiometricPrompt`
  - `EncryptedPrefs` — `EncryptedSharedPreferences` with `MasterKey.AES256_GCM`, fallback to plain prefs on emulators
  - `EncryptedFileManager` — `EncryptedFile.AES256_GCM_HKDF_4KB` for `files/resumes/`, fallback plain on emulator
  - `BackendConfig` — `backend_url` (default `10.0.2.2:8080/api/v1/`), `backend_enabled`, `openai_api_key` (encrypted)

- **core:worker**
  - `JobScanWorker` (HiltWorker) — `jobRepo.scanJobs()`, triggers high-match notification if ≥75% **and** pending approvals reminder via `showPendingApplicationsReminder`
  - `NotificationHelper` — `NotificationChannel` `ai_job_agent_channel`, `showHighMatchNotification` (1001), `showPendingApplicationsReminder` (1002) with `BigTextStyle`, `showReminder`

- **data:local**
  - `AppDatabase` v3 — Room with `Converters`, `fallbackToDestructiveMigration(true)`
  - Entities: `UserProfileEntity`, `JobEntity`, `ApplicationEntity`, `CoverLetterEntity`, `InterviewPrepEntity`
  - DAOs: `ProfileDao`, `JobDao`, `ApplicationDao`, `CoverLetterDao`, `InterviewPrepDao`

- **data:remote**
  - `api/BackendApiService` — Retrofit for Spring Boot (`/jobs/scan`, `/ai/*`, `/profile`, `/applications`, `/dashboard/stats`)
  - `api/OpenAiApiService` — direct `https://api.openai.com/v1/chat/completions` fallback
  - `dto/*` — Moshi `JsonClass` mirrors backend DTOs
  - `mapper/RemoteMappers` — `JobRemoteDto.toDomainModel()` via `Instant.parse`, enum safe-parse

- **data:repository**
  - `ProfileRepositoryImpl`, `JobRepositoryImpl`, `ApplicationRepositoryImpl`, `AiRepositoryImpl`
  - `JobRepositoryImpl.scanJobs()` — if `backendEnabled` try `backendApi.scanJobs()` → `toDomainModel()` → insert; else mock 12 jobs from 5 sources → `AiRepository.analyzeJob()` per job
  - `AiRepositoryImpl` — tiered: 1) backend `analyze`/`coverLetter`/`interviewPrep` if enabled, 2) direct OpenAI with `openai_api_key`, 3) heuristic (skill overlap + title/country bonus + jitter)

- **domain:model**
  - `UserProfile` (single row id=1, lists as `joinToString("|")` in DB)
  - `Job` with `JobSource`, `WorkMode`, `Seniority`, `TechStack` enums, AI fields: `matchPercentage`, `matchingSkills`, `missingSkills`, `experienceFit`, `salaryFit`, `whyMatches/whyNotMatches`
  - `ApplicationTrack` with `ApplicationStatus` (SAVED, PENDING_APPROVAL, APPLIED, INTERVIEW_SCHEDULED/COMPLETED, REJECTED, OFFER_RECEIVED)
  - `CoverLetter`, `InterviewPrep` + `InterviewQuestion`

- **domain:repository** interfaces — `ProfileRepository`, `JobRepository`, `ApplicationRepository`, `AiRepository`

- **presentation**
  - `navigation: Routes` sealed `NavKey` + `NavigationState`/`Navigator` (Navigation3 state-driven)
  - `security: LockScreen/ViewModel`, `PinSetupScreen/ViewModel`
  - `dashboard: DashboardViewModel` combines `ApplicationRepository.getDashboardStats()` + `JobRepository.getHighMatchJobs()`
  - `profile: ProfileViewModel` editable copy pattern
  - `jobs: JobListViewModel` (filter state + `toggleTech/WorkMode/Seniority`), `JobDetailViewModel` (setJobId + approval workflow)
  - `tracker: TrackerViewModel`, `coverletter: CoverLetterViewModel`, `interview: InterviewViewModel`
  - `main: MainScaffold` bottom nav (Dashboard/Jobs/Tracker/Profile) + `AppNavigation` root stack (Lock → Main → JobDetail → CoverLetter/Interview)

- **presentation/settings**
  - `SettingsViewModel` — `backendEnabled`/`backendUrl`/`openAiKey` backed by `BackendConfig`/`EncryptedPrefs`

- **di**
  - `DatabaseModule` provides Room + DAOs
  - `AppModule` binds repos (`@Binds`)
  - `NetworkModule` provides `Moshi` + `@Named("auth")` interceptor (JWT Bearer) + `@Named("backendOkHttp")`/`@Named("openAiOkHttp")` + `@Named("backendRetrofit")` (default `10.0.2.2:8080/api/v1/`) + `@Named("openAiRetrofit")` + `BackendApiService` + `OpenAiApiService`

- **worker schedule**
  - `AIJobAgentApp : HiltAndroidApp + Configuration.Provider` injects `HiltWorkerFactory`, schedules `PeriodicWorkRequestBuilder<JobScanWorker>(24h)` with `ExistingPeriodicWorkPolicy.KEEP` and `setInitialDelay(1h)`

### Backend (Spring Boot, see `backend/`)

- **config**: `SecurityConfig` (JWT filter, CORS `*`, `permitAll` for single-user but validates if present), `WebConfig` (`WebClient.Builder`)
- **security**: `JwtTokenProvider` (JJWT 0.12.5, `secret`, `expirationMs`/`refresh`), `JwtAuthFilter` (`Authorization: Bearer`)
- **providers**: `JobProvider` interface, `RemoteOkProvider` (live `remoteok.com/api` + Jsoup text), `WeWorkRemotelyProvider` (Jsoup scrape `weworkremotely.com/categories/programming`), `WellfoundProvider` (mock 2 jobs, TODO GraphQL), `MockProvider` (8 random jobs)
- **services**: `JobSearchService` (parallel `FixedThreadPool(4)` fan-out, dedup by url, `OpenAiService.analyzeJob` per job, persist via JPA), `OpenAiService` (`WebClient` `/chat/completions`, prompt builders, JSON parse, heuristic fallback), `MappingService` (Jackson JSON for `preferredCountries` etc., CSV for techStacks), `ProfileService`, `ApplicationService` (`combine` not needed; JPA), `AiService` (`coverLetter`/`interviewPrep` generation), `DailyScanScheduler` (`@Scheduled cron 9am`)
- **controllers**: `AuthController` (`/auth/register`, `/refresh` with `DeviceEntity`), `ProfileController`, `JobController` (search via JPA `@Query`), `AiController`, `ApplicationController`, `DashboardController`, `GlobalExceptionHandler` (`ApiErrorResponse`)
- **entities**: `JobEntity` (enums `JobSource/WorkMode/Seniority` + AI fields, indexes `matchPercentage/source/postedAt`), `ApplicationEntity` (`ApplicationStatus` + indexes `jobId/status`), `CoverLetterEntity`, `InterviewPrepEntity` (JSON `questionsJson`), `UserProfileEntity` (singleton `id=singleton`, JSON text for lists), `DeviceEntity` (`deviceId`, `apiKey`)
- **infra**: `docker-compose.yml` (postgres:16 + backend:8080), `Dockerfile` (multi-stage `temurin:17`), `application.yml` (`DATABASE_URL`, `JWT_SECRET`, `OPENAI_API_KEY/ENABLED`)

## 3. Security Flow

1. App launch → `Route.Lock`
2. `LockViewModel` checks `PinManager.isPinSet` + `isPinEnabled` + `BiometricHelper.isBiometricAvailable()`
3. If no PIN or disabled → auto `unlocked = true` → navigate to `Route.Main`
4. If PIN set → user enters PIN → `PinManager.verifyPin()` (SHA-256) or biometric prompt → success → `Route.Main`
5. First run → optional `Route.PinSetup` (4-digit, confirm, SHA-256 store). Skip allowed.
6. Data encryption: Room DB file can be encrypted via `SQLCipher` (hook ready), sensitive prefs via `EncryptedSharedPreferences`, resume PDF stored in app-private encrypted dir (prefix `resumePath`).

## 4. Folder Structure

```
/
├── app/src/main/java/com/example/aijobagent/
│   ├── AIJobAgentApp.kt          (Hilt + WorkManager)
│   ├── MainActivity.kt           (FragmentActivity, edgeToEdge, AppNavigation)
│   ├── core/
│   │   ├── config/ BackendConfig
│   │   ├── security/ PinManager, BiometricHelper, EncryptedPrefs, EncryptedFileManager
│   │   └── worker/ JobScanWorker, NotificationHelper
│   ├── data/
│   │   ├── local/ AppDatabase, Converters, dao/*, entity/*
│   │   ├── remote/ api/*, dto/*, mapper/*
│   │   └── repository/ *Impl.kt
│   ├── domain/ model/*, repository/*, usecase/*
│   ├── di/ DatabaseModule, AppModule, NetworkModule
│   ├── presentation/ navigation/*, security/*, dashboard/*, profile/* (+ResumePdfPicker, BackendSettings), jobs/*, tracker/*, coverletter/*, interview/*, settings/*, main/*
│   └── ui/theme/ Theme, Color, Type
├── backend/src/main/java/com/aijobagent/
│   ├── AiJobAgentBackendApplication.java
│   ├── config/ SecurityConfig, WebConfig
│   ├── security/ JwtTokenProvider, JwtAuthFilter
│   ├── controller/ Auth, Profile, Job, Ai, Application, Dashboard
│   ├── service/ MappingService, OpenAiService, JobSearchService, ProfileService, ApplicationService, AiService, DailyScanScheduler, provider/*
│   ├── repository/ JobRepository, ApplicationRepository, CoverLetterRepository, InterviewPrepRepository, UserProfileRepository, DeviceRepository
│   ├── entity/ JobEntity, ApplicationEntity, CoverLetterEntity, InterviewPrepEntity, UserProfileEntity, DeviceEntity
│   ├── dto/ JobDto, ApplicationDto, DashboardStatsDto, ...
│   └── exception/ GlobalExceptionHandler
├── docker-compose.yml, backend/Dockerfile, backend/application.yml
└── docs/ ARCHITECTURE.md, DATABASE_SCHEMA.md, API_DESIGN.md, IMPLEMENTATION_PLAN.md
```

## 5. Key Design Decisions vs Original Template

| Before (template) | After (AI Job Agent) |
|---|---|
| Auth: Login/Register, JWT, TokenManager, AuthApiService | Removed; single-user local, PIN+Biometric, EncryptedPrefs |
| `UserEntity`/`UserDao` (multi-user) | Replaced by `UserProfileEntity` single row, `ProfileDao` |
| `AppDatabase` v2 [UserEntity] | v3 [Profile, Job, Application, CoverLetter, InterviewPrep] + TypeConverters |
| Retrofit base `api.example.com` with AuthInterceptor | Retrofit base `api.openai.com` with logging only; local AI heuristics fallback |
| Navigation Login→Main | Navigation Lock→Main (Lock auto-unlocks if no PIN), JobDetail/CoverLetter/Interview stacked |
| MainScreen placeholder | MainScaffold bottom nav + Dashboard stats |
| No workers | `JobScanWorker` + `NotificationHelper` scheduled 24h |
| Permissions only INTERNET | + USE_BIOMETRIC, POST_NOTIFICATIONS |
