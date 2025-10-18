package org.creati.sicloReservationsApi.service.impl;

import org.creati.sicloReservationsApi.dao.postgre.PaymentTransactionRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.service.ReportService;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReservationRepository reservationRepository;
    private final PaymentTransactionRepository paymentRepository;

    public ReportServiceImpl(
            final ReservationRepository reservationRepository,
            final PaymentTransactionRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ReservationReportDto getGroupedReport(ReservationReportDto.GroupBy groupBy, LocalDate from, LocalDate to, String timeUnit) {

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
        Page<ReservationTableDto> pageResponse = reservationRepository.getReservationTable(pageable);

        return new PagedResponse<>(
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
        Page<PaymentTableDto> pageResponse = paymentRepository.getPaymentTable(pageable);
        return new PagedResponse<>(
                pageResponse.getContent(),
                pageResponse.getNumber(),
                pageResponse.getSize(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages(),
                pageResponse.isLast()
        );
    }


}
