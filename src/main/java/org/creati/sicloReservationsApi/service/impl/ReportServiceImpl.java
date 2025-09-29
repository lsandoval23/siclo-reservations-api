package org.creati.sicloReservationsApi.service.impl;

import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.service.ReportService;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationSeriesDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReservationRepository reservationRepository;

    public ReportServiceImpl(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public ReservationReportDto getGroupedReport(String groupBy, LocalDate from, LocalDate to, String timeUnit) {

        List<ReservationReportProjection> rows = reservationRepository.getReservationsReportByDay(groupBy, from, to);
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
}
