package edu.college.transport.config;

import edu.college.transport.entity.*;
import edu.college.transport.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private final TransportRouteRepository routes;
    private final RouteStopRepository stops;
    private final BusRepository buses;
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    @Value("${INITIAL_ADMIN_USERNAME:transport-admin}") private String adminUsername;
    @Value("${INITIAL_ADMIN_PASSWORD:}") private String adminPassword;

    public DemoDataSeeder(TransportRouteRepository routes, RouteStopRepository stops, BusRepository buses,
                          UserAccountRepository users, PasswordEncoder encoder) {
        this.routes = routes; this.stops = stops; this.buses = buses; this.users = users; this.encoder = encoder;
    }

    @Override @Transactional
    public void run(String... args) {
        TransportRoute north = routes.findByCode("NORTH-01").orElseGet(() -> addRoute("NORTH-01", "North Campus Loop", new String[][]{{"Maple Street", "0"}, {"Central Library", "8"}, {"North Gate", "12"}}));
        TransportRoute east = routes.findByCode("EAST-02").orElseGet(() -> addRoute("EAST-02", "East Connector", new String[][]{{"Riverside Park", "0"}, {"Market Square", "10"}, {"East Gate", "9"}}));
        int addedBuses = 0;
        for (int number = 1; number <= 20; number++) {
            String busNumber = String.format("Campus %02d", number);
            if (!buses.existsByBusNumber(busNumber)) {
                TransportRoute route = number % 2 == 1 ? north : east;
                addBus(busNumber, String.format("DEMO-%03d", number), 32 + (number % 3) * 4, route);
                addedBuses++;
            }
        }
        if (addedBuses > 0) log.info("Added {} demonstration buses. Replace these entries with your campus fleet.", addedBuses);
        if (!adminPassword.isBlank() && !users.existsByUsername(adminUsername)) {
            UserAccount admin = new UserAccount(); admin.setUsername(adminUsername); admin.setFullName("Transport Administrator");
            admin.setPassword(encoder.encode(adminPassword)); admin.setRole(Role.ADMIN); admin.setEnabled(true); users.save(admin);
            log.info("Initial administrator account created from environment configuration.");
        } else if (adminPassword.isBlank() && !users.existsByUsername(adminUsername)) {
            log.warn("No administrator seeded: set INITIAL_ADMIN_PASSWORD before startup to create the initial admin account.");
        }
    }

    private TransportRoute addRoute(String code, String name, String[][] stopData) {
        TransportRoute route = new TransportRoute(); route.setCode(code); route.setName(name); route.setActive(true); routes.save(route);
        int order = 1;
        for (String[] data : stopData) {
            RouteStop stop = new RouteStop(); stop.setRoute(route); stop.setName(data[0]); stop.setStopOrder(order++); stop.setTravelMinutesFromPrevious(Integer.parseInt(data[1])); stops.save(stop);
        }
        return route;
    }

    private void addBus(String number, String registration, int capacity, TransportRoute route) {
        Bus bus = new Bus(); bus.setBusNumber(number); bus.setRegistrationNumber(registration); bus.setCapacity(capacity); bus.setRoute(route); bus.setStatus(BusStatus.AVAILABLE); buses.save(bus);
    }
}
