package com.example.labadaptor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interpretation {

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(name = "critical_alerts", columnDefinition = "TEXT")
    private String criticalAlerts;

    @Column(columnDefinition = "TEXT")
    private String comments;
}
