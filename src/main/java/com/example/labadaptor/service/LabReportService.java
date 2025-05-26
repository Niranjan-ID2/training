package com.example.labadaptor.service;

import com.example.labadaptor.dto.*;
import com.example.labadaptor.model.*;
import com.example.labadaptor.repository.LaboratoryReportRepository; // Assuming this repository exists or will be created
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
@Transactional
public class LabReportService {

    private final LaboratoryReportRepository laboratoryReportRepository; // Updated repository

    // DateTimeFormatters - consider making them static final if used frequently
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // Log error or throw custom exception
            System.err.println("Error parsing date: " + dateStr + " - " + e.getMessage());
            return null; // Or handle more gracefully
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Log error or throw custom exception
            System.err.println("Error parsing time: " + timeStr + " - " + e.getMessage());
            return null; // Or handle more gracefully
        }
    }

    public LaboratoryReport createLabReport(LaboratoryReportDTO reportDTO) {
        LaboratoryReport laboratoryReport = new LaboratoryReport();
        laboratoryReport.setLabCode(reportDTO.getLabCode());
        laboratoryReport.setLabName(reportDTO.getLabName());
        laboratoryReport.setDescription(reportDTO.getDescription());

        // Map PatientInformation
        if (reportDTO.getPatientInformation() != null) {
            PatientInformationDTO patientDTO = reportDTO.getPatientInformation();
            PatientInformation patientInformation = new PatientInformation();
            patientInformation.setName(patientDTO.getName());
            patientInformation.setAge(patientDTO.getAge());
            patientInformation.setGender(patientDTO.getGender());
            patientInformation.setPatientId(patientDTO.getId()); // Assuming DTO's id maps to patientId
            patientInformation.setLaboratoryReport(laboratoryReport); // Set back-reference

            if (patientDTO.getContactInformation() != null) {
                patientInformation.setContactInformation(new ArrayList<>());
                for (ContactInformationDTO contactDTO : patientDTO.getContactInformation()) {
                    ContactInformation contact = new ContactInformation();
                    contact.setPhone(contactDTO.getPhone());
                    contact.setEmail(contactDTO.getEmail());
                    contact.setAddress(contactDTO.getAddress());
                    contact.setPatientInformation(patientInformation); // Set back-reference
                    patientInformation.getContactInformation().add(contact);
                }
            }
            laboratoryReport.setPatientInformation(patientInformation);
        }

        // Map TestInformation list
        if (reportDTO.getTestInformation() != null) {
            laboratoryReport.setTestInformation(new ArrayList<>());
            for (TestInformationDTO testInfoDTO : reportDTO.getTestInformation()) {
                TestInformation testInformation = new TestInformation();
                testInformation.setTestType(testInfoDTO.getTestType());
                testInformation.setTestPerformedDate(parseDate(testInfoDTO.getTestPerformedDate()));
                testInformation.setTestPerformedTime(parseTime(testInfoDTO.getTestPerformedTime()));
                testInformation.setTestReportedDate(parseDate(testInfoDTO.getTestReportedDate()));
                testInformation.setTestReportedTime(parseTime(testInfoDTO.getTestReportedTime()));
                testInformation.setLaboratoryReport(laboratoryReport); // Set back-reference

                // Map Specimen
                if (testInfoDTO.getSpecimen() != null) {
                    SpecimenDTO specimenDTO = testInfoDTO.getSpecimen();
                    Specimen specimen = new Specimen();
                    specimen.setType(specimenDTO.getType());
                    specimen.setCollectionMethod(specimenDTO.getCollectionMethod());
                    specimen.setCollectionDate(parseDate(specimenDTO.getCollectionDate()));
                    specimen.setCollectionTime(parseTime(specimenDTO.getCollectionTime()));
                    testInformation.setSpecimen(specimen);
                }

                // Map Interpretation
                if (testInfoDTO.getInterpretation() != null) {
                    InterpretationDTO interpretationDTO = testInfoDTO.getInterpretation();
                    Interpretation interpretation = new Interpretation();
                    interpretation.setObservations(interpretationDTO.getObservations());
                    interpretation.setCriticalAlerts(interpretationDTO.getCriticalAlerts());
                    interpretation.setComments(interpretationDTO.getComments());
                    testInformation.setInterpretation(interpretation);
                }

                // Map PathologistLabTechnicianInformation
                if (testInfoDTO.getPathologistLabTechnicianInformation() != null) {
                    PathologistLabTechnicianInformationDTO ptDTO = testInfoDTO.getPathologistLabTechnicianInformation();
                    PathologistLabTechnicianInformation ptInfo = new PathologistLabTechnicianInformation();
                    ptInfo.setName(ptDTO.getName());
                    ptInfo.setContact(ptDTO.getContact());
                    testInformation.setPathologistLabTechnicianInformation(ptInfo);
                }

                // Map TestResults
                if (testInfoDTO.getResults() != null) {
                    testInformation.setResults(new ArrayList<>());
                    for (TestResultDTO resultDTO : testInfoDTO.getResults()) {
                        TestResult testResult = new TestResult();
                        testResult.setParameter(resultDTO.getParameter());
                        testResult.setValue(resultDTO.getValue());
                        testResult.setUnits(resultDTO.getUnits());
                        testResult.setComments(resultDTO.getComments());
                        testResult.setTestInformation(testInformation); // Set back-reference

                        // Map ReferenceRange
                        if (resultDTO.getReferenceRange() != null) {
                            ReferenceRangeDTO rangeDTO = resultDTO.getReferenceRange();
                            ReferenceRange referenceRange = new ReferenceRange();
                            referenceRange.setMinRange(rangeDTO.getMinRange());
                            referenceRange.setMaxRange(rangeDTO.getMaxRange());
                            testResult.setReferenceRange(referenceRange);
                        }
                        testInformation.getResults().add(testResult);
                    }
                }
                laboratoryReport.getTestInformation().add(testInformation);
            }
        }

        return laboratoryReportRepository.save(laboratoryReport);
    }

    @Transactional(readOnly = true)
    public Optional<LaboratoryReport> getLabReportById(Long id) {
        return laboratoryReportRepository.findById(id);
    }
    
    // Add methods for other CRUD operations (getAll, update, delete) later
}
