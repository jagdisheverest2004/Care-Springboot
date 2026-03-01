package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<PatientDrug> prescriptions = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    // NEW: List of all visits this patient has had
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientDoctor> doctorVisits = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}