package org.example.care.dto.doctor;

import lombok.Data;

@Data
public class UpdateDoctorProfile {
    private String name;
    private String specialization;
    private String contactInfo;
    private String hospitalName;
}
