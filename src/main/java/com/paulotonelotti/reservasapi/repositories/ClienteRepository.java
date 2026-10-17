package com.paulotonelotti.reservasapi.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paulotonelotti.reservasapi.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByEmail(String email);
}
