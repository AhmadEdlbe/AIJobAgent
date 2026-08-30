# Project Plan

Build a production-ready AI Job Agent application. 

The user originally requested a web application, but this project is for an Android application. 
Goal: Create an Android platform where users upload their CV, define job preferences, and an AI agent automatically searches for jobs, analyzes compatibility, generates cover letters, tracks applications, and provides detailed activity logs.

Features:
1. Authentication (Register, Login, JWT, Refresh Tokens)
2. User Profile (Personal Info, Skills, Experience, Education, LinkedIn, GitHub)
3. CV Management (Upload PDF/DOCX, Parse CV using AI, Save extracted data)
4. Job Preferences (Countries, Remote/Hybrid/Onsite, Tech, Salary, Job Title)
5. AI Job Search Agent (Search, Analyze, Match Score, Recommendations)
6. Cover Letter Generator (PDF/DOCX export)
7. Interview Preparation (Technical/Behavioral questions & answers)
8. Application Tracker (Saved, Applied, Interview, Offer, Rejected)
9. Activity Log (Recording all agent actions)
10. Dashboard (Summary cards and charts for applications and scores)

Tech Stack for Android:
- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM / Clean Architecture
- Networking: Retrofit / Ktor
- Database: Room (local cache)
- DI: Hilt
- AI: OpenAI API Integration
- Backend: The user suggested Spring Boot for backend, which we will assume exists or needs to be integrated with.

UI Pages to implement in Android:
- Login/Register
- Dashboard
- Profile
- CV Upload/Management
- Job Search/List
- Job Details
- Matches
- Application Tracker
- Cover Letter Generator
- Interview Prep
- Activity Logs
- Settings

Admin Panel (optional/simplified for mobile)

Quality Requirements:
- Clean Architecture, SOLID, DTOs, Unit/Integration tests.

## Project Brief

# AI Job Agent - Project Brief

## Features
1. **CV Management & AI Parsing**: Allows users to upload resumes (PDF/DOCX) and leverages AI to parse and extract skills, experience, and education into a structured, editable profile.
2. **AI Job Search & Matching**: An autonomous agent that searches for jobs based on user preferences and calculates a "Match Score" by comparing the parsed CV against job descriptions.
3. **Application Tracker**: A centralized dashboard to manage the status of job applications, tracking progress from "Saved" to "Interview" and "Offer."
4. **Activity Logs**: A transparent record of all automated agent actions, including search queries, analysis results, and recommendation logic.

## High-Level Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation Strategy**: **Jetpack Navigation 3** (State-driven navigation model for robust flow management)
- **Adaptive Strategy**: **Compose Material Adaptive** (Ensuring a seamless experience across phones, foldables, and tablets)
- **Architecture**: MVVM with Clean Architecture principles
- **Asynchronous Programming**: Coroutines & Flow
- **Dependency Injection**: Hilt
- **Networking**: Retrofit (Communication with the Spring Boot backend and AI services)
- **Local Persistence**: Room (Caching job data and user profiles for offline availability)

---
*Note: The UI Design Image section is omitted as the generation tool is currently unavailable.*

## Implementation Steps

### Task_1_BaseArchitectureAndAuth: Set up project architecture (Hilt, Retrofit, Room), Navigation 3 backbone, and implement Authentication flow (Login, Register, JWT handling).
- **Status:** DONE
- **Acceptance Criteria:**
  - Hilt dependency injection is functional
  - Room database and Retrofit clients are configured
  - Navigation 3 state-driven routing is implemented
  - Authentication screens (Login/Register) are functional
  - Build passes
- **StartTime:** 2026-08-30 08:50:31 EEST
- **EndTime:** 2026-08-30 14:30 EEST
- **Fixes Applied:**
  - Fixed NavDisplay type mismatch (entryProvider<NavKey> generic + Routes data object)
  - Replaced missing Greeting placeholder with MainScreen (`presentation/main/MainScreen.kt:1`)
  - Fixed LoginViewModel/RegisterViewModel loading state bug (isLoading set before usecase, single flow collection)
  - Implemented JWT handling: TokenManager (DataStore), AuthInterceptor, AuthResponseDto with accessToken/refreshToken, refresh endpoint, Room fallback migration
  - Fixed Login/Register LaunchedEffect navigation to avoid recomposition loops
  - Verified BUILD SUCCESSFUL and testDebugUnitTest PASSED
  - Added fallbackToDestructiveMigration for UserEntity schema change

### Task_2_ProfileAndCVManagement: Implement User Profile management and CV upload with AI parsing using OpenAI API.
- **Status:** PENDING
- **Acceptance Criteria:**
  - User profile can be viewed and edited
  - CV upload (PDF/DOCX) is functional
  - AI Parsing successfully extracts skills and experience via OpenAI
  - API_KEY for OpenAI is securely integrated
  - App does not crash during parsing

### Task_3_AIJobSearchAndDashboard: Develop the AI Job Search Agent, Job Matching logic, Dashboard, and Activity Logs.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Dashboard displays relevant job matches with 'Match Score'
  - AI Agent autonomously searches based on preferences
  - Activity Log accurately records agent actions
  - Job data is cached using Room for offline availability

### Task_4_AppTrackerAndTools: Implement Application Tracker, Cover Letter Generator, and Interview Preparation tools.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Application tracker manages status (Saved, Interview, Offer)
  - AI-generated cover letters are tailored to CV and job description
  - Interview preparation questions are generated
  - Integration with UI tools for smooth user flow

### Task_5_RunAndVerify: Refine UI using Compose Material Adaptive, perform final integration, and verify application stability.
- **Status:** PENDING
- **Acceptance Criteria:**
  - UI is adaptive across phones, tablets, and foldables
  - Project builds successfully
  - Make sure all existing tests pass
  - App does not crash
  - Critic_agent verifies stability and alignment with requirements

