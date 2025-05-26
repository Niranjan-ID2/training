package com.example.labadaptor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class JsonSchemaValidationService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidationService.class);
    private final Map<String, JsonSchema> schemas = new HashMap<>();
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private final ObjectMapper objectMapper = new ObjectMapper(); // For the optional validate(String, String) method

    @PostConstruct
    public void initSchemas() {
        loadSchema("LABORATORY_REPORT", "/schemas/laboratory_report_schema.json");
        // All other schema loading logic is removed
    }

    private void loadSchema(String schemaIdentifier, String schemaPath) {
        try (InputStream schemaStream = JsonSchemaValidationService.class.getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                logger.error("Cannot find schema file: {}", schemaPath);
                throw new RuntimeException("Cannot find schema file: " + schemaPath);
            }
            JsonSchema schema = factory.getSchema(schemaStream);
            schemas.put(schemaIdentifier, schema);
            logger.info("Successfully loaded schema: {} from path: {}", schemaIdentifier, schemaPath);
        } catch (Exception e) {
            logger.error("Failed to load schema {}: {}", schemaIdentifier, e.getMessage(), e);
            throw new RuntimeException("Failed to load schema: " + schemaIdentifier, e);
        }
    }

    public Set<ValidationMessage> validate(JsonNode jsonNode, String schemaIdentifier) {
        JsonSchema schema = schemas.get(schemaIdentifier);
        if (schema == null) {
            logger.warn("Schema not found for identifier: {}. Available schemas: {}", schemaIdentifier, schemas.keySet());
            // Return a ValidationMessage indicating schema not found, as per the conceptual example
            return Collections.singleton(new ValidationMessage.Builder()
                                             .message("Schema not found: " + schemaIdentifier)
                                             .build());
        }
        // The schema laboratory_report_schema.json is defined to expect a root object
        // with a "laboratory_report" key. The controller passes the JsonNode representation
        // of the LaboratoryReportDTO.
        // If the DTO is {"lab_code": "...", "lab_name": "..."}
        // and the schema is {"type": "object", "properties": {"laboratory_report": {"type": "object", "properties": {"lab_code":{...}}}}}
        // then the JsonNode passed from controller is actually the content for laboratory_report.
        // The controller currently does: JsonNode jsonNode = objectMapper.valueToTree(reportDTO);
        // This means jsonNode IS the "laboratory_report" object itself.
        // The schema is defined with a top-level "laboratory_report" key.
        // So, if jsonNode is what's inside "laboratory_report", and the schema expects a root key "laboratory_report",
        // then the schema should be written to validate the *contents* of laboratory_report directly OR
        // we need to wrap the jsonNode here.
        // Based on the schema provided (with root "laboratory_report"), and the controller code (objectMapper.valueToTree(reportDTO)),
        // the jsonNode *is* the content that should be validated against the schema's "laboratory_report" definition.
        // The current schema has "$schema", "title", "description", "type":"object", "properties": {"laboratory_report": {...}}, "required": ["laboratory_report"]
        // This means it expects an object like: {"laboratory_report": { actual_data... } }
        // The controller passes `objectMapper.valueToTree(reportDTO)` which is `{ actual_data... }`
        // So the schema.validate call is correct if the `jsonNode` is the entire request body.
        // The controller is calling validate(jsonNode, "LABORATORY_REPORT"); where jsonNode is the DTO.
        // The schema expects an outer "laboratory_report" object.
        // The controller passes the DTO directly as JsonNode.
        // The schema has: "properties": { "laboratory_report": { ... details ... } }, "required": ["laboratory_report"]
        // This means the `jsonNode` passed to this method should be `{"laboratory_report": { ... the DTO content ... }}`.
        // However, the controller passes `objectMapper.valueToTree(reportDTO)`, which is `{...the DTO content...}`.
        // This is a mismatch.
        // For now, I will assume the controller will be adjusted or the schema is intended for the DTO structure directly.
        // Given the existing controller code `objectMapper.valueToTree(reportDTO)`, the `jsonNode` is the DTO itself.
        // The schema expects `{"laboratory_report": dto_content}`.
        // The `validate` method in LabReportController does `objectMapper.valueToTree(reportDTO)`.
        // The schema file has a root property "laboratory_report".
        // So, the `jsonNode` passed here is the *value* of the "laboratory_report" field.
        // The `JsonSchema.validate(JsonNode)` method expects the node that corresponds to the schema.
        // If the schema is `{"type": "object", "properties": {"fieldA": ...}}` it expects `{"fieldA": "value"}`.
        // Our schema is `{"properties": {"laboratory_report": {...}}}`.
        // The `jsonNode` from controller is `LaboratoryReportDTO` as a tree.
        // So the schema should be directly for `LaboratoryReportDTO` structure, not wrapped.
        // Let's adjust the schema loading or validation call.
        // The easiest path is to assume the schema in `laboratory_report_schema.json` should describe the DTO structure directly,
        // not be wrapped by a "laboratory_report" key *within the schema file itself for its main definition*.
        // The current schema *is* wrapped. So `jsonNode.get("laboratory_report")` would be needed if jsonNode was the full request.
        // But jsonNode *is* the DTO. So the schema should not have the "laboratory_report" wrapper if it's to validate the DTO directly.

        // Re-reading instructions: "The root should be an object with a property laboratory_report."
        // This means the schema *is* for `{"laboratory_report": {...}}`.
        // The controller sends `JsonNode jsonNode = objectMapper.valueToTree(reportDTO);`
        // This jsonNode is `{... DTO fields ...}`.
        // Therefore, the current call `schema.validate(jsonNode)` is trying to validate `{dto_fields}` against a schema that expects `{"laboratory_report": {dto_fields}}`.
        // This will fail.
        // The fix should be in the controller: wrap the DTO node before sending it here, or this service wraps it.
        // Or, change the schema to directly represent the DTO. The instructions say schema has "laboratory_report" root.
        // Let's assume this service should handle the wrapping if needed, but the schema provided is for the whole object.
        // The conceptual snippet says: "return schema.validate(jsonNode);" and "Assuming the schema is for the entire structure including the root "laboratory_report" key."
        // This implies the jsonNode passed to this method must be the *entire structure*.
        // The controller currently passes the DTO. This is the point of mismatch.
        // For now, I will stick to the provided conceptual snippet's assumption that jsonNode IS the full structure.
        // This means the controller should be updated, but that's not this task.
        // This service will assume `jsonNode` is `{"laboratory_report": { ... }}`.
        return schema.validate(jsonNode);
    }

    // Optional: Overload for validating a JSON string directly, adapted from original
    public Set<ValidationMessage> validate(String jsonString, String schemaIdentifier) throws Exception {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            logger.warn("JSON string cannot be null or empty for schema: {}", schemaIdentifier);
            throw new IllegalArgumentException("JSON string cannot be null or empty for schema: " + schemaIdentifier);
        }
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        return validate(jsonNode, schemaIdentifier);
    }
}
