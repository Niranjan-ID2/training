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
public class ReferenceRange {

    @Column(name = "min_range")
    private String minRange;

    @Column(name = "max_range")
    private String maxRange;
}
