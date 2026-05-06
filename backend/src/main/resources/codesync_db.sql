-- ══════════════════════════════════════════════════════════════
--  CodeSync Database Schema
--  Run this in MySQL to create the database and all tables
--  OR just start the Spring Boot app — JPA auto-creates tables
-- ══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS codesync_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE codesync_db;

-- ── 1. Users (Auth Service) ────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id       VARCHAR(36)  NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100),
    role          ENUM('DEVELOPER','ADMIN') NOT NULL DEFAULT 'DEVELOPER',
    avatar_url    VARCHAR(500),
    provider      ENUM('LOCAL','GITHUB','GOOGLE') NOT NULL DEFAULT 'LOCAL',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bio           VARCHAR(500),
    INDEX idx_users_email    (email),
    INDEX idx_users_username (username),
    INDEX idx_users_role     (role)
);

-- ── 2. Projects (Project Service) ─────────────────────────────
CREATE TABLE IF NOT EXISTS projects (
    project_id    VARCHAR(36)  NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    owner_id      VARCHAR(36)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    language      VARCHAR(50)  NOT NULL,
    visibility    ENUM('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PRIVATE',
    template_id   VARCHAR(36),
    is_archived   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    star_count    INT NOT NULL DEFAULT 0,
    fork_count    INT NOT NULL DEFAULT 0,
    forked_from_id VARCHAR(36),
    INDEX idx_projects_owner      (owner_id),
    INDEX idx_projects_visibility (visibility),
    INDEX idx_projects_language   (language)
);

-- ── 3. Project Members ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS project_members (
    member_id   VARCHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    project_id  VARCHAR(36) NOT NULL,
    user_id     VARCHAR(36) NOT NULL,
    member_role ENUM('OWNER','EDITOR','VIEWER') NOT NULL DEFAULT 'EDITOR',
    joined_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_project_member (project_id, user_id),
    INDEX idx_pm_project (project_id),
    INDEX idx_pm_user    (user_id)
);

-- ── 4. Project Stars ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS project_stars (
    star_id    VARCHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    user_id    VARCHAR(36) NOT NULL,
    starred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_project_star (project_id, user_id)
);

-- ── 5. Code Files (File Service) ──────────────────────────────
CREATE TABLE IF NOT EXISTS code_files (
    file_id       VARCHAR(36)  NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    project_id    VARCHAR(36)  NOT NULL,
    name          VARCHAR(200) NOT NULL,
    path          VARCHAR(500) NOT NULL,
    language      VARCHAR(50),
    content       LONGTEXT,
    size          BIGINT DEFAULT 0,
    is_folder     BOOLEAN NOT NULL DEFAULT FALSE,
    parent_path   VARCHAR(500),
    created_by_id VARCHAR(36) NOT NULL,
    last_edited_by VARCHAR(36),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_files_project   (project_id),
    INDEX idx_files_path      (project_id, path),
    INDEX idx_files_deleted   (is_deleted)
);

-- ── 6. Collab Sessions (Collab Service) ───────────────────────
CREATE TABLE IF NOT EXISTS collab_sessions (
    session_id           VARCHAR(36)  NOT NULL PRIMARY KEY,
    project_id           VARCHAR(36)  NOT NULL,
    file_id              VARCHAR(36)  NOT NULL,
    owner_id             VARCHAR(36)  NOT NULL,
    status               ENUM('ACTIVE','ENDED') NOT NULL DEFAULT 'ACTIVE',
    language             VARCHAR(50),
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at             DATETIME,
    max_participants     INT NOT NULL DEFAULT 10,
    is_password_protected BOOLEAN NOT NULL DEFAULT FALSE,
    session_password     VARCHAR(100),
    last_activity_at     DATETIME,
    INDEX idx_sessions_project (project_id),
    INDEX idx_sessions_status  (status)
);

-- ── 7. Session Participants ────────────────────────────────────
CREATE TABLE IF NOT EXISTS session_participants (
    participant_id VARCHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    session_id     VARCHAR(36) NOT NULL,
    user_id        VARCHAR(36) NOT NULL,
    role           ENUM('HOST','EDITOR','VIEWER') NOT NULL DEFAULT 'EDITOR',
    joined_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at        DATETIME,
    cursor_line    INT DEFAULT 1,
    cursor_col     INT DEFAULT 1,
    color          VARCHAR(20),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_participants_session (session_id),
    INDEX idx_participants_user    (user_id)
);

-- ── 8. Execution Jobs (Execution Service) ─────────────────────
CREATE TABLE IF NOT EXISTS execution_jobs (
    job_id           VARCHAR(36) NOT NULL PRIMARY KEY,
    project_id       VARCHAR(36),
    file_id          VARCHAR(36),
    user_id          VARCHAR(36) NOT NULL,
    language         VARCHAR(50) NOT NULL,
    source_code      LONGTEXT    NOT NULL,
    stdin            TEXT,
    status           ENUM('QUEUED','RUNNING','COMPLETED','FAILED','TIMED_OUT','CANCELLED') NOT NULL DEFAULT 'QUEUED',
    stdout           LONGTEXT,
    stderr           TEXT,
    exit_code        INT,
    execution_time_ms BIGINT,
    memory_used_kb   BIGINT,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at     DATETIME,
    INDEX idx_jobs_user    (user_id),
    INDEX idx_jobs_project (project_id),
    INDEX idx_jobs_status  (status)
);

-- ── 9. Supported Languages ────────────────────────────────────
CREATE TABLE IF NOT EXISTS supported_languages (
    language_id   VARCHAR(50)  NOT NULL PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL,
    version       VARCHAR(20)  NOT NULL,
    extension     VARCHAR(10)  NOT NULL,
    sandbox_image VARCHAR(100) NOT NULL,
    is_enabled    BOOLEAN NOT NULL DEFAULT TRUE
);

-- ── 10. Snapshots (Version Service) ───────────────────────────
CREATE TABLE IF NOT EXISTS snapshots (
    snapshot_id        VARCHAR(36)  NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    project_id         VARCHAR(36)  NOT NULL,
    file_id            VARCHAR(36)  NOT NULL,
    author_id          VARCHAR(36)  NOT NULL,
    message            VARCHAR(300) NOT NULL,
    content            LONGTEXT     NOT NULL,
    hash               VARCHAR(64)  NOT NULL,
    parent_snapshot_id VARCHAR(36),
    branch             VARCHAR(100) NOT NULL DEFAULT 'main',
    tag                VARCHAR(50),
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_snapshots_file    (file_id),
    INDEX idx_snapshots_project (project_id),
    INDEX idx_snapshots_branch  (branch),
    INDEX idx_snapshots_hash    (hash)
);

-- ── 11. Branches ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS branches (
    branch_id        VARCHAR(36)  NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    project_id       VARCHAR(36)  NOT NULL,
    name             VARCHAR(100) NOT NULL,
    head_snapshot_id VARCHAR(36),
    created_by_id    VARCHAR(36)  NOT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_branch (project_id, name),
    INDEX idx_branches_project (project_id)
);

-- ── 12. Comments (Comment Service) ────────────────────────────
CREATE TABLE IF NOT EXISTS comments (
    comment_id        VARCHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    project_id        VARCHAR(36) NOT NULL,
    file_id           VARCHAR(36) NOT NULL,
    author_id         VARCHAR(36) NOT NULL,
    content           TEXT        NOT NULL,
    line_number       INT         NOT NULL,
    column_number     INT         DEFAULT 1,
    parent_comment_id VARCHAR(36),
    resolved          BOOLEAN NOT NULL DEFAULT FALSE,
    snapshot_id       VARCHAR(36),
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comments_file    (file_id),
    INDEX idx_comments_project (project_id),
    INDEX idx_comments_line    (file_id, line_number)
);

-- ── 13. Notifications (Notification Service) ──────────────────
CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36)  NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    recipient_id    VARCHAR(36)  NOT NULL,
    actor_id        VARCHAR(36)  NOT NULL,
    type            ENUM('SESSION_INVITE','PARTICIPANT_JOINED','PARTICIPANT_LEFT',
                         'COMMENT','MENTION','SNAPSHOT','FORK','BROADCAST') NOT NULL,
    title           VARCHAR(200) NOT NULL,
    message         TEXT         NOT NULL,
    related_id      VARCHAR(36),
    related_type    VARCHAR(50),
    deep_link_url   VARCHAR(500),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifs_recipient (recipient_id),
    INDEX idx_notifs_read      (recipient_id, is_read)
);

