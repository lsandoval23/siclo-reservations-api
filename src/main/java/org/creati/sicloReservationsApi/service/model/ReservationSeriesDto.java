package org.creati.sicloReservationsApi.service.model;

import java.util.List;

public record ReservationSeriesDto(
        String group,
        List<Long> values
) {
}
