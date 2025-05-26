package com.example.labadaptor.controller;

import com.example.labadaptor.dto.LaboratoryReportDTO;
import com.example.labadaptor.model.LaboratoryReport;
import com.example.labadaptor.service.JsonSchemaValidationService;
import com.example.labadaptor.service.LabReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import jakarta.validation.Valid;
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

    @PostMapping("/")
    public ResponseEntity<?> createLabReport(@Valid @RequestBody LaboratoryReportDTO reportDTO) {
        try {
            // Schema validation (assuming service takes JsonNode and uses a default schema for lab reports)
            JsonNode jsonNode = objectMapper.valueToTree(reportDTO);
            // Assuming schemaValidationService.validate(JsonNode) exists or will be created
            // For now, let's assume a generic schema key "LABORATORY_REPORT" or the service infers it.
            Set<ValidationMessage> schemaErrors = schemaValidationService.validate(jsonNode, "LABORATORY_REPORT");

            if (schemaErrors != null && !schemaErrors.isEmpty()) {
                String errors = schemaErrors.stream()
                                          .map(ValidationMessage::getMessage)
                                          .collect(Collectors.joining(", "));
                return ResponseEntity.badRequest().body("JSON schema validation failed: " + errors);
            }

            LaboratoryReport createdReport = labReportService.createLabReport(reportDTO);
            return new ResponseEntity<>(createdReport, HttpStatus.CREATED);

        } catch (JsonProcessingException e) { // From objectMapper.valueToTree if used
            return ResponseEntity.badRequest().body("Error processing report DTO: " + e.getMessage());
        } catch (IllegalArgumentException e) { // Catch potential errors from service layer
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) { // Catch-all for other unexpected errors
            // Log the exception for server-side diagnostics
            // logger.error("Unexpected error during report creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaboratoryReport> getLabReportById(@PathVariable Long id) {
        return labReportService.getLabReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
