package org.example.care.dto.doctor;

import lombok.Data;
import org.example.care.dto.appointment.GetAppointmentResponses;
import org.example.care.dto.consultation.GetConsultationResponses;

@Data
public class GetDoctorProfile {
    private String name;
    private String specialization;
    private String contactInfo;
    private String hospitalName;
    private GetAppointmentResponses appointments;
    private GetConsultationResponses consultations;
}
