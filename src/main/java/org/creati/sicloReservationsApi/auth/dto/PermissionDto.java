package org.creati.sicloReservationsApi.auth.dto;

import lombok.Data;
import org.creati.sicloReservationsApi.auth.model.Permission;

@Data
public class PermissionDto {

    private Long id;
    private String name;
    private String description;
    private String resource;
    private String action;

    public PermissionDto() {}

    public PermissionDto(Permission permission) {
        this.id = permission.getId();
        this.name = permission.getName();
        this.description = permission.getDescription();
        this.resource = permission.getResource();
        this.action = permission.getAction();
    }
}
