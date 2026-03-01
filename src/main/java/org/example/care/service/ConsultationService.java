package org.example.care.service;

import org.example.care.dto.consultation.GetConsultationResponse;
import org.example.care.dto.drug.PatientDrugRetreival;
import org.example.care.dto.medicalrecord.MedicalRecordRetreival;
import org.example.care.dto.patient.CreateConsultationRequest;
import org.example.care.dto.patient.GetConsultationForPatientResponse;
import org.example.care.dto.patient.UpdateConsultationRequest;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Appointment;
import org.example.care.model.Consultation;
import org.example.care.model.Doctor;
import org.example.care.model.enumeration.AppointmentStatus;
import org.example.care.repository.AppointmentRepository;
import org.example.care.repository.ConsultationRepository;
import org.example.care.repository.DoctorRepository;
import org.example.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;


    @Transactional
    public Consultation createVisitation(Consultation consultation, CreateConsultationRequest visit) {
        consultation.setPurpose(visit.getPurpose());
        consultation.setNotes(visit.getNotes());
        consultation.setRiskLevel(visit.getRiskLevel());
        consultation.setVisitedAt(LocalDateTime.now());
        consultationRepository.save(consultation);
        if(visit.getNewDrugs() != null) {
            prescriptionService.createPatientDrug(visit,consultation);
        }
        consultationRepository.save(consultation);
        consultation.getPatient().getVisits().add(consultation);
        consultation.getDoctor().getPatientConsultations().add(consultation);
        patientRepository.save(consultation.getPatient());
        doctorRepository.save(consultation.getDoctor());

        return consultation;
    }

    public List<GetConsultationForPatientResponse> getPatientDoctorDetails(List<Consultation> doctorVisits) {

        return doctorVisits.stream().map(visit -> {
            Doctor treatedDoctor = visit.getDoctor();
            GetConsultationForPatientResponse visitRetreival = new GetConsultationForPatientResponse();
            visitRetreival.setId(visit.getId());
            visitRetreival.setPrescribedDoctorId(treatedDoctor.getId());
            visitRetreival.setPrescribedDoctorName(treatedDoctor.getName());
            visitRetreival.setPurpose(visit.getPurpose());
            visitRetreival.setNotes(visit.getNotes());
            visitRetreival.setVisitedAt(visit.getVisitedAt());
            visitRetreival.setRiskLevel(visit.getRiskLevel());

            List<PatientDrugRetreival> drugRetreivals = prescriptionService.getPatientDrugDetails(visit.getPrescriptions());

            List<MedicalRecordRetreival> recordRetreivals = medicalRecordService.getMedicalRecordDetails(visit.getVisitRecords());

            visitRetreival.setDrugsPrescribed(drugRetreivals);
            visitRetreival.setMedicalRecords(recordRetreivals);

            return visitRetreival;
        }).toList();
    }


    public void startConsultation(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Ensure the appointment was actually accepted/scheduled
        if (appointment.getStatus() != AppointmentStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot start consultation for an unaccepted appointment.");
        }

        // Properly create and link the consultation
        Consultation consultation = Consultation.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .purpose(appointment.getReasonForAppointment())
                .build();

        // Change appointment status to completed
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setConsultation(consultation);

        consultationRepository.save(consultation);
        appointmentRepository.save(appointment);
    }

    public List<GetConsultationResponse> getConsultationDetails(List<Consultation> patientConsultations) {
        return patientConsultations.stream().map(consultation -> {
            GetConsultationResponse response = new GetConsultationResponse();
            response.setConsultationId(consultation.getId());
            response.setPurpose(consultation.getPurpose());
            response.setNotes(consultation.getNotes());
            response.setVisitedAt(consultation.getVisitedAt());
            response.setRiskLevel(consultation.getRiskLevel());
            return response;
        }).toList();
    }

    @Transactional
    public Consultation updateConsultation(Long consultationId, UpdateConsultationRequest updateConsultationRequest) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + consultationId));

        if (updateConsultationRequest.getChronicConditions() != null) {
            consultation.getPatient().setChronicConditions(updateConsultationRequest.getChronicConditions());
        }

        if(updateConsultationRequest.getAllergies() != null) {
            consultation.getPatient().setAllergies(updateConsultationRequest.getAllergies());
        }

        return createVisitation(consultation, updateConsultationRequest.getDoctorVisit());
    }
}
