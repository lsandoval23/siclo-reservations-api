package org.creati.sicloReservationsApi.service.model;

import java.time.LocalDate;
import java.util.List;

public record ReservationReportDto(
        Range range,
        String timeUnit,
        List<ReservationSeriesDto> series
) {

    public record Range(
            LocalDate from,
            LocalDate to
    ) { }
}
