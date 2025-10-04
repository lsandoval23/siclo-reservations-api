package org.creati.sicloReservationsApi.service.model;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ProcessingResult {

    private boolean success;
    private int totalRows;
    private int processedRows;
    private int errorRows;
    private int skippedRows;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

}
