package org.example.care.service;

import org.example.care.dto.appointment.CreateAppointmentRequest;
import org.example.care.dto.appointment.GetAppointmentResponse;
import org.example.care.dto.appointment.GetAppointmentResponses;
import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.consultation.GetConsultationResponse;
import org.example.care.dto.consultation.GetConsultationResponses;
import org.example.care.dto.doctor.GetDoctorProfile;
import org.example.care.dto.doctor.UpdateDoctorProfile;
import org.example.care.model.Appointment;
import org.example.care.model.Doctor;
import org.example.care.model.enumeration.AppointmentStatus;
import org.example.care.repository.AppointmentRepository;
import org.example.care.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private ConsultationService consultationService;
    @Autowired
    private AppointmentRepository appointmentRepository;

    public Doctor getDoctorByUserId(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with user id: " + id));
    }

    public AuthResponse updateDoctorProfile(Long id, UpdateDoctorProfile request) {
        Doctor doctor = getDoctorByUserId(id);

        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }

        if (request.getName() != null) {
            doctor.setName(request.getName());
        }

        if (request.getHospitalName() != null) {
            doctor.setHospitalName(request.getHospitalName());
        }

        if (request.getContactInfo() != null) {
            doctor.setContactInfo(request.getContactInfo());
        }

        doctorRepository.save(doctor);

        return AuthResponse.builder()
                .message("Doctor profile updated successfully")
                .role(doctor.getUser().getRole())
                .username(doctor.getUser().getUsername())
                .build();
    }

    public GetDoctorProfile getDoctorProfile(Long id) {
        Doctor doctor = getDoctorByUserId(id);

        GetDoctorProfile getDoctorProfile = new GetDoctorProfile();
        getDoctorProfile.setName(doctor.getName());
        getDoctorProfile.setSpecialization(doctor.getSpecialization());
        getDoctorProfile.setHospitalName(doctor.getHospitalName());
        getDoctorProfile.setContactInfo(doctor.getContactInfo());

        List<GetAppointmentResponse> appointResponsesList = appointmentService.getAppointmentsForDoctor(doctor.getAppointments());
        List<GetConsultationResponse> consultationResponsesList = consultationService.getConsultationDetails(doctor.getPatientConsultations());
        getDoctorProfile.setAppointments(new GetAppointmentResponses());
        getDoctorProfile.getAppointments().setAppointments(appointResponsesList);
        getDoctorProfile.setConsultations(new GetConsultationResponses());
        getDoctorProfile.getConsultations().setConsultations(consultationResponsesList);

        return getDoctorProfile;
    }

    public String updatePatientAppointmentStatus(AppointmentStatus appointmentStatus, Long appointmentId) {
        Appointment appointment =  appointmentRepository.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));
        appointment.setStatus(appointmentStatus);
        appointmentRepository.save(appointment);
        if(appointmentStatus == AppointmentStatus.ACCEPTED){
            consultationService.startConsultation(appointmentId);
        }
        return "Appointment status updated successfully to " + appointmentStatus;
    }

    public GetAppointmentResponses getDoctorAppointments(AppointmentStatus appointmentStatus, Long id) {
        Doctor doctor = getDoctorByUserId(id);
        List<Appointment> appointments = doctor.getAppointments();

        if (appointmentStatus != null) {
            appointments = appointments.stream()
                    .filter(appointment -> appointment.getStatus() == appointmentStatus)
                    .toList();
        }

        List<GetAppointmentResponse> responseList = appointmentService.getAppointmentsForDoctor(appointments);
        GetAppointmentResponses response = new GetAppointmentResponses();
        response.setAppointments(responseList);
        return response;
    }

    public String schedulePatientAppointment(Long id,Long patientId, CreateAppointmentRequest request) {
        Doctor doctor = getDoctorByUserId(id);
        appointmentService.schedulePatientAppointment(doctor,patientId, request);
        return "Appointment scheduled successfully";
    }

    public GetConsultationResponses getDoctorConsultations(Long id) {
        Doctor doctor = getDoctorByUserId(id);
        List<GetConsultationResponse> consultationResponsesList = consultationService.getConsultationDetails(doctor.getPatientConsultations());
        GetConsultationResponses response = new GetConsultationResponses();
        response.setConsultations(consultationResponsesList);
        return response;
    }
}
