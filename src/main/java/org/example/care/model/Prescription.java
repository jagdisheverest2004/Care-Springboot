package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.care.model.enumeration.DrugTime;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Consultation visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor prescribedBy;

    private String dosage;
    private String instructions;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @ElementCollection(targetClass = DrugTime.class)
    @CollectionTable(name = "prescription_times", joinColumns = @JoinColumn(name = "prescription_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "drug_time")
    private List<DrugTime> drugTimes;
}