package org.creati.sicloReservationsApi.service.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationTableDto (
        Long reservationId,
        Long classId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        String orderCreator,
        String paymentMethod,
        String status,
        String clientEmail,
        String studioName,
        String roomName,
        String disciplineName,
        String instructorName
){
}
