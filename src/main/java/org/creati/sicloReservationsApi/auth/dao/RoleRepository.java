package org.creati.sicloReservationsApi.auth.dao;

import org.creati.sicloReservationsApi.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("""
        SELECT r FROM Role r
        JOIN FETCH r.permissions
        JOIN r.users u
        WHERE u.username = :username
    """)
    List<Role> findRolesByUsername(@Param("username") String username);



}