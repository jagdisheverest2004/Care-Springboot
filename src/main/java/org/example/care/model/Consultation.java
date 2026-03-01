package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.care.model.enumeration.RiskLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NEW: Link this consultation to the appointment that initiated it (Optional)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    // FIXED: mappedBy MUST match the variable name in MedicalRecord.java ("consultation")
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<MedicalRecord> visitRecords = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
}