package edu.college.transport.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;

@Getter @Setter
public class BusForm {
    private Long id;
    @NotBlank @Size(max = 30) private String busNumber;
    @NotBlank @Size(max = 30) private String registrationNumber;
    @Min(1) private int capacity;
    private Long routeId;
    private Long driverId;
}
