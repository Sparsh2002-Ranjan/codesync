-- ── Execution Jobs ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS execution_jobs (
    job_id            VARCHAR(36)   NOT NULL PRIMARY KEY,
    user_id           VARCHAR(36)   NOT NULL,
    project_id        VARCHAR(36),
    file_id           VARCHAR(36),
    language          VARCHAR(50)   NOT NULL,
    code              MEDIUMTEXT    NOT NULL,
    stdin             TEXT,
    status            VARCHAR(20)   NOT NULL DEFAULT 'QUEUED',
    stdout            MEDIUMTEXT,
    stderr            MEDIUMTEXT,
    exit_code         INT,
    execution_time_ms BIGINT,
    created_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    started_at        DATETIME(3),
    finished_at       DATETIME(3),
    container_id      VARCHAR(128),
    INDEX idx_exec_jobs_user    (user_id),
    INDEX idx_exec_jobs_project (project_id),
    INDEX idx_exec_jobs_status  (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Supported Languages ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS execution_supported_languages (
    id              VARCHAR(50)   NOT NULL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    version         VARCHAR(50),
    extension       VARCHAR(20),
    sandbox_image   VARCHAR(200)  NOT NULL,
    is_enabled      TINYINT(1)    NOT NULL DEFAULT 1,
    run_command     TEXT,
    compile_command TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed languages — FIX 4: compile/run columns corrected for Rust and C++
INSERT IGNORE INTO execution_supported_languages
    (id, name, version, extension, sandbox_image, is_enabled, run_command, compile_command)
VALUES
    ('python',     'Python',     '3.11',    '.py',   'python:3.11-slim',  1, 'python {file}',                          NULL),
    ('javascript', 'JavaScript', 'Node 20', '.js',   'node:20-slim',      1, 'node {file}',                            NULL),
    ('typescript', 'TypeScript', '5.x',     '.ts',   'node:20-slim',      1, 'npx ts-node {file}',                     NULL),
    ('java',       'Java',       '21',      '.java', 'openjdk:21-slim',   1, 'java {classname}',                       'javac {file}'),
    ('go',         'Go',         '1.21',    '.go',   'golang:1.21-slim',  1, 'go run {file}',                          NULL),
    ('rust',       'Rust',       '1.75',    '.rs',   'rust:1.75-slim',    1, '/tmp/program',                           'rustc {file} -o /tmp/program'),
    ('cpp',        'C++',        'C++17',   '.cpp',  'gcc:13-slim',       1, '/tmp/program',                           'g++ -std=c++17 {file} -o /tmp/program'),
    ('kotlin',     'Kotlin',     '1.9',     '.kt',   'openjdk:21-slim',   1, 'java -jar /tmp/program.jar',             'kotlinc {file} -include-runtime -d /tmp/program.jar');
