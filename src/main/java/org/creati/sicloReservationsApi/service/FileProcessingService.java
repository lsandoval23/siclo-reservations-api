package org.creati.sicloReservationsApi.service;

import org.creati.sicloReservationsApi.service.model.FileType;

import java.io.File;

public interface FileProcessingService {
    void processFile(File fileData, Long jobId, FileType fileType);
}
