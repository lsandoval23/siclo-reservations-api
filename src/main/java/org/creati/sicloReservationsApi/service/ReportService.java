package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.ClientReservationsPaymentsDto;
import org.creati.sicloReservationsApi.service.model.PagedResponse;
import org.creati.sicloReservationsApi.service.model.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.SortDirection;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReservationReportDto getReservationGroupedReport(ReservationReportDto.GroupBy groupBy, LocalDate from, LocalDate to, String timeUnit);

    PagedResponse<ReservationTableDto> getReservationTable(LocalDate from, LocalDate to, int page, int size, ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir);

    PagedResponse<PaymentTableDto> getPaymentTable(LocalDate from, LocalDate to, int page, int size, PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir);

    List<ClientReservationsPaymentsDto> getClientReservationsPayments(LocalDate from, LocalDate to, @Nullable Long clientId);

}

