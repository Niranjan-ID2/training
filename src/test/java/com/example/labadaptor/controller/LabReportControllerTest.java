package com.example.labadaptor.controller;

import com.example.labadaptor.dto.LaboratoryReportDTO;
import com.example.labadaptor.dto.PatientInformationDTO;
import com.example.labadaptor.model.LaboratoryReport;
import com.example.labadaptor.service.JsonSchemaValidationService;
import com.example.labadaptor.service.LabReportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LabReportController.class)
class LabReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LabReportService labReportService;

    @MockBean
    private JsonSchemaValidationService schemaValidationService;

    private LaboratoryReportDTO validReportDTO;
    private LaboratoryReport sampleReportEntity;

    @BeforeEach
    void setUp() {
        // Setup validReportDTO
        validReportDTO = new LaboratoryReportDTO();
        validReportDTO.setLabCode(UUID.randomUUID().toString());
        validReportDTO.setLabName("Valid Lab");
        PatientInformationDTO patientInfo = new PatientInformationDTO();
        patientInfo.setName("John Doe");
        patientInfo.setAge(30);
        patientInfo.setGender("Male");
        patientInfo.setId("P123");
        validReportDTO.setPatientInformation(patientInfo);
        validReportDTO.setTestInformation(Collections.emptyList()); // Assuming empty list is valid for simplicity

        // Setup sampleReportEntity
        sampleReportEntity = new LaboratoryReport();
        sampleReportEntity.setId(1L);
        sampleReportEntity.setLabCode(validReportDTO.getLabCode());
        sampleReportEntity.setLabName(validReportDTO.getLabName());
        // ... other fields if needed for response assertion
    }

    @Test
    void createLabReport_validRequest_returnsCreated() throws Exception {
        when(schemaValidationService.validate(any(JsonNode.class), eq("LABORATORY_REPORT")))
                .thenReturn(Collections.emptySet());
        when(labReportService.createLabReport(any(LaboratoryReportDTO.class)))
                .thenReturn(sampleReportEntity);

        mockMvc.perform(post("/reports/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReportDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.labCode", is(validReportDTO.getLabCode())))
                .andExpect(jsonPath("$.labName", is(validReportDTO.getLabName())));
    }

    @Test
    void createLabReport_schemaValidationFails_returnsBadRequest() throws Exception {
        Set<ValidationMessage> schemaErrors = new HashSet<>();
        // The .path() method used here was incorrect for ValidationMessage.Builder
        // Path information is typically part of the message or derived by the validator.
        schemaErrors.add(ValidationMessage.builder().customMessage("Schema validation error at path $.lab_code").build());
        when(schemaValidationService.validate(any(JsonNode.class), eq("LABORATORY_REPORT")))
                .thenReturn(schemaErrors);

        mockMvc.perform(post("/reports/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReportDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsString("JSON schema validation failed: Schema validation error at path $.lab_code")));
    }

    @Test
    void createLabReport_beanValidationFails_returnsBadRequest() throws Exception {
        // Example: labCode is @NotBlank in DTO.
        LaboratoryReportDTO invalidDto = new LaboratoryReportDTO(); // labCode is null
        invalidDto.setLabName("Test Lab");
        // no need to mock schema validation if bean validation catches it first
        // Spring's @Valid on @RequestBody in controller method handles this

        mockMvc.perform(post("/reports/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                // The exact error message depends on how Spring formats bean validation errors
                .andExpect(jsonPath("$.labCode", containsString("cannot be blank")));
                // Or for a general check:
                // .andExpect(jsonPath("$.errors").exists());
    }
    
    @Test
    void createLabReport_JsonProcessingException_returnsBadRequest() throws Exception {
        // Simulate a scenario where objectMapper.valueToTree(reportDTO) in controller fails
        // This is hard to trigger directly with MockMvc if the DTO itself is fine for request binding.
        // However, if the incoming JSON string is malformed, it will be caught by Spring before controller method.
        // This test case is more about the controller's internal catch block for JsonProcessingException.
        // For now, we'll assume the service layer might throw it, or the schema validation step.

        // Let's assume the schemaValidationService.validate call itself might throw it, or the conversion to JsonNode
        // The current controller structure directly converts DTO to JsonNode. If DTO is fine, this won't be an issue.
        // A malformed JSON string test:
         mockMvc.perform(post("/reports/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"labCode\": \"123\", \"labName\": \"Test Lab\"")) // Missing closing brace for patientInfo
                .andExpect(status().isBadRequest()); // Spring Boot's default error handling for malformed JSON
    }


    @Test
    void getLabReportById_existingId_returnsOk() throws Exception {
        when(labReportService.getLabReportById(1L)).thenReturn(Optional.of(sampleReportEntity));

        mockMvc.perform(get("/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.labCode", is(sampleReportEntity.getLabCode())));
    }

    @Test
    void getLabReportById_nonExistingId_returnsNotFound() throws Exception {
        when(labReportService.getLabReportById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reports/2"))
                .andExpect(status().isNotFound());
    }
}
