package org.creati.sicloReservationsApi.auth.dto;

import lombok.Data;
import org.creati.sicloReservationsApi.auth.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RoleDto {

    private Long id;
    private String name;
    private String description;
    private Set<PermissionDto> permissions;

    public RoleDto() {}

    public RoleDto(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream()
                .map(PermissionDto::new)
                .collect(Collectors.toSet());
    }
}
