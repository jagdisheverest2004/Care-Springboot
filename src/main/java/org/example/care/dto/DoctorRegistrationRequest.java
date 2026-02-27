package org.example.care.dto;

import lombok.Data;

@Data
public class DoctorRegistrationRequest {
    private String specialization;
    private String licenseNumber;
    private String hospitalName;
    private String contactInfo;
}
