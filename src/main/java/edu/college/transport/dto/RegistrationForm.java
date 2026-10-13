package edu.college.transport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistrationForm {
    @NotBlank @Size(max = 80) private String username;
    @NotBlank @Size(min = 8, max = 72) private String password;
    @NotBlank @Size(max = 120) private String fullName;
    @NotBlank @Size(max = 40) private String registerNumber;
    @NotNull private Long pickupStopId;
}
