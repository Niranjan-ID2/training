package com.example.labadaptor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Not using Spring context for this unit test for simplicity and speed
class JsonSchemaValidationServiceTest {

    private JsonSchemaValidationService jsonSchemaValidationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir; // JUnit 5 temporary directory

    @BeforeEach
    void setUp() throws IOException {
        jsonSchemaValidationService = new JsonSchemaValidationService();

        // Create a dummy schema file in a temporary directory that the service can load
        // This approach tests the schema loading mechanism more directly than mocking PathMatchingResourcePatternResolver
        // To do this, we need to adjust how JsonSchemaValidationService finds schemas.
        // The current service uses "classpath:schemas/*.json".
        // For a unit test without Spring, we could:
        // 1. Place a test schema in src/test/resources/schemas and ensure test classpath is set up.
        // 2. Modify the service to allow injecting the schema path (better for testability).

        // Let's assume src/test/resources/schemas/ is on the classpath for tests.
        // The file src/test/resources/schemas/CRP.json was created in a previous step.
        // We will rely on the classloader to find it.
        // The service's initSchemas() will be called manually here.
        jsonSchemaValidationService.initSchemas();
    }

    @Test
    void initSchemas_shouldLoadSchemasFromTestResources() {
        // After setUp, initSchemas() is called.
        // We expect the "CRP" schema from src/test/resources/schemas/CRP.json to be loaded.
        JsonNode validJson = objectMapper.createObjectNode().put("name", "Test");
        Set<ValidationMessage> errors = jsonSchemaValidationService.validate("CRP", validJson);
        assertTrue(errors.isEmpty(), "Schema 'CRP' should be loaded and validate correct JSON.");
    }


    @Test
    void whenValidJson_thenValidationPasses() throws Exception {
        // src/test/resources/schemas/CRP.json expects: { "name": "some_string" }
        JsonNode validJson = objectMapper.readTree("{\"name\": \"Jules\"}");
        Set<ValidationMessage> errors = jsonSchemaValidationService.validate("CRP", validJson);
        assertTrue(errors.isEmpty(), "Validation should pass for valid JSON against test CRP schema.");
    }

    @Test
    void whenInvalidJson_MissingRequiredField_thenValidationFails() throws Exception {
        JsonNode invalidJson = objectMapper.readTree("{\"age\": 30}"); // Missing 'name'
        Set<ValidationMessage> errors = jsonSchemaValidationService.validate("CRP", invalidJson);
        assertFalse(errors.isEmpty(), "Validation should fail for invalid JSON (missing required field).");
        // Example of checking a specific error message:
        assertTrue(errors.stream()
                        .anyMatch(msg -> msg.getMessage().contains("$.name: is missing but it is required")),
                "Error message should indicate 'name' is missing.");
    }
    
    @Test
    void whenInvalidJson_WrongType_thenValidationFails() throws Exception {
        JsonNode invalidJson = objectMapper.readTree("{\"name\": 123}"); // 'name' should be string
        Set<ValidationMessage> errors = jsonSchemaValidationService.validate("CRP", invalidJson);
        assertFalse(errors.isEmpty(), "Validation should fail for invalid JSON (wrong type).");
        assertTrue(errors.stream()
                        .anyMatch(msg -> msg.getMessage().contains("$.name: integer found, string expected")),
                "Error message should indicate wrong type for 'name'.");
    }


    @Test
    void whenSchemaNotFound_thenThrowsException() {
        JsonNode json = objectMapper.createObjectNode();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jsonSchemaValidationService.validate("NONEXISTENT_SCHEMA", json);
        });
        assertEquals("No schema found for name: NONEXISTENT_SCHEMA. Cannot validate.", exception.getMessage());
    }

    @Test
    void validate_withJsonString_Valid() throws Exception {
        Set<ValidationMessage> errors = jsonSchemaValidationService.validate("CRP", "{\"name\": \"Valid String\"}");
        assertTrue(errors.isEmpty(), "Validation should pass for a valid JSON string.");
    }

    @Test
    void validate_withJsonString_Invalid() throws Exception {
        Set<ValidationMessage> errors = jsonSchemaValidationService.validate("CRP", "{\"name\": 123}");
        assertFalse(errors.isEmpty(), "Validation should fail for an invalid JSON string.");
    }
    
    @Test
    void validate_withEmptyJsonString_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jsonSchemaValidationService.validate("CRP", "");
        });
        assertEquals("JSON string cannot be null or empty for schema: CRP", exception.getMessage());
    }

    @Test
    void validate_withNullJsonString_ThrowsException() {
         Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            jsonSchemaValidationService.validate("CRP", (String) null);
        });
        assertEquals("JSON string cannot be null or empty for schema: CRP", exception.getMessage());
    }
}
