package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.service.model.ReservationReportDto;
import org.creati.sicloReservationsApi.service.model.ReservationSeriesDto;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
        List<Object[]> rawData = switch (groupBy.toLowerCase()) {
            case "studio" -> reservationRepository.findByStudio(from, to);
            case "instructor" -> reservationRepository.findByInstructor(from, to);
            case "discipline" -> reservationRepository.findByDiscipline(from, to);
            default -> throw new IllegalArgumentException("Unsupported groupBy: " + groupBy);
        };

        Map<String, Map<LocalDate, Long>> grouped = new LinkedHashMap<>();
        for (Object[] row : rawData) {
            String group = (String) row[0];
            Date sqlDate = (java.sql.Date) row[1];
            LocalDate date = sqlDate.toLocalDate();
            Long total = ((Number) row[2]).longValue();

            grouped
                    .computeIfAbsent(group, k -> new LinkedHashMap<>())
                    .put(date, total);
        }

        List<ReservationSeriesDto> series = grouped.entrySet().stream()
                .map(e -> {
                    List<Long> values = from.datesUntil(to.plusDays(1))
                            .map(d -> e.getValue().getOrDefault(d, 0L))
                            .collect(Collectors.toList());
                    return new ReservationSeriesDto(
                            Map.of(groupBy, e.getKey()), values
                    );
                })
                .toList();

        return new ReservationReportDto(
                new ReservationReportDto.Range(from, to),
                timeUnit,
                series
        );
    }
}
