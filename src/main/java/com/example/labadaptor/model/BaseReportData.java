package com.example.labadaptor.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BaseReportData {

    private LocalDate reportDate;
    private String labName;
    private String patientName;
    private Integer patientAge;
    private String patientSex;
}
