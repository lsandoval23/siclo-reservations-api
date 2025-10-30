package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.reports.ClientReservationsPaymentsDto;
import org.creati.sicloReservationsApi.service.model.reports.PagedResponse;
import org.creati.sicloReservationsApi.service.model.reports.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationGraphReportDto;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.reports.SortDirection;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReservationGraphReportDto getReservationGroupedReport(ReservationGraphReportDto.GroupBy groupBy, LocalDate from, LocalDate to, String timeUnit);

    PagedResponse<ReservationTableDto> getReservationTable(LocalDate from, LocalDate to, int page, int size, ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir);

    PagedResponse<PaymentTableDto> getPaymentTable(LocalDate from, LocalDate to, int page, int size, PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir);

    List<ClientReservationsPaymentsDto> getClientReservationsPayments(LocalDate from, LocalDate to, @Nullable Long clientId);

    PagedResponse<ReservationTableDto> getReservationTableByClientId(LocalDate from, LocalDate to, Long clientId, int page, int size, ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir);

    PagedResponse<PaymentTableDto> getPaymentTableByClientId(LocalDate from, LocalDate to, Long clientId, int page, int size, PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir);


}

