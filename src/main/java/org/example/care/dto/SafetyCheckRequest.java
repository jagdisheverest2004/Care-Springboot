package org.example.care.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SafetyCheckRequest {

    @NotEmpty
    private List<String> newDrugs;

    @NotEmpty
    private List<String> currentMeds;
}
