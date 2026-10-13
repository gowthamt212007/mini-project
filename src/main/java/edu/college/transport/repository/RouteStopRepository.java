package edu.college.transport.repository;

import edu.college.transport.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteIdOrderByStopOrder(Long routeId);
    Optional<RouteStop> findByRouteIdAndNameIgnoreCase(Long routeId, String name);
}
