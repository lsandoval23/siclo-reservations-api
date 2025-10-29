package org.creati.sicloReservationsApi.service.model;

import java.util.List;
import java.util.Map;

public record PagedResponse<T>(
        Map<String, Object> summary,
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
