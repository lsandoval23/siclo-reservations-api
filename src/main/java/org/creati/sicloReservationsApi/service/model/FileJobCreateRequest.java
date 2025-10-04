package org.creati.sicloReservationsApi.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileJobCreateRequest {

    private final String fileName;
    private final String fileExtension;

}
