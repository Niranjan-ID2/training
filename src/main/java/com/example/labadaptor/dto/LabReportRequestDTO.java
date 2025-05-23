package com.example.labadaptor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabReportRequestDTO<T> {

    @NotNull(message = "Report date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @NotBlank(message = "Lab name cannot be blank")
    private String labName;

    @NotBlank(message = "Patient name cannot be blank")
    private String patientName;

    @NotNull(message = "Patient age cannot be null")
    @Min(value = 0, message = "Patient age must be non-negative")
    private Integer patientAge;

    @NotBlank(message = "Patient sex cannot be blank")
    private String patientSex;

    @NotNull(message = "Report values cannot be null")
    @Valid // This ensures that nested validation on T is triggered
    private T values;
}
