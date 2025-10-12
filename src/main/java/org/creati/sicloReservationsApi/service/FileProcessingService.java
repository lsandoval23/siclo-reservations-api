package org.creati.sicloReservationsApi.service;

import java.io.File;

public interface FileProcessingService {

    void processReservationsFile(File fileData, Long jobId);
    void processPaymentTransactionsFile(File fileData, Long jobId);

}
