# API Design

## Part A — Local (On-Device) APIs — Repository Layer

All data is local; these are Kotlin `suspend`/`Flow` contracts.

### ProfileRepository

```kotlin
interface ProfileRepository {
  fun getProfile(): Flow<UserProfile?>
  suspend fun getProfileOnce(): UserProfile?
  suspend fun saveProfile(profile: UserProfile)
  suspend fun updateResume(path: String, text: String)
}
```

### JobRepository

```kotlin
interface JobRepository {
  fun getAllJobs(): Flow<List<Job>>
  fun getHighMatchJobs(): Flow<List<Job>>
  suspend fun getJobById(id: String): Job?
  fun getJobFlowById(id: String): Flow<Job?>
  fun getFilteredJobs(filter: JobFilter): Flow<List<Job>>
  suspend fun insertJobs(jobs: List<Job>)
  suspend fun updateFavorite(id: String, isFav: Boolean)
  suspend fun deleteJob(id: String)
  suspend fun scanJobs(query: String? = null): List<Job>
}
```

Filter model:

```kotlin
data class JobFilter(
  val techStacks: Set<TechStack> = emptySet(), // FULL_STACK, BACKEND, FRONTEND, ANDROID, JAVA, SPRING_BOOT, REACT, NODE_JS
  val workModes: Set<WorkMode> = emptySet(), // REMOTE, HYBRID, ONSITE
  val seniorities: Set<Seniority> = emptySet(), // JUNIOR, MID_LEVEL, SENIOR
  val minMatchScore: Int = 0,
  val countries: Set<String> = emptySet(),
  val sources: Set<JobSource> = emptySet(),
  val searchQuery: String = ""
)
```

### ApplicationRepository

```kotlin
interface ApplicationRepository {
  fun getAll(): Flow<List<ApplicationTrack>>
  fun getByStatus(status: ApplicationStatus): Flow<List<ApplicationTrack>>
  suspend fun getById(id: String): ApplicationTrack?
  suspend fun getByJobId(jobId: String): ApplicationTrack?
  suspend fun upsert(track: ApplicationTrack)
  suspend fun updateStatus(id: String, status: ApplicationStatus)
  suspend fun delete(id: String)
  fun getDashboardStats(): Flow<DashboardStats>
}
```

### AiRepository

```kotlin
interface AiRepository {
  suspend fun analyzeJob(job: Job): Job // fills matchPercentage, matchingSkills, etc.
  suspend fun generateCoverLetter(job: Job): CoverLetter
  suspend fun generateInterviewPrep(job: Job): InterviewPrep
}
```

`AiRepositoryImpl` now tiered: 1) `backendApi.analyze/coverLetter/interviewPrep` if `BackendConfig.backendEnabled`, 2) direct `OpenAiApiService.chatCompletions` if `openai_api_key` present, 3) heuristic + random bonus (title/country +10). `JobRepositoryImpl.scanJobs()` similarly tries `backendApi.scanJobs()` first.

## Part B — Remote Spring Boot Backend — REST (Implemented, `/api/v1`)

Base `http://10.0.2.2:8080/api/v1/` (emulator) or `http://localhost:8080/api/v1/`. Android `BackendApiService` + `MappingService`.

### Auth (single-device, implemented)

```
POST /auth/register  Body: { deviceId?, deviceName? } -> TokenResponse { accessToken, refreshToken, deviceId, expiresIn }
POST /auth/refresh   Body: { refreshToken } -> TokenResponse
GET  /auth/health    -> "ok"
```
`DeviceEntity` stored, JWT `Bearer` via `JwtAuthFilter` (optional; `SecurityConfig` `permitAll` but validates if present). Refresh verifies and reissues.

### Profile

```
GET    /profile              -> UserProfileDto
PUT    /profile              Body: UserProfileDto -> UserProfileDto
POST   /profile/resume       multipart file PDF -> { path, extractedText }
```

### Jobs

