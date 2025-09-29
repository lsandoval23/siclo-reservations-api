package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.service.model.PaymentDto;
import org.creati.sicloReservationsApi.service.model.ReservationDto;

import java.util.List;

public interface BatchPersistenceService {

    void persistReservationsBatch(List<ReservationDto> reservationDtoList, EntityCache cache);

    void persistPaymentsBatch(List<PaymentDto> paymentDtoList, EntityCache cache);

}
