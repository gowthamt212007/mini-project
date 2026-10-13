-- Optional manual sample data for a freshly created, empty schema only.
-- Do not import after DemoDataSeeder has populated the routes and buses.
INSERT INTO transport_routes (id, code, name, active) VALUES
	(1, 'NORTH-01', 'North Campus Loop', TRUE),
	(2, 'EAST-02', 'East Connector', TRUE);

INSERT INTO route_stops (id, route_id, name, stop_order, travel_minutes_from_previous) VALUES
	(1, 1, 'Maple Street', 1, 0),
	(2, 1, 'Central Library', 2, 8),
	(3, 1, 'North Gate', 3, 12),
	(4, 2, 'Riverside Park', 1, 0),
	(5, 2, 'Market Square', 2, 10),
	(6, 2, 'East Gate', 3, 9);

INSERT INTO buses (id, bus_number, registration_number, capacity, status, route_id, driver_id) VALUES
	(1, 'Campus 01', 'DEMO-001', 40, 'AVAILABLE', 1, NULL),
	(2, 'Campus 02', 'DEMO-002', 32, 'AVAILABLE', 2, NULL),
	(3, 'Campus 03', 'DEMO-003', 32, 'AVAILABLE', 1, NULL),
	(4, 'Campus 04', 'DEMO-004', 36, 'AVAILABLE', 2, NULL),
	(5, 'Campus 05', 'DEMO-005', 40, 'AVAILABLE', 1, NULL),
	(6, 'Campus 06', 'DEMO-006', 32, 'AVAILABLE', 2, NULL),
	(7, 'Campus 07', 'DEMO-007', 36, 'AVAILABLE', 1, NULL),
	(8, 'Campus 08', 'DEMO-008', 40, 'AVAILABLE', 2, NULL),
	(9, 'Campus 09', 'DEMO-009', 32, 'AVAILABLE', 1, NULL),
	(10, 'Campus 10', 'DEMO-010', 36, 'AVAILABLE', 2, NULL),
	(11, 'Campus 11', 'DEMO-011', 40, 'AVAILABLE', 1, NULL),
	(12, 'Campus 12', 'DEMO-012', 32, 'AVAILABLE', 2, NULL),
	(13, 'Campus 13', 'DEMO-013', 36, 'AVAILABLE', 1, NULL),
	(14, 'Campus 14', 'DEMO-014', 40, 'AVAILABLE', 2, NULL),
	(15, 'Campus 15', 'DEMO-015', 32, 'AVAILABLE', 1, NULL),
	(16, 'Campus 16', 'DEMO-016', 36, 'AVAILABLE', 2, NULL),
	(17, 'Campus 17', 'DEMO-017', 40, 'AVAILABLE', 1, NULL),
	(18, 'Campus 18', 'DEMO-018', 32, 'AVAILABLE', 2, NULL),
	(19, 'Campus 19', 'DEMO-019', 36, 'AVAILABLE', 1, NULL),
	(20, 'Campus 20', 'DEMO-020', 40, 'AVAILABLE', 2, NULL);

-- A secure administrator account is created only when INITIAL_ADMIN_PASSWORD is configured.
