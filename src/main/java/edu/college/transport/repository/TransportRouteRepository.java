package edu.college.transport.repository;

import edu.college.transport.entity.TransportRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {
    List<TransportRoute> findByActiveTrueOrderByNameAsc();
    Optional<TransportRoute> findByCode(String code);
    boolean existsByCode(String code);
    long countByActiveTrue();
}
