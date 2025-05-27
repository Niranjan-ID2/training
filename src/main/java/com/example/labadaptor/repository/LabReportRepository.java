package com.example.labadaptor.repository;

import com.example.labadaptor.model.LaboratoryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // Not strictly needed for now

@Repository
public interface LabReportRepository extends JpaRepository<LaboratoryReport, Long> {
    // Optional: Add custom query methods here later if needed
    // For example:
    // List<LaboratoryReport> findByReportType(String reportType);
    // List<LaboratoryReport> findByReportType(String reportType);
    // List<LaboratoryReport> findByCommonDataPatientName(String patientName);
}
