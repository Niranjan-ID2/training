package com.example.labadaptor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {

    @NotBlank(message = "Parameter cannot be blank")
    private String parameter;

    @NotBlank(message = "Value cannot be blank")
    private String value;

    private String units;
    private String comments;

    @Valid
    private ReferenceRangeDTO referenceRange;
}
