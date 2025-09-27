package org.creati.sicloReservationsApi.auth.dto;

import lombok.Data;
import org.creati.sicloReservationsApi.auth.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<RoleDto> roles;

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles().stream()
                .map(RoleDto::new)
                .collect(Collectors.toSet());
    }

}
