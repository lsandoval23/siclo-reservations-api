package org.creati.sicloReservationsApi.dao.postgre.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ClientReservationsPaymentsProjection {

    Long getClientId();
    String getClientName();
    String getClientEmail();
    String getClientPhone();
    Integer getTotalReservations();
    Integer getTotalPayments();
    BigDecimal getTotalAmountReceived();
    LocalDateTime getLastPaymentDate();
    LocalDate getLastReservationDate();
    String getTopDiscipline();

}
