package com.example.labadaptor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contact_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private String email;
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_information_id")
    private PatientInformation patientInformation;
}
