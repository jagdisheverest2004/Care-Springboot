package org.example.care.dto.doctor;

import lombok.Data;
import org.example.care.dto.appointment.GetAppointmentResponse;
import org.example.care.dto.consultation.GetConsultationResponse;

import java.util.List;

@Data
public class GetDoctorProfile {
    private String name;
    private String specialization;
    private String contactInfo;
    private String hospitalName;
    private List<GetAppointmentResponse> appointments;
    private List<GetConsultationResponse> consultations;
}
