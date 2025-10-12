package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.service.excel.util.ExcelUtils;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class StreamingExcelParser {

    private final ColumnMappingService columnMappingService;
    private final EntityCacheService entityCacheService;

    public StreamingExcelParser(
            final ColumnMappingService columnMappingService,
            final EntityCacheService entityCacheService) {
        this.columnMappingService = columnMappingService;
        this.entityCacheService = entityCacheService;
    }


    public <T> void parseFromFile(
            File file,
            String fileType,
            int batchSize,
            BiConsumer<List<T>, EntityCache> batchProcessor,
            String sheetName) throws IOException {

        try (Workbook workbook = ExcelUtils.createWorkbook(file)) {

            Map<String, String> headerToFieldMap = columnMappingService.getHeaderToFieldMapping(fileType);
            Sheet sheet = Optional.ofNullable(sheetName)
                    .map(workbook::getSheet)
                    .orElse(workbook.getSheetAt(0));

            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("The Excel file is empty.");
            }


            Row headerRow = rowIterator.next();
            Map<Integer, String> columnIndexToFieldMap = new HashMap<>();

            // Validate required headers
            Set<String> excelHeaders = new HashSet<>();
            headerRow.forEach(cell -> excelHeaders.add(ExcelUtils.getCellStringValue(cell)));
            if (!columnMappingService.validateRequiredHeaders(excelHeaders, fileType)) {
                throw new IllegalArgumentException("Missing required headers in the Excel file.");
            }

            //Build mapping of column indexes to field names
            for (Cell cell : headerRow) {
                String headerValue = ExcelUtils.getCellStringValue(cell);
                String fieldName = headerToFieldMap.get(headerValue);

                if (fieldName != null) {
                    columnIndexToFieldMap.put(cell.getColumnIndex(), fieldName);
                    log.debug("Mapped column {} '{}' to field '{}'", cell.getColumnIndex(), headerValue, fieldName);
                } else {
                    log.warn("No mapping found for header '{}' at column {}", headerValue, cell.getColumnIndex());
                }
            }

            // Process data rows
            List<T> batch = new ArrayList<>();
            Class<T> dtoClass = switch (fileType) {
                case "RESERVATION" -> (Class<T>) ReservationDto.class;
                case "PAYMENT" -> (Class<T>) PaymentDto.class;
                default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
            };


            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row == null || ExcelUtils.isEmptyRow(row)) {
                    continue;
                }

                T dto = parseRowDynamic(row, columnIndexToFieldMap, dtoClass);
                batch.add(dto);

                if (batch.size() >= batchSize) {
                    EntityCache cache = (dto instanceof ReservationDto)
                            ? entityCacheService.preloadEntitiesForReservation((List<ReservationDto>) batch)
                            : entityCacheService.preloadEntitiesForPayments((List<PaymentDto>) batch);
                    batchProcessor.accept(batch, cache);
                    batch.clear();
                }

            }

            if (!batch.isEmpty()) {
                EntityCache cache = (batch.get(0) instanceof ReservationDto)
                        ? entityCacheService.preloadEntitiesForReservation((List<ReservationDto>) batch)
                        : entityCacheService.preloadEntitiesForPayments((List<PaymentDto>) batch);
                batchProcessor.accept(batch, cache);
            }


        }

    }

    private <T> T parseRowDynamic(Row row, Map<Integer, String> columnIndexToFieldMap, Class<T> dtoClass) {
        try {
            T dto = dtoClass.getDeclaredConstructor().newInstance();

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

        } catch (Exception e) {
            throw new RuntimeException("Error creating DTO instance of type " + dtoClass.getSimpleName(), e);
        }
    }



    private <T> void setFieldValue(T dto, String fieldName, Cell cell) {
        try {
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

            Method setter = Arrays.stream(dto.getClass().getMethods())
                    .filter(m -> m.getName().equals(setterName) && m.getParameterCount() == 1)
                    .findFirst()
                    .orElse(null);

            if (setter == null) {
                log.warn("No setter found for field: {}", fieldName);
                return;
            }

            Class<?> paramType = setter.getParameterTypes()[0];
            Object value = ExcelUtils.convertCellValue(cell, paramType);

            setter.invoke(dto, value);

        } catch (Exception e) {
            log.error("Error setting field value for {}: {}", fieldName, e.getMessage());
            throw new RuntimeException("Error setting field: " + fieldName, e);
        }
    }



}
