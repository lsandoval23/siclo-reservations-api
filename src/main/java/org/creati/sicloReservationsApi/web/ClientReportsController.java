package org.creati.sicloReservationsApi.web;

import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.model.ClientReservationsPaymentsDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@PreAuthorize("hasAuthority('REPORT_VIEW')")
@RequestMapping("/reports/clients")
public class ClientReportsController {

    private final ReportService reportService;

    public ClientReportsController(final ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("")
    public List<ClientReservationsPaymentsDto> getReservationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long clientId
    ) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("From date must be before To date");
        }

        return reportService.getClientReservationsPayments(from, to, clientId);
    }
}
