package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transport_routes")
@Getter @Setter @NoArgsConstructor
public class TransportRoute {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 24) private String code;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false) private boolean active = true;
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC") private List<RouteStop> stops = new ArrayList<>();
}
