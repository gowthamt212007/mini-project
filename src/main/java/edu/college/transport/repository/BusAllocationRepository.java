package edu.college.transport.repository;

import edu.college.transport.entity.BusAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BusAllocationRepository extends JpaRepository<BusAllocation, Long> {
    long countByBusIdAndActiveTrue(Long busId);
    List<BusAllocation> findByBusIdAndActiveTrue(Long busId);
    Optional<BusAllocation> findByStudentIdAndActiveTrue(Long studentId);
    List<BusAllocation> findByActiveTrueOrderByAllocatedAtDesc();
}
