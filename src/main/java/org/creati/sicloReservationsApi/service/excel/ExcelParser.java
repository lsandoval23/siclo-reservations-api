package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.excel.util.ExcelUtils;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExcelParser {

    // Method to parse reservations from the uploaded Excel file, generates DTO's only
    public List<ReservationDto> parseReservationsFromFile(File file) throws IOException, FileProcessingException {

        List<ReservationDto> reservations = new ArrayList<>();

        try (Workbook workbook = ExcelUtils.createWorkbook(file)){
            Sheet sheet = workbook.getSheetAt(0);
            if (validateReservationHeaders(sheet.getRow(0))) {
                throw new FileProcessingException("Invalid Excel headers");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelUtils.isEmptyRow(row)) continue;
                try {
                    ReservationDto dto = parseReservationRow(row);
                    log.info("Parsed Reservation DTO: {} for index: {}", dto, rowIndex);
                    reservations.add(dto);
                } catch (Exception e) {
                    throw new FileProcessingException("Error parsing row " + (rowIndex + 1), e);
                }
            }
        }

        return reservations;
    }



    private boolean validateReservationHeaders(Row headerRow) {
        String[] expectedHeaders = {
                "Id reserva", "Id clase", "País", "Ciudad", "Disciplina",
                "Estudio", "Salón", "Instructor", "Día", "Hora",
                "Cliente", "Creador del pedido", "Método de pago", "Estatus"
        };

        if (headerRow.getPhysicalNumberOfCells() < expectedHeaders.length) {
            return false;
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = ExcelUtils.getCellStringValue(cell);
            if (cell == null || !expectedHeaders[i].equalsIgnoreCase(cellValue)) {
                return false;
            }
        }
        return true;
    }


    private ReservationDto parseReservationRow(Row row) {
        ReservationDto dto = new ReservationDto();
        dto.setReservationId(ExcelUtils.getCellLongValue(row.getCell(0)));
        dto.setClassId(ExcelUtils.getCellLongValue(row.getCell(1)));
        dto.setCountry(ExcelUtils.getCellStringValue(row.getCell(2)));
        dto.setCity(ExcelUtils.getCellStringValue(row.getCell(3)));
        dto.setDisciplineName(ExcelUtils.getCellStringValue(row.getCell(4)));
        dto.setStudioName(ExcelUtils.getCellStringValue(row.getCell(5)));
        dto.setRoomName(ExcelUtils.getCellStringValue(row.getCell(6)));
        dto.setInstructorName(ExcelUtils.getCellStringValue(row.getCell(7)));
        dto.setDay(ExcelUtils.getCellDateValue(row.getCell(8)));
        dto.setTime(ExcelUtils.getCellTimeValue(row.getCell(9)));
        dto.setClientEmail(ExcelUtils.getCellStringValue(row.getCell(10)));
        dto.setOrderCreator(ExcelUtils.getCellStringValue(row.getCell(11)));
        dto.setPaymentMethod(ExcelUtils.getCellStringValue(row.getCell(12)));
        dto.setStatus(ExcelUtils.getCellStringValue(row.getCell(13)));
        return dto;
    }



}
