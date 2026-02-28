package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String bloodGroup;

    @Column(columnDefinition = "TEXT")
    private String chronicConditions;

    // RESTORED: This is highly useful for the patient app to query all meds quickly
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientDrug> prescriptions;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<MedicalRecord> medicalRecords;

    // NEW: List of all visits this patient has had
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientDoctor> doctorVisits;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}