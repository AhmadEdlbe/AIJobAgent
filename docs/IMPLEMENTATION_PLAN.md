# Implementation Plan — AI Job Agent (Module by Module)

## Phase 0 — Foundation (Done)
- Update `gradle/libs.versions.toml` add `biometric 1.2.0-alpha05`, `security-crypto 1.1.0-alpha06`, `work-runtime-ktx 2.10.1`, `hilt-work 1.2.0`, `kotlinx-serialization-json 1.9.0`, `splashscreen 1.0.1`
- Update `app/build.gradle.kts` add those impls + `kotlinx-serialization-json`
- Create `docs/` folder

## Phase 1 — Security (Done)
- `core/security/PinManager` DataStore hash + `isPinSet`/`isPinEnabled` flows
- `core/security/BiometricHelper` canAuthenticate + Prompt
- `core/security/EncryptedPrefs` EncryptedSharedPreferences fallback
- `presentation/security/LockScreen` + `LockViewModel` (verify + biometric callback + auto-unlock if no PIN)
- `presentation/security/PinSetupScreen` + `PinSetupViewModel` (4-digit, confirm, save, skip)
- `AndroidManifest` add USE_BIOMETRIC, POST_NOTIFICATIONS
- `MainActivity` change to `FragmentActivity` for BiometricPrompt

## Phase 2 — Data Layer (Done)
- Domain models: `UserProfile`, `Job`+enums (`JobSource`, `WorkMode`, `Seniority`, `TechStack`), `ApplicationTrack`+`ApplicationStatus`, `CoverLetter`, `InterviewPrep`
- `Converters` pipe/csv/enum
- Entities: `UserProfileEntity`, `JobEntity`, `ApplicationEntity`, `CoverLetterEntity`, `InterviewPrepEntity` + `toDomain`/`toEntity` extensions
- DAOs with Flows + counts
- `AppDatabase` v3 `@TypeConverters(Converters::class)` + `fallbackToDestructiveMigration`
- Delete legacy `UserEntity`/`UserDao`/`TokenManager`/`AuthApiService`

## Phase 3 — Repositories & AI (Done)
- Interfaces: `ProfileRepository`, `JobRepository`, `ApplicationRepository`, `AiRepository`
- Impls: `ProfileRepositoryImpl` (Room), `JobRepositoryImpl` (Room + mock scan from 5 sources + `AiRepository.analyzeJob` per job), `ApplicationRepositoryImpl` (combine apps+jobs for dashboard, pendingApprovals), `AiRepositoryImpl` (heuristic: matchingSkills vs requirements + title/country bonus, random jitter, experience/salary fit strings, cover letter template, interview questions 7-pack)
- `NetworkModule` keep OkHttp+Retrofit for OpenAI base

## Phase 4 — DI (Done)
- `DatabaseModule` provide DB + 5 DAOs
- `AppModule` binds 4 repos
- `NetworkModule` provide logging, OkHttp, Retrofit
- `AIJobAgentApp` implements `Configuration.Provider` with `HiltWorkerFactory`, schedules `PeriodicWorkRequestBuilder<JobScanWorker>(24h)`

## Phase 5 — Workers & Notifications (Done)
- `JobScanWorker` (HiltWorker) `scanJobs()` + `NotificationHelper.showHighMatchNotification` if >=75%
- `NotificationHelper` channel `ai_job_agent_channel`, `ensureChannel`, `showReminder`
- `WorkManagerInitializer` removed via `tools:node="remove"` + Hilt factory

## Phase 6 — Presentation - Dashboard/Profile (Done)
- `DashboardViewModel` combines `getDashboardStats` + `getHighMatchJobs` + scanning flag
- `DashboardScreen` stat cards (Total/HighMatch/Applied/Interviews/Offers) + high-match list + Scan button
- `ProfileViewModel` editable `MutableStateFlow<UserProfile>` + `updateField` transform + `addSkill`/`removeSkill` + validation + save
- `ProfileScreen` fields: fullName/email/phone/LinkedIn/GitHub/resumeText + countries/titles comma split + FlowRow skill chips + Save

## Phase 7 — Presentation - Jobs & Filters (Done)
- `JobListViewModel` holds `JobFilter` StateFlow, `jobsFiltered` from `getAllJobs` + `applyFilter` helper, toggles for TechStack/WorkMode/Seniority, search query, minMatchScore, `scan()`, `toggleFavorite()`
- `JobListScreen` TopAppBar + search TextField + LazyRow filter chips (TechStack entries, WorkMode/Seniority) + LazyColumn `JobCard`
- `JobCard` card with title/company/location, source/workMode chips, description 2 lines, tech chips, LinearProgressIndicator match%, favorite icon
- `JobDetailViewModel` `setJobId` pattern ( Navigation3 SavedStateHandle workaround) + `load`, `generateCoverLetter`, `requestApproval` → PENDING_APPROVAL, `approveAndApply` → APPLIED, `saveJob` → SAVED
- `JobDetailScreen` scrollable: title/company/mode, salary, description, AI section (match%, experienceFit, salaryFit, whyMatches/whyNot, matching/missing), workflow buttons (Request Approval / Approve & Mark Applied / Save), Generate Cover Letter + preview, links to CoverLetter/Interview, Open Original Posting

## Phase 8 — Tracker / Cover Letter / Interview (Done)
- `TrackerViewModel` `applications: StateFlow<List<ApplicationTrack>>` via `getAll()`, `updateStatus`, `delete`
- `TrackerScreen` filter chips per `ApplicationStatus`, list cards with status, date, Change Status dropdown + Delete
- `CoverLetterViewModel` `setJobId`, `generate` via `AiRepository`, `onEdit`, `saveEdited` to Room
- `CoverLetterScreen` job header, editable OutlinedTextField 300dp, Save/Regenerate, edited flag, warning "Never submit automatically"
- `InterviewViewModel` similarly `generate` via `AiRepository`
- `InterviewScreen` LazyColumn cards per question category, question + suggestedAnswer, Generate/Regenerate

## Phase 9 — Navigation (Done)
- `Routes` sealed `NavKey`: Lock, PinSetup, Main, JobDetail(jobId), CoverLetter(jobId), Interview(jobId) + Dashboard/JobList/Tracker/Profile legacy top-level
- `NavigationState`/`Navigator` Navigation3 state-driven (topLevelRoute + backStacks map, `rememberNavBackStack`, `NavDisplay`)
- `AppNavigation`: Lock → auto to Main if unlocked, Main → MainScaffold, JobDetail with `LaunchedEffect(key.jobId) { vm.setJobId }`, same for CoverLetter/Interview
- `MainScaffold` bottom nav 4 tabs (Dashboard/Jobs/Tracker/Profile) state `selected` int, Box padding
- `MainScreen` legacy wrapper delegating to `MainScaffold` for backward compat
- `MainActivity` edgeToEdge + `AIJobAgentTheme` + `AppNavigation`

## Phase 10 — Verification (Done)
- `compileDebugKotlin` w/o errors (after deleting UserEntity ambiguity, adding serialization-json, fixing 6-arg combine)
- `assembleDebug` BUILD SUCCESSFUL (41 tasks, 12 executed, warnings only for deprecated ArrowBack/Divider)
- `gradle/libs.versions.toml` splashscreen downgrade 1.1.0→1.0.1 to resolve maven not found

## Phase 11 — Backend Spring Boot (Done)
- Create `backend/` Gradle project (`Spring Boot 3.2.5, Java 17, JPA, Security, WebFlux, PostgreSQL, JJWT 0.12.5, Jsoup 1.17.2`)
- Entities: `UserProfileEntity` (singleton), `JobEntity` (indexes), `ApplicationEntity`, `CoverLetterEntity`, `InterviewPrepEntity`, `DeviceEntity`
- Repositories: JPA `JobRepository.search(...) @Query`, `ApplicationRepository`, etc.
- DTOs: `UserProfileDto`, `JobDto`, `ApplicationDto`, `CoverLetterDto`, `InterviewPrepDto`, `DashboardStatsDto`, `AuthDtos`
- Services: `MappingService` (Jackson JSON), `OpenAiService` (WebClient `chat/completions` + heuristic fallback), `JobSearchService` (parallel `FixedThreadPool(4)` providers), providers `RemoteOkProvider` (live), `WeWorkRemotelyProvider` (Jsoup), `WellfoundProvider` (mock), `MockProvider`, `AiService`, `ProfileService`, `ApplicationService`, `DailyScanScheduler` (cron 9am)
- Security: `JwtTokenProvider`, `JwtAuthFilter`, `SecurityConfig` (CORS `*`, stateless, `permitAll`+JWT if present)
- Controllers: `AuthController`, `ProfileController`, `JobController`, `AiController`, `ApplicationController`, `DashboardController`, `GlobalExceptionHandler`
- Config: `application.yml` (`DATABASE_URL`, `JWT_SECRET`, `OPENAI_API_KEY`), `application-test.yml` (H2), `WebConfig`, `Dockerfile`, `docker-compose.yml` (postgres:16 + backend:8080)

