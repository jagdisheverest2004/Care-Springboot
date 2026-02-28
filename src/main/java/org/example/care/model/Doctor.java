package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Doctor {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String licenseNumber;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    private String contactInfo;

    @OneToMany(mappedBy = "doctor")
    private List<MedicalRecord> treatedRecords;

    @OneToMany(mappedBy = "prescribedBy")
    private List<PatientDrug> writtenPrescriptions;

    //List of all visits/consultations this doctor has handled
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientDoctor> patientConsultations;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}