# Database Schema

## A) Android Room v3

`AppDatabase.DATABASE_NAME = "ai_job_agent_db"`

> Encrypted via `EncryptedPrefs` + `EncryptedFileManager`; SQLCipher hook ready for `SupportFactory`.

## B) Backend PostgreSQL (Spring Data JPA, `ddl-auto: update`)

`jdbc:postgresql://localhost:5432/ai_job_agent` (docker `aija_postgres`), `spring.jpa.hibernate.ddl-auto=update`, dialect `PostgreSQLDialect`. H2 for tests (`application-test.yml`).

Backend entities mirror Room but add `devices` and JPA indexes. Lists stored as JSON (Jackson) or `|`/` ,` via `MappingService`.

`AppDatabase.DATABASE_NAME = "ai_job_agent_db"` (Room) // kept for Android

## Entities

### 1. user_profile (single row id=1)
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY | always 1 |
| fullName | TEXT | encrypted* |
| email | TEXT | encrypted* |
| phoneNumber | TEXT | encrypted* |
| linkedInUrl | TEXT | |
| gitHubUrl | TEXT | |
| resumePath | TEXT? | file path in app-private dir, encrypted |
| resumeText | TEXT? | extracted text |
| preferredCountries | TEXT | pipe `\|` delimited `Converters.fromStringList` |
| preferredJobTitles | TEXT | pipe |
| skills | TEXT | pipe |
| updatedAt | LONG | epoch millis |

*Marked encrypted in future SQLCipher layer; currently via EncryptedPrefs fallback.

```sql
CREATE TABLE user_profile (
  id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
  fullName TEXT NOT NULL,
  email TEXT NOT NULL,
  phoneNumber TEXT NOT NULL,
  linkedInUrl TEXT NOT NULL,
  gitHubUrl TEXT NOT NULL,
  resumePath TEXT,
  resumeText TEXT,
  preferredCountries TEXT NOT NULL,
  preferredJobTitles TEXT NOT NULL,
  skills TEXT NOT NULL,
  updatedAt INTEGER NOT NULL
);
```

### 2. jobs
| Column | Type |
|---|---|
| id | TEXT PK |
| title | TEXT |
| company | TEXT |
| description | TEXT |
| location | TEXT |
| country | TEXT |
| workMode | TEXT (WorkMode.name) |
| seniority | TEXT (Seniority.name) |
| techStacksCsv | TEXT (CSV TechStack.name) |
| source | TEXT (JobSource.name) |
| url | TEXT |
| salaryMin | INTEGER? |
| salaryMax | INTEGER? |
| currency | TEXT |
| postedAt | LONG |
| requirementsCsv | TEXT pipe |
| isFavorite | BOOLEAN |
| matchPercentage | INTEGER 0-100 |
| matchingSkillsCsv | TEXT pipe |
| missingSkillsCsv | TEXT pipe |
| experienceFit | TEXT |
| salaryFit | TEXT |
| whyMatches | TEXT |
| whyNotMatches | TEXT |
| analyzedAt | LONG? |

Converters: `WorkMode`, `Seniority`, `JobSource` via `Converters`; TechStack list via CSV; skills via pipe.

### 3. applications
| Column | Type |
|---|---|
| id | TEXT PK (UUID) |
| jobId | TEXT FK → jobs.id (logical, no enforcement) |
| status | TEXT (ApplicationStatus.name) |
| appliedAt | LONG? |
| interviewDate | LONG? |
| notes | TEXT |
| coverLetterId | TEXT? |
| createdAt | LONG |
| updatedAt | LONG |

Statuses: SAVED, PENDING_APPROVAL, APPLIED, INTERVIEW_SCHEDULED, INTERVIEW_COMPLETED, REJECTED, OFFER_RECEIVED, WITHDRAWN

### 4. cover_letters
| Column | Type |
|---|---|
| id | TEXT PK |
| jobId | TEXT (unique per job) |
| content | TEXT |
| generatedAt | LONG |
| isEdited | BOOLEAN |

### 5. interview_preps
| Column | Type |
|---|---|
| id | TEXT PK |
| jobId | TEXT UNIQUE |
| questionsJson | TEXT (JSON array of SerializableQuestion) |
| generatedAt | LONG |

`questionsJson` e.g. `[{"question":"...","category":"TECHNICAL","suggestedAnswer":"..."}]`

## DAOs

