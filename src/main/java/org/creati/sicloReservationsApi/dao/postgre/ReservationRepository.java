package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.dto.ReservationReportProjection;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                SELECT new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto(
                    r.reservationId,
                    r.classId,
                    r.reservationDate,
                    r.reservationTime,
                    r.orderCreator,
                    r.paymentMethod,
                    r.status,
                    new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto$ClientInfo(
                        c.name,
                        c.email,
                        c.phone
                    ),
                    new org.creati.sicloReservationsApi.service.model.reports.ReservationTableDto$LocationInfo(
                        s.name,
                        rm.name,
                        s.country,
                        s.city
                    ),
                    d.name,
                    i.name
                )
                FROM Reservation r
                JOIN r.client c
                JOIN r.room rm
                JOIN rm.studio s
                JOIN r.discipline d
                JOIN r.instructor i
                WHERE r.reservationDate >= :fromDate AND r.reservationDate <= :toDate
            """)
    Page<ReservationTableDto> getReservationTable(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
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
