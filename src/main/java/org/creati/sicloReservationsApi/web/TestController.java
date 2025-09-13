package org.creati.sicloReservationsApi.web;


import org.creati.sicloReservationsApi.file.FileProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final FileProcessingService fileProcessingService;

    public TestController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }


    @PostMapping(
            value = "/reservations",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<Void> process(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        fileProcessingService.processReservationsFile(fileContent);
        return ResponseEntity.ok().build();
    }

    @PostMapping(
            value = "/payments",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<Void> processPayments(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        fileProcessingService.processPaymentTransactionsFile(fileContent);
        return ResponseEntity.ok().build();
    }
}
