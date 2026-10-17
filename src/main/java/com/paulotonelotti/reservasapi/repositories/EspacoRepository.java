package com.paulotonelotti.reservasapi.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paulotonelotti.reservasapi.entity.Espaco;

public interface EspacoRepository extends JpaRepository<Espaco, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);
}
