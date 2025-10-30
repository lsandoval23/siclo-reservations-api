package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;
import org.creati.sicloReservationsApi.service.model.job.FileJobCreateRequest;
import org.creati.sicloReservationsApi.service.model.job.FileJobUpdateRequest;

import java.util.Optional;

public interface FileJobService {

    FileJob createFileJob(FileJobCreateRequest createRequest);

    void updateStatus(Long jobId, FileJobUpdateRequest updateRequest, FileJob existingJob);

    Optional<FileJob> getFileJobById(Long jobId);

}
