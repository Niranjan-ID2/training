package com.example.labadaptor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "patient_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String gender;

    @Column(name = "patient_id")
    private String patientId;

    @OneToMany(mappedBy = "patientInformation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactInformation> contactInformation;

    @OneToOne(mappedBy = "patientInformation")
    private LaboratoryReport laboratoryReport;
}
