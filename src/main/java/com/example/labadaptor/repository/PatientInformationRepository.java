package com.example.labadaptor.repository;

import com.example.labadaptor.model.PatientInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientInformationRepository extends JpaRepository<PatientInformation, Long> {

    /**
     * Finds a patient by their application-specific ID.
     * The 'patientId' field in PatientInformation entity maps to the 'patient_app_id' column.
     * @param patientId The application-specific ID of the patient.
     * @return An Optional containing the PatientInformation if found, or empty otherwise.
     */
    Optional<PatientInformation> findByPatientId(String patientId);
}
