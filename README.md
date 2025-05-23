# Lab Adaptor Service

The Lab Adaptor Service is a Spring Boot application designed to manage and serve lab report data via a RESTful API. It supports CRUD operations for various types of lab reports, validating incoming data against predefined JSON schemas.

## Features

- REST APIs for CRUD operations on lab reports.
- JSON schema validation for incoming POST/PUT requests.
- Support for multiple lab report types (CRP, TSH, anti_TPO, etc.).
- PostgreSQL integration for data persistence.
- Built with Java 21 and Maven.

## Prerequisites

- Java 21 JDK
- Maven 3.6+
- PostgreSQL server

## Configuration

1.  **Database Setup:**
    - Ensure you have a PostgreSQL database created for this application.
    - Update the database connection properties in `src/main/resources/application.properties`:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/your_actual_database_name
      spring.datasource.username=your_actual_postgres_username
      spring.datasource.password=your_actual_postgres_password
      # spring.jpa.hibernate.ddl-auto can be set to 'validate' in production
      ```
    Replace placeholder values with your actual PostgreSQL configuration.

## Build and Run

1.  **Clone the repository:**
    ```bash
    git clone <repository_url>
    cd lab_adaptor
    ```

2.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```

3.  **Run the application:**
    ```bash
    java -jar target/lab_adaptor-0.0.1-SNAPSHOT.jar
    ```
    (The JAR file name might vary based on the version in `pom.xml`)

The application will start on the default port (usually 8080).

## API Endpoints

The base URL for the API is `/reports`.

### Common Fields for all reports:

When sending data via POST or PUT, the following common fields are expected at the root of the JSON object:

- `reportDate` (string, format: `yyyy-MM-dd`): Date of the report.
- `labName` (string): Name of the lab.
- `patientName` (string): Name of the patient.
- `patientAge` (integer): Age of the patient.
- `patientSex` (string): Sex of the patient.

### Report-Specific Fields:

Report-specific values are nested under a `values` object.

---

### 1. Create Lab Report

- **Endpoint:** `POST /reports/{reportType}`
- **`{reportType}` can be one of:** `CRP`, `ANTI_TPO`, `ANTI_TG`, `TSH`, `FREE_T4`, `VITAMIN_D`, `SELENIUM`.
- **Request Body:** JSON object representing the lab report.
- **Description:** Creates a new lab report of the specified type. The request body is validated against the corresponding JSON schema.

**Example Request (`POST /reports/CRP`):**
```json
{
  "reportDate": "2023-10-26",
  "labName": "Central Lab",
  "patientName": "John Doe",
  "patientAge": 45,
  "patientSex": "Male",
  "values": {
    "value": 7.2,
    "unit": "mg/L",
    "reference_range": {
      "min": 0.0,
      "max": 5.0
    }
  }
}
```

**Example Response (201 Created):**
```json
{
  "id": 1,
  "reportType": "CRP",
  "commonData": {
    "reportDate": "2023-10-26",
    "labName": "Central Lab",
    "patientName": "John Doe",
    "patientAge": 45,
    "patientSex": "Male"
  },
  "specificValuesJson": "{\"value\":7.2,\"unit\":\"mg/L\",\"reference_range\":{\"min\":0.0,\"max\":5.0}}"
  // Note: The actual response from GET includes deserialized 'values'
}
```

---

### 2. Get Lab Report by ID

- **Endpoint:** `GET /reports/{reportType}/{id}`
- **Description:** Retrieves a specific lab report by its ID and type.

**Example Request:** `GET /reports/CRP/1`

**Example Response (200 OK):**
```json
// This is based on the controller returning LabReportResponseDTO or similar
{
  "id": 1,
  "reportType": "CRP",
  "commonData": {
    "reportDate": "2023-10-26",
    "labName": "Central Lab",
    "patientName": "John Doe",
    "patientAge": 45,
    "patientSex": "Male"
  },
  "values": { // Deserialized values
    "value": 7.2,
    "unit": "mg/L",
    "reference_range": {
      "min": 0.0,
      "max": 5.0
    }
  }
}
```
**Response (404 Not Found):** If the report with the given ID or type is not found.

---
*(Placeholder for other endpoints like GET all by type, PUT, DELETE, which are not yet implemented but planned)*

## Future Enhancements
- Add PUT and DELETE operations.
- Implement GET all reports by type with pagination and filtering.
- Enhance security with authentication and authorization.
- Improve error handling with `@ControllerAdvice`.
- Use database migrations (e.g., Flyway, Liquibase).

## Built With
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven](https://maven.apache.org/)
- [Java 21](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
- [PostgreSQL](https://www.postgresql.org/)
- [JSON Schema Validator (networknt)](https://github.com/networknt/json-schema-validator)
- [Lombok](https://projectlombok.org/)