## Phase 12 — Android Remote & AI Enhancement (Done)
- Create `data/remote/api/BackendApiService` + `OpenAiApiService` + `dto/*` (Moshi `JsonClass`) + `mapper/RemoteMappers` (`toDomainModel`)
- Update `di/NetworkModule` → `Moshi`, `@Named("auth")` JWT interceptor, `@Named("backendOkHttp")`/`@Named("openAiOkHttp")`, `@Named("backendRetrofit")` (`10.0.2.2:8080/api/v1/`), `@Named("openAiRetrofit")`, `BackendApiService`/`OpenAiApiService` providers
- Add `core/config/BackendConfig` (encrypted prefs for `backend_url/enabled/openai_api_key`), `core/security/EncryptedFileManager` (`EncryptedFile.AES256_GCM_HKDF_4KB`), `presentation/settings/SettingsViewModel`
- Enhance `data/repository/AiRepositoryImpl` → tiered backend→OpenAI→heuristic, `JobRepositoryImpl` → try `backendApi.scanJobs()` if enabled else mock, `JobScanWorker` → also `pendingApprovals` reminder (`1002` BigText), `NotificationHelper.showPendingApplicationsReminder`
- Enhance `presentation/profile/ProfileScreen` → `ResumePdfPicker` (GetContent `application/pdf` + `EncryptedFileManager.saveResumeFile` + text extract), `BackendSettingsSection` (Switch backend, URL field, OpenAI key `PasswordVisualTransformation` + Show/Hide)
- Verify `compileDebugKotlin` + `assembleDebug` BUILD SUCCESSFUL after `toDomain` disambiguation (`toLocalDomain` vs `toDomainModel`), missing space fix

## Phase 13 — Docs & Polish (Done)
- Update `docs/ARCHITECTURE.md` with backend modules, remote layer, security flow, expanded folder structure
- Update `docs/DATABASE_SCHEMA.md` with PostgreSQL tables + encrypted storage complement
- Update `docs/API_DESIGN.md` with implemented REST endpoints, tiered AI, notifications scheduler, security headers
- Create `README.md` comprehensive (tech stack, folder, API, setup docker & gradle, PIN/biometric, filters, workflow)
- Add `docker-compose.yml` healthcheck, backend env, volumes

## Phase 14 — Remaining Polish (Pending for prod)
- Add `SQLCipher` for Room: `SupportFactory(SQLiteDatabase.getBytes(passphrase))`
- PDF text extraction via `PdfRenderer`/`PDFBox`/`Tika` instead of `String(bytes).take(4000)`
- Deep link `Intent.ACTION_VIEW` for job `url` in `JobDetailScreen` "Open Original Posting"
- Adaptive layout: `adaptive-navigation3` + `NavigationSuiteScaffold` for tablets/foldables
- Unit tests: `ProfileRepositoryTest`, `AiRepositoryMatchCalculationTest`, `DashboardStatsTest`
- Instrumentation test: lock flow + job scan + approval workflow
- Configure `proguard-rules.pro` for Moshi/Room/Serialization
- FCM push for backend high-match (currently log only)
- Rate limit `Bucket4j` 60 req/min

## Step-by-Step Build Commands

```bash
./gradlew :app:kspDebugKotlin
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest   # requires emulator
```

## Deliverables Checklist

- [x] Complete Android project structure
- [x] Complete Spring Boot backend (`backend/` + `docker-compose.yml`) with PostgreSQL, JWT, OpenAI, providers, scheduler
- [x] Database schema (docs/DATABASE_SCHEMA.md) + Room v3 + PostgreSQL `devices`
- [x] API endpoints (docs/API_DESIGN.md) — local Flow + REST `/api/v1`
- [x] Repository layer (4 repos + impl + remote `BackendApiService`/`OpenAiApiService` + `MappingService`)
- [x] ViewModels (9 VMs: +SettingsViewModel)
- [x] Compose UI screens (Lock, PinSetup, Dashboard, Profile+PdfPicker+Settings, JobList, JobDetail, Tracker, CoverLetter, Interview, MainScaffold)
- [x] Notification system (WorkManager + `showHighMatch` + `showPendingApplicationsReminder` + backend `DailyScanScheduler`)
- [x] Background job scanner (JobScanWorker 24h + backend cron 9am + providers fan-out)
- [x] Step-by-step plan (this file) + README + docs updates
