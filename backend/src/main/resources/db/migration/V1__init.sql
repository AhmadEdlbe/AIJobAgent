-- AI Job Agent Backend - Initial Schema (PostgreSQL)
-- Compatible with JPA entities; used when Flyway enabled (ddl-auto: validate)
-- For MVP ddl-auto: update remains, but this file documents production schema.

CREATE TABLE IF NOT EXISTS user_profiles (
    id VARCHAR(255) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    linked_in_url VARCHAR(1024),
    git_hub_url VARCHAR(1024),
    resume_text TEXT,
    resume_file_path VARCHAR(1024),
    resume_file_name VARCHAR(255),
    preferred_countries TEXT,
    preferred_job_titles TEXT,
    skills TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobs (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    company VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    country VARCHAR(255),
    work_mode VARCHAR(50),
    seniority VARCHAR(50),
    tech_stacks TEXT,
    source VARCHAR(50),
    url TEXT,
    salary_min INTEGER,
    salary_max INTEGER,
    currency VARCHAR(10) DEFAULT 'USD',
    posted_at TIMESTAMP,
    requirements TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    match_percentage INTEGER DEFAULT 0,
    matching_skills TEXT,
    missing_skills TEXT,
    experience_fit VARCHAR(500),
    salary_fit VARCHAR(500),
    why_matches TEXT,
    why_not_matches TEXT,
    analyzed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_match ON jobs(match_percentage);
CREATE INDEX IF NOT EXISTS idx_source ON jobs(source);
CREATE INDEX IF NOT EXISTS idx_posted ON jobs(posted_at);

CREATE TABLE IF NOT EXISTS applications (
    id VARCHAR(255) PRIMARY KEY,
    job_id VARCHAR(255) NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    applied_at TIMESTAMP,
    interview_date TIMESTAMP,
    notes TEXT,
    cover_letter_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_id ON applications(job_id);
CREATE INDEX IF NOT EXISTS idx_status ON applications(status);

CREATE TABLE IF NOT EXISTS cover_letters (
    id VARCHAR(255) PRIMARY KEY,
    job_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    generated_at TIMESTAMP,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_cover ON cover_letters(job_id);

CREATE TABLE IF NOT EXISTS interview_preps (
    id VARCHAR(255) PRIMARY KEY,
    job_id VARCHAR(255) UNIQUE NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    questions_json TEXT,
    generated_at TIMESTAMP,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS devices (
    device_id VARCHAR(255) PRIMARY KEY,
    device_name VARCHAR(255),
    api_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP,
    last_seen TIMESTAMP
);

-- Seed singleton profile placeholder (optional)
INSERT INTO user_profiles (id, full_name, email, skills, created_at, updated_at)
VALUES ('singleton', '', '', '[]', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
