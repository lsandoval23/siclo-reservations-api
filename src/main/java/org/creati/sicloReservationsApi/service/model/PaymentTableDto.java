package org.creati.sicloReservationsApi.service.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTableDto(
        Integer month,
        Integer day,
        Integer week,
        LocalDateTime purchaseDate,
        LocalDateTime accreditationDate,
        LocalDateTime releaseDate,
        String operationType,
        BigDecimal productValue,
        BigDecimal transactionFee,
        BigDecimal amountReceived,
        Integer installments,
        String paymentMethod,
        String packageName,
        Integer classCount,
        ClientInfo clientInfo
) {

    public record ClientInfo(
            String name,
            String email,
            String phone
    ) {
    }


    @Getter
    public enum PaymentSortFiled {

        ACCREDITATION_DATE("accreditationDate"),
        PURCHASE_DATE("purchaseDate"),
        RELEASE_DATE("releaseDate");

        private final String fieldName;

        PaymentSortFiled(String fieldName) {
            this.fieldName = fieldName;
        }

        public static PaymentSortFiled fromValue(String value) {
            for (PaymentSortFiled field : values()) {
                if (field.fieldName.equalsIgnoreCase(value) || field.name().equalsIgnoreCase(value)) {
                    return field;
                }
            }
            throw new IllegalArgumentException("Invalid sortBy value: " + value);
        }
    }
}
