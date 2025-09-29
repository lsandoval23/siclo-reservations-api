package org.creati.sicloReservationsApi.service.excel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;


@Slf4j
public class ExcelUtils {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_TIME
    );

    public static Workbook createWorkbook(MultipartFile multipartFile) throws IOException {

        String filename = Optional.ofNullable(multipartFile.getOriginalFilename())
                .orElseThrow(() -> new IllegalArgumentException("Filename is null"));
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "xlsx" -> new XSSFWorkbook(multipartFile.getInputStream());
            case "xls"  -> new HSSFWorkbook(multipartFile.getInputStream());
            default     -> throw new IllegalArgumentException("The provided file is not a valid Excel file: " + extension);
        };
    }

    public static boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }


    public static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public static Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Long.parseLong(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    log.error("Error parsing Long from cell: {}", cell.getStringCellValue(), e);
                    yield null;
                }
            }
            default -> {
                log.error("Unsupported cell type for Long conversion: {}", cell.getCellType());
                yield null;
            }
        };
    }

    public static Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    log.error("Error parsing Integer from cell: {}", cell.getStringCellValue(), e);
                    yield null;
                }
            }
            default -> {
                log.error("Unsupported cell type for Integer conversion: {}", cell.getCellType());
                yield null;
            }
        };
    }

    public static BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                try {
                    yield new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    log.error("Error parsing BigDecimal from cell: {}", cell.getStringCellValue(), e);
                    yield null;
                }
            }
            default -> {
                log.error("Unsupported cell type for BigDecimal conversion: {}", cell.getCellType());
                yield null;
            }
        };
    }

    public static LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) return null;
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    log.info("failed to parse date {} with formatter {}", value, formatter);
                }
            }
        }

        return null;
    }

    public static LocalTime getCellTimeValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        }

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) return null;
            for (DateTimeFormatter formatter : TIME_FORMATTERS) {
                try {
                    return LocalTime.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    log.info("failed to parse time {} with formatter {}", value, formatter);
                }
            }
        }

        return null;
    }








}
