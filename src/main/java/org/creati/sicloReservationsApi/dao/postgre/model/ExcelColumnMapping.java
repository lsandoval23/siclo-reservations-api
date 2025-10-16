package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.creati.sicloReservationsApi.service.model.ColumnMappingDto;

import java.time.LocalDateTime;

@Data
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "excel_column_mapping")
public class ExcelColumnMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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


    public ColumnMappingDto toDto() {
        return ColumnMappingDto.builder()
                .mappingId(this.getMappingId())
                .fileType(this.getFileType())
                .fieldName(this.getFieldName())
                .excelHeader(this.getExcelHeader())
                .required(this.isRequired())
                .dataType(this.getDataType())
                .createdAt(this.getCreatedAt())
                .build();
    }

    public static ExcelColumnMapping fromDto(ColumnMappingDto dto) {
        return ExcelColumnMapping.builder()
                .mappingId(dto.getMappingId())
                .fileType(dto.getFileType())
                .fieldName(dto.getFieldName())
                .excelHeader(dto.getExcelHeader())
                .required(dto.getRequired() != null ? dto.getRequired() : false)
                .dataType(dto.getDataType())
                .createdAt(dto.getCreatedAt())
                .build();
    }





}
