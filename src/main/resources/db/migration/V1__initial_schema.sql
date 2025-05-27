-- Schema for patient_information
CREATE TABLE patient_information (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    age INTEGER,
    gender VARCHAR(255),
    patient_app_id VARCHAR(255) UNIQUE
);

-- Schema for contact_information
CREATE TABLE contact_information (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(255),
    email VARCHAR(255),
    address VARCHAR(255),
    patient_information_id BIGINT NOT NULL,
    CONSTRAINT fk_contact_patient_information
        FOREIGN KEY(patient_information_id)
        REFERENCES patient_information(id)
);

-- Schema for laboratory_reports
CREATE TABLE laboratory_reports (
    id BIGSERIAL PRIMARY KEY,
    lab_code VARCHAR(255),
    lab_name VARCHAR(255),
    description TEXT,
    patient_information_id BIGINT,
    CONSTRAINT fk_report_patient_information
        FOREIGN KEY(patient_information_id)
        REFERENCES patient_information(id),
    CONSTRAINT uk_report_patient_information_id UNIQUE (patient_information_id) -- For OneToOne
);

-- Schema for test_information
CREATE TABLE test_information (
    id BIGSERIAL PRIMARY KEY,
    test_type VARCHAR(255),
    test_performed_date DATE,
    test_performed_time TIME,
    test_reported_date DATE,
    test_reported_time TIME,
    -- Embedded Specimen
    specimen_type VARCHAR(255),
    specimen_collection_method VARCHAR(255),
    specimen_collection_date DATE,
    specimen_collection_time TIME,
    -- Embedded Interpretation
    interpretation_observations TEXT,
    interpretation_critical_alerts TEXT,
    interpretation_comments TEXT,
    -- Embedded PathologistLabTechnicianInformation
    pathologistLabTechnicianInformation_name VARCHAR(255),
    pathologistLabTechnicianInformation_contact VARCHAR(255),
    laboratory_report_id BIGINT NOT NULL,
    CONSTRAINT fk_testinfo_lab_report
        FOREIGN KEY(laboratory_report_id)
        REFERENCES laboratory_reports(id)
);

-- Schema for test_results
CREATE TABLE test_results (
    id BIGSERIAL PRIMARY KEY,
    parameter VARCHAR(255),
    value VARCHAR(255),
    units VARCHAR(255),
    comments TEXT,
    -- Embedded ReferenceRange
    referenceRange_min_range VARCHAR(255),
    referenceRange_max_range VARCHAR(255),
    test_information_id BIGINT NOT NULL,
    CONSTRAINT fk_testresult_test_info
        FOREIGN KEY(test_information_id)
        REFERENCES test_information(id)
);
