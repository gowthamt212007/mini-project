package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Getter @Setter @NoArgsConstructor
public class Trip {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bus_id", nullable = false) private Bus bus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "driver_id", nullable = false) private Driver driver;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "route_id", nullable = false) private TransportRoute route;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "current_stop_id") private RouteStop currentStop;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TripStatus status = TripStatus.SCHEDULED;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
