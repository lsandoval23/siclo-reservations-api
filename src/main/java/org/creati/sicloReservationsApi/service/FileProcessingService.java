package org.creati.sicloReservationsApi.service;

import java.io.File;

public interface FileProcessingService {
    void processFile(File fileData, Long jobId, String fileType);
}
