package org.creati.sicloReservationsApi.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingService {

    void processReservationsFile(MultipartFile fileData);
    void processTransactionsFile(MultipartFile fileData);

}
