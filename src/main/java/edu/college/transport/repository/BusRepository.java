package edu.college.transport.repository;

import edu.college.transport.entity.Bus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    boolean existsByBusNumber(String busNumber);
    List<Bus> findByBusNumberContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(String busNumber, String registrationNumber);
    List<Bus> findByRouteId(Long routeId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from Bus b where b.id = :id") Optional<Bus> findLockedById(@Param("id") Long id);
}
