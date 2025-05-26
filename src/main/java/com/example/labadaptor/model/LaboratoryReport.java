package com.example.labadaptor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "laboratory_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lab_code")
    private String labCode;

    @Column(name = "lab_name")
    private String labName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_information_id", referencedColumnName = "id")
    private PatientInformation patientInformation;

    @OneToMany(mappedBy = "laboratoryReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestInformation> testInformation;
}
