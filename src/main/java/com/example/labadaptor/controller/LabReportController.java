package com.example.labadaptor.controller;

import com.example.labadaptor.dto.LabReportRequestDTO;
import com.example.labadaptor.model.LabReport;
import com.example.labadaptor.model.values.*; // Import all value types
import com.example.labadaptor.service.JsonSchemaValidationService;
import com.example.labadaptor.service.LabReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import com.example.labadaptor.model.BaseReportData; // Added for LabReportResponseDTO
import lombok.AllArgsConstructor; // Added for LabReportResponseDTO
import lombok.Data; // Added for LabReportResponseDTO
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor // Lombok for constructor injection
public class LabReportController {

    private final LabReportService labReportService;
    private final JsonSchemaValidationService schemaValidationService;
    private final ObjectMapper objectMapper; // Spring Boot provides a pre-configured one

    // Helper method to determine the class type for the 'values' field based on reportType
    private Class<?> getValueType(String reportType) {
        switch (reportType.toUpperCase()) {
            case "CRP": return CrpValue.class;
            case "ANTI_TPO": return AntiTpoValue.class;
            case "ANTI_TG": return AntiTgValue.class;
            case "TSH": return TshValue.class;
            case "FREE_T4": return FreeT4Value.class;
            case "VITAMIN_D": return VitaminDValue.class;
            case "SELENIUM": return SeleniumValue.class;
            default: throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    @PostMapping("/{reportType}")
    public ResponseEntity<?> createLabReport(@PathVariable String reportType,
                                             @RequestBody String rawJsonBody) { // Accept raw JSON string first
        try {
            // 1. JSON Schema Validation
            JsonNode jsonNode = objectMapper.readTree(rawJsonBody); // rawJsonBody is the JSON string from the request
            Set<ValidationMessage> schemaErrors = schemaValidationService.validate(reportType.toUpperCase(), jsonNode);
            if (!schemaErrors.isEmpty()) {
                String errors = schemaErrors.stream()
                                          .map(ValidationMessage::getMessage)
                                          .collect(Collectors.joining(", "));
                return ResponseEntity.badRequest().body("JSON schema validation failed: " + errors);
            }

            // 2. Deserialize to DTO with correct 'values' type
            Class<?> valueClass = getValueType(reportType);
            // Construct the specific type for LabReportRequestDTO<SpecificValueType>
            LabReportRequestDTO<?> requestDTO = (LabReportRequestDTO<?>) objectMapper.readValue(rawJsonBody,
                objectMapper.getTypeFactory().constructParametricType(LabReportRequestDTO.class, valueClass));
            
            // Note: Spring's @Valid bean validation on LabReportRequestDTO fields (e.g. @NotNull on reportDate)
            // and on the fields of the 'values' object (e.g. @NotNull on CrpValue.value)
            // will be triggered automatically by Spring when it deserializes `rawJsonBody` into the `requestDTO` object,
            // if the controller method were to take `@Valid @RequestBody LabReportRequestDTO<SpecificValueType> dto`.
            // Since we are manually deserializing after schema validation, if we want to also trigger bean validation,
            // we would need to inject a Validator and call validator.validate(requestDTO) manually.
            // For this iteration, we rely on schema validation for structure and DTO/POJO field annotations for constraints
            // which are expected to be picked up by Jackson during its deserialization process if annotations are correctly configured
            // (e.g. @JsonDeserialize with a custom deserializer that triggers validation, or if using Hibernate Validator with Jackson).
            // The current setup relies on the @Valid within the DTOs being checked by the framework, which is standard.

            LabReport createdReport = labReportService.createLabReport(requestDTO, reportType.toUpperCase());
            return new ResponseEntity<>(createdReport, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) { // Catches unknown report type
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (JsonProcessingException e) { // Catches errors during JSON parsing or DTO construction
            return ResponseEntity.badRequest().body("Invalid JSON format or data: " + e.getMessage());
        } catch (Exception e) { // Catch-all for other unexpected errors
            // Log the exception for server-side diagnostics
            // logger.error("Unexpected error during report creation for type {}: {}", reportType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{reportType}/{id}")
    public ResponseEntity<?> getLabReportById(@PathVariable String reportType, @PathVariable Long id) {
        return labReportService.getLabReportById(id)
                .filter(report -> report.getReportType().equalsIgnoreCase(reportType)) // Ensure correct report type
                .map(report -> {
                    try {
                        Class<?> valueClass = getValueType(report.getReportType());
                        Object valuesPojo = objectMapper.readValue(report.getSpecificValuesJson(), valueClass);
                        
                        // Create a temporary map or a dedicated response DTO to return structured data
                        // This avoids sending the raw specificValuesJson and provides the typed 'values'
                        LabReportResponseDTO response = new LabReportResponseDTO(report.getId(), report.getReportType(), report.getCommonData(), valuesPojo);
                        return ResponseEntity.ok(response);
                    } catch (IllegalArgumentException e) { // Unknown report type from DB?
                        // logger.error("Error processing report id {}: Unknown report type '{}' found in database.", id, report.getReportType(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing report: Invalid report type in stored data.");
                    }
                    catch (JsonProcessingException e) {
                        // logger.error("Error deserializing specificValuesJson for report id {}: {}", id, e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing report values: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Placeholder for a generic response DTO to structure the GET response
    // This should ideally be in its own file in the 'dto' package
    @Data 
    @AllArgsConstructor
    private static class LabReportResponseDTO {
        private Long id;
        private String reportType;
        private BaseReportData commonData; 
        private Object values; 
    }
}
