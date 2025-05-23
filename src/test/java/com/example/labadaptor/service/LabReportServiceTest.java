package com.example.labadaptor.service;

import com.example.labadaptor.dto.LabReportRequestDTO;
import com.example.labadaptor.model.BaseReportData;
import com.example.labadaptor.model.LabReport;
import com.example.labadaptor.model.values.CrpValue;
import com.example.labadaptor.repository.LabReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabReportServiceTest {

    @Mock
    private LabReportRepository labReportRepository;

    // LabReportService creates its own ObjectMapper internally, so we don't mock it here.
    // We will use a separate ObjectMapper for test assertions if needed for JSON content.
    private final ObjectMapper testObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private LabReportService labReportService;

    private LabReportRequestDTO<CrpValue> requestDTO;
    private CrpValue crpValueData;
    private String reportType;
    private LocalDate reportDate;

    @BeforeEach
    void setUp() {
        reportDate = LocalDate.of(2024, 3, 15);
        crpValueData = new CrpValue();
        crpValueData.setValue(10.5);
        crpValueData.setUnit("mg/L");
        crpValueData.setReferenceRange(new CrpValue.ReferenceRange(0.0, 5.0));

        requestDTO = new LabReportRequestDTO<>();
        requestDTO.setReportDate(reportDate);
        requestDTO.setLabName("Test Lab");
        requestDTO.setPatientName("Test Patient");
        requestDTO.setPatientAge(30);
        requestDTO.setPatientSex("Male");
        requestDTO.setValues(crpValueData);

        reportType = "CRP";
    }

    @Test
    void createLabReport_shouldMapDtoToEntityAndSaveChanges() throws JsonProcessingException {
        LabReport savedLabReportMock = new LabReport();
        savedLabReportMock.setId(1L);
        savedLabReportMock.setReportType(reportType);
        
        BaseReportData commonDataMock = new BaseReportData(reportDate, "Test Lab", "Test Patient", 30, "Male");
        savedLabReportMock.setCommonData(commonDataMock);
        savedLabReportMock.setSpecificValuesJson(testObjectMapper.writeValueAsString(crpValueData));

        when(labReportRepository.save(any(LabReport.class))).thenReturn(savedLabReportMock);

        LabReport result = labReportService.createLabReport(requestDTO, reportType);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(reportType, result.getReportType());

        ArgumentCaptor<LabReport> labReportCaptor = ArgumentCaptor.forClass(LabReport.class);
        verify(labReportRepository).save(labReportCaptor.capture());
        LabReport capturedReport = labReportCaptor.getValue();

        // Verify common data mapping
        assertEquals(requestDTO.getReportDate(), capturedReport.getCommonData().getReportDate());
        assertEquals(requestDTO.getLabName(), capturedReport.getCommonData().getLabName());
        assertEquals(requestDTO.getPatientName(), capturedReport.getCommonData().getPatientName());
        assertEquals(requestDTO.getPatientAge(), capturedReport.getCommonData().getPatientAge());
        assertEquals(requestDTO.getPatientSex(), capturedReport.getCommonData().getPatientSex());

        // Verify specific values JSON serialization
        String expectedJson = testObjectMapper.writeValueAsString(requestDTO.getValues());
        assertEquals(expectedJson, capturedReport.getSpecificValuesJson());
        
        // Verify the returned object is what the repository's save method returned
        assertEquals(savedLabReportMock.getSpecificValuesJson(), result.getSpecificValuesJson());
    }

    @Test
    void getLabReportById_whenReportExists_shouldReturnReport() {
        Long reportId = 1L;
        LabReport mockReport = new LabReport();
        mockReport.setId(reportId);
        mockReport.setReportType("TSH");
        mockReport.setCommonData(new BaseReportData(reportDate, "Another Lab", "Jane Doe", 45, "Female"));
        mockReport.setSpecificValuesJson("{\"value\": 2.5, \"unit\": \"µIU/mL\"}"); // Example JSON

        when(labReportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));

        Optional<LabReport> result = labReportService.getLabReportById(reportId);

        assertTrue(result.isPresent());
        assertEquals(reportId, result.get().getId());
        assertEquals("TSH", result.get().getReportType());
        assertEquals("Jane Doe", result.get().getCommonData().getPatientName());
        verify(labReportRepository, times(1)).findById(reportId);
    }

    @Test
    void getLabReportById_whenReportDoesNotExist_shouldReturnEmptyOptional() {
        Long reportId = 2L;
        when(labReportRepository.findById(reportId)).thenReturn(Optional.empty());

        Optional<LabReport> result = labReportService.getLabReportById(reportId);

        assertFalse(result.isPresent(), "Expected Optional.empty() when report does not exist.");
        verify(labReportRepository, times(1)).findById(reportId);
    }
}
