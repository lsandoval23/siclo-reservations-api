package org.creati.sicloReservationsApi.web;

import jakarta.validation.constraints.Min;
import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.model.reports.ClientReservationsPaymentsDto;
import org.creati.sicloReservationsApi.service.model.reports.PagedResponse;
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationGraphReportDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.reports.SortDirection;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@PreAuthorize("hasAuthority('REPORT_VIEW')")
@RequestMapping("/reports")
public class ReportsController {

    private final ReportService reportService;

    public ReportsController(final ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/clients")
    public ResponseEntity<PagedResponse<ClientReservationsPaymentsDto>> getReservationsPaymentsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long clientId,
            @Min(value = 0, message = "Page number must be 0 or greater")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @RequestParam(defaultValue = "10") int size
    ) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("From date must be before To date");
        }

        return ResponseEntity.ok(reportService.getClientReservationsPayments(from, to, clientId, page, size));
    }

    @GetMapping("/clients/{clientId}/reservations")
    public ResponseEntity<PagedResponse<ReservationTableDto>> getReservationTableByClient(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PathVariable Long clientId,
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

        return ResponseEntity.ok(
                reportService.getReservationTableByClientId(
                        from, to, clientId,
                        page, size,
                        ReservationTableDto.ReservationSortField.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)));

    }

    @GetMapping("/clients/{clientId}/payments")
    public ResponseEntity<PagedResponse<PaymentTableDto>> getPaymentTableByClient(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PathVariable Long clientId,
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

        return ResponseEntity.ok(
                reportService.getPaymentTableByClientId(
                        from, to, clientId,
                        page, size,
                        PaymentTableDto.PaymentSortFiled.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)));

    }

    @GetMapping("/reservations")
    public ReservationGraphReportDto getReservationGraphReport(
            @RequestParam String groupBy,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String timeUnit
    ) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("From date must be before To date");
        }
        return reportService.getReservationGroupedReport(ReservationGraphReportDto.GroupBy.fromValue(groupBy), from, to, timeUnit);
    }


    @GetMapping("/reservations/table")
    public ResponseEntity<PagedResponse<ReservationTableDto>> getReservationTable(
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
                reportService.getReservationTable(
                        from, to, page, size,
                        ReservationTableDto.ReservationSortField.fromValue(sortBy),
                        SortDirection.fromValue(sortDir)));
    }

    @GetMapping("/payments/table")
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
