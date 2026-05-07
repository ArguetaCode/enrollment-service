\connect student_db;

CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    student_code VARCHAR(30) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

\connect course_db;

CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course_sections (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    section_code VARCHAR(30) NOT NULL,
    max_capacity INT NOT NULL,
    reserved_seats INT NOT NULL DEFAULT 0,
    confirmed_seats INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_course_section UNIQUE(course_id, section_code),
    CONSTRAINT chk_capacity CHECK (max_capacity >= 0),
    CONSTRAINT chk_reserved CHECK (reserved_seats >= 0),
    CONSTRAINT chk_confirmed CHECK (confirmed_seats >= 0),
    CONSTRAINT chk_total_capacity CHECK ((reserved_seats + confirmed_seats) <= max_capacity)
);

CREATE TABLE IF NOT EXISTS section_schedules (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL REFERENCES course_sections(id),
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    classroom VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

\connect enrollment_db;

CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT,
    CONSTRAINT uk_student_section UNIQUE(student_id, section_id)
);

CREATE INDEX IF NOT EXISTS idx_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_section ON enrollments(section_id);
CREATE INDEX IF NOT EXISTS idx_status ON enrollments(status);
