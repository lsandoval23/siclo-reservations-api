package org.creati.sicloReservationsApi.web;


import jakarta.validation.constraints.Min;
import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.model.PagedResponse;
import org.creati.sicloReservationsApi.service.model.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.SortDirection;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@PreAuthorize("hasAuthority('REPORT_VIEW')")
@RequestMapping("/reports/payments")
public class PaymentReportsController {

    private final ReportService reportService;

    public PaymentReportsController(ReportService reportService) {
        this.reportService = reportService;
    }


    @GetMapping("/table")
    public ResponseEntity<PagedResponse<PaymentTableDto>> getPaymentTable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("From date must be before To date");
        }

        return ResponseEntity.ok().body(
                reportService.getPaymentTable(
                        from, to, page, size,
                        PaymentTableDto.PaymentSortFiled.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)
                )
        );
    }
}
