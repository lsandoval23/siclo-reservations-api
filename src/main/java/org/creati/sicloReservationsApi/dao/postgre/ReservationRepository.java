package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends BaseRepository<Reservation, Long> {

    @Query(value = "SELECT * FROM get_reservations_time_series(:groupBy, :timeUnit,:fromDate, :toDate)", nativeQuery = true)
    List<ReservationReportProjection> getReservationsReportsTimeSeries(
            @Param("groupBy") String groupBy,
            @Param("timeUnit") String timeUnit,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto$ReservationTableSummary(
                    r.status,
                    COUNT(r)
                )
                FROM Reservation r
                WHERE r.reservationDate >= :from AND r.reservationDate <= :to
                GROUP BY r.status
            """)
    List<ReservationTableDto.ReservationTableSummary> getReservationSummary(@Param("from") LocalDate from, @Param("to") LocalDate to);










}
