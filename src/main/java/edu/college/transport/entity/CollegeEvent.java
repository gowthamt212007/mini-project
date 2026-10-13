package edu.college.transport.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "college_events")
@Getter @Setter @NoArgsConstructor
public class CollegeEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 140) private String name;
    @Column(nullable = false) private LocalDate eventDate;
    @Column(nullable = false) private int expectedAttendees;
    @Column(nullable = false) private int busesRequired;
    @Column(length = 500) private String notes;
}
