package org.creati.sicloReservationsApi.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.exception.FileProcessingException;
import org.creati.sicloReservationsApi.service.FileJobService;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.creati.sicloReservationsApi.service.model.FileJobUpdateRequest;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ProcessingResult;
import org.creati.sicloReservationsApi.service.BatchPersistenceService;
import org.creati.sicloReservationsApi.service.model.ReservationDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessingService implements FileProcessingService {

    private final StreamingExcelParser streamingParser;
    private final BatchPersistenceService batchPersistenceService;
    private final FileJobService fileJobService;
    private final ObjectMapper objectMapper;


    public ExcelProcessingService(
            final StreamingExcelParser streamingParser,
            final BatchPersistenceService batchPersistenceService,
            final FileJobService fileJobService,
            final ObjectMapper objectMapper) {
        this.streamingParser = streamingParser;
        this.batchPersistenceService = batchPersistenceService;
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

            List<ProcessingResult> batchResults = new ArrayList<>();
            streamingParser.<ReservationDto> parseFromFile(
                    fileData,
                    "RESERVATION",
                    100,
                    (reservationBatch, cache) -> {
                        ProcessingResult batchResult = batchPersistenceService.persistReservationsBatch(reservationBatch, cache);
                        batchResults.add(batchResult);
                        log.info("Processed batch of {} reservations. Result: {}", reservationBatch.size(), batchResult);
                    },
                    null);

            // Aggregate batch results
            ProcessingResult batchProcessingResult = ProcessingResult.builder()
                    .success(batchResults.stream().allMatch(ProcessingResult::isSuccess))
                    .totalRows(batchResults.stream().mapToInt(ProcessingResult::getTotalRows).sum())
                    .processedRows(batchResults.stream().mapToInt(ProcessingResult::getProcessedRows).sum())
                    .skippedRows(batchResults.stream().mapToInt(ProcessingResult::getSkippedRows).sum())
                    .errorRows(batchResults.stream().mapToInt(ProcessingResult::getErrorRows).sum())
                    .errors(batchResults.stream().flatMap(r -> r.getErrors().stream()).toList())
                    .build();

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
    @Async
    @Transactional
    public void processPaymentTransactionsFile(File fileData, Long jobId) {

        log.info("Starting processing payment file: {} with jobId: {}", fileData.getName(), jobId);
        FileJob jobFound = fileJobService.getFileJobById(jobId)
                .orElseThrow(() -> new FileProcessingException("File job not found with id: " + jobId));

        // Update job status to IN_PROGRESS
        fileJobService.updateStatus(jobFound.getJobId(), FileJobUpdateRequest.builder()
                .status(FileJob.JobStatus.IN_PROGRESS)
                .build(), jobFound);

        try {

            List<ProcessingResult> batchResults = new ArrayList<>();
            streamingParser.<PaymentDto> parseFromFile(
                    fileData,
                    "PAYMENT",
                    100,
                    (paymentBatch, cache) -> {
                        ProcessingResult batchResult = batchPersistenceService.persistPaymentsBatch(paymentBatch, cache);
                        batchResults.add(batchResult);
                        log.info("Processed batch of {} payments. Result: {}", paymentBatch.size(), batchResult);
                    },
                    "M-pago");

            // Aggregate batch results
            ProcessingResult batchProcessingResult = ProcessingResult.builder()
                    .success(batchResults.stream().allMatch(ProcessingResult::isSuccess))
                    .totalRows(batchResults.stream().mapToInt(ProcessingResult::getTotalRows).sum())
                    .processedRows(batchResults.stream().mapToInt(ProcessingResult::getProcessedRows).sum())
                    .skippedRows(batchResults.stream().mapToInt(ProcessingResult::getSkippedRows).sum())
                    .errorRows(batchResults.stream().mapToInt(ProcessingResult::getErrorRows).sum())
                    .errors(batchResults.stream().flatMap(r -> r.getErrors().stream()).toList())
                    .build();

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
            log.info("Completed processing payment file: {} with jobId: {}. Result: {}", fileData.getName(), jobId, batchProcessingResult);

        } catch (FileProcessingException | IOException exception) {
            // If any error occurs, update job status to FAILED
            log.error("Error processing payment file: {}", exception.getMessage(), exception);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .status(FileJob.JobStatus.FAILED)
                    .build(), jobFound);
        }

    }



}
