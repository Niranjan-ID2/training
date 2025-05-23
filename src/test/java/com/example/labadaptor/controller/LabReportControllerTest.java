package com.example.labadaptor.controller;

import com.example.labadaptor.dto.LabReportRequestDTO;
import com.example.labadaptor.model.BaseReportData;
import com.example.labadaptor.model.LabReport;
import com.example.labadaptor.model.values.CrpValue;
import com.example.labadaptor.service.JsonSchemaValidationService;
import com.example.labadaptor.service.LabReportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*; // Import verify
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString); // For error message checks


@ExtendWith(SpringExtension.class)
@WebMvcTest(LabReportController.class)
class LabReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Provided by Spring Boot for JSON conversions

    @MockBean
    private LabReportService labReportService;

    @MockBean
    private JsonSchemaValidationService schemaValidationService;

    @Test
    void createLabReport_whenValidRequest_shouldReturnCreated() throws Exception {
        String reportType = "CRP";
        CrpValue.ReferenceRange crpRefRange = new CrpValue.ReferenceRange(0.0, 5.0);
        CrpValue crpValue = new CrpValue(5.0, "mg/L", crpRefRange);
        
        LabReportRequestDTO<CrpValue> requestContent = new LabReportRequestDTO<>(
            LocalDate.now(), "Test Lab", "John Doe", 30, "Male", crpValue);
        String rawJsonBody = objectMapper.writeValueAsString(requestContent);

        LabReport mockSavedReport = new LabReport();
        mockSavedReport.setId(1L);
        mockSavedReport.setReportType(reportType.toUpperCase());
        BaseReportData commonData = new BaseReportData(requestContent.getReportDate(), requestContent.getLabName(), requestContent.getPatientName(), requestContent.getPatientAge(), requestContent.getPatientSex());
        mockSavedReport.setCommonData(commonData);
        mockSavedReport.setSpecificValuesJson(objectMapper.writeValueAsString(crpValue)); // Controller expects this to be raw JSON

        // Mock schema validation to pass
        when(schemaValidationService.validate(anyString(), any(JsonNode.class))).thenReturn(Collections.emptySet());
        // Mock service layer
        // The controller constructs its own DTO, so we need to match that, or use a captor.
        // For simplicity, using any() for the DTO type for now, but more specific matching or captor is better.
        when(labReportService.createLabReport(any(LabReportRequestDTO.class), eq(reportType.toUpperCase()))).thenReturn(mockSavedReport);

        mockMvc.perform(post("/reports/" + reportType)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawJsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reportType", is(reportType.toUpperCase())))
                .andExpect(jsonPath("$.commonData.patientName", is("John Doe")));
        
        verify(schemaValidationService).validate(eq(reportType.toUpperCase()), any(JsonNode.class));
        verify(labReportService).createLabReport(any(LabReportRequestDTO.class), eq(reportType.toUpperCase()));
    }

    @Test
    void createLabReport_whenSchemaValidationFails_shouldReturnBadRequest() throws Exception {
        String reportType = "CRP";
        // A valid JSON structure, but schema validation will be mocked to fail
        String rawJsonBody = "{\"reportDate\": \"2024-01-01\", \"labName\": \"LabX\", \"patientName\": \"Test\", \"patientAge\": 20, \"patientSex\": \"M\", \"values\": {\"value\": 10}}"; 

        Set<ValidationMessage> schemaErrors = new HashSet<>();
        ValidationMessage error = ValidationMessage.builder().customMessage("Schema validation error").path("$.field").build();
        schemaErrors.add(error);
        when(schemaValidationService.validate(anyString(), any(JsonNode.class))).thenReturn(schemaErrors);

        mockMvc.perform(post("/reports/" + reportType)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawJsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsString("JSON schema validation failed: Schema validation error")));
    }
    
    @Test
    void createLabReport_whenInvalidJsonFormat_shouldReturnBadRequest() throws Exception {
        String reportType = "CRP";
        String malformedJsonBody = "{\"reportDate\": \"2024-01-01\", \"labName\": \"LabX\""; // Malformed

        // No need to mock schemaValidationService as Jackson parsing will fail first
        mockMvc.perform(post("/reports/" + reportType)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJsonBody))
                .andExpect(status().isBadRequest())
                // The controller's catch (JsonProcessingException e) should handle this.
                .andExpect(jsonPath("$", containsString("Invalid JSON format or data"))); 
    }


    @Test
    void createLabReport_whenUnknownReportType_shouldReturnBadRequest() throws Exception {
        String reportType = "UNKNOWN_TYPE";
        CrpValue.ReferenceRange crpRefRange = new CrpValue.ReferenceRange(0.0, 5.0);
        CrpValue crpValue = new CrpValue(5.0, "mg/L", crpRefRange);
        LabReportRequestDTO<CrpValue> requestContent = new LabReportRequestDTO<>(
            LocalDate.now(), "Test Lab", "John Doe", 30, "Male", crpValue);
        String rawJsonBody = objectMapper.writeValueAsString(requestContent);

        // Mock schema validation to pass, so it reaches the unknown type check
        when(schemaValidationService.validate(anyString(), any(JsonNode.class))).thenReturn(Collections.emptySet());

        mockMvc.perform(post("/reports/" + reportType)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawJsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", is("Unknown report type: " + reportType)));
    }


    @Test
    void getLabReportById_whenReportExistsAndTypeMatches_shouldReturnOk() throws Exception {
        String reportType = "CRP";
        Long reportId = 1L;
        CrpValue.ReferenceRange crpRefRange = new CrpValue.ReferenceRange(0.0, 5.0);
        CrpValue crpValue = new CrpValue(5.0, "mg/L", crpRefRange);

        LabReport mockReport = new LabReport();
        mockReport.setId(reportId);
        mockReport.setReportType(reportType.toUpperCase());
        mockReport.setCommonData(new BaseReportData(LocalDate.now(), "Some Lab", "Jane Doe", 25, "Female"));
        mockReport.setSpecificValuesJson(objectMapper.writeValueAsString(crpValue));

        when(labReportService.getLabReportById(reportId)).thenReturn(Optional.of(mockReport));

        mockMvc.perform(get("/reports/" + reportType + "/" + reportId))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print()) // Print response for debugging
                .andExpect(jsonPath("$.id", is(reportId.intValue())))
                .andExpect(jsonPath("$.reportType", is(reportType.toUpperCase())))
                .andExpect(jsonPath("$.commonData.patientName", is("Jane Doe")))
                .andExpect(jsonPath("$.values.value", is(5.0)))
                .andExpect(jsonPath("$.values.unit", is("mg/L")))
                .andExpect(jsonPath("$.values.referenceRange.min", is(0.0)))
                .andExpect(jsonPath("$.values.referenceRange.max", is(5.0)));
    }

    @Test
    void getLabReportById_whenReportNotFound_shouldReturnNotFound() throws Exception {
        String reportType = "CRP";
        Long reportId = 1L;
        when(labReportService.getLabReportById(reportId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reports/" + reportType + "/" + reportId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLabReportById_whenReportExistsButTypeMismatches_shouldReturnNotFound() throws Exception {
        String pathReportType = "CRP";
        String actualReportTypeInDb = "TSH"; // Different type stored in DB
        Long reportId = 1L;

        LabReport mockReport = new LabReport();
        mockReport.setId(reportId);
        mockReport.setReportType(actualReportTypeInDb.toUpperCase()); // This is what's "in the database"
        mockReport.setCommonData(new BaseReportData(LocalDate.now(), "Some Lab", "Jane Doe", 25, "Female"));
        // Specific values JSON content doesn't strictly matter for this test path, but make it valid
        mockReport.setSpecificValuesJson("{\"value\": 2.0, \"unit\": \"mIU/L\", \"reference_range\": {\"min\": 0.5, \"max\": 4.5}}");

        when(labReportService.getLabReportById(reportId)).thenReturn(Optional.of(mockReport));

        mockMvc.perform(get("/reports/" + pathReportType + "/" + reportId))
                .andExpect(status().isNotFound()); // Because controller filters by type, expects path type to match DB type
    }
}
