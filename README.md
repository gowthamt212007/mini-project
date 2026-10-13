# Smart College Transportation Management System

A server-rendered campus transport management application built with Java 17, Spring Boot, Spring MVC, Thymeleaf, Spring Security, Spring Data JPA, Hibernate and MySQL. All workflows use ordinary HTML forms and page reloads; the project contains no JavaScript.

## Features

- Student registration with server-side Jakarta validation, BCrypt password hashing and session-based authentication.
- Role-based student, administrator and driver portals, enforced by Spring Security on the server. CSRF protection remains enabled for forms.
- Student pickup-stop profile, automatic route-compatible seat recommendation, allocation view, estimated arrival from driver-reported stop timings, transport notices and missed-bus recovery requests.
- Bus management with route/driver assignments, availability and capacity checks; transactional allocation locks the bus row before checking seats.
- Ordered route-stop management with travel-minute estimates; edits preserve existing stop identity so pickup references stay valid.
- Driver trip start/end, current-stop updates, delay and breakdown reporting, and trip history.
- Admin management of drivers, student accounts, bus allocations, routes, updates, recovery reviews, events and reports.
- Event planning estimates additional buses at 40 seats per vehicle.
- Responsive HTML/CSS interface, accessible form labels, and reusable Thymeleaf fragments.

GPS-based live location is not implemented. Arrival estimates are a simulation using configured segment durations and the driver's last submitted stop. No external APIs or AI/ML are used.

## Requirements

- JDK 17+
- Maven 3.8+
- MySQL 8+ when using the default `mysql` configuration (optional for the embedded local demo profile)

## Database and startup

### Quick local demo (no MySQL server)

1. Install JDK 17+ and Maven 3.8+.
2. Set a strong `INITIAL_ADMIN_PASSWORD` before the first startup. Optionally set `INITIAL_ADMIN_USERNAME` (default `transport-admin`). If no initial password is set, the app deliberately does not create an administrator account.
3. Run with the `local` Spring profile: `mvn spring-boot:run -Dspring-boot.run.profiles=local`.
4. Visit `http://localhost:8080`. The local profile uses a persistent H2 database file under `data/` and seeds example routes and buses.

### MySQL mode

Create or grant permission to create the `college_transport` database, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then run `mvn spring-boot:run` without the `local` profile. By default, the JDBC URL creates the database if it does not exist. Keep credentials in environment variables rather than source control.

Environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/college_transport?...` | JDBC connection URL |
| `DB_USERNAME` | `root` | MySQL login |
| `DB_PASSWORD` | empty | MySQL password |
| `INITIAL_ADMIN_USERNAME` | `transport-admin` | Initial admin login ID |
| `INITIAL_ADMIN_PASSWORD` | empty (no admin seeded) | BCrypt-hashed initial admin password |

The JPA entities create/update the development schema (`spring.jpa.hibernate.ddl-auto=update`). The reference MySQL DDL is in `src/main/resources/db/schema.sql`. Demo routes and twenty buses (ten per demo route) are added idempotently on startup by `DemoDataSeeder`. The student dashboard recommends up to three other buses serving the same pickup route, ordered by driver-reported arrival estimate when available. Bus proximity is a route-based simulation; buses without an active trip are marked as needing dispatch, not as live-GPS nearby. Alternatively, after applying the schema manually, import `src/main/resources/db/sample-data.sql` into the still-empty database before starting the app; do not import it after the startup seeder has run. Use Flyway or Liquibase and set `ddl-auto=validate` before production deployment.

## First demonstration

1. Configure `INITIAL_ADMIN_PASSWORD` and MySQL settings, then start the app.
2. Sign in through **Admin login** using the configured initial admin ID and password.
3. Open **Drivers**, create a driver, then open **Bus fleet** and assign that driver to a bus.
4. Create student accounts from **Student login → Create a student account**. The demo routes are available as pickup stops. Registration automatically allocates a compatible available bus when a seat exists; the admin can also manually assign or request a recommendation.
5. Sign in as the created driver to start a trip, update the current route stop, and post delay or breakdown reports.
6. Use the admin dashboard and reports to demonstrate capacity, updates, allocations, recovery review and event planning.

## Project layout

```text
src/main/java/edu/college/transport/
├── config/       Security, startup demonstration data
├── controller/   Public, student, driver and admin MVC controllers
├── dto/          Validated form objects
├── entity/       JPA domain model and enums
├── exception/    MVC/domain error handling
├── repository/   Spring Data JPA repositories
└── service/      Registration, allocation, capacity, trip and reporting logic
src/main/resources/
├── db/           Reference schema and demo seed notes
├── static/css/   Responsive styling (no JavaScript)
└── templates/    Thymeleaf pages and reusable fragments
```

## Notes

- Admin can deactivate buses/routes instead of deleting records referenced by trips or allocations.
- Bus allocation, including recovery approval, verifies route compatibility, bus state and capacity in a transaction. It takes a pessimistic write lock on the bus while allocating.
- The reports and tables are server-rendered; refresh the page to see newly submitted updates.
- Do not use the development Hibernate schema-update setting or demo users as production deployment policy. Configure secret values outside source control and apply migrations before deployment.
