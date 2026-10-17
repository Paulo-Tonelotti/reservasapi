package com.paulotonelotti.reservasapi.repositories;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.paulotonelotti.reservasapi.entity.Espaco;
import org.springframework.data.jpa.repository.Lock;

public interface EspacoRepository extends JpaRepository<Espaco, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Espaco> findComLockById(UUID id);
}
