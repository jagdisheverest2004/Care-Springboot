package org.example.care.dto.patient;

import lombok.Data;

@Data
public class UpdatePatientProfile {
    private String name;
    private String phoneNumber;
    private String address;
}
