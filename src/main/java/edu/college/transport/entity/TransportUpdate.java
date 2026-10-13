package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_updates")
@Getter @Setter @NoArgsConstructor
public class TransportUpdate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private UpdateType type;
    @Column(nullable = false, length = 700) private String message;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "bus_id") private Bus bus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "route_id") private TransportRoute route;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private UserAccount createdBy;
    @CreationTimestamp @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
}
