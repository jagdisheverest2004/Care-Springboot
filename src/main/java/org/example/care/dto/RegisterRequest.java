package org.example.care.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.care.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    private Role role = Role.PATIENT;

    // Patient
    private Integer age;
    private String gender;
    private String bloodGroup;

    // Doctor
    private String specialization;
    private String licenseNumber;
    private String hospitalName;
    private String contactInfo;

}
