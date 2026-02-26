package org.example.care.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SafetyCheckRequest {

    @Column(nullable = false)
    private Long patientId;

    @NotEmpty
    private List<String> newDrugs;
}
