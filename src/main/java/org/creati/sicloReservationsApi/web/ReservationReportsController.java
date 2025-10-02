package org.creati.sicloReservationsApi.web;

import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.impl.ReportServiceImpl;
import org.creati.sicloReservationsApi.service.model.PagedResponse;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationTableDto;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports/reservations")
public class ReservationReportsController {

    private final ReportService reportService;

    public ReservationReportsController(ReportServiceImpl reportService) {
        this.reportService = reportService;
    }

    @GetMapping("")
    public ReservationReportDto getReservationReport(
            @RequestParam String groupBy,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String timeUnit
    ) {
        return reportService.getGroupedReport(groupBy, from, to, timeUnit);
    }


    @GetMapping("/table")
    public ResponseEntity<PagedResponse<ReservationTableDto>> getReservationTable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reservationDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok().body(reportService.getReservationTable(from, to, page, size, sortBy, sortDir));
    }











}
