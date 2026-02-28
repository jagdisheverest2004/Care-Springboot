package org.example.care.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddDrugRequest {

    @NotEmpty
    private String drugName;

    @NotEmpty
    private String generalDescription;

}
