package com.example.labadaptor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInformationDTO {

    @NotBlank(message = "Patient name cannot be blank")
    private String name;

    @NotNull(message = "Patient age cannot be null")
    @Min(value = 0, message = "Age must be a positive value")
    private Integer age;

    @NotBlank(message = "Patient gender cannot be blank")
    private String gender;

    @NotBlank(message = "Patient ID cannot be blank")
    private String id;

    @Valid
    private List<ContactInformationDTO> contactInformation;
}
