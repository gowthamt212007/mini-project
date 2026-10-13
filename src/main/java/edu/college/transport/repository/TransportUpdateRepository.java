package edu.college.transport.repository;

import edu.college.transport.entity.TransportUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransportUpdateRepository extends JpaRepository<TransportUpdate, Long> {
    List<TransportUpdate> findTop12ByOrderByCreatedAtDesc();
}
