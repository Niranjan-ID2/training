package com.example.labadaptor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parameter;
    private String value;
    private String units;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Embedded
    private ReferenceRange referenceRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_information_id")
    private TestInformation testInformation;
}
