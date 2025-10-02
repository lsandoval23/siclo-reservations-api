package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.service.model.ReservationTableDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT new org.creati.sicloReservationsApi.service.model.ReservationTableDto(
               r.reservationId AS reservationId,
               r.classId AS classId,
               r.reservationDate AS reservationDate,
               r.reservationTime AS reservationTime,
               r.orderCreator AS orderCreator,
               r.paymentMethod AS paymentMethod,
               r.status AS status,
               c.email AS clientEmail,
               s.name AS studioName,
               rm.name AS roomName,
               d.name AS disciplineName,
               i.name AS instructorName )
        FROM Reservation r
        JOIN r.client c
        JOIN r.room rm
        JOIN rm.studio s
        JOIN r.discipline d
        JOIN r.instructor i
        """)
    Page<ReservationTableDto> getReservationTable(Pageable pageable);







}
