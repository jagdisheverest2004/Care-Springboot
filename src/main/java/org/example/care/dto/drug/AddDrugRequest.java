package org.example.care.dto.drug;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddDrugRequest {

    @NotEmpty
    private String drugName;

    @NotEmpty
    private String generalDescription;

}
