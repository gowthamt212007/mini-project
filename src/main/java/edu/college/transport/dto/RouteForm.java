package edu.college.transport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RouteForm {
    private Long id;
    @NotBlank @Size(max = 24) private String code;
    @NotBlank @Size(max = 100) private String name;
    @NotBlank @Size(max = 10000) private String stopLines;
}