```
GET    /jobs?source=linkedin,indeed&q=android&workMode=remote&techStack=java,spring_boot&seniority=senior&minMatch=75
       -> Page<JobDto>
GET    /jobs/{id}            -> JobDto
POST   /jobs/scan            Body: { query, preferredCountries, preferredTitles } -> List<JobDto>
GET    /jobs/high-match      -> List<JobDto>
```

JobDto mirrors Room entity plus `match { percentage, matchingSkills, missingSkills, experienceFit, salaryFit, whyMatches, whyNot }`.

### AI (implemented)

```
POST /ai/analyze/{jobId}        -> JobDto with match fields (via OpenAiService.analyzeJob)
POST /ai/cover-letter/{jobId}   -> CoverLetterDto
POST /ai/interview-prep/{jobId} -> InterviewPrepDto
```

`AiController` delegates to `AiService`; `OpenAiService` builds prompts, calls `WebClient` `https://api.openai.com/v1/chat/completions` (if `app.openai.enabled` + `OPENAI_API_KEY`), parses JSON `{"matchPercentage",...}`, fallback heuristic.

Example prompt (match):
```
Analyze job fit. Candidate skills: [Java, Spring Boot] titles: [Backend] countries: [UAE]
Job title: Senior Android ... Description: ... Requirements: [...]
Return JSON: {"matchPercentage":0-100, "matchingSkills":[], ...} Only return JSON.
```

### Tracker

```
GET    /applications               -> List<ApplicationDto> (with embedded Job)
GET    /applications?status=APPLIED
POST   /applications               Body: { jobId, status } -> ApplicationDto
PATCH  /applications/{id}/status  Body: { status } -> ApplicationDto
DELETE /applications/{id}
GET    /dashboard/stats            -> { totalJobsFound, highMatchJobs, applicationsSent, interviews, offers, pendingApprovals }
```

### Notifications (WorkManager + Backend Scheduler)

- Client `JobScanWorker` (24h `PeriodicWorkRequest`, `ExistingPeriodicWorkPolicy.KEEP`) → `jobRepo.scanJobs()` → `showHighMatchNotification(1001)` if ≥75% + `showPendingApplicationsReminder(1002)` via `ApplicationRepository.getDashboardStats().first().pendingApprovals` (BigTextStyle).
- Server `DailyScanScheduler` `@Scheduled(cron="0 0 9 * * *")` triggers `JobSearchService.scan()` for singleton profile, logs `{} jobs, {} high match`. TODO FCM push via `X-API-Key`.
- Channel `ai_job_agent_channel` (`IMPORTANCE_DEFAULT`, description "Job scan and application reminders").

### DTOs (Moshi)

```kotlin
@JsonClass(generateAdapter=true) data class UserProfileDto(...)
@JsonClass(generateAdapter=true) data class JobDto(
  val id: String, val title: String, val company: String, val location: String,
  val workMode: String, val seniority: String, val techStacks: List<String>,
  val source: String, val salaryMin: Int?, val salaryMax: Int?, val url: String,
  val matchPercentage: Int, val matchingSkills: List<String>, ...
)
```

### Error Format

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "...", "path": "/api/v1/jobs" }
```

### Security Headers (Spring Security implemented)

- `Authorization: Bearer <JWT>` (from `/auth/register`, stored in `EncryptedPrefs.jwt_access_token`, added by `@Named("auth")` OkHttp interceptor)
- `X-API-Key` alternative for `DeviceEntity.apiKey` (handled in `JwtAuthFilter` fallback)
- `SecurityConfig` CORS `*`, `SessionCreationPolicy.STATELESS`, `csrf.disable()`, `permitAll` (single-user) + `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`
- Rate limit: 60 req/min per device (TODO `Bucket4j`)

### OpenAI Direct (ClientFallback)

Base `https://api.openai.com/v1/`:

```
POST /chat/completions
Authorization: Bearer $OPENAI_API_KEY
Body: { model: "gpt-4o-mini", messages: [{ role: "user", content: prompt }] }
```

Heuristic path is used when key absent, keeping app functional offline.
