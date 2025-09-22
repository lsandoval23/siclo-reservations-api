package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends BaseRepository<Reservation, Long> {

    @Query(value = "SELECT * FROM get_reservations_report(:groupBy, :fromDate, :toDate)", nativeQuery = true)
    List<ReservationReportProjection> getReservationsReportByDay(
            @Param("groupBy") String groupBy,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );




}
