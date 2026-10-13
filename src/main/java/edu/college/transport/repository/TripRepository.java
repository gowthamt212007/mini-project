package edu.college.transport.repository;

import edu.college.transport.entity.Trip;
import edu.college.transport.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findTop8ByOrderByStartedAtDesc();
    List<Trip> findByDriverUserUsernameOrderByStartedAtDesc(String username);
    List<Trip> findByStatus(TripStatus status);
    long countByStartedAtBetween(LocalDateTime from, LocalDateTime to);
}
