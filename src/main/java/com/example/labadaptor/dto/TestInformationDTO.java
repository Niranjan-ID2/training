package com.example.labadaptor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TestInformationDTO {

    @NotBlank(message = "Test type cannot be blank")
    private String testType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String testPerformedDate;

    @JsonFormat(pattern = "HH:mm") // Corrected pattern to HH:mm
    private String testPerformedTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String testReportedDate;

    @JsonFormat(pattern = "HH:mm") // Corrected pattern to HH:mm
    private String testReportedTime;

    @NotNull(message = "Specimen information cannot be null")
    @Valid
    private SpecimenDTO specimen;

    @NotNull(message = "Results cannot be null")
    @Valid
    private List<TestResultDTO> results;

    @Valid
    private InterpretationDTO interpretation;

    @Valid
    private PathologistLabTechnicianInformationDTO pathologistLabTechnicianInformation;
}