- **ProfileDao**: `getProfileFlow(): Flow<UserProfileEntity?>`, `getProfile(): UserProfileEntity?`, `upsert()`, `delete()`
- **JobDao**: `getAllJobs(): Flow<List<JobEntity>>`, `getJobById`, `getJobFlowById`, `getHighMatchJobs() WHERE matchPercentage>=75 ORDER BY matchPercentage DESC`, `getJobCountFlow()`, `getHighMatchCountFlow()`, `insertAll`, `updateFavorite`, `deleteById`, `clearAll`
- **ApplicationDao**: `getAll()`, `getById`, `getByJobId`, `getByStatus(status)`, `countFlow()`, `countInterviewsFlow() WHERE status IN (...)`, `countOffersFlow()`, `upsert`, `deleteById`
- **CoverLetterDao**: `getForJob(jobId)`, `getForJobFlow`, `upsert`, `deleteById`
- **InterviewPrepDao**: `getForJob`, `getForJobFlow`, `upsert`

## TypeConverters

```kotlin
class Converters {
  fun fromStringList -> joinToString("|")
  fun toStringList -> split("|")
  fun fromTechStackList -> joinToString(",") { it.name }
  fun toTechStackList -> split(",").mapNotNull { valueOf }
  fun fromWorkMode/toWorkMode, fromSeniority/toSeniority, fromJobSource/toJobSource
}
```

## Migration

- v2→v3: `fallbackToDestructiveMigration(true)` (single-user, no prod data loss concern). For production, provide `Migration(2,3)` creating new tables and copying `UserEntity` → `UserProfileEntity` if needed.

## Encrypted Storage Complement (Android)

- `PinManager` DataStore `pin_store` holds `pin_hash` (SHA-256) + `pin_enabled`
- `EncryptedPrefs` (`ai_job_agent_encrypted`) holds `biometric_enabled`, `openai_api_key` (encrypted), `backend_url`, `backend_enabled`, `jwt_access_token`, `last_scan_timestamp`
- `EncryptedFileManager` `files/resumes/*.pdf` via `EncryptedFile.AES256_GCM_HKDF_4KB`

## Backend PostgreSQL Tables (JPA)

### user_profiles
| Column | Type | Notes |
|---|---|---|
| id | VARCHAR PK | `singleton` |
| full_name | VARCHAR | |
| email | VARCHAR | |
| phone_number | VARCHAR | |
| linked_in_url | VARCHAR | |
| git_hub_url | VARCHAR | |
| resume_text | TEXT | |
| resume_file_path | VARCHAR | |
| resume_file_name | VARCHAR | |
| preferred_countries | TEXT | JSON array |
| preferred_job_titles | TEXT | |
| skills | TEXT | JSON |
| created_at | TIMESTAMP | `@CreationTimestamp` |
| updated_at | TIMESTAMP | `@UpdateTimestamp` |

### jobs (indexes: `matchPercentage`, `source`, `postedAt`)
Same columns as Room plus `is_favorite` BOOLEAN, `created_at`, `updated_at`. Enums `@Enumerated(STRING)`.

### applications (indexes: `job_id`, `status`)
| id | VARCHAR PK | UUID |
| job_id | VARCHAR | FK logical |
| status | VARCHAR | Enum SAVED... |
| applied_at | TIMESTAMP | |
| interview_date | TIMESTAMP | |
| notes | TEXT | |
| cover_letter_id | VARCHAR | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### cover_letters (index `job_id`)
| id | VARCHAR PK | UUID |
| job_id | VARCHAR | UNIQUE per job |
| content | TEXT | |
| generated_at | TIMESTAMP | |
| is_edited | BOOLEAN | |
| created_at | TIMESTAMP | |

### interview_preps (unique `job_id`)
| id | VARCHAR PK |
| job_id | VARCHAR UNIQUE |
| questions_json | TEXT | JSON `[{"question","category","suggestedAnswer"}]` |
| generated_at | TIMESTAMP |
| created_at | TIMESTAMP |

### devices
| device_id | VARCHAR PK | UUID from app |
| device_name | VARCHAR | |
| api_key | VARCHAR UNIQUE | fallback `X-API-Key` |
| created_at | TIMESTAMP | |
| last_seen | TIMESTAMP | |

## Indexes & Queries

- Index on `jobs.matchPercentage` implicit via `WHERE matchPercentage>=75`
- `applications.jobId` should have index for quick join in `ApplicationRepositoryImpl.getAll()` (in-memory `associateBy`)

## Sample Join (Application + Job)

Done in repository, not SQL join:

```kotlin
combine(dao.getAll(), jobDao.getAllJobs()) { apps, jobs ->
  val jobMap = jobs.associateBy { it.id }
  apps.map { it.toDomain().copy(job = jobMap[it.jobId]?.toDomain()) }
}
```
