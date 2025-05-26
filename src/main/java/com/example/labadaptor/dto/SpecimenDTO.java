package com.example.labadaptor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecimenDTO {

    @NotBlank(message = "Specimen type cannot be blank")
    private String type;

    private String collectionMethod;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String collectionDate;

    @JsonFormat(pattern = "HH:mm") // Corrected pattern to HH:mm as per previous DTO
    private String collectionTime;
}
