package org.creati.sicloReservationsApi.service.model;

import lombok.Getter;

@Getter
public enum ReservationSortField {

    RESERVATION_DATE("reservationDate"),
    RESERVATION_ID("reservationId");

    private final String fieldName;

    ReservationSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public static ReservationSortField fromValue(String value) {
        for (ReservationSortField field : values()) {
            if (field.fieldName.equalsIgnoreCase(value) || field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Invalid sortBy value: " + value);
    }

}
