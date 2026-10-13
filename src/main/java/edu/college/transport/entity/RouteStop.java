package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "route_stops", uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "stop_order"}))
@Getter @Setter @NoArgsConstructor
public class RouteStop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "route_id", nullable = false) private TransportRoute route;
    @Column(nullable = false, length = 120) private String name;
    @Column(name = "stop_order", nullable = false) private int stopOrder;
    @Column(name = "travel_minutes_from_previous", nullable = false) private int travelMinutesFromPrevious;
}
