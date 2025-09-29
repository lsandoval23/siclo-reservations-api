package org.creati.sicloReservationsApi.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingService {

    void processReservationsFile(MultipartFile fileData);
    void processPaymentTransactionsFile(MultipartFile fileData);

}
