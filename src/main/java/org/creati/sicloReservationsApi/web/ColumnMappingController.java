package org.creati.sicloReservationsApi.web;

import jakarta.validation.Valid;
import org.creati.sicloReservationsApi.service.excel.ColumnMappingService;
import org.creati.sicloReservationsApi.service.model.BulkUpdateColumnMappingRequest;
import org.creati.sicloReservationsApi.service.model.ColumnMappingDto;
import org.creati.sicloReservationsApi.service.model.ProcessingResult;
import org.creati.sicloReservationsApi.service.model.UpdateColumnMappingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/column-mapping")
public class ColumnMappingController {

    private final ColumnMappingService columnMappingService;

    public ColumnMappingController(ColumnMappingService columnMappingService) {
        this.columnMappingService = columnMappingService;
    }

    @GetMapping("")
    public ResponseEntity<List<ColumnMappingDto>> getAllMappings() {
        List<ColumnMappingDto> mappings = columnMappingService.getAllMappings();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/file-type/{fileType}")
    public ResponseEntity<List<ColumnMappingDto>> getMappingsByFileType(@PathVariable String fileType) {
        List<ColumnMappingDto> mappings = columnMappingService.getMappingsByFileType(fileType);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColumnMappingDto> getMappingById(@PathVariable Long id) {
        ColumnMappingDto updated = columnMappingService.getMappingById(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColumnMappingDto> updateMapping(
            @PathVariable Long id,
            @Valid @RequestBody UpdateColumnMappingRequest updateRequest) {
        ColumnMappingDto updated = columnMappingService.updateMapping(id, updateRequest);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/bulk")
    public ResponseEntity<ProcessingResult> bulkUpdateMappings(
            @Valid @RequestBody List<BulkUpdateColumnMappingRequest> updates) {

        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ProcessingResult result = columnMappingService.bulkUpdateMappings(updates);
        return ResponseEntity.ok(result);
    }



}
