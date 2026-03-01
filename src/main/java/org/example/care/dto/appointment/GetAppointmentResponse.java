package org.example.care.dto.appointment;

import lombok.Data;
import org.example.care.model.enumeration.AppointmentStatus;

import java.time.LocalDateTime;

@Data
public class GetAppointmentResponse {
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private LocalDateTime appointmentDateTime;
    private String reasonForAppointment;
    private AppointmentStatus appointmentStatus;
}
