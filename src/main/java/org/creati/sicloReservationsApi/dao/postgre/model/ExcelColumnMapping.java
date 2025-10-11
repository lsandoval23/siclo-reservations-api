package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "excel_column_mapping")
public class ExcelColumnMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mappingId;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String excelHeader;

    private boolean required;

    private String dataType;

    private LocalDateTime createdAt;
}
