package edu.college.transport.service;

import edu.college.transport.entity.*;
import edu.college.transport.dto.NearbyBusSuggestion;
import edu.college.transport.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TransportService {
    private final StudentRepository students;
    private final DriverRepository drivers;
    private final UserAccountRepository users;
    private final BusRepository buses;
    private final TransportRouteRepository routes;
    private final RouteStopRepository stops;
    private final BusAllocationRepository allocations;
    private final TripRepository trips;
    private final TransportUpdateRepository updates;
    private final RecoveryRequestRepository recoveries;
    private final CollegeEventRepository events;
    private final PasswordEncoder passwordEncoder;

    public TransportService(StudentRepository students, DriverRepository drivers, UserAccountRepository users,
            BusRepository buses, TransportRouteRepository routes, RouteStopRepository stops,
            BusAllocationRepository allocations, TripRepository trips, TransportUpdateRepository updates,
            RecoveryRequestRepository recoveries, CollegeEventRepository events, PasswordEncoder passwordEncoder) {
        this.students = students; this.drivers = drivers; this.users = users; this.buses = buses;
        this.routes = routes; this.stops = stops; this.allocations = allocations; this.trips = trips;
        this.updates = updates; this.recoveries = recoveries; this.events = events; this.passwordEncoder = passwordEncoder;
    }

    public List<TransportRoute> activeRoutes() { return routes.findByActiveTrueOrderByNameAsc(); }
    public List<TransportRoute> activeRoutes(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return activeRoutes().stream().filter(route -> q.isBlank() || route.getCode().toLowerCase().contains(q)
                || route.getName().toLowerCase().contains(q) || route.getStops().stream().anyMatch(stop -> stop.getName().toLowerCase().contains(q))).toList();
    }
    public List<TransportRoute> allRoutes(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return routes.findAll().stream().filter(route -> q.isBlank() || route.getCode().toLowerCase().contains(q)
                || route.getName().toLowerCase().contains(q) || route.getStops().stream().anyMatch(stop -> stop.getName().toLowerCase().contains(q))).toList();
    }
    public List<RouteStop> allStops() { return stops.findAll(); }
    public List<RouteStop> activeStops() { return activeRoutes().stream().flatMap(route -> route.getStops().stream()).toList(); }
    public List<Bus> allBuses() { return buses.findAll(); }
    public List<Bus> allBuses(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return allBuses().stream().filter(bus -> q.isBlank() || bus.getBusNumber().toLowerCase().contains(q)
                || bus.getRegistrationNumber().toLowerCase().contains(q) || (bus.getRoute() != null && bus.getRoute().getName().toLowerCase().contains(q))).toList();
    }
    public Page<Student> studentPage(String query, int page) {
        int safePage = Math.max(0, page);
        return students.search(query == null ? "" : query.trim(), PageRequest.of(safePage, 20, Sort.by("registerNumber").ascending()));
    }
    public List<Student> allStudents(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return students.findAll().stream().filter(s -> q.isBlank() || s.getRegisterNumber().toLowerCase().contains(q)
                || s.getUser().getFullName().toLowerCase().contains(q) || s.getUser().getUsername().toLowerCase().contains(q)).toList();
    }
    public List<Driver> allDrivers() { return drivers.findAll(); }
    public List<BusAllocation> allAllocations() { return allocations.findByActiveTrueOrderByAllocatedAtDesc(); }
    public List<Trip> recentTrips() { return trips.findTop8ByOrderByStartedAtDesc(); }
    public List<TransportUpdate> recentUpdates() { return updates.findTop12ByOrderByCreatedAtDesc(); }
    public List<RecoveryRequest> allRecoveries() { return recoveries.findAllByOrderByRequestedAtDesc(); }
    public List<RecoveryRequest> recoveriesForStudent(String username) { return recoveries.findByStudentUserUsernameOrderByRequestedAtDesc(username); }
    public List<RouteStop> stopsForRoute(Long routeId) { return stops.findByRouteIdOrderByStopOrder(routeId); }
    public Bus busForDriver(String username) { return buses.findAll().stream().filter(b -> b.getDriver() != null && b.getDriver().getUser().getUsername().equals(username)).findFirst().orElse(null); }
    public List<CollegeEvent> allEvents() { return events.findAllByOrderByEventDateAsc(); }
    public long studentCount() { return students.count(); }
    public long busCount() { return buses.count(); }
    public long routeCount() { return routes.countByActiveTrue(); }
    public long tripCountToday() { return trips.countByStartedAtBetween(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay()); }
    public long occupiedSeats(Long busId) { return allocations.countByBusIdAndActiveTrue(busId); }
    public Optional<BusAllocation> allocationFor(String username) {
        return students.findByUserUsername(username).flatMap(s -> allocations.findByStudentIdAndActiveTrue(s.getId()));
    }
    public Student studentFor(String username) { return students.findByUserUsername(username).orElseThrow(() -> new IllegalArgumentException("Student profile not found.")); }
    public Driver driverFor(String username) { return drivers.findByUserUsername(username).orElseThrow(() -> new IllegalArgumentException("Driver profile not found.")); }
    public List<Trip> driverTrips(String username) { return trips.findByDriverUserUsernameOrderByStartedAtDesc(username); }
    public Optional<Trip> activeTrip(String username) {
        return driverTrips(username).stream().filter(t -> t.getStatus() == TripStatus.IN_PROGRESS).findFirst();
    }

    public String estimatedArrival(String username) {
        Optional<BusAllocation> allocation = allocationFor(username);
        if (allocation.isEmpty()) return "No bus assigned";
        BusAllocation assigned = allocation.get();
        Optional<Trip> active = trips.findByStatus(TripStatus.IN_PROGRESS).stream()
                .filter(t -> t.getBus().getId().equals(assigned.getBus().getId())).findFirst();
        if (active.isEmpty() || active.get().getCurrentStop() == null) return "Awaiting trip update";
        List<RouteStop> ordered = stops.findByRouteIdOrderByStopOrder(assigned.getBus().getRoute().getId());
        int current = active.get().getCurrentStop().getStopOrder();
        int pickup = assigned.getPickupStop().getStopOrder();
        if (pickup <= current) return "Pickup stop reached / passed";
        int minutes = ordered.stream().filter(s -> s.getStopOrder() > current && s.getStopOrder() <= pickup)
                .mapToInt(RouteStop::getTravelMinutesFromPrevious).sum();
        return "About " + minutes + " min from latest driver update";
    }

    public List<NearbyBusSuggestion> nearbyBusSuggestions(String username) {
        Student student = studentFor(username);
        RouteStop pickup = student.getPickupStop();
        if (pickup == null || !pickup.getRoute().isActive()) return List.of();
        Long assignedBusId = allocations.findByStudentIdAndActiveTrue(student.getId()).map(a -> a.getBus().getId()).orElse(null);
        List<Trip> activeTrips = trips.findByStatus(TripStatus.IN_PROGRESS);
        return buses.findAll().stream()
                .filter(bus -> bus.getRoute() != null && bus.getRoute().isActive()
                        && bus.getRoute().getId().equals(pickup.getRoute().getId()))
                .filter(bus -> assignedBusId == null || !bus.getId().equals(assignedBusId))
                .filter(bus -> bus.getStatus() == BusStatus.AVAILABLE || bus.getStatus() == BusStatus.IN_SERVICE || bus.getStatus() == BusStatus.DELAYED)
                .filter(bus -> allocations.countByBusIdAndActiveTrue(bus.getId()) < bus.getCapacity())
                .map(bus -> {
                    Trip activeTrip = activeTrips.stream().filter(trip -> trip.getBus().getId().equals(bus.getId())).findFirst().orElse(null);
                    Integer minutes = activeTrip == null ? null : estimateMinutesToPickup(activeTrip, pickup);
                    String availability = activeTrip == null ? "Available · dispatch required" : "On route · awaiting driver stop";
                    return new NearbyBusSuggestion(bus, bus.getCapacity() - allocations.countByBusIdAndActiveTrue(bus.getId()), minutes, availability);
                })
                .sorted(Comparator.comparingInt(suggestion -> suggestion.etaMinutes() == null ? Integer.MAX_VALUE : suggestion.etaMinutes()))
                .limit(3)
                .toList();
    }

    private Integer estimateMinutesToPickup(Trip trip, RouteStop pickup) {
        RouteStop current = trip.getCurrentStop();
        if (current == null) return null;
        List<RouteStop> routeStops = stops.findByRouteIdOrderByStopOrder(trip.getRoute().getId());
        if (current.getId().equals(pickup.getId())) return 0;
        int minutes = routeStops.stream().filter(stop -> stop.getStopOrder() > current.getStopOrder())
                .mapToInt(RouteStop::getTravelMinutesFromPrevious).sum();
        minutes += routeStops.stream().filter(stop -> stop.getStopOrder() <= pickup.getStopOrder())
                .mapToInt(RouteStop::getTravelMinutesFromPrevious).sum();
        if (pickup.getStopOrder() > current.getStopOrder()) {
            return routeStops.stream().filter(stop -> stop.getStopOrder() > current.getStopOrder() && stop.getStopOrder() <= pickup.getStopOrder())
                    .mapToInt(RouteStop::getTravelMinutesFromPrevious).sum();
        }
        return minutes;
    }

    @Transactional
    public void saveBus(Long id, String number, String registration, int capacity, Long routeId, Long driverId) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least one seat.");
        Bus bus = id == null ? new Bus() : buses.findById(id).orElseThrow(() -> new IllegalArgumentException("Bus not found."));
        if (id != null && allocations.countByBusIdAndActiveTrue(id) > capacity) throw new IllegalArgumentException("Capacity cannot be lower than occupied seats.");
        TransportRoute selectedRoute = routeId == null ? null : routes.findById(routeId).orElseThrow(() -> new IllegalArgumentException("Route not found."));
        if (id != null && allocations.findByBusIdAndActiveTrue(id).stream().anyMatch(a -> selectedRoute == null || !a.getPickupStop().getRoute().getId().equals(selectedRoute.getId())))
            throw new IllegalArgumentException("Reassign students before changing this bus to a route that does not serve their pickup stops.");
        bus.setBusNumber(number.trim()); bus.setRegistrationNumber(registration.trim()); bus.setCapacity(capacity);
        bus.setRoute(selectedRoute);
        bus.setDriver(driverId == null ? null : drivers.findById(driverId).orElseThrow(() -> new IllegalArgumentException("Driver not found.")));
        if (bus.getStatus() == null) bus.setStatus(BusStatus.AVAILABLE);
        buses.save(bus);
    }

    @Transactional
    public void deactivateBus(Long id) {
        Bus bus = buses.findById(id).orElseThrow(() -> new IllegalArgumentException("Bus not found."));
        if (allocations.countByBusIdAndActiveTrue(id) > 0) throw new IllegalArgumentException("Reassign students before deactivating this bus.");
        bus.setStatus(BusStatus.INACTIVE);
    }

    @Transactional
    public void restoreBus(Long id) {
        Bus bus = buses.findById(id).orElseThrow(() -> new IllegalArgumentException("Bus not found."));
        if (trips.findByStatus(TripStatus.IN_PROGRESS).stream().anyMatch(trip -> trip.getBus().getId().equals(id)))
            throw new IllegalArgumentException("Complete or cancel the active trip before restoring this bus.");
        if (bus.getStatus() != BusStatus.BREAKDOWN && bus.getStatus() != BusStatus.DELAYED)
            throw new IllegalArgumentException("Only a delayed or breakdown bus can be restored here.");
        bus.setStatus(BusStatus.AVAILABLE);
    }

    @Transactional
    public void saveRoute(Long id, String code, String name, String stopLines) {
        String normalizedCode = code.trim().toUpperCase();
        Optional<TransportRoute> duplicate = routes.findByCode(normalizedCode);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) throw new IllegalArgumentException("That route code is already in use.");
        List<String[]> parsedStops = new java.util.ArrayList<>();
        for (String line : (stopLines == null ? new String[0] : stopLines.split("\\R"))) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\\|", 2);
            if (parts.length != 2 || parts[0].isBlank()) throw new IllegalArgumentException("Each stop line must use Stop name | minutes from previous stop.");
            int minutes;
            try { minutes = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException ex) { throw new IllegalArgumentException("Travel minutes must be a whole number."); }
            if (minutes < 0) throw new IllegalArgumentException("Travel minutes cannot be negative.");
            parsedStops.add(new String[]{parts[0].trim(), Integer.toString(minutes)});
        }
        if (parsedStops.isEmpty()) throw new IllegalArgumentException("Add at least one route stop.");
        TransportRoute route = id == null ? new TransportRoute() : routes.findById(id).orElseThrow(() -> new IllegalArgumentException("Route not found."));
        List<RouteStop> existingStops = id == null ? List.of() : stops.findByRouteIdOrderByStopOrder(id);
        if (id != null && existingStops.size() != parsedStops.size()) throw new IllegalArgumentException("Keep the existing number of stops when editing a route so student pickup assignments remain valid.");
        route.setCode(normalizedCode); route.setName(name.trim()); route.setActive(true);
        routes.saveAndFlush(route);
        if (id == null) {
            int order = 1;
            for (String[] data : parsedStops) {
                RouteStop stop = new RouteStop(); stop.setRoute(route); stop.setName(data[0]); stop.setStopOrder(order++); stop.setTravelMinutesFromPrevious(Integer.parseInt(data[1])); stops.save(stop);
            }
        } else {
            existingStops.forEach(stop -> stop.setStopOrder(stop.getStopOrder() + 1000));
            stops.saveAllAndFlush(existingStops);
            for (int index = 0; index < existingStops.size(); index++) {
                RouteStop stop = existingStops.get(index); stop.setName(parsedStops.get(index)[0]); stop.setStopOrder(index + 1);
                stop.setTravelMinutesFromPrevious(Integer.parseInt(parsedStops.get(index)[1]));
            }
            stops.saveAll(existingStops);
        }
    }

    @Transactional
    public void deactivateRoute(Long id) {
        TransportRoute route = routes.findById(id).orElseThrow(() -> new IllegalArgumentException("Route not found."));
        route.setActive(false);
    }

    @Transactional
    public void activateRoute(Long id) {
        TransportRoute route = routes.findById(id).orElseThrow(() -> new IllegalArgumentException("Route not found."));
        route.setActive(true);
    }

    @Transactional
    public void allocate(Long studentId, Long busId) {
        Student student = students.findLockedById(studentId).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        Bus bus = buses.findLockedById(busId).orElseThrow(() -> new IllegalArgumentException("Bus not found."));
        if (bus.getStatus() == BusStatus.INACTIVE || bus.getStatus() == BusStatus.BREAKDOWN || bus.getRoute() == null || !bus.getRoute().isActive())
            throw new IllegalArgumentException("This bus is unavailable for allocation.");
        RouteStop pickup = student.getPickupStop();
        if (pickup == null || !pickup.getRoute().getId().equals(bus.getRoute().getId())) throw new IllegalArgumentException("The bus route does not serve this student's pickup stop.");
        Optional<BusAllocation> existing = allocations.findByStudentIdAndActiveTrue(studentId);
        if (existing.isPresent() && existing.get().getBus().getId().equals(busId)) return;
        existing.ifPresent(a -> a.setActive(false));
        if (allocations.countByBusIdAndActiveTrue(busId) >= bus.getCapacity()) throw new IllegalArgumentException("No seats remain on this bus.");
        BusAllocation allocation = new BusAllocation(); allocation.setStudent(student); allocation.setBus(bus); allocation.setPickupStop(pickup); allocation.setActive(true);
        allocations.save(allocation);
    }

    @Transactional
    public boolean recommendAllocation(Long studentId) {
        Student student = students.findById(studentId).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        if (student.getPickupStop() == null) return false;
        Bus best = buses.findAll().stream().filter(b -> b.getRoute() != null && b.getRoute().isActive() && b.getRoute().getId().equals(student.getPickupStop().getRoute().getId()))
                .filter(b -> b.getStatus() == BusStatus.AVAILABLE && allocations.countByBusIdAndActiveTrue(b.getId()) < b.getCapacity())
                .min(Comparator.comparingLong(b -> allocations.countByBusIdAndActiveTrue(b.getId()))).orElse(null);
        if (best == null) return false;
        allocate(studentId, best.getId());
        return true;
    }

    @Transactional
    public void saveDriver(String username, String fullName, String password, String license) {
        if (users.existsByUsername(username.trim())) throw new IllegalArgumentException("Login ID already exists.");
        UserAccount user = new UserAccount(); user.setUsername(username.trim()); user.setFullName(fullName.trim()); user.setPassword(passwordEncoder.encode(password)); user.setRole(Role.DRIVER); user.setEnabled(true); users.save(user);
        Driver driver = new Driver(); driver.setUser(user); driver.setLicenseNumber(license.trim()); drivers.save(driver);
    }

    @Transactional
    public void setAccountEnabled(Long id, boolean enabled) { UserAccount user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found.")); user.setEnabled(enabled); }

    @Transactional
    public void requestRecovery(String username, String reason) {
        Student student = studentFor(username);
        RecoveryRequest request = new RecoveryRequest(); request.setStudent(student); request.setReason(reason.trim()); request.setStatus(RequestStatus.PENDING); recoveries.save(request);
    }

    @Transactional
    public void reviewRecovery(Long requestId, boolean approve, String adminUsername) {
        RecoveryRequest request = recoveries.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Request not found."));
        if (request.getStatus() != RequestStatus.PENDING) throw new IllegalArgumentException("This request has already been reviewed.");
        request.setReviewedBy(users.findByUsername(adminUsername).orElseThrow());
        if (approve) {
            if (!recommendAllocation(request.getStudent().getId())) throw new IllegalArgumentException("No suitable bus has an available seat.");
            BusAllocation newAllocation = allocations.findByStudentIdAndActiveTrue(request.getStudent().getId()).orElseThrow(() -> new IllegalArgumentException("No suitable bus has an available seat."));
            request.setProposedBus(newAllocation.getBus()); request.setStatus(RequestStatus.APPROVED);
        } else request.setStatus(RequestStatus.DECLINED);
    }

    @Transactional
    public void createUpdate(UpdateType type, String message, Long busId, Long routeId, String username) {
        TransportUpdate update = new TransportUpdate(); update.setType(type); update.setMessage(message.trim());
        update.setBus(busId == null ? null : buses.findById(busId).orElseThrow(() -> new IllegalArgumentException("Bus not found.")));
        update.setRoute(routeId == null ? null : routes.findById(routeId).orElseThrow(() -> new IllegalArgumentException("Route not found.")));
        update.setCreatedBy(users.findByUsername(username).orElseThrow()); updates.save(update);
    }

    @Transactional
    public void startTrip(String username) {
        Driver driver = driverFor(username);
        if (activeTrip(username).isPresent()) throw new IllegalArgumentException("You already have a trip in progress.");
        Bus bus = buses.findAll().stream().filter(b -> b.getDriver() != null && b.getDriver().getId().equals(driver.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No bus is assigned to your driver profile."));
        if (bus.getStatus() == BusStatus.BREAKDOWN || bus.getStatus() == BusStatus.INACTIVE) throw new IllegalArgumentException("This bus is not available to start a trip.");
        if (bus.getRoute() == null) throw new IllegalArgumentException("Assign a route to this bus before starting a trip.");
        Trip trip = new Trip(); trip.setDriver(driver); trip.setBus(bus); trip.setRoute(bus.getRoute()); trip.setStatus(TripStatus.IN_PROGRESS); trip.setStartedAt(LocalDateTime.now());
        List<RouteStop> routeStops = stops.findByRouteIdOrderByStopOrder(bus.getRoute().getId());
        if (!routeStops.isEmpty()) trip.setCurrentStop(routeStops.get(0));
        trips.save(trip); bus.setStatus(BusStatus.IN_SERVICE);
        createUpdate(UpdateType.TRIP_STARTED, "Trip started on " + bus.getBusNumber(), bus.getId(), bus.getRoute().getId(), username);
    }

    @Transactional
    public void endTrip(String username) {
        Trip trip = activeTrip(username).orElseThrow(() -> new IllegalArgumentException("No trip is currently in progress."));
        trip.setStatus(TripStatus.COMPLETED); trip.setEndedAt(LocalDateTime.now());
        if (trip.getBus().getStatus() != BusStatus.BREAKDOWN) trip.getBus().setStatus(BusStatus.AVAILABLE);
        createUpdate(UpdateType.TRIP_COMPLETED, "Trip completed on " + trip.getBus().getBusNumber(), trip.getBus().getId(), trip.getRoute().getId(), username);
    }

    @Transactional
    public void updateCurrentStop(String username, Long stopId) {
        Trip trip = activeTrip(username).orElseThrow(() -> new IllegalArgumentException("Start a trip before updating its stop."));
        RouteStop stop = stops.findById(stopId).orElseThrow(() -> new IllegalArgumentException("Stop not found."));
        if (!stop.getRoute().getId().equals(trip.getRoute().getId())) throw new IllegalArgumentException("Select a stop on your assigned route.");
        trip.setCurrentStop(stop);
        createUpdate(UpdateType.NOTICE, "" + trip.getBus().getBusNumber() + " is now at " + stop.getName(), trip.getBus().getId(), trip.getRoute().getId(), username);
    }

    @Transactional
    public void reportIssue(String username, boolean breakdown, String message) {
        Trip trip = activeTrip(username).orElse(null);
        Bus bus = trip != null ? trip.getBus() : buses.findAll().stream().filter(b -> b.getDriver() != null && b.getDriver().getUser().getUsername().equals(username)).findFirst().orElseThrow(() -> new IllegalArgumentException("No bus is assigned to your account."));
        if (breakdown) bus.setStatus(BusStatus.BREAKDOWN); else if (bus.getStatus() != BusStatus.BREAKDOWN) bus.setStatus(BusStatus.DELAYED);
        createUpdate(breakdown ? UpdateType.BREAKDOWN : UpdateType.DELAY, message, bus.getId(), bus.getRoute() == null ? null : bus.getRoute().getId(), username);
    }

    @Transactional
    public void saveEvent(String name, LocalDate date, int attendees, String notes) {
        if (attendees < 1) throw new IllegalArgumentException("Expected attendees must be greater than zero.");
        CollegeEvent event = new CollegeEvent(); event.setName(name.trim()); event.setEventDate(date); event.setExpectedAttendees(attendees);
        event.setBusesRequired((attendees + 39) / 40); event.setNotes(notes == null ? "" : notes.trim()); events.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) { events.deleteById(id); }
}
