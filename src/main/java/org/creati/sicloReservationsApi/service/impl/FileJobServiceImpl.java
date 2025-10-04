package org.creati.sicloReservationsApi.service.impl;

import org.creati.sicloReservationsApi.dao.postgre.FileJobRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.service.FileJobService;
import org.creati.sicloReservationsApi.service.model.FileJobCreateRequest;
import org.creati.sicloReservationsApi.service.model.FileJobUpdateRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FileJobServiceImpl implements FileJobService {

    private final FileJobRepository fileJobRepository;

    public FileJobServiceImpl(FileJobRepository fileJobRepository) {
        this.fileJobRepository = fileJobRepository;
    }

    @Override
    public FileJob createFileJob(FileJobCreateRequest createRequest) {
        return fileJobRepository.save(FileJob.builder()
                .fileName(createRequest.getFileName())
                .fileExtension(createRequest.getFileExtension())
                .status(FileJob.JobStatus.PENDING)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Override
    public void updateStatus(Long jobId, FileJobUpdateRequest updateRequest, FileJob existingJob) {
        fileJobRepository.save(FileJob.builder()
                .jobId(jobId)
                .fileName(existingJob.getFileName())
                .fileExtension(existingJob.getFileExtension())
                .status(updateRequest.getStatus())
                .createdAt(existingJob.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .finishedAt(updateRequest.getFinishedAt())
                .totalRecords(updateRequest.getTotalRecords())
                .processedRecords(updateRequest.getProcessedRecords())
                .skippedRecords(updateRequest.getSkippedRecords())
                .errorRecords(updateRequest.getErrorRecords())
                .processingResult(updateRequest.getProcessingResult())
                .build());


    }

    @Override
    public Optional<FileJob> getFileJobById(Long jobId) {
        return fileJobRepository.findById(jobId);
    }
}
