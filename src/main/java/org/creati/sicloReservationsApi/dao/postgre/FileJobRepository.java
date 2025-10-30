package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FileJobRepository extends BaseRepository<FileJob, Long> {

    Page<FileJob> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
