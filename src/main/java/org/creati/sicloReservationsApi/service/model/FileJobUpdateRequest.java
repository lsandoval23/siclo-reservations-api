package org.creati.sicloReservationsApi.service.model;

import lombok.Builder;
import lombok.Data;
import org.creati.sicloReservationsApi.dao.postgre.model.FileJob;

import java.time.LocalDateTime;

@Data
@Builder
public class FileJobUpdateRequest {

    private final FileJob.JobStatus status;
    private final String errorMessage;
    private final LocalDateTime finishedAt;
    private final Integer totalRecords;
    private final Integer processedRecords;
    private final Integer skippedRecords;
    private final Integer errorRecords;
    private final String processingResult;


}
