package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ClientReservationsPaymentsProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClientRepository extends BaseRepository<Client, Long> {

    @Query(value = "SELECT * FROM get_clients_reservations_payments(:fromDate, :toDate, :clientId)", nativeQuery = true)
    List<ClientReservationsPaymentsProjection> getClientReservationsPayments(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("clientId") Long clientId
    );

}
