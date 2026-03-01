package org.example.care.dto.appointment;

import lombok.Data;

import java.util.List;

@Data
public class GetAppointmentResponses {
    List<GetAppointmentResponse> appointments;
}
