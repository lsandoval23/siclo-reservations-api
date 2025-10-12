package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.service.model.FileType;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_processing_job")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private String fileName;

    private String fileExtension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finishedAt;

    private Integer totalRecords;
    private Integer processedRecords;
    private Integer skippedRecords;
    private Integer errorRecords;

    @Column(columnDefinition = "TEXT")
    private String processingResult;


    public enum JobStatus {
        PENDING,
        IN_PROGRESS,
        SUCCESS,
        FAILED
    }



}
