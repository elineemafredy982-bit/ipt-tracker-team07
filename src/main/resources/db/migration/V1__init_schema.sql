CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL
        CHECK (role IN ('ADMIN','STUDENT','INDUSTRIAL_SUPERVISOR','ACADEMIC_SUPERVISOR')),
    phone         VARCHAR(30),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE organisations (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    sector        VARCHAR(100) NOT NULL,
    region        VARCHAR(100) NOT NULL,
    address       VARCHAR(255),
    contact_email VARCHAR(150)
);

CREATE TABLE placements (
    id                       BIGSERIAL PRIMARY KEY,
    student_id               BIGINT       NOT NULL REFERENCES users (id),
    organisation_id          BIGINT       NOT NULL REFERENCES organisations (id),
    industrial_supervisor_id BIGINT       NOT NULL REFERENCES users (id),
    academic_supervisor_id   BIGINT       NOT NULL REFERENCES users (id),
    start_date               DATE         NOT NULL,
    end_date                 DATE         NOT NULL,
    department               VARCHAR(100) NOT NULL,
    status                   VARCHAR(20)  NOT NULL
        CHECK (status IN ('ACTIVE','COMPLETED','CANCELLED')),
    CONSTRAINT ck_placement_dates CHECK (start_date < end_date)
);
CREATE INDEX idx_placements_student ON placements (student_id);

CREATE TABLE logbook_entries (
    id                 BIGSERIAL PRIMARY KEY,
    placement_id       BIGINT        NOT NULL REFERENCES placements (id),
    entry_date         DATE          NOT NULL,
    tasks_done         VARCHAR(2000) NOT NULL,
    skills_learned     VARCHAR(2000),
    hours_worked       INTEGER       NOT NULL,
    status             VARCHAR(20)   NOT NULL
        CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED')),
    supervisor_comment VARCHAR(1000),
    submitted_at       TIMESTAMPTZ,
    reviewed_at        TIMESTAMPTZ,
    CONSTRAINT uq_entry_per_day UNIQUE (placement_id, entry_date),
    CONSTRAINT ck_hours CHECK (hours_worked BETWEEN 1 AND 12)
);
CREATE INDEX idx_entries_status ON logbook_entries (status);

CREATE TABLE assessments (
    id           BIGSERIAL PRIMARY KEY,
    placement_id BIGINT        NOT NULL REFERENCES placements (id),
    assessor_id  BIGINT        NOT NULL REFERENCES users (id),
    visit_date   DATE          NOT NULL,
    score        INTEGER       NOT NULL CHECK (score BETWEEN 0 AND 100),
    remarks      VARCHAR(2000)
);
