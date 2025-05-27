package com.example.labadaptor.repository;

import com.example.labadaptor.model.ContactInformation;
import com.example.labadaptor.model.PatientInformation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PatientInformationRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientInformationRepository patientInformationRepository;

    @Test
    void findByPatientId_whenPatientExists_returnsPatient() {
        // Given
        String patientAppId = "PAT_ID_" + UUID.randomUUID().toString();
        PatientInformation patient = new PatientInformation();
        patient.setPatientId(patientAppId);
        patient.setName("John Doe");
        patient.setAge(30);
        patient.setGender("Male");
        entityManager.persist(patient);
        entityManager.flush();

        // When
        Optional<PatientInformation> foundPatientOpt = patientInformationRepository.findByPatientId(patientAppId);

        // Then
        assertThat(foundPatientOpt).isPresent();
        assertThat(foundPatientOpt.get().getPatientId()).isEqualTo(patientAppId);
        assertThat(foundPatientOpt.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByPatientId_whenPatientDoesNotExist_returnsEmpty() {
        // Given
        String nonExistentPatientAppId = "NON_EXISTENT_PAT_ID";

        // When
        Optional<PatientInformation> foundPatientOpt = patientInformationRepository.findByPatientId(nonExistentPatientAppId);

        // Then
        assertThat(foundPatientOpt).isNotPresent();
    }

    @Test
    void persistPatientWithContactInformation_retrievesPatientAndContacts() {
        // Given
        String patientAppId = "PAT_CONTACT_" + UUID.randomUUID().toString();
        PatientInformation patient = new PatientInformation();
        patient.setPatientId(patientAppId);
        patient.setName("Jane Smith");
        patient.setAge(45);
        patient.setGender("Female");

        List<ContactInformation> contacts = new ArrayList<>();
        ContactInformation emailContact = new ContactInformation();
        emailContact.setEmail("jane.smith@example.com");
        emailContact.setPatientInformation(patient);
        contacts.add(emailContact);

        ContactInformation phoneContact = new ContactInformation();
        phoneContact.setPhone("555-001122");
        phoneContact.setPatientInformation(patient);
        contacts.add(phoneContact);

        patient.setContactInformation(contacts);

        // When
        PatientInformation savedPatient = patientInformationRepository.save(patient);
        entityManager.flush(); // Ensure persistence
        entityManager.clear(); // Clear persistence context to ensure fresh load

        Optional<PatientInformation> retrievedPatientOpt = patientInformationRepository.findById(savedPatient.getId());

        // Then
        assertThat(retrievedPatientOpt).isPresent();
        PatientInformation retrievedPatient = retrievedPatientOpt.get();
        assertThat(retrievedPatient.getName()).isEqualTo("Jane Smith");
        assertThat(retrievedPatient.getPatientId()).isEqualTo(patientAppId);

        assertThat(retrievedPatient.getContactInformation()).isNotNull();
        assertThat(retrievedPatient.getContactInformation()).hasSize(2);
        assertThat(retrievedPatient.getContactInformation())
                .extracting(ContactInformation::getEmail)
                .contains("jane.smith@example.com");
        assertThat(retrievedPatient.getContactInformation())
                .extracting(ContactInformation::getPhone)
                .contains("555-001122");
    }
}
