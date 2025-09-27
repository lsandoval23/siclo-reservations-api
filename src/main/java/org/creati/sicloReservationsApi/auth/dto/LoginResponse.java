package org.creati.sicloReservationsApi.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private UserJwtInfo user;
    private Long expiresIn;


    public LoginResponse(String token, UserJwtInfo user, Long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }
}
