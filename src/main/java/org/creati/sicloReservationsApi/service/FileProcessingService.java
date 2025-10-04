package org.creati.sicloReservationsApi.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileProcessingService {

    void processReservationsFile(File fileData, Long jobId);
    void processPaymentTransactionsFile(File fileData);

}
