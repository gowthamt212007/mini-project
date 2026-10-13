package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_requests")
@Getter @Setter @NoArgsConstructor
public class RecoveryRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @Column(nullable = false, length = 500) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RequestStatus status = RequestStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proposed_bus_id") private Bus proposedBus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private UserAccount reviewedBy;
    @CreationTimestamp @Column(nullable = false, updatable = false) private LocalDateTime requestedAt;
}
