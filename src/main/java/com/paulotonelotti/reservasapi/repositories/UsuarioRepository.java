package com.paulotonelotti.reservasapi.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paulotonelotti.reservasapi.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // O usuário (login) ligado a um cliente; vazio para clientes cadastrados antes do login existir
    Optional<Usuario> findByClienteId(UUID clienteId);
}
