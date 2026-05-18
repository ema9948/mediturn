CREATE TABLE specialties (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID         NOT NULL REFERENCES organizations(id),
    name             VARCHAR(255) NOT NULL,
    duration_minutes INT          NOT NULL DEFAULT 30,
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE doctors (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL UNIQUE REFERENCES users(id),
    organization_id UUID        NOT NULL REFERENCES organizations(id),
    license_number  VARCHAR(100),
    active          BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE doctor_specialties (
    doctor_id    UUID NOT NULL REFERENCES doctors(id),
    specialty_id UUID NOT NULL REFERENCES specialties(id),
    PRIMARY KEY (doctor_id, specialty_id)
);

CREATE TABLE doctor_schedules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id    UUID    NOT NULL REFERENCES doctors(id),
    day_of_week  INT     NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time   TIME    NOT NULL,
    end_time     TIME    NOT NULL,
    CONSTRAINT valid_schedule CHECK (start_time < end_time)
);

CREATE TABLE patients (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL UNIQUE REFERENCES users(id),
    organization_id UUID        NOT NULL REFERENCES organizations(id),
    dni             VARCHAR(20),
    birth_date      DATE,
    phone           VARCHAR(20)
);

CREATE TABLE appointments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID        NOT NULL REFERENCES organizations(id),
    patient_id      UUID        NOT NULL REFERENCES patients(id),
    doctor_id       UUID        NOT NULL REFERENCES doctors(id),
    specialty_id    UUID        NOT NULL REFERENCES specialties(id),
    datetime        TIMESTAMP   NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT valid_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED'))
);

CREATE INDEX idx_appointments_doctor_datetime ON appointments(doctor_id, datetime);
CREATE INDEX idx_appointments_patient         ON appointments(patient_id);
CREATE INDEX idx_appointments_org_status      ON appointments(organization_id, status);
