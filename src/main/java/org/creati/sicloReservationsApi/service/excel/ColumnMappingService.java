package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.creati.sicloReservationsApi.dao.postgre.ExcelColumnMappingRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.ExcelColumnMapping;
import org.creati.sicloReservationsApi.service.model.mapping.BulkUpdateColumnMappingRequest;
import org.creati.sicloReservationsApi.service.model.mapping.ColumnMappingDto;
import org.creati.sicloReservationsApi.service.model.job.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.mapping.UpdateColumnMappingRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ColumnMappingService {

    private final ExcelColumnMappingRepository columnMappingRepository;

    public ColumnMappingService(ExcelColumnMappingRepository columnMappingRepository) {
        this.columnMappingRepository = columnMappingRepository;
    }


    public List<ColumnMappingDto> getAllMappings() {
        return columnMappingRepository.findAll().stream()
                .map(ExcelColumnMapping::toDto)
                .toList();
    }

    public List<ColumnMappingDto> getMappingsByFileType(String fileType) {
        return columnMappingRepository.findByFileType(fileType).stream()
                .map(ExcelColumnMapping::toDto)
                .toList();
    }

    public ColumnMappingDto getMappingById(Long id) {
        return columnMappingRepository.findById(id)
                .map(ExcelColumnMapping::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Excel column mapping not found with id: " + id));
    }

    public ColumnMappingDto updateMapping(Long id, UpdateColumnMappingRequest updateRequest) {
        ExcelColumnMapping existingMapping = columnMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Excel column mapping not found with id: " + id));

        ExcelColumnMapping mappingModified = existingMapping.toBuilder()
                .excelHeader(updateRequest.getExcelHeader())
                .required(updateRequest.getRequired())
                .build();

        ExcelColumnMapping updatedMapping = columnMappingRepository.save(mappingModified);
        return updatedMapping.toDto();
    }

    public ProcessingResult bulkUpdateMappings(List<BulkUpdateColumnMappingRequest> updateRequests) {

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (BulkUpdateColumnMappingRequest request : updateRequests) {
            try {
                ExcelColumnMapping existingMapping = columnMappingRepository.findById(request.getMappingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Excel column mapping not found with id: " + request.getMappingId()));

                ExcelColumnMapping mappingModified = existingMapping.toBuilder()
                        .excelHeader(request.getExcelHeader())
                        .required(request.getRequired())
                        .build();

                columnMappingRepository.save(mappingModified);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                String errorMsg = String.format("Failed to update mapping with id %d: %s", request.getMappingId(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        return ProcessingResult.builder()
                .success(failureCount == 0)
                .totalProcessed(updateRequests.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();

    }

    public Map<String, String> getHeaderToFieldMapping(String fileType) {
        List<ExcelColumnMapping> mappings = columnMappingRepository.findByFileType(fileType);
        Map<String, String> headerToFieldMap = new HashMap<>();
        for (ExcelColumnMapping mapping: mappings) {
            headerToFieldMap.put(mapping.getExcelHeader(), mapping.getFieldName());
        }

        log.info("Loaded {} active column mappings for file type: {}", headerToFieldMap.size(), fileType);
        return headerToFieldMap;
    }

    public Boolean validateRequiredHeaders(Set<String> excelHeaders, String fileType) {

        List<ExcelColumnMapping> requiredMappings = columnMappingRepository.findByFileType(fileType).stream()
                .filter(ExcelColumnMapping::isRequired)
                .toList();

        Set<String> normalizedExcelHeaders = excelHeaders.stream()
                .map(header -> header.toLowerCase().trim())
                .collect(Collectors.toSet());

        List<String> missingHeaders = requiredMappings.stream()
                .map(ExcelColumnMapping::getExcelHeader)
                .filter(excelHeader -> !normalizedExcelHeaders.contains(excelHeader.toLowerCase().trim()))
                .toList();

        if (!missingHeaders.isEmpty()) {
            log.warn("Missing required headers for file type {}: {}", fileType, missingHeaders);
            return false;
        }

        return true;
    }


}
