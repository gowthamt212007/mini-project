-- Reference MySQL 8 schema. Hibernate manages the development schema from JPA entities.
-- Run this file only against an empty database if using manual schema management.
CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE transport_routes (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(24) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE route_stops (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  route_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  stop_order INT NOT NULL,
  travel_minutes_from_previous INT NOT NULL,
  CONSTRAINT fk_stop_route FOREIGN KEY (route_id) REFERENCES transport_routes(id),
  CONSTRAINT uq_route_stop_order UNIQUE (route_id, stop_order),
  CONSTRAINT chk_stop_minutes CHECK (travel_minutes_from_previous >= 0)
) ENGINE=InnoDB;

CREATE TABLE students (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  register_number VARCHAR(40) NOT NULL UNIQUE,
  pickup_stop_id BIGINT NULL,
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_student_pickup FOREIGN KEY (pickup_stop_id) REFERENCES route_stops(id)
) ENGINE=InnoDB;

CREATE TABLE drivers (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  license_number VARCHAR(40) NOT NULL UNIQUE,
  CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE buses (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  bus_number VARCHAR(30) NOT NULL UNIQUE,
  registration_number VARCHAR(30) NOT NULL UNIQUE,
  capacity INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  route_id BIGINT NULL,
  driver_id BIGINT NULL,
  CONSTRAINT fk_bus_route FOREIGN KEY (route_id) REFERENCES transport_routes(id),
  CONSTRAINT fk_bus_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
  CONSTRAINT chk_bus_capacity CHECK (capacity > 0)
) ENGINE=InnoDB;

CREATE TABLE bus_allocations (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  bus_id BIGINT NOT NULL,
  pickup_stop_id BIGINT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  allocated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_allocation_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_allocation_bus FOREIGN KEY (bus_id) REFERENCES buses(id),
  CONSTRAINT fk_allocation_stop FOREIGN KEY (pickup_stop_id) REFERENCES route_stops(id),
  INDEX idx_bus_allocations_active (bus_id, active),
  INDEX idx_student_allocations_active (student_id, active)
) ENGINE=InnoDB;

CREATE TABLE trips (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  bus_id BIGINT NOT NULL,
  driver_id BIGINT NOT NULL,
  route_id BIGINT NOT NULL,
  current_stop_id BIGINT NULL,
  status VARCHAR(20) NOT NULL,
  started_at DATETIME NULL,
  ended_at DATETIME NULL,
  CONSTRAINT fk_trip_bus FOREIGN KEY (bus_id) REFERENCES buses(id),
  CONSTRAINT fk_trip_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
  CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES transport_routes(id),
  CONSTRAINT fk_trip_stop FOREIGN KEY (current_stop_id) REFERENCES route_stops(id)
) ENGINE=InnoDB;

CREATE TABLE transport_updates (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(24) NOT NULL,
  message VARCHAR(700) NOT NULL,
  bus_id BIGINT NULL,
  route_id BIGINT NULL,
  created_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_update_bus FOREIGN KEY (bus_id) REFERENCES buses(id),
  CONSTRAINT fk_update_route FOREIGN KEY (route_id) REFERENCES transport_routes(id),
  CONSTRAINT fk_update_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE recovery_requests (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL,
  proposed_bus_id BIGINT NULL,
  reviewed_by BIGINT NULL,
  requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_recovery_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_recovery_bus FOREIGN KEY (proposed_bus_id) REFERENCES buses(id),
  CONSTRAINT fk_recovery_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE college_events (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(140) NOT NULL,
  event_date DATE NOT NULL,
  expected_attendees INT NOT NULL,
  buses_required INT NOT NULL,
  notes VARCHAR(500) NULL,
  CONSTRAINT chk_event_attendees CHECK (expected_attendees > 0)
) ENGINE=InnoDB;
