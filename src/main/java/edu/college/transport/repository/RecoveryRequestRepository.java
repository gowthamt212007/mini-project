package edu.college.transport.repository;

import edu.college.transport.entity.RecoveryRequest;
import edu.college.transport.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecoveryRequestRepository extends JpaRepository<RecoveryRequest, Long> {
    List<RecoveryRequest> findByStudentUserUsernameOrderByRequestedAtDesc(String username);
    List<RecoveryRequest> findAllByOrderByRequestedAtDesc();
    List<RecoveryRequest> findByStatus(RequestStatus status);
}
