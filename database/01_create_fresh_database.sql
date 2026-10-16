-- Academia One: NEW EMPTY database (no accounts, subjects, grades or schedules).
-- This creates a SEPARATE database. Existing databases are not modified.
CREATE DATABASE IF NOT EXISTS academia_one_fresh
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE academia_one_fresh;

CREATE TABLE IF NOT EXISTS students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(200),
  school_student_id VARCHAR(60),
  program VARCHAR(150),
  year_level VARCHAR(30),
  section VARCHAR(50),
  email VARCHAR(200),
  mobile VARCHAR(60),
  address TEXT,
  gender VARCHAR(50),
  profile_picture VARCHAR(200),
  major VARCHAR(160) NOT NULL DEFAULT ''
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS subjects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL,
  default_units INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS curriculum_entries (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  year_level INT NOT NULL,
  semester INT NOT NULL,
  units INT NOT NULL,
  UNIQUE KEY uq_curriculum(student_id, subject_id, year_level, semester),
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS class_schedules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NULL,
  original_subject VARCHAR(255) NOT NULL,
  year_level INT NOT NULL,
  semester INT NOT NULL,
  day_expression VARCHAR(120) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  room VARCHAR(150),
  class_type VARCHAR(60),
  instructor VARCHAR(200),
  units INT NOT NULL DEFAULT 3,
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_grades (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  grade_value VARCHAR(30) NOT NULL,
  UNIQUE KEY uq_grade(student_id, subject_id),
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS legacy_archive (
  document_key VARCHAR(255) PRIMARY KEY,
  content LONGBLOB NOT NULL
) ENGINE=InnoDB;

-- Verify all data tables are empty:
SELECT COUNT(*) AS student_accounts FROM students;
SELECT COUNT(*) AS subjects_count FROM subjects;
SELECT COUNT(*) AS schedules_count FROM class_schedules;
