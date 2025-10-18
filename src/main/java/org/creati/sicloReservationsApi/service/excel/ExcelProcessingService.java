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
import org.creati.sicloReservationsApi.service.model.FileType;
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
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class ExcelProcessingService implements FileProcessingService {

    public static Integer MAX_ITEMS_IN_BATCH = 1000;

    private final StreamingExcelParser streamingParser;
    private final EntityCacheService entityCacheService;
    private final BatchPersistenceService batchPersistenceService;
    private final FileJobService fileJobService;
    private final ObjectMapper objectMapper;


    public ExcelProcessingService(
            final StreamingExcelParser streamingParser,
            final EntityCacheService entityCacheService,
            final BatchPersistenceService batchPersistenceService,
            final FileJobService fileJobService,
            final ObjectMapper objectMapper) {
        this.streamingParser = streamingParser;
        this.entityCacheService = entityCacheService;
        this.batchPersistenceService = batchPersistenceService;
        this.fileJobService = fileJobService;
        this.objectMapper = objectMapper;
    }


    @Override
    @Async
    @Transactional
    public void processFile(File fileData, Long jobId, FileType fileType) {
        log.info("Starting processing file: {} of type {} with jobId: {}", fileData.getName(), fileType, jobId);

        FileJob jobFound = fileJobService.getFileJobById(jobId)
                .orElseThrow(() -> new FileProcessingException("File job not found with id: " + jobId));
        fileJobService.updateStatus(jobFound.getJobId(), FileJobUpdateRequest.builder()
                .status(FileJob.JobStatus.IN_PROGRESS)
                .build(), jobFound);

        try {
            List<ProcessingResult> batchResults = new ArrayList<>();
            FileProcessingStrategy<?> strategy = getFileProcessingStrategy(fileType);

            streamingParser.parseFromFile(
                    fileData,
                    fileType.name(),
                    strategy.dtoClass(),
                    MAX_ITEMS_IN_BATCH,
                    (batch) -> {
                        ProcessingResult batchResult = strategy.persist(batch);
                        batchResults.add(batchResult);
                        log.info("Processed batch of {} payments. Result: {}", batch.size(), batchResult);
                    },
                    strategy.extraParam());

            ProcessingResult batchProcessingResult = ProcessingResult.builder()
                    .success(batchResults.stream().allMatch(ProcessingResult::isSuccess))
                    .totalProcessed(batchResults.stream().mapToInt(ProcessingResult::getTotalProcessed).sum())
                    .successCount(batchResults.stream().mapToInt(ProcessingResult::getSuccessCount).sum())
                    .skipped(batchResults.stream().mapToInt(ProcessingResult::getSkipped).sum())
                    .failureCount(batchResults.stream().mapToInt(ProcessingResult::getFailureCount).sum())
                    .errors(batchResults.stream().flatMap(r -> r.getErrors().stream()).toList())
                    .build();

            FileJob.JobStatus jobStatus = batchProcessingResult.isSuccess()
                    ? FileJob.JobStatus.SUCCESS
                    : FileJob.JobStatus.FAILED;

            FileJobUpdateRequest updateRequest = FileJobUpdateRequest.builder()
                    .status(jobStatus)
                    .finishedAt(LocalDateTime.now())
                    .totalRecords(batchProcessingResult.getTotalProcessed())
                    .processedRecords(batchProcessingResult.getSuccessCount())
                    .skippedRecords(batchProcessingResult.getSkipped())
                    .errorRecords(batchProcessingResult.getFailureCount())
                    .processingResult(objectMapper.writeValueAsString(batchProcessingResult))
                    .build();

            fileJobService.updateStatus(jobId, updateRequest, jobFound);
            log.info("Completed processing file: {} (type: {}) with jobId: {}. Result: {}",
                    fileData.getName(), fileType, jobId, batchProcessingResult);

        } catch (FileProcessingException | IOException exception) {
            log.error("Error processing file {} of type {}: {}", fileData.getName(), fileType, exception.getMessage(), exception);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .status(FileJob.JobStatus.FAILED)
                    .errorMessage(exception.getMessage())
                    .build(), jobFound);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("Error in input file format: {}", illegalArgumentException.getMessage(), illegalArgumentException);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .errorMessage(illegalArgumentException.getMessage())
                    .status(FileJob.JobStatus.FAILED)
                    .build(), jobFound);
        } catch (Exception e) {
            log.error("Unexpected error processing file {} of type {}: {}", fileData.getName(), fileType, e.getMessage(), e);
            fileJobService.updateStatus(jobId, FileJobUpdateRequest.builder()
                    .status(FileJob.JobStatus.FAILED)
                    .errorMessage("Unexpected error: " + e.getMessage())
                    .build(), jobFound);
        } finally {
            // Clean up the temporary file
            if (fileData.exists()) {
                boolean deleted = fileData.delete();
                if (!deleted) {
                    log.warn("Failed to delete temporary file: {}", fileData.getAbsolutePath());
                }
            }
        }

    }

    private FileProcessingStrategy<?> getFileProcessingStrategy(FileType fileType) {

        Map<FileType, FileProcessingStrategy<?>> strategyMap = Map.of(
                FileType.RESERVATION, new FileProcessingStrategy<>(
                        ReservationDto.class,
                        (batch) -> {
                            EntityCache cache = entityCacheService.preloadEntitiesForReservation(batch);
                            return batchPersistenceService.persistReservationsBatch(batch, cache);
                        },
                        null
                ),
                FileType.PAYMENT, new FileProcessingStrategy<>(
                        PaymentDto.class,
                        (batch) -> {
                            EntityCache cache = entityCacheService.preloadEntitiesForPayments(batch);
                            return batchPersistenceService.persistPaymentsBatch(batch, cache);
                        },
                        "M-pago"
                )
        );

        FileProcessingStrategy<?> strategy = strategyMap.get(fileType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
        return strategy;
    }


    public record FileProcessingStrategy<T>(
            Class<T> dtoClass,
            Function<List<T>, ProcessingResult> persistFunction,
            String extraParam) {

        public ProcessingResult persist(List<?> batch) {
            @SuppressWarnings("unchecked")
            List<T> typedBatch = (List<T>) batch;
            return persistFunction.apply(typedBatch);
        }
    }



}
