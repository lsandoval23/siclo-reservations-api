package org.creati.sicloReservationsApi.auth.service.impl;

import org.creati.sicloReservationsApi.auth.dao.UserRepository;
import org.creati.sicloReservationsApi.auth.dto.UserDto;
import org.creati.sicloReservationsApi.auth.exception.DuplicateResourceException;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.creati.sicloReservationsApi.auth.model.User;
import org.creati.sicloReservationsApi.auth.service.RoleService;
import org.creati.sicloReservationsApi.auth.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            final UserRepository userRepository,
            final RoleService roleService,
            final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + userDto.getEmail());
        }

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("Username already in use: " + userDto.getUsername());
        }

        User user = User.fromDto(userDto, passwordEncoder);
        User savedUser = userRepository.save(user);
        return savedUser.toDto();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return user.toDto();
    }

    @Override
    public UserDto getUserByUsername(String username) {
        return null;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return List.of();
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }

    @Override
    public UserDto addRolesToUser(String username, Set<Long> roleIds) {
        return null;
    }

    @Override
    public UserDto removeRolesFromUser(String username, Set<Long> roleIds) {
        return null;
    }
}
