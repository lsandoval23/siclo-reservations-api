package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.PagedResponse;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationSortField;
import org.creati.sicloReservationsApi.service.model.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.SortDirection;

import java.time.LocalDate;

public interface ReportService {

    ReservationReportDto getGroupedReport(ReservationReportDto.GroupBy groupBy, LocalDate from, LocalDate to, String timeUnit);

    PagedResponse<ReservationTableDto> getReservationTable(LocalDate from, LocalDate to, int page, int size, ReservationSortField sortBy, SortDirection sortDir);

}
