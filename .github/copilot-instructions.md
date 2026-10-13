# Smart College Transportation Management System

- Java 17, Maven, Spring Boot MVC, Thymeleaf, Spring Security, JPA/Hibernate and MySQL.
- Keep backend logic in services; controllers handle request binding and navigation.
- Use Jakarta Bean Validation on form inputs and server-side authorization for every protected action.
- Keep CSRF enabled and use BCrypt for account passwords.
- Frontend must remain plain HTML, Thymeleaf and CSS; do not introduce JavaScript or client-side dependencies.
- Keep database credentials in environment variables, not committed configuration.
- Preserve the route-stop relationships and enforce bus capacity transactionally.
- Validate changes with Maven compilation and check every newly added Thymeleaf reference.
