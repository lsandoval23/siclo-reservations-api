package org.creati.sicloReservationsApi.service.model;

import java.util.List;
import java.util.Map;

public record ReservationSeriesDto(
        Map<String, String> group,
        List<Long> values
) {
}
