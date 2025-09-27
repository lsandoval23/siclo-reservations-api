package org.creati.sicloReservationsApi.auth.service.impl;

import org.creati.sicloReservationsApi.auth.dao.UserRepository;
import org.creati.sicloReservationsApi.auth.dto.LoginRequest;
import org.creati.sicloReservationsApi.auth.dto.LoginResponse;
import org.creati.sicloReservationsApi.auth.dto.UserJwtInfo;
import org.creati.sicloReservationsApi.auth.model.User;
import org.creati.sicloReservationsApi.auth.service.AuthService;
import org.creati.sicloReservationsApi.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final Long jwtExpiration;


    public AuthServiceImpl(
            final UserRepository userRepository,
            final JwtService jwtService,
            final AuthenticationManager authenticationManager,
            @Value("${jwt.expiration}") Long jwtExpiration) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtExpiration = jwtExpiration;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwtToken = jwtService.generateToken(user);
            UserJwtInfo userInfo = new UserJwtInfo(user);

            return new LoginResponse(jwtToken, userInfo, jwtExpiration);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials", e);
        }
    }
}
