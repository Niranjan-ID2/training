package com.example.labadaptor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Specimen {

    private String type;

    @Column(name = "collection_method")
    private String collectionMethod;

    @Column(name = "collection_date")
    private LocalDate collectionDate;

    @Column(name = "collection_time")
    private LocalTime collectionTime;
}
