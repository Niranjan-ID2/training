package com.example.labadaptor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryReportDTO {

    @NotBlank(message = "Lab code cannot be blank")
    private String labCode;

    @NotBlank(message = "Lab name cannot be blank")
    private String labName;

    private String description;

    @NotNull(message = "Patient information cannot be null")
    @Valid
    private PatientInformationDTO patientInformation;

    @NotNull(message = "Test information cannot be null")
    @Valid
    private List<TestInformationDTO> testInformation;
}
