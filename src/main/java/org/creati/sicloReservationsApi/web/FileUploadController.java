package org.creati.sicloReservationsApi.web;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.creati.sicloReservationsApi.service.FileProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/files/upload")
public class FileUploadController {

    private final FileProcessingService fileProcessingService;

    public FileUploadController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping(value = "/reservations",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> process(
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

            // Process the file asynchronously
            fileProcessingService.processReservationsFile(tempFilePath.toFile());

            return ResponseEntity.accepted().body("File request accepted for processing");
        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + fileContent.getOriginalFilename());
        }
    }

}
