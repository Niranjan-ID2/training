package com.example.labadaptor.service;

import com.example.labadaptor.dto.*;
import com.example.labadaptor.model.*;
import com.example.labadaptor.repository.LaboratoryReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabReportServiceTest {

    @Mock
    private LaboratoryReportRepository laboratoryReportRepository;

    @InjectMocks
    private LabReportService labReportService;

    private LaboratoryReportDTO sampleReportDTO;

    @BeforeEach
    void setUp() {
        // Create a comprehensive DTO for testing
        sampleReportDTO = new LaboratoryReportDTO();
        sampleReportDTO.setLabCode(UUID.randomUUID().toString());
        sampleReportDTO.setLabName("General Hospital Labs");
        sampleReportDTO.setDescription("Annual Checkup Report");

        PatientInformationDTO patientInfoDTO = new PatientInformationDTO();
        patientInfoDTO.setId("PATIENT_001");
        patientInfoDTO.setName("Johnathan Doe");
        patientInfoDTO.setAge(42);
        patientInfoDTO.setGender("Male");

        ContactInformationDTO contactInfoDTO = new ContactInformationDTO();
        contactInfoDTO.setEmail("john.doe@example.com");
        contactInfoDTO.setPhone("555-123-4567");
        contactInfoDTO.setAddress("123 Main St, Anytown, USA");
        patientInfoDTO.setContactInformation(Collections.singletonList(contactInfoDTO));
        sampleReportDTO.setPatientInformation(patientInfoDTO);

        TestInformationDTO testInfoDTO = new TestInformationDTO();
        testInfoDTO.setTestType("Blood Panel");
        testInfoDTO.setTestPerformedDate("2023-10-01");
        testInfoDTO.setTestPerformedTime("09:30");
        testInfoDTO.setTestReportedDate("2023-10-02");
        testInfoDTO.setTestReportedTime("14:00");

        SpecimenDTO specimenDTO = new SpecimenDTO();
        specimenDTO.setType("Blood");
        specimenDTO.setCollectionMethod("Venipuncture");
        specimenDTO.setCollectionDate("2023-10-01");
        specimenDTO.setCollectionTime("09:15");
        testInfoDTO.setSpecimen(specimenDTO);

        TestResultDTO testResultDTO = new TestResultDTO();
        testResultDTO.setParameter("Hemoglobin");
        testResultDTO.setValue("14.5");
        testResultDTO.setUnits("g/dL");
        testResultDTO.setComments("Normal range");
        ReferenceRangeDTO refRangeDTO = new ReferenceRangeDTO();
        refRangeDTO.setMinRange("13.5");
        refRangeDTO.setMaxRange("17.5");
        testResultDTO.setReferenceRange(refRangeDTO);
        testInfoDTO.setResults(Collections.singletonList(testResultDTO));

        InterpretationDTO interpretationDTO = new InterpretationDTO();
        interpretationDTO.setObservations("All values within normal limits.");
        interpretationDTO.setCriticalAlerts("None");
        interpretationDTO.setComments("Routine follow-up recommended.");
        testInfoDTO.setInterpretation(interpretationDTO);

        PathologistLabTechnicianInformationDTO techInfoDTO = new PathologistLabTechnicianInformationDTO();
        techInfoDTO.setName("Dr. Emily White");
        techInfoDTO.setContact("ext. 789");
        testInfoDTO.setPathologistLabTechnicianInformation(techInfoDTO);

        sampleReportDTO.setTestInformation(Collections.singletonList(testInfoDTO));
    }

    @Test
    void createLabReport_mapsDtoToEntityCorrectly_andSaves() {
        LaboratoryReport savedEntityMock = new LaboratoryReport();
        savedEntityMock.setId(1L); // Simulate saved entity
        when(laboratoryReportRepository.save(any(LaboratoryReport.class))).thenReturn(savedEntityMock);

        LaboratoryReport result = labReportService.createLabReport(sampleReportDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        ArgumentCaptor<LaboratoryReport> reportCaptor = ArgumentCaptor.forClass(LaboratoryReport.class);
        verify(laboratoryReportRepository).save(reportCaptor.capture());
        LaboratoryReport capturedReport = reportCaptor.getValue();

        // Assert top-level fields
        assertEquals(sampleReportDTO.getLabCode(), capturedReport.getLabCode());
        assertEquals(sampleReportDTO.getLabName(), capturedReport.getLabName());
        assertEquals(sampleReportDTO.getDescription(), capturedReport.getDescription());

        // Assert PatientInformation
        assertNotNull(capturedReport.getPatientInformation());
        PatientInformation capturedPatientInfo = capturedReport.getPatientInformation();
        PatientInformationDTO patientInfoDTO = sampleReportDTO.getPatientInformation();
        assertEquals(patientInfoDTO.getName(), capturedPatientInfo.getName());
        assertEquals(patientInfoDTO.getAge(), capturedPatientInfo.getAge());
        assertEquals(patientInfoDTO.getGender(), capturedPatientInfo.getGender());
        assertEquals(patientInfoDTO.getId(), capturedPatientInfo.getPatientId());
        assertNotNull(capturedPatientInfo.getLaboratoryReport()); // Back-reference

        // Assert ContactInformation (assuming one in sample)
        assertNotNull(capturedPatientInfo.getContactInformation());
        assertFalse(capturedPatientInfo.getContactInformation().isEmpty());
        ContactInformation capturedContact = capturedPatientInfo.getContactInformation().get(0);
        ContactInformationDTO contactInfoDTO = patientInfoDTO.getContactInformation().get(0);
        assertEquals(contactInfoDTO.getEmail(), capturedContact.getEmail());
        assertEquals(contactInfoDTO.getPhone(), capturedContact.getPhone());
        assertEquals(contactInfoDTO.getAddress(), capturedContact.getAddress());
        assertNotNull(capturedContact.getPatientInformation()); // Back-reference

        // Assert TestInformation (assuming one in sample)
        assertNotNull(capturedReport.getTestInformation());
        assertFalse(capturedReport.getTestInformation().isEmpty());
        TestInformation capturedTestInfo = capturedReport.getTestInformation().get(0);
        TestInformationDTO testInfoDTO = sampleReportDTO.getTestInformation().get(0);
        assertEquals(testInfoDTO.getTestType(), capturedTestInfo.getTestType());
        assertEquals(LocalDate.parse("2023-10-01"), capturedTestInfo.getTestPerformedDate());
        assertEquals(LocalTime.parse("09:30"), capturedTestInfo.getTestPerformedTime());
        assertEquals(LocalDate.parse("2023-10-02"), capturedTestInfo.getTestReportedDate());
        assertEquals(LocalTime.parse("14:00"), capturedTestInfo.getTestReportedTime());
        assertNotNull(capturedTestInfo.getLaboratoryReport()); // Back-reference

        // Assert Specimen
        assertNotNull(capturedTestInfo.getSpecimen());
        Specimen capturedSpecimen = capturedTestInfo.getSpecimen();
        SpecimenDTO specimenDTO = testInfoDTO.getSpecimen();
        assertEquals(specimenDTO.getType(), capturedSpecimen.getType());
        assertEquals(specimenDTO.getCollectionMethod(), capturedSpecimen.getCollectionMethod());
        assertEquals(LocalDate.parse("2023-10-01"), capturedSpecimen.getCollectionDate());
        assertEquals(LocalTime.parse("09:15"), capturedSpecimen.getCollectionTime());

        // Assert TestResult (assuming one in sample)
        assertNotNull(capturedTestInfo.getResults());
        assertFalse(capturedTestInfo.getResults().isEmpty());
        TestResult capturedResult = capturedTestInfo.getResults().get(0);
        TestResultDTO testResultDTO = testInfoDTO.getResults().get(0);
        assertEquals(testResultDTO.getParameter(), capturedResult.getParameter());
        assertEquals(testResultDTO.getValue(), capturedResult.getValue());
        assertEquals(testResultDTO.getUnits(), capturedResult.getUnits());
        assertEquals(testResultDTO.getComments(), capturedResult.getComments());
        assertNotNull(capturedResult.getTestInformation()); // Back-reference

        // Assert ReferenceRange
        assertNotNull(capturedResult.getReferenceRange());
        ReferenceRange capturedRefRange = capturedResult.getReferenceRange();
        ReferenceRangeDTO refRangeDTO = testResultDTO.getReferenceRange();
        assertEquals(refRangeDTO.getMinRange(), capturedRefRange.getMinRange());
        assertEquals(refRangeDTO.getMaxRange(), capturedRefRange.getMaxRange());

        // Assert Interpretation
        assertNotNull(capturedTestInfo.getInterpretation());
        Interpretation capturedInterpretation = capturedTestInfo.getInterpretation();
        InterpretationDTO interpretationDTO = testInfoDTO.getInterpretation();
        assertEquals(interpretationDTO.getObservations(), capturedInterpretation.getObservations());
        assertEquals(interpretationDTO.getCriticalAlerts(), capturedInterpretation.getCriticalAlerts());
        assertEquals(interpretationDTO.getComments(), capturedInterpretation.getComments());

        // Assert PathologistLabTechnicianInformation
        assertNotNull(capturedTestInfo.getPathologistLabTechnicianInformation());
        PathologistLabTechnicianInformation capturedTechInfo = capturedTestInfo.getPathologistLabTechnicianInformation();
        PathologistLabTechnicianInformationDTO techInfoDTO = testInfoDTO.getPathologistLabTechnicianInformation();
        assertEquals(techInfoDTO.getName(), capturedTechInfo.getName());
        assertEquals(techInfoDTO.getContact(), capturedTechInfo.getContact());
    }

    @Test
    void createLabReport_withMalformedDate_returnsNullAndLogsError() {
        // System.err is mocked in LabReportService currently for parse errors
        // So we won't see console output, but the behavior is null return from parseDate/Time
        sampleReportDTO.getTestInformation().get(0).setTestPerformedDate("INVALID-DATE");

        LaboratoryReport savedEntityMock = new LaboratoryReport();
        savedEntityMock.setId(1L);
        when(laboratoryReportRepository.save(any(LaboratoryReport.class))).thenReturn(savedEntityMock);

        labReportService.createLabReport(sampleReportDTO);

        ArgumentCaptor<LaboratoryReport> reportCaptor = ArgumentCaptor.forClass(LaboratoryReport.class);
        verify(laboratoryReportRepository).save(reportCaptor.capture());
        LaboratoryReport capturedReport = reportCaptor.getValue();
        assertNull(capturedReport.getTestInformation().get(0).getTestPerformedDate(),
                "Date should be null after parsing error.");
    }

    @Test
    void createLabReport_withMalformedTime_returnsNullAndLogsError() {
        sampleReportDTO.getTestInformation().get(0).setTestPerformedTime("INVALID-TIME");

        LaboratoryReport savedEntityMock = new LaboratoryReport();
        savedEntityMock.setId(1L);
        when(laboratoryReportRepository.save(any(LaboratoryReport.class))).thenReturn(savedEntityMock);


        labReportService.createLabReport(sampleReportDTO);
        ArgumentCaptor<LaboratoryReport> reportCaptor = ArgumentCaptor.forClass(LaboratoryReport.class);
        verify(laboratoryReportRepository).save(reportCaptor.capture());
        LaboratoryReport capturedReport = reportCaptor.getValue();

        assertNull(capturedReport.getTestInformation().get(0).getTestPerformedTime(),
                "Time should be null after parsing error.");
    }


    @Test
    void getLabReportById_whenReportExists_returnsOptionalOfReport() {
        Long reportId = 1L;
        LaboratoryReport mockReport = new LaboratoryReport(); // Populate if needed for deeper assertion
        mockReport.setId(reportId);
        when(laboratoryReportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));

        Optional<LaboratoryReport> result = labReportService.getLabReportById(reportId);

        assertTrue(result.isPresent());
        assertEquals(reportId, result.get().getId());
        verify(laboratoryReportRepository).findById(reportId);
    }

    @Test
    void getLabReportById_whenReportDoesNotExist_returnsEmptyOptional() {
        Long reportId = 2L;
        when(laboratoryReportRepository.findById(reportId)).thenReturn(Optional.empty());

        Optional<LaboratoryReport> result = labReportService.getLabReportById(reportId);

        assertFalse(result.isPresent());
        verify(laboratoryReportRepository).findById(reportId);
    }
}
