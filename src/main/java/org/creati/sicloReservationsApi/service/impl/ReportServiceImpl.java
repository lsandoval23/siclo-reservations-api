package org.creati.sicloReservationsApi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.PaymentTransactionRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ClientReservationsPaymentsProjection;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.model.ClientReservationsPaymentsDto;
import org.creati.sicloReservationsApi.service.model.PagedResponse;
import org.creati.sicloReservationsApi.service.model.PaymentTableDto;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationSeriesDto;
import org.creati.sicloReservationsApi.service.model.ReservationTableDto;
import org.creati.sicloReservationsApi.service.model.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final ReservationRepository reservationRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final ClientRepository clientRepository;

    public ReportServiceImpl(
            final ReservationRepository reservationRepository,
            final PaymentTransactionRepository paymentRepository,
            final ClientRepository clientRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public ReservationReportDto getReservationGroupedReport(ReservationReportDto.GroupBy groupBy, LocalDate from, LocalDate to, String timeUnit) {

        // TODO Add timeUnit handling (week, month, etc.)
        List<ReservationReportProjection> rows = reservationRepository.getReservationsReportByDay(groupBy.getFieldName(), from, to);
        Map<String, List<ReservationReportProjection>> grouped = rows.stream()
                .collect(Collectors.groupingBy(ReservationReportProjection::getGroupName));
        List<LocalDate> allDates = from.datesUntil(to.plusDays(1)).toList();

        List<ReservationSeriesDto> series = grouped.entrySet().stream()
                .map(entry -> {
                    Map<LocalDate, Long> totalsByDate = entry.getValue().stream()
                            .collect(Collectors.toMap(
                                    ReservationReportProjection::getReservationDate,
                                    ReservationReportProjection::getTotal
                            ));

                    List<Long> values = allDates.stream()
                            .map(date -> totalsByDate.getOrDefault(date, 0L))
                            .toList();

                    return new ReservationSeriesDto(entry.getKey(), values);
                })
                .toList();

        return new ReservationReportDto(
                new ReservationReportDto.Range(from, to),
                timeUnit,
                series
        );
    }

    @Override
    public PagedResponse<ReservationTableDto> getReservationTable(
            LocalDate from, LocalDate to,
            int page, int size,
            ReservationTableDto.ReservationSortField sortBy, SortDirection sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.getValue()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReservationTableDto> pageResponse = reservationRepository.getReservationTable(from, to, pageable);

        // Build summary
        List<ReservationTableDto.ReservationTableSummary> summaryList = reservationRepository.getReservationSummary(from, to);

        long totalAccepted = 0;
        long totalCancelled = 0;
        long totalPending = 0;

        for (ReservationTableDto.ReservationTableSummary summary : summaryList) {
            switch (summary.status().toUpperCase()) {
                case "ACEPTADA" -> totalAccepted = summary.count();
                default -> log.warn("Unknow status: {}", summary.status());
            }
        }

        long totalClasses = totalAccepted + totalCancelled + totalPending;
        Map<String, Object> summary = Map.of(
                "totalClasses", totalClasses,
                "accepted", totalAccepted,
                "cancelled", totalCancelled,
                "pending", totalPending
        );

        return new PagedResponse<>(
                summary,
                pageResponse.getContent(),
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }

    @Override
    public PagedResponse<PaymentTableDto> getPaymentTable(
            LocalDate from, LocalDate to,
            int page, int size,
            PaymentTableDto.PaymentSortFiled sortBy, SortDirection sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.getValue()), sortBy.getFieldName());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentTableDto> pageResponse = paymentRepository.getPaymentTable(
                LocalDateTime.of(from, LocalTime.MIN),
                LocalDateTime.of(to, LocalTime.MAX),
                pageable);

        List<PaymentTableDto.PaymentTableSummary> summaryList = paymentRepository.getPaymentSummary(
                LocalDateTime.of(from, LocalTime.MIN),
                LocalDateTime.of(to, LocalTime.MAX));

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, Long> operationCounts = new HashMap<>();

        for (PaymentTableDto.PaymentTableSummary summary : summaryList) {
            operationCounts.put(summary.status(), summary.count());
            if (summary.totalAmount() != null) {
                totalAmount = totalAmount.add(summary.totalAmount());
            }
        }

        Map<String, Object> summary = Map.of(
                "totalAmountReceived", totalAmount,
                "operationSummary", operationCounts
        );


        return new PagedResponse<>(
                summary,
                pageResponse.getContent(),
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }

    @Override
    public List<ClientReservationsPaymentsDto> getClientReservationsPayments(LocalDate from, LocalDate to, @Nullable Long clientId) {
        List<ClientReservationsPaymentsProjection> rows = clientRepository.getClientReservationsPayments(from, to, clientId);
        return rows.stream()
                .map(rowItem -> new ClientReservationsPaymentsDto(
                        new ClientReservationsPaymentsDto.ClientInfo(
                                rowItem.getClientId(),
                                rowItem.getClientName(),
                                rowItem.getClientEmail(),
                                rowItem.getClientPhone()),
                        rowItem.getTotalReservations(),
                        rowItem.getTotalPayments(),
                        rowItem.getTotalAmountReceived(),
                        rowItem.getLastPaymentDate(),
                        rowItem.getLastReservationDate(),
                        rowItem.getTopDiscipline()
                ))
                .toList();
    }


}
