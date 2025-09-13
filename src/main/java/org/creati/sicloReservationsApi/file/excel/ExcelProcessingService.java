package org.creati.sicloReservationsApi.file.excel;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.file.FileProcessingService;
import org.creati.sicloReservationsApi.file.model.PaymentDto;
import org.creati.sicloReservationsApi.file.model.ProcessingResult;
import org.creati.sicloReservationsApi.file.model.ReservationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessingService implements FileProcessingService {

    private final ExcelParser parser;
    private final ReservationRepository reservationRepository;
    private final EntityCacheService entityCacheService;


    public ExcelProcessingService(final ExcelParser parser, final ReservationRepository reservationRepository,
                                  final EntityCacheService entityCacheService) {
        this.parser = parser;
        this.reservationRepository = reservationRepository;
        this.entityCacheService = entityCacheService;
    }


    @Override
    @Transactional
    public void processReservationsFile(MultipartFile fileData) {
        log.info("Starting processing reservation file: {}", fileData.getOriginalFilename());
        List<ReservationDto> reservationList = parser.parseReservationsFromFile(fileData);
        EntityCache cache = entityCacheService.preloadEntitiesForReservation(reservationList);
        processReservationsBatch(reservationList, cache);
    }

    @Override
    @Transactional
    public void processPaymentTransactionsFile(MultipartFile fileData) {
        log.info("Starting processing transaction file: {}", fileData.getOriginalFilename());
        List<PaymentDto> paymentList = parser.parsePaymentsFromFile(fileData);
        EntityCache cache =  entityCacheService.preloadEntitiesForPayments(paymentList);


    }


    private void processReservationsBatch(List<ReservationDto> reservations, EntityCache cache) {
        List<String> errors = new ArrayList<>();
        List<Reservation> reservationsToSave = new ArrayList<>();

        int processedRows = 0;
        int errorRows = 0;

        for (int i = 1; i < reservations.size(); i++) {
            ReservationDto reservation = reservations.get(i);
            try {
                if (cache.getExistingReservationIds().contains(reservation.getReservationId())){
                    log.warn("Skipping existing reservation ID: {}", reservation.getReservationId());
                    continue;
                }
                Reservation reservationEntity = parser.buildReservationFromDto(reservation, cache);
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

        ProcessingResult result = ProcessingResult.builder()
                .success(errorRows == 0)
                .message(String.format("Processed %d rows with %d errors", processedRows, errorRows))
                .totalRows(reservations.size())
                .processedRows(processedRows)
                .errorRows(errorRows)
                .errors(errors)
                .build();
    }

}
