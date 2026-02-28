package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patient_doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDoctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private String purpose; // e.g., "Routine Checkup", "Knee Pain"

    @Column(columnDefinition = "TEXT")
    private String notes; // Doctor's clinical notes for this specific visit

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    // NEW: Links all X-rays or PDF reports uploaded during this specific visit
    @OneToMany(mappedBy = "patientDoctor", cascade = CascadeType.ALL)
    private List<MedicalRecord> visitRecords;

    // CORRECTED: Now maps to the 'visit' field inside PatientDrug
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientDrug> prescriptions;
}