-- ================================================================
--  韧云智护 (RENYUN) — MySQL schema
--  Run once against an empty database:
--    mysql -u root -p renyun < sql/schema.sql
-- ================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------
-- doctors
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    gender        VARCHAR(10)  DEFAULT NULL,
    title         VARCHAR(100) DEFAULT NULL,
    department    VARCHAR(100) DEFAULT NULL,
    phone         VARCHAR(30)  DEFAULT NULL,
    hospital      VARCHAR(200) DEFAULT NULL,
    speciality    VARCHAR(200) DEFAULT NULL,
    bio           TEXT         DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- patients
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    name              VARCHAR(100) NOT NULL,
    gender            VARCHAR(10)  DEFAULT NULL,
    age               INT UNSIGNED DEFAULT NULL,
    phone             VARCHAR(30)  DEFAULT NULL,
    emergency_contact VARCHAR(100) DEFAULT NULL,
    surgery_date      DATE         DEFAULT NULL,
    notes             TEXT         DEFAULT NULL,
    doctor_id         INT UNSIGNED DEFAULT NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT '康复中',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patients_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL,
    INDEX idx_patients_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- tasks — rehab exercises assigned to a patient
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tasks (
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_id  INT UNSIGNED NOT NULL,
    name        VARCHAR(100) NOT NULL,
    count       INT UNSIGNED NOT NULL DEFAULT 1,
    unit        VARCHAR(20)  NOT NULL DEFAULT '次',
    key_points  VARCHAR(255) DEFAULT NULL,
    details     TEXT         DEFAULT NULL,
    done        TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    UNIQUE KEY uniq_patient_task_name (patient_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- checkins — daily checkin/streak history
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS checkins (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_id    INT UNSIGNED NOT NULL,
    checkin_date  DATE         NOT NULL,
    done          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_checkins_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    UNIQUE KEY uniq_patient_date (patient_id, checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- records — action/motion log entries shown in patient & doctor UI
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS records (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_id    INT UNSIGNED NOT NULL,
    action        VARCHAR(100) NOT NULL,
    pitch         DECIMAL(6,2) DEFAULT NULL,
    abnormal      INT UNSIGNED NOT NULL DEFAULT 0,
    status        VARCHAR(20)  NOT NULL DEFAULT '标准',
    recorded_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_records_patient_time (patient_id, recorded_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- messages — patient <-> doctor chat
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS messages (
    id             VARCHAR(64)  PRIMARY KEY,
    from_role      VARCHAR(10)  NOT NULL,
    from_name      VARCHAR(100) DEFAULT NULL,
    to_patient_id  INT UNSIGNED NOT NULL,
    type           VARCHAR(20)  NOT NULL DEFAULT 'text',
    text           MEDIUMTEXT   DEFAULT NULL,
    msg_time       VARCHAR(10)  DEFAULT NULL,
    msg_date       DATE         DEFAULT NULL,
    is_read        TINYINT(1)   NOT NULL DEFAULT 0,
    recalled       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_patient FOREIGN KEY (to_patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_messages_patient_time (to_patient_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- reminders — doctor-set reminders for a patient
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reminders (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_id    INT UNSIGNED NOT NULL,
    remind_time   VARCHAR(10)  NOT NULL,
    label         VARCHAR(200) DEFAULT NULL,
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminders_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_reminders_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- monitor_readings — raw MQTT sensor telemetry (pitch/roll/ked)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS monitor_readings (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_id    INT UNSIGNED NOT NULL,
    mode          VARCHAR(20)  DEFAULT NULL,
    pitch         DECIMAL(6,2) DEFAULT NULL,
    roll          DECIMAL(6,2) DEFAULT NULL,
    ked           DECIMAL(6,2) DEFAULT NULL,
    recorded_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_monitor_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_monitor_patient_time (patient_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
