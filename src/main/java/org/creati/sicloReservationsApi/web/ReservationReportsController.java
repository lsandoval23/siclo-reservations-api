package org.creati.sicloReservationsApi.web;

import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.ReportServiceImpl;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.springframework.format.annotation.DateTimeFormat;
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




}
