package org.creati.sicloReservationsApi.cache;

import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.file.model.ReservationExcel;

import java.util.List;

public interface EntityCacheService {
    EntityCache preloadEntitiesForReservation(List<ReservationExcel> reservations);
}
