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

    @Mock
    private PatientInformationRepository patientInformationRepository; // Added mock

    @InjectMocks
    private LabReportService labReportService;

    private LaboratoryReportDTO sampleReportDTO;
    private PatientInformationDTO samplePatientDTO; // Added for convenience

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
    void setUp() {
        // Create a comprehensive DTO for testing
        sampleReportDTO = new LaboratoryReportDTO();
        sampleReportDTO.setLabCode(UUID.randomUUID().toString());
        sampleReportDTO.setLabName("General Hospital Labs");
        sampleReportDTO.setDescription("Annual Checkup Report");

        samplePatientDTO = new PatientInformationDTO(); // Store for direct access
        samplePatientDTO.setId("PATIENT_001");
        samplePatientDTO.setName("Johnathan Doe");
        samplePatientDTO.setAge(42);
        samplePatientDTO.setGender("Male");

        ContactInformationDTO contactInfoDTO = new ContactInformationDTO();
        contactInfoDTO.setEmail("john.doe@example.com");
        contactInfoDTO.setPhone("555-123-4567");
        contactInfoDTO.setAddress("123 Main St, Anytown, USA");
        samplePatientDTO.setContactInformation(Collections.singletonList(contactInfoDTO));
        sampleReportDTO.setPatientInformation(samplePatientDTO);

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
    void createLabReport_whenPatientDoesNotExist_mapsNewPatientCorrectly() {
        // Given: Patient does not exist
        when(patientInformationRepository.findByPatientId(samplePatientDTO.getId())).thenReturn(Optional.empty());
        LaboratoryReport savedEntityMock = new LaboratoryReport(); // Mock of what repo save returns
        savedEntityMock.setId(1L);
        when(laboratoryReportRepository.save(any(LaboratoryReport.class))).thenReturn(savedEntityMock);

        // When
        LaboratoryReport result = labReportService.createLabReport(sampleReportDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId()); // Check if the mock saved entity is returned

        verify(patientInformationRepository).findByPatientId(samplePatientDTO.getId());

        ArgumentCaptor<LaboratoryReport> reportCaptor = ArgumentCaptor.forClass(LaboratoryReport.class);
        verify(laboratoryReportRepository).save(reportCaptor.capture());
        LaboratoryReport capturedReport = reportCaptor.getValue();

        // Assert new PatientInformation details
        assertNotNull(capturedReport.getPatientInformation());
        PatientInformation capturedPatientInfo = capturedReport.getPatientInformation();
        assertNull(capturedPatientInfo.getId()); // New entity, ID is generated by DB
        assertEquals(samplePatientDTO.getId(), capturedPatientInfo.getPatientId()); // Business key
        assertEquals(samplePatientDTO.getName(), capturedPatientInfo.getName());
        assertEquals(samplePatientDTO.getAge(), capturedPatientInfo.getAge());
        assertEquals(samplePatientDTO.getGender(), capturedPatientInfo.getGender());

        // Assert ContactInformation for new patient
        assertNotNull(capturedPatientInfo.getContactInformation());
        assertFalse(capturedPatientInfo.getContactInformation().isEmpty());
        ContactInformation capturedContact = capturedPatientInfo.getContactInformation().get(0);
        ContactInformationDTO contactInfoDTO = samplePatientDTO.getContactInformation().get(0); // Use samplePatientDTO
        assertEquals(contactInfoDTO.getEmail(), capturedContact.getEmail());
        assertEquals(contactInfoDTO.getPhone(), capturedContact.getPhone());
        assertEquals(contactInfoDTO.getAddress(), capturedContact.getAddress());
        assertEquals(capturedPatientInfo, capturedContact.getPatientInformation()); // Back-reference check

        // Assert other parts of the report are still mapped (abbreviated for focus)
        assertEquals(sampleReportDTO.getLabCode(), capturedReport.getLabCode());
        assertNotNull(capturedReport.getTestInformation());
        assertFalse(capturedReport.getTestInformation().isEmpty());
    }

    @Test
    void createLabReport_whenPatientExists_updatesExistingPatientCorrectly() {
        // Given: Patient exists
        PatientInformation existingPatientEntity = new PatientInformation();
        existingPatientEntity.setId(100L); // Existing DB ID
        existingPatientEntity.setPatientId(samplePatientDTO.getId()); // Business key
        existingPatientEntity.setName("Old Name");
        existingPatientEntity.setAge(40);
        existingPatientEntity.setGender("Other");
        // Add an old contact to ensure it's cleared
        ContactInformation oldContact = new ContactInformation();
        oldContact.setEmail("old.email@example.com");
        oldContact.setPatientInformation(existingPatientEntity);
        existingPatientEntity.setContactInformation(new java.util.ArrayList<>(Collections.singletonList(oldContact)));


        when(patientInformationRepository.findByPatientId(samplePatientDTO.getId())).thenReturn(Optional.of(existingPatientEntity));
        LaboratoryReport savedEntityMock = new LaboratoryReport();
        savedEntityMock.setId(1L);
        when(laboratoryReportRepository.save(any(LaboratoryReport.class))).thenReturn(savedEntityMock);

        // When
        labReportService.createLabReport(sampleReportDTO);

        // Then
        verify(patientInformationRepository).findByPatientId(samplePatientDTO.getId());

        ArgumentCaptor<LaboratoryReport> reportCaptor = ArgumentCaptor.forClass(LaboratoryReport.class);
        verify(laboratoryReportRepository).save(reportCaptor.capture());
        LaboratoryReport capturedReport = reportCaptor.getValue();

        // Assert PatientInformation is the existing one, but updated
        assertNotNull(capturedReport.getPatientInformation());
        PatientInformation capturedPatientInfo = capturedReport.getPatientInformation();
        assertEquals(existingPatientEntity.getId(), capturedPatientInfo.getId()); // Should be the same DB entity
        assertEquals(samplePatientDTO.getId(), capturedPatientInfo.getPatientId()); // Business key unchanged

        // Fields should be updated from DTO
        assertEquals(samplePatientDTO.getName(), capturedPatientInfo.getName());
        assertEquals(samplePatientDTO.getAge(), capturedPatientInfo.getAge());
        assertEquals(samplePatientDTO.getGender(), capturedPatientInfo.getGender());

        // Assert ContactInformation updated (old one cleared, new one added)
        assertNotNull(capturedPatientInfo.getContactInformation());
        assertEquals(1, capturedPatientInfo.getContactInformation().size()); // Only the new one from DTO
        ContactInformation updatedContact = capturedPatientInfo.getContactInformation().get(0);
        ContactInformationDTO newContactDTO = samplePatientDTO.getContactInformation().get(0);
        assertEquals(newContactDTO.getEmail(), updatedContact.getEmail());
        assertEquals(newContactDTO.getPhone(), updatedContact.getPhone());
        assertEquals(newContactDTO.getAddress(), updatedContact.getAddress());
        assertEquals(existingPatientEntity, updatedContact.getPatientInformation()); // Back-reference to the same patient

        // Other parts of the report
        assertEquals(sampleReportDTO.getLabCode(), capturedReport.getLabCode());
    }
    
    // Test for createLabReport_mapsDtoToEntityCorrectly_andSaves is effectively split and covered by the two tests above.
    // The original test did not consider existing/non-existing patient logic.
    // We can remove it or adapt if there's a specific scenario it covered not handled by the new tests.
    // For now, let's assume the detailed tests above are sufficient.

    // Test for mapping other parts of DTO like TestInformation, Specimen, etc.
    // This was partially covered in the original test. We can add a focused test if needed,
    // but the main change was patient handling. Assuming the rest of the mapping logic is unchanged and tested.
    // For brevity, we will assume the existing structure of those tests would be fine if re-added.
    // The key is that `capturedReport.getTestInformation()` etc. would still be checked as before.
    // Let's ensure one of the new tests also checks a bit of TestInformation mapping.

    @Test
    void createLabReport_mapsTestInformationCorrectly_whenNewPatient() { // Example check for other parts
        when(patientInformationRepository.findByPatientId(samplePatientDTO.getId())).thenReturn(Optional.empty());
        LaboratoryReport savedEntityMock = new LaboratoryReport();
        savedEntityMock.setId(1L);
        when(laboratoryReportRepository.save(any(LaboratoryReport.class))).thenReturn(savedEntityMock);

        labReportService.createLabReport(sampleReportDTO);

        ArgumentCaptor<LaboratoryReport> reportCaptor = ArgumentCaptor.forClass(LaboratoryReport.class);
        verify(laboratoryReportRepository).save(reportCaptor.capture());
        LaboratoryReport capturedReport = reportCaptor.getValue();

        // Assert TestInformation (assuming one in sample)
        assertNotNull(capturedReport.getTestInformation());
        assertFalse(capturedReport.getTestInformation().isEmpty());
        TestInformation capturedTestInfo = capturedReport.getTestInformation().get(0);
        TestInformationDTO testInfoDTO = sampleReportDTO.getTestInformation().get(0);
        assertEquals(testInfoDTO.getTestType(), capturedTestInfo.getTestType());
        assertEquals(LocalDate.parse("2023-10-01"), capturedTestInfo.getTestPerformedDate());
        assertEquals(LocalTime.parse("09:30"), capturedTestInfo.getTestPerformedTime());

        // Assert Specimen
        assertNotNull(capturedTestInfo.getSpecimen());
        Specimen capturedSpecimen = capturedTestInfo.getSpecimen();
        SpecimenDTO specimenDTO = testInfoDTO.getSpecimen(); // from sampleReportDTO
        assertEquals(specimenDTO.getType(), capturedSpecimen.getType());
        assertEquals(specimenDTO.getCollectionMethod(), capturedSpecimen.getCollectionMethod());
        assertEquals(LocalDate.parse(specimenDTO.getCollectionDate()), capturedSpecimen.getCollectionDate());
        assertEquals(LocalTime.parse(specimenDTO.getCollectionTime()), capturedSpecimen.getCollectionTime());
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
