package com.example.labadaptor.repository;

import com.example.labadaptor.model.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // Not strictly needed for now

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    // Optional: Add custom query methods here later if needed
    // For example:
    // List<LabReport> findByReportType(String reportType);
    // List<LabReport> findByCommonDataPatientName(String patientName);
}