-- ── Seed supported languages ───────────────────────────────────
INSERT IGNORE INTO supported_languages VALUES
('python',     'Python',     '3.11',    '.py',    'python:3.11-slim',    TRUE),
('java',       'Java',       '21',      '.java',  'openjdk:21-slim',     TRUE),
('javascript', 'JavaScript', 'Node 20', '.js',    'node:20-slim',        TRUE),
('typescript', 'TypeScript', '5.x',     '.ts',    'node:20-slim',        TRUE),
('go',         'Go',         '1.21',    '.go',    'golang:1.21-slim',    TRUE),
('rust',       'Rust',       '1.75',    '.rs',    'rust:1.75-slim',      TRUE),
('cpp',        'C++',        'C++17',   '.cpp',   'gcc:13-slim',         TRUE),
('c',          'C',          'C17',     '.c',     'gcc:13-slim',         TRUE),
('kotlin',     'Kotlin',     '1.9',     '.kt',    'openjdk:21-slim',     TRUE),
('php',        'PHP',        '8.3',     '.php',   'php:8.3-cli',         TRUE),
('ruby',       'Ruby',       '3.3',     '.rb',    'ruby:3.3-slim',       TRUE),
('r',          'R',          '4.3',     '.r',     'r-base:4.3',          TRUE),
('swift',      'Swift',      '5.9',     '.swift', 'swift:5.9-slim',      FALSE);
