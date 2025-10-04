package org.creati.sicloReservationsApi.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.creati.sicloReservationsApi.service.BatchPersistenceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessingService implements FileProcessingService {

    private final ExcelParser parser;
    private final BatchPersistenceService batchPersistenceService;
    private final EntityCacheService entityCacheService;


    public ExcelProcessingService(
            final ExcelParser parser,
            final BatchPersistenceService batchPersistenceService,
            final EntityCacheService entityCacheService) {
        this.parser = parser;
        this.batchPersistenceService = batchPersistenceService;
        this.entityCacheService = entityCacheService;
    }


    @Override
    @Async
    @Transactional
    public void processReservationsFile(File fileData) {
        log.info("Starting processing reservation file: {}", fileData.getName());
        List<ReservationDto> reservationList = parser.parseReservationsFromFile(fileData);
        EntityCache cache = entityCacheService.preloadEntitiesForReservation(reservationList);
        batchPersistenceService.persistReservationsBatch(reservationList, cache);
    }

    @Override
    @Transactional
    public void processPaymentTransactionsFile(MultipartFile fileData) {

    }



}
