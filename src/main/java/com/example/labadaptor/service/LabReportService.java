package com.example.labadaptor.service;

import com.example.labadaptor.dto.*;
import com.example.labadaptor.model.*;
import com.example.labadaptor.repository.LabReportRepository; // Corrected import
import com.example.labadaptor.repository.PatientInformationRepository; // Added import
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

    private final LabReportRepository labReportRepository; // Corrected type
    private final PatientInformationRepository patientInformationRepository; // Injected repository

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

        // Handle PatientInformation
        if (reportDTO.getPatientInformation() != null) {
            PatientInformationDTO patientDTO = reportDTO.getPatientInformation();
            String patientBusinessId = patientDTO.getId(); // The unique ID from the DTO

            Optional<PatientInformation> existingPatientOpt = patientInformationRepository.findByPatientId(patientBusinessId);

            PatientInformation patientEntity;
            if (existingPatientOpt.isPresent()) {
                patientEntity = existingPatientOpt.get();
                // Update existing patient's details
                patientEntity.setName(patientDTO.getName());
                patientEntity.setAge(patientDTO.getAge());
                patientEntity.setGender(patientDTO.getGender());
                // patientEntity.setPatientId(patientDTO.getId()); // ID should not change

                // Manage contacts: Clear old and add new
                if (patientEntity.getContactInformation() == null) { // Ensure list exists
                    patientEntity.setContactInformation(new ArrayList<>());
                }
                patientEntity.getContactInformation().clear(); // Clear existing contacts
                if (patientDTO.getContactInformation() != null) {
                    for (ContactInformationDTO contactDTO : patientDTO.getContactInformation()) {
                        ContactInformation contactEntity = new ContactInformation();
                        contactEntity.setPhone(contactDTO.getPhone());
                        contactEntity.setEmail(contactDTO.getEmail());
                        contactEntity.setAddress(contactDTO.getAddress());
                        contactEntity.setPatientInformation(patientEntity); // Set back-reference
                        patientEntity.getContactInformation().add(contactEntity);
                    }
                }
            } else {
                patientEntity = new PatientInformation();
                // Map new patient details
                patientEntity.setPatientId(patientBusinessId);
                patientEntity.setName(patientDTO.getName());
                patientEntity.setAge(patientDTO.getAge());
                patientEntity.setGender(patientDTO.getGender());
                
                patientEntity.setContactInformation(new ArrayList<>()); // Initialize list
                if (patientDTO.getContactInformation() != null) {
                    for (ContactInformationDTO contactDTO : patientDTO.getContactInformation()) {
                        ContactInformation contactEntity = new ContactInformation();
                        contactEntity.setPhone(contactDTO.getPhone());
                        contactEntity.setEmail(contactDTO.getEmail());
                        contactEntity.setAddress(contactDTO.getAddress());
                        contactEntity.setPatientInformation(patientEntity); // Set back-reference
                        patientEntity.getContactInformation().add(contactEntity);
                    }
                }
            }
            // Removed: patientInformation.setLaboratoryReport(laboratoryReport); 
            // This back-reference is not present in PatientInformation anymore due to ManyToOne from LabReport
            laboratoryReport.setPatientInformation(patientEntity);
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

        return labReportRepository.save(laboratoryReport);
    }

    @Transactional(readOnly = true)
    public Optional<LaboratoryReport> getLabReportById(Long id) {
        return labReportRepository.findById(id);
    }
    
    // Add methods for other CRUD operations (getAll, update, delete) later
}
