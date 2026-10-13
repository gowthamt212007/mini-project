package edu.college.transport.repository;

import edu.college.transport.entity.CollegeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollegeEventRepository extends JpaRepository<CollegeEvent, Long> {
    List<CollegeEvent> findAllByOrderByEventDateAsc();
}
