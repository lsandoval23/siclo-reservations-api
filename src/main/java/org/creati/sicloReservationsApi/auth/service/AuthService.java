package org.creati.sicloReservationsApi.auth.service;

import org.creati.sicloReservationsApi.auth.dao.RoleRepository;
import org.creati.sicloReservationsApi.auth.dao.UserRepository;
import org.creati.sicloReservationsApi.auth.dto.LoginRequest;
import org.creati.sicloReservationsApi.auth.dto.LoginResponse;
import org.creati.sicloReservationsApi.auth.dto.RegisterRequest;
import org.creati.sicloReservationsApi.auth.dto.UserJwtInfo;
import org.creati.sicloReservationsApi.auth.model.Role;
import org.creati.sicloReservationsApi.auth.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final Long jwtExpiration;


    public AuthService(
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final PasswordEncoder passwordEncoder,
            final JwtService jwtService,
            final AuthenticationManager authenticationManager,
            @Value("${jwt.expiration:86400000}") Long jwtExpiration) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtExpiration = jwtExpiration;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            User user = userRepository.findByUsernameWithRolesAndPermissions(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Role> userRoles = roleRepository.findRolesByUsername(user.getUsername());
            user.setRoles(Set.copyOf(userRoles));

            String jwtToken = jwtService.generateToken(user);
            UserJwtInfo userInfo = new UserJwtInfo(user);

            return new LoginResponse(jwtToken, userInfo, jwtExpiration);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials", e);
        }
    }

    public User register(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFirstName(),
                registerRequest.getLastName()
        );

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(defaultRole));
        return userRepository.save(user);
    }

    public UserJwtInfo getCurrentUser(String token) {
        return jwtService.extractUserInfo(token);
    }




}
