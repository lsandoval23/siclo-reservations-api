package org.creati.sicloReservationsApi.cache;

import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.file.model.PaymentDto;
import org.creati.sicloReservationsApi.file.model.ReservationDto;

import java.util.List;

public interface EntityCacheService {
    EntityCache preloadEntitiesForReservation(List<ReservationDto> reservations);

    EntityCache preloadEntitiesForPayments(List<PaymentDto> payments);
}
