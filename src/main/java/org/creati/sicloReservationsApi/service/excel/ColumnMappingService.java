package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.postgre.ExcelColumnMappingRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.ExcelColumnMapping;
import org.springframework.stereotype.Service;

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
