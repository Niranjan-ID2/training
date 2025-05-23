package com.example.labadaptor.service;

import com.example.labadaptor.dto.LabReportRequestDTO;
import com.example.labadaptor.model.BaseReportData;
import com.example.labadaptor.model.LabReport;
import com.example.labadaptor.repository.LabReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Ensure ObjectMapper is configured for Java Time
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
@Transactional
public class LabReportService {

    private final LabReportRepository labReportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); // Ensure JavaTimeModule is registered

    public LabReport createLabReport(LabReportRequestDTO<?> requestDTO, String reportType) throws JsonProcessingException {
        LabReport labReport = new LabReport();
        labReport.setReportType(reportType);

        BaseReportData commonData = new BaseReportData();
        commonData.setReportDate(requestDTO.getReportDate());
        commonData.setLabName(requestDTO.getLabName());
        commonData.setPatientName(requestDTO.getPatientName());
        commonData.setPatientAge(requestDTO.getPatientAge());
        commonData.setPatientSex(requestDTO.getPatientSex());
        labReport.setCommonData(commonData);

        // Serialize the 'values' part of the DTO to JSON string
        labReport.setSpecificValuesJson(objectMapper.writeValueAsString(requestDTO.getValues()));

        return labReportRepository.save(labReport);
    }

    @Transactional(readOnly = true)
    public Optional<LabReport> getLabReportById(Long id) {
        return labReportRepository.findById(id);
    }
    
    // Add methods for other CRUD operations (getAll, update, delete) later
}
