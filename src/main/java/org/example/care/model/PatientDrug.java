package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.care.model.enumeration.DrugTime;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patient_drugs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientDrug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    // NEW: Links this specific prescription to the visit it was created in
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private PatientDoctor visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor prescribedBy;

    private String dosage; // e.g., "500mg"

    private String instructions; // e.g., "Take after food"

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @ElementCollection(targetClass = DrugTime.class)
    @CollectionTable(name = "patient_drug_times", joinColumns = @JoinColumn(name = "patient_drug_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "drug_time")
    private List<DrugTime> drugTimes;
}