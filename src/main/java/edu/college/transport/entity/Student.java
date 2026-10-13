package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true) private UserAccount user;
    @Column(nullable = false, unique = true, length = 40) private String registerNumber;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pickup_stop_id") private RouteStop pickupStop;
}
