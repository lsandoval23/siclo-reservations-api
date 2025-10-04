package org.creati.sicloReservationsApi.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.cache.EntityCacheService;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.FileJobService;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.creati.sicloReservationsApi.service.model.FileJobUpdateRequest;
import org.creati.sicloReservationsApi.service.model.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.creati.sicloReservationsApi.service.BatchPersistenceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessingService implements FileProcessingService {

    private final ExcelParser parser;
    private final BatchPersistenceService batchPersistenceService;
    private final EntityCacheService entityCacheService;
    private final FileJobService fileJobService;
    private final ObjectMapper objectMapper;


    public ExcelProcessingService(
            final ExcelParser parser,
            final BatchPersistenceService batchPersistenceService,
            final EntityCacheService entityCacheService,
            final FileJobService fileJobService,
            final ObjectMapper objectMapper) {
        this.parser = parser;
        this.batchPersistenceService = batchPersistenceService;
        this.entityCacheService = entityCacheService;
        this.fileJobService = fileJobService;
        this.objectMapper = objectMapper;
    }


    @Override
    @Async
    @Transactional
    public void processReservationsFile(File fileData, Long jobId) {

        log.info("Starting processing reservation file: {} with jobId: {}", fileData.getName(), jobId);
        FileJob jobFound = fileJobService.getFileJobById(jobId)
                .orElseThrow(() -> new FileProcessingException("File job not found with id: " + jobId));

        // Update job status to IN_PROGRESS
        fileJobService.updateStatus(jobFound.getJobId(), FileJobUpdateRequest.builder()
                .status(FileJob.JobStatus.IN_PROGRESS)
                .build(), jobFound);

        try {
            List<ReservationDto> reservationList = parser.parseReservationsFromFile(fileData);
            EntityCache cache = entityCacheService.preloadEntitiesForReservation(reservationList);
            ProcessingResult batchProcessingResult = batchPersistenceService.persistReservationsBatch(reservationList, cache);

            // Determine job status based on the processing result
            FileJob.JobStatus jobStatus = batchProcessingResult.isSuccess()
                    ? FileJob.JobStatus.SUCCESS
                    : FileJob.JobStatus.FAILED;

            // Build the common update request
            FileJobUpdateRequest updateRequest = FileJobUpdateRequest.builder()
                    .status(jobStatus)
                    .finishedAt(LocalDateTime.now())
                    .totalRecords(batchProcessingResult.getTotalRows())
                    .processedRecords(batchProcessingResult.getProcessedRows())
                    .skippedRecords(batchProcessingResult.getSkippedRows())
                    .errorRecords(batchProcessingResult.getErrorRows())
                    .processingResult(objectMapper.writeValueAsString(batchProcessingResult))
                    .build();

            // Update job status
            fileJobService.updateStatus(jobId, updateRequest, jobFound);
            log.info("Completed processing reservation file: {} with jobId: {}. Result: {}", fileData.getName(), jobId, batchProcessingResult);

        } catch (FileProcessingException | IOException exception) {
            // If any error occurs, update job status to FAILED
            log.error("Error processing reservation file: {}", exception.getMessage(), exception);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .status(FileJob.JobStatus.FAILED)
                    .build(), jobFound);
        }

    }

    @Override
    @Transactional
    public void processPaymentTransactionsFile(File fileData) {

    }



}
