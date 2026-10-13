package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buses")
@Getter @Setter @NoArgsConstructor
public class Bus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 30) private String busNumber;
    @Column(nullable = false, unique = true, length = 30) private String registrationNumber;
    @Column(nullable = false) private int capacity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private BusStatus status = BusStatus.AVAILABLE;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "route_id") private TransportRoute route;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "driver_id") private Driver driver;
}
