package org.example.care.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drugs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Drug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String drugName;

    @Column(columnDefinition = "TEXT")
    private String generalDescription;

    @OneToMany(mappedBy = "drug")
    private List<PatientDrug> patientDrugs = new ArrayList<>();
}