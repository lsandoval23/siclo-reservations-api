package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.ReservationReportDto;

import java.time.LocalDate;

public interface ReportService {

    ReservationReportDto getGroupedReport(String groupBy, LocalDate from, LocalDate to, String timeUnit);

}
