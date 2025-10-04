package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.springframework.stereotype.Repository;

@Repository
public interface FileJobRepository extends BaseRepository<FileJob, Long> {

}
