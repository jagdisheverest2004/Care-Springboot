package org.example.care.dto.auth;

import lombok.Data;

@Data
public class DoctorRegistrationRequest {
    private String specialization;
    private String licenseNumber;
    private String hospitalName;
    private String contactInfo;
}
