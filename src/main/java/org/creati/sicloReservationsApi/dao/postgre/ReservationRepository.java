package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends BaseRepository<Reservation, Long> {

    @Query(value = "SELECT \n" +
            "\ts.name AS studio, \n" +
            "\tr.reservation_date  AS date, \n" +
            "\tCOUNT(r.reservation_id ) AS total    \n" +
            "FROM Reservation r\n" +
            "JOIN Room rm ON r.room_id  = rm.room_id \n" +
            "JOIN Studio s ON rm.studio_id  = s.studio_id \n" +
            "WHERE r.reservation_date  BETWEEN :from AND :to \n" +
            "GROUP BY s.name, r.reservation_date \n" +
            "ORDER BY s.name, r.reservation_date;", nativeQuery = true)
    List<Object[]> findByStudio(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = "SELECT \n" +
            "\ti.\"name\" ,\n" +
            "\tr.reservation_date ,\n" +
            "\tCOUNT(r.reservation_id)\n" +
            "FROM reservation r \n" +
            "JOIN instructor i ON r.instructor_id  = i.instructor_id \n" +
            "WHERE r.reservation_date  BETWEEN :from AND :to \n" +
            "GROUP BY i.\"name\", r.reservation_date \n" +
            "ORDER BY i.\"name\", r.reservation_date;", nativeQuery = true)
    List<Object[]> findByInstructor(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = "SELECT\n" +
            "\td.\"name\" ,\n" +
            "\tr.reservation_date ,\n" +
            "\tCOUNT(r.reservation_id )\n" +
            "FROM reservation r \n" +
            "JOIN discipline d ON d.discipline_id = r.discipline_id \n" +
            "WHERE r.reservation_date  BETWEEN :from AND :to \n" +
            "GROUP BY d.\"name\", r.reservation_date \n" +
            "ORDER BY d.\"name\", r.reservation_date;", nativeQuery = true)
    List<Object[]> findByDiscipline(@Param("from") LocalDate from, @Param("to") LocalDate to);


}
