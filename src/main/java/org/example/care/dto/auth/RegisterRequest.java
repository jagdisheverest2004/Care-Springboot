package org.example.care.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.care.model.enumeration.Role;
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

    @NotBlank
    private String name;

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
