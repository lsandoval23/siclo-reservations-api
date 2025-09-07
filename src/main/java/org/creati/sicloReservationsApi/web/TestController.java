package org.creati.sicloReservationsApi.web;


import org.creati.sicloReservationsApi.excel.ExcelProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final ExcelProcessingService excelProcessingService;

    public TestController(ExcelProcessingService excelProcessingService) {
        this.excelProcessingService = excelProcessingService;
    }


    @PostMapping(
            value = "",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<Void> process(
            @RequestPart ("file") MultipartFile fileContent
    ) {
        excelProcessingService.processReservationExcel(fileContent);
        return ResponseEntity.ok().build();
    }
}
