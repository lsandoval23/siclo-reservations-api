package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.PagedResponse;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationTableDto;

import java.time.LocalDate;

public interface ReportService {

    ReservationReportDto getGroupedReport(String groupBy, LocalDate from, LocalDate to, String timeUnit);

    PagedResponse<ReservationTableDto> getReservationTable(LocalDate from, LocalDate to, int page, int size, String sortBy, String sortDir);

}
