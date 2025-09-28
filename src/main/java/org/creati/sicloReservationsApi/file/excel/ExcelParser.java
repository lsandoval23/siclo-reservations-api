package org.creati.sicloReservationsApi.file.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.DisciplineRepository;
import org.creati.sicloReservationsApi.dao.postgre.InstructorRepository;
import org.creati.sicloReservationsApi.dao.postgre.RoomRepository;
import org.creati.sicloReservationsApi.dao.postgre.StudioRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.creati.sicloReservationsApi.dao.postgre.model.Instructor;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.creati.sicloReservationsApi.dao.postgre.model.Studio;
import org.creati.sicloReservationsApi.file.excel.util.ExcelUtils;
import org.creati.sicloReservationsApi.file.model.PaymentDto;
import org.creati.sicloReservationsApi.file.model.ReservationDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ExcelParser {

    private final ClientRepository clientRepository;
    private final StudioRepository studioRepository;
    private final RoomRepository roomRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;

    public ExcelParser(
            final ClientRepository clientRepository, final StudioRepository studioRepository,
            final RoomRepository roomRepository, final DisciplineRepository disciplineRepository,
            final InstructorRepository instructorRepository) {
        this.clientRepository = clientRepository;
        this.studioRepository = studioRepository;
        this.roomRepository = roomRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
    }

    // Method to parse reservations from the uploaded Excel file, generates DTO's only
    public List<ReservationDto> parseReservationsFromFile(MultipartFile file) {
        List<ReservationDto> reservations = new ArrayList<>();
        try (Workbook workbook = ExcelUtils.createWorkbook(file)){
            Sheet sheet = workbook.getSheetAt(0);
            if (validateReservationHeaders(sheet.getRow(0))) {
                throw new RuntimeException("Invalid Excel headers");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelUtils.isEmptyRow(row)) continue;
                try {
                    ReservationDto dto = parseReservationRow(row);
                    log.info("Parsed Reservation DTO: {} for index: {}", dto, rowIndex);
                    reservations.add(dto);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing row " + (rowIndex + 1), e);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error reading Excel file: " + exception.getMessage(), exception);
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


    public Reservation buildReservationFromDto(ReservationDto dto, EntityCache cache ){

        Client newClient = cache.getClientsByEmail().computeIfAbsent(dto.getClientEmail(), email -> {
            log.info("Creating new client for email: {}", email);
            Client client = Client.builder()
                    .email(email)
                    .build();
            return clientRepository.save(client);
        });

        Studio newStudio = cache.getStudiosByName().computeIfAbsent(dto.getStudioName(), name -> {
            log.info("Creating new studio for name: {}", name);
            Studio studio = Studio.builder()
                    .name(name)
                    .build();
            return studioRepository.save(studio);
        });

        String roomKey = newStudio.getName() + "|" + dto.getRoomName();
        Room newRoom = cache.getRoomsByStudioAndName().computeIfAbsent(roomKey, key -> {
            log.info("Creating new room: {} for studio: {}", dto.getRoomName(), newStudio.getStudioId());
            Room room = Room.builder()
                    .name(dto.getRoomName())
                    .studio(newStudio)
                    .build();
            return roomRepository.save(room);
        });

        Discipline newDiscipline = cache.getDisciplinesByName().computeIfAbsent(dto.getDisciplineName(), name -> {
            log.info("Creating new discipline for name: {}", name);
            Discipline discipline = Discipline.builder()
                    .name(name)
                    .build();
            return disciplineRepository.save(discipline);
        });

        Instructor newInstructor = cache.getInstructorsByName().computeIfAbsent(dto.getInstructorName(), name -> {
            log.info("Creating new instructor for name: {}", name);
            Instructor instructor = Instructor.builder()
                    .name(name)
                    .build();
            return instructorRepository.save(instructor);
        });

        return Reservation.builder()
                .reservationId(dto.getReservationId())
                .classId(dto.getClassId())
                .room(newRoom)
                .discipline(newDiscipline)
                .instructor(newInstructor)
                .client(newClient)
                .reservationDate(dto.getDay())
                .reservationTime(dto.getTime())
                .orderCreator(dto.getOrderCreator())
                .paymentMethod(dto.getPaymentMethod())
                .status(dto.getStatus())
                .build();
    }
}
