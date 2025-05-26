package com.example.labadaptor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterpretationDTO {

    private String observations;
    private String criticalAlerts;
    private String comments;
}
