package org.creati.sicloReservationsApi.auth.dto;

import lombok.Data;
import org.creati.sicloReservationsApi.auth.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserJwtInfo {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<RoleJwtInfo> roles;

    public UserJwtInfo() {}

    public UserJwtInfo(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles().stream()
                .map(RoleJwtInfo::new)
                .collect(Collectors.toSet());
    }

}
