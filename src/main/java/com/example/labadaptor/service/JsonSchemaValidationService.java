package com.example.labadaptor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JsonSchemaValidationService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidationService.class);
    private final Map<String, JsonSchema> schemas = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void initSchemas() {
        // Using V7 as specified in task, compatible with simple schemas provided
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V7); 
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:schemas/*.json");
            if (resources.length == 0) {
                logger.warn("No JSON schemas found in classpath:schemas/");
                // Depending on application requirements, this might be a critical error.
                // For now, we'll log a warning and continue.
            }
            for (Resource resource : resources) {
                try (InputStream schemaStream = resource.getInputStream()) {
                    JsonSchema schema = schemaFactory.getSchema(schemaStream);
                    String schemaName = resource.getFilename();
                    if (schemaName != null) {
                        // Remove .json extension to get the plain name like "CRP"
                        String key = schemaName.substring(0, schemaName.lastIndexOf('.'));
                        schemas.put(key, schema);
                        logger.info("Loaded schema: {} as key: {}", schemaName, key);
                    } else {
                        logger.warn("Could not determine filename for schema resource: {}", resource.getDescription());
                    }
                } catch (Exception e) {
                    logger.error("Error loading schema: {}", resource.getFilename(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load JSON schemas from classpath:schemas/", e);
            // Consider if application should fail to start if schemas are critical
            // For example, by re-throwing a runtime exception here.
        }
    }

    public Set<ValidationMessage> validate(String schemaName, JsonNode jsonNode) {
        JsonSchema schema = schemas.get(schemaName);
        if (schema == null) {
            logger.warn("No schema found for name: {}. Available schemas: {}", schemaName, schemas.keySet());
            // Throwing an exception makes it clear to the caller that validation cannot proceed.
            throw new IllegalArgumentException("No schema found for name: " + schemaName + ". Cannot validate.");
        }
        return schema.validate(jsonNode);
    }

    // Optional: Overload for validating a JSON string directly
    public Set<ValidationMessage> validate(String schemaName, String jsonString) throws Exception {
        if (jsonString == null || jsonString.trim().isEmpty()) {
             throw new IllegalArgumentException("JSON string cannot be null or empty for schema: " + schemaName);
        }
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        return validate(schemaName, jsonNode);
    }
}
