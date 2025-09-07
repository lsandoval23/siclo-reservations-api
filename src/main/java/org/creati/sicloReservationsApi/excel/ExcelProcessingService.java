package org.creati.sicloReservationsApi.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.DisciplineRepository;
import org.creati.sicloReservationsApi.dao.postgre.InstructorRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.RoomRepository;
import org.creati.sicloReservationsApi.dao.postgre.StudioRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.creati.sicloReservationsApi.dao.postgre.model.Instructor;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.creati.sicloReservationsApi.dao.postgre.model.Studio;
import org.creati.sicloReservationsApi.excel.model.ExcelProcessingResult;
import org.creati.sicloReservationsApi.excel.model.ReservationExcel;
import org.creati.sicloReservationsApi.excel.util.ExcelUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessingService {

    private final ClientRepository clientRepository;
    private final StudioRepository studioRepository;
    private final RoomRepository roomRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;
    private final ReservationRepository reservationRepository;
    private final EntityCacheService entityCacheService;


    public ExcelProcessingService(ClientRepository clientRepository, StudioRepository studioRepository, RoomRepository roomRepository, DisciplineRepository disciplineRepository, InstructorRepository instructorRepository, ReservationRepository reservationRepository, EntityCacheService entityCacheService) {
        this.clientRepository = clientRepository;
        this.studioRepository = studioRepository;
        this.roomRepository = roomRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
        this.reservationRepository = reservationRepository;
        this.entityCacheService = entityCacheService;
    }

    @Transactional
    public ExcelProcessingResult processReservationExcel(MultipartFile excelFile) {
        log.info("Starting processing of Excel file: {}", excelFile.getOriginalFilename());
        List<ReservationExcel> reservationList = parseReservationsFromFile(excelFile);
        EntityCache cache = entityCacheService.preloadEntitiesForReservation(reservationList);
        return processReservationsBatch(reservationList, cache);
    }


    private List<ReservationExcel> parseReservationsFromFile(MultipartFile file) {
        List<ReservationExcel> reservations = new ArrayList<>();
        try (Workbook workbook = ExcelUtils.createWorkbook(file)){
            Sheet sheet = workbook.getSheetAt(0);
            if (validateHeaders(sheet.getRow(0))) {
                throw new RuntimeException("Invalid Excel headers");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelUtils.isEmptyRow(row)) continue;
                try {
                    ReservationExcel dto = parseReservationRow(row);
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


    private boolean validateHeaders(Row headerRow) {
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

    private ReservationExcel parseReservationRow(Row row) {
        ReservationExcel dto = new ReservationExcel();
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

    private ExcelProcessingResult processReservationsBatch(List<ReservationExcel> reservations, EntityCache cache) {
        List<String> errors = new ArrayList<>();
        List<Reservation> reservationsToSave = new ArrayList<>();

        int processedRows = 0;
        int errorRows = 0;

        for (int i = 1; i < reservations.size(); i++) {
            ReservationExcel reservation = reservations.get(i);
            try {
                if (cache.getExistingReservationIds().contains(reservation.getReservationId())){
                    log.warn("Skipping existing reservation ID: {}", reservation.getReservationId());
                    continue;
                }
                Reservation reservationEntity = buildReservationFromDto(reservation, cache);
                reservationsToSave.add(reservationEntity);
                processedRows++;
            } catch (Exception e) {
                errorRows++;
                errors.add(String.format("Error processing row %d: %s", i + 1, e.getMessage()));
                log.error("Error processing reservation at row {}: exception: {}", i + 1, e.getMessage());
            }
        }

        if (!reservationsToSave.isEmpty()) {
            reservationRepository.saveAll(reservationsToSave);
            log.info("Saved {} new reservations", reservationsToSave.size());
        }

        return ExcelProcessingResult.builder()
                .success(errorRows == 0)
                .message(String.format("Processed %d rows with %d errors", processedRows, errorRows))
                .totalRows(reservations.size())
                .processedRows(processedRows)
                .errorRows(errorRows)
                .errors(errors)
                .build();
    }


    private Reservation buildReservationFromDto(ReservationExcel dto, EntityCache cache ){

        Client newClient = cache.getClientsByName().computeIfAbsent(dto.getClientEmail(), email -> {
            Client client = Client.builder()
                    .email(email)
                    .build();
            return clientRepository.saveAndFlush(client);
        });

        Studio newStudio = cache.getStudiosByName().computeIfAbsent(dto.getStudioName(), name -> {
            Studio studio = Studio.builder()
                    .name(name)
                    .build();
            return studioRepository.save(studio);
        });

        String roomKey = newStudio.getName() + "|" + dto.getRoomName();
        Room newRoom = cache.getRoomsByStudioAndName().computeIfAbsent(roomKey, key -> {
            Room room = Room.builder()
                    .name(dto.getRoomName())
                    .studio(newStudio)
                    .build();
            return roomRepository.save(room);
        });

        Discipline newDiscipline = cache.getDisciplinesByName().computeIfAbsent(dto.getDisciplineName(), name -> {
            Discipline discipline = Discipline.builder()
                    .name(name)
                    .build();
            return disciplineRepository.save(discipline);
        });

        Instructor newInstructor = cache.getInstructorsByName().computeIfAbsent(dto.getInstructorName(), name -> {
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
