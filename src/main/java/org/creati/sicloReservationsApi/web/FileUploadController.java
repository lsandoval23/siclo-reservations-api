package org.creati.sicloReservationsApi.web;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.service.FileJobService;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.creati.sicloReservationsApi.service.model.FileJobCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@PreAuthorize("hasAuthority('FILE_UPLOAD')")
@RequestMapping("/files/upload")
public class FileUploadController {

    private final FileProcessingService fileProcessingService;
    private final FileJobService fileJobService;

    public FileUploadController(
            final FileProcessingService fileProcessingService,
            final FileJobService fileJobService) {
        this.fileProcessingService = fileProcessingService;
        this.fileJobService = fileJobService;
    }

    @PostMapping(value = "/reservations",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processReservation(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        try {
            // Saving in local storage temporarily for processing asynchronously
            String filename = Optional.ofNullable(fileContent.getOriginalFilename())
                    .orElseThrow(() -> new IllegalArgumentException("Filename is null"));

            Path tempFilePath = Files.createTempFile(
                    String.format("%s-%s", FilenameUtils.getBaseName(filename), UUID.randomUUID()),
                    "." + FilenameUtils.getExtension(filename).toLowerCase());
            fileContent.transferTo(tempFilePath);
            File tempFile = tempFilePath.toFile();

            // Start job tracking in DB
            FileJob createdJob = fileJobService.createFileJob(FileJobCreateRequest.builder()
                    .fileName(tempFile.getName())
                    .fileExtension(FilenameUtils.getExtension(filename).toLowerCase())
                    .build());

            // Process the file asynchronously
            fileProcessingService.processFile(tempFile, createdJob.getJobId(), "RESERVATION");

            return ResponseEntity.accepted().body("File request accepted for processing with Job ID: " + createdJob.getJobId());
        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + fileContent.getOriginalFilename());
        }
    }

    @PostMapping(value = "/payments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processPayment(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        try {
            // Saving in local storage temporarily for processing asynchronously
            String filename = Optional.ofNullable(fileContent.getOriginalFilename())
                    .orElseThrow(() -> new IllegalArgumentException("Filename is null"));

            Path tempFilePath = Files.createTempFile(
                    String.format("%s-%s", FilenameUtils.getBaseName(filename), UUID.randomUUID()),
                    "." + FilenameUtils.getExtension(filename).toLowerCase());
            fileContent.transferTo(tempFilePath);
            File tempFile = tempFilePath.toFile();

            // Start job tracking in DB
            FileJob createdJob = fileJobService.createFileJob(FileJobCreateRequest.builder()
                    .fileName(tempFile.getName())
                    .fileExtension(FilenameUtils.getExtension(filename).toLowerCase())
                    .build());

            // Process the file asynchronously
            fileProcessingService.processFile(tempFile, createdJob.getJobId(), "PAYMENT");

            return ResponseEntity.accepted().body("File request accepted for processing with Job ID: " + createdJob.getJobId());
        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + fileContent.getOriginalFilename());
        }
    }

}
