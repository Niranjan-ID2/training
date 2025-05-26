package com.example.labadaptor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "test_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_type")
    private String testType;

    @Column(name = "test_performed_date")
    private LocalDate testPerformedDate;

    @Column(name = "test_performed_time")
    private LocalTime testPerformedTime;

    @Column(name = "test_reported_date")
    private LocalDate testReportedDate;

    @Column(name = "test_reported_time")
    private LocalTime testReportedTime;

    @Embedded
    private Specimen specimen;

    @OneToMany(mappedBy = "testInformation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResult> results;

    @Embedded
    private Interpretation interpretation;

    @Embedded
    private PathologistLabTechnicianInformation pathologistLabTechnicianInformation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_report_id")
    private LaboratoryReport laboratoryReport;
}
