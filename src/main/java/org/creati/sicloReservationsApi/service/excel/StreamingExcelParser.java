package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.service.excel.util.ExcelUtils;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class StreamingExcelParser {

    private final ColumnMappingService columnMappingService;

    public StreamingExcelParser(
            final ColumnMappingService columnMappingService) {
        this.columnMappingService = columnMappingService;
    }

    public void parseReservationsFromFile(
            File file,
            String fileType,
            int batchSize,
            EntityCacheService entityCacheService,
            BiConsumer<List<ReservationDto>, EntityCache> batchProcessor) throws IOException {

        try (InputStream inputStream = new FileInputStream(file);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream)) {

            Map<String, String> headerToFieldMap = columnMappingService.getHeaderToFieldMapping(fileType);
            Sheet sheet = xssfWorkbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("The Excel file is empty.");
            }

            // Build mapping of column indexes to field names
            Row headerRow = rowIterator.next();
            Map<Integer, String> columnIndexToFieldMap = new HashMap<>();
            for (Cell cell: headerRow) {
                String headerValue = ExcelUtils.getCellStringValue(cell);
                String fieldName = headerToFieldMap.get(headerValue);

                if (fieldName != null) {
                    columnIndexToFieldMap.put(cell.getColumnIndex(), fieldName);
                    log.debug("Mapped column {} '{}' to field '{}'", cell.getColumnIndex(), headerValue, fieldName);
                } else {
                    log.warn("No mapping found for header '{}' at column {}", headerValue, cell.getColumnIndex());
                }
            }

            // Validate required headers
            Set<String> excelHeaders = new HashSet<>();
            headerRow.forEach(cell -> excelHeaders.add(ExcelUtils.getCellStringValue(cell)));
            if (!columnMappingService.validateRequiredHeaders(excelHeaders, fileType)) {
                throw new IllegalArgumentException("Missing required headers in the Excel file.");
            }

            // Process data rows
            int rowIndex = 1; // Start after header
            List<ReservationDto> batch = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Continue if row is null or empty
                if (row == null || ExcelUtils.isEmptyRow(row)) {
                    rowIndex++;
                    continue;
                }

                ReservationDto dto = parseReservationRowDynamic(row, columnIndexToFieldMap);
                batch.add(dto);

                if (batch.size() >= batchSize) {
                    EntityCache cache = entityCacheService.preloadEntitiesForReservation(batch);
                    batchProcessor.accept(batch, cache);
                    batch.clear();
                }
            }

            // Process any remaining records in the last batch
            if (!batch.isEmpty()) {
                EntityCache cache = entityCacheService.preloadEntitiesForReservation(batch);
                batchProcessor.accept(batch, cache);
            }
        }

    }

    public void parsePaymentsFromFile(
            File file,
            String fileType,
            int batchSize,
            EntityCacheService entityCacheService,
            BiConsumer<List<PaymentDto>, EntityCache> batchProcessor) throws IOException {

        try (InputStream inputStream = new FileInputStream(file);
             XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream)) {

            Map<String, String> headerToFieldMap = columnMappingService.getHeaderToFieldMapping(fileType);
            Sheet sheet = xssfWorkbook.getSheet("M-pago");
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("The Excel file is empty.");
            }

            // Build mapping of column indexes to field names
            Row headerRow = rowIterator.next();
            Map<Integer, String> columnIndexToFieldMap = new HashMap<>();
            for (Cell cell: headerRow) {
                String headerValue = ExcelUtils.getCellStringValue(cell);
                String fieldName = headerToFieldMap.get(headerValue);

                if (fieldName != null) {
                    columnIndexToFieldMap.put(cell.getColumnIndex(), fieldName);
                    log.debug("Mapped column {} '{}' to field '{}'", cell.getColumnIndex(), headerValue, fieldName);
                } else {
                    log.warn("No mapping found for header '{}' at column {}", headerValue, cell.getColumnIndex());
                }
            }

            // Validate required headers
            Set<String> excelHeaders = new HashSet<>();
            headerRow.forEach(cell -> excelHeaders.add(ExcelUtils.getCellStringValue(cell)));
            if (!columnMappingService.validateRequiredHeaders(excelHeaders, fileType)) {
                throw new IllegalArgumentException("Missing required headers in the Excel file.");
            }

            // Process data rows
            int rowIndex = 1; // Start after header
            List<PaymentDto> batch = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Continue if row is null or empty
                if (row == null || ExcelUtils.isEmptyRow(row)) {
                    rowIndex++;
                    continue;
                }

                PaymentDto dto = parsePaymentRowDynamic(row, columnIndexToFieldMap);
                batch.add(dto);

                if (batch.size() >= batchSize) {
                    EntityCache cache = entityCacheService.preloadEntitiesForPayments(batch);
                    batchProcessor.accept(batch, cache);
                    batch.clear();
                }
            }

            // Process any remaining records in the last batch
            if (!batch.isEmpty()) {
                EntityCache cache = entityCacheService.preloadEntitiesForPayments(batch);
                batchProcessor.accept(batch, cache);
            }
        }

    }




    private ReservationDto parseReservationRowDynamic(Row row, Map<Integer, String> columnIndexToFieldMap) {
        ReservationDto dto = new ReservationDto();

        for (Map.Entry<Integer, String> entry : columnIndexToFieldMap.entrySet()) {
            int columnIndex = entry.getKey();
            String fieldName = entry.getValue();
            Cell cell = row.getCell(columnIndex);

            try {
                setFieldValue(dto, fieldName, cell);
            } catch (Exception e) {
                log.error("Error setting field '{}' from column {}: {}", fieldName, columnIndex, e.getMessage());
                throw new RuntimeException("Error processing field: " + fieldName, e);
            }
        }

        return dto;
    }

    private PaymentDto parsePaymentRowDynamic(Row row, Map<Integer, String> columnIndexToFieldMap) {
        PaymentDto dto = new PaymentDto();

        for (Map.Entry<Integer, String> entry : columnIndexToFieldMap.entrySet()) {
            int columnIndex = entry.getKey();
            String fieldName = entry.getValue();
            Cell cell = row.getCell(columnIndex);

            try {
                setFieldValuePayment(dto, fieldName, cell);
            } catch (Exception e) {
                log.error("Error setting field '{}' from column {}: {}", fieldName, columnIndex, e.getMessage());
                throw new RuntimeException("Error processing field: " + fieldName, e);
            }
        }

        return dto;
    }

    private void setFieldValue(ReservationDto dto, String fieldName, Cell cell) {
        try {
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method setter = findSetterMethod(dto.getClass(), setterName);

            if (setter == null) {
                log.warn("No setter found for field: {}", fieldName);
                return;
            }

            Class<?> paramType = setter.getParameterTypes()[0];
            Object value = convertCellValue(cell, paramType);

            setter.invoke(dto, value);

        } catch (Exception e) {
            log.error("Error setting field value for {}: {}", fieldName, e.getMessage());
            throw new RuntimeException("Error setting field: " + fieldName, e);
        }
    }

    private void setFieldValuePayment(PaymentDto dto, String fieldName, Cell cell) {
        try {
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method setter = findSetterMethod(dto.getClass(), setterName);

            if (setter == null) {
                log.warn("No setter found for field: {}", fieldName);
                return;
            }

            Class<?> paramType = setter.getParameterTypes()[0];
            Object value = convertCellValue(cell, paramType);

            setter.invoke(dto, value);

        } catch (Exception e) {
            log.error("Error setting field value for {}: {}", fieldName, e.getMessage());
            throw new RuntimeException("Error setting field: " + fieldName, e);
        }
    }

    private Method findSetterMethod(Class<?> clazz, String setterName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    private Object convertCellValue(Cell cell, Class<?> targetType) {
        if (cell == null) {
            return null;
        }

        if (targetType == Long.class || targetType == long.class) {
            return ExcelUtils.getCellLongValue(cell);
        } else if (targetType == String.class) {
            return ExcelUtils.getCellStringValue(cell);
        } else if (targetType == LocalDate.class) {
            return ExcelUtils.getCellDateValue(cell);
        } else if (targetType == LocalTime.class) {
            return ExcelUtils.getCellTimeValue(cell);
        } else if (targetType == Integer.class || targetType == int.class) {
            Long value = ExcelUtils.getCellLongValue(cell);
            return value != null ? value.intValue() : null;
        } else if (targetType == Double.class || targetType == double.class) {
            return cell.getNumericCellValue();
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return cell.getBooleanCellValue();
        } else if (targetType == BigDecimal.class ) {
            return ExcelUtils.getCellBigDecimalValue(cell);
        }

        return ExcelUtils.getCellStringValue(cell);
    }

















}
