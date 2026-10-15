package com.paulotonelotti.reservasapi.repositories;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paulotonelotti.reservasapi.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID>{
    
    @Query("""
    SELECT COUNT(r) > 0 FROM Reserva r
    WHERE r.espaco.id = :espacoId
      AND r.status <> 'CANCELADA'
      AND r.inicio < :fim
      AND r.fim > :inicio
    """)
    boolean existeConflito(@Param("espacoId") Long espacoId, 
                            @Param("inicio") LocalDateTime inicio, 
                            @Param("fim") LocalDateTime fim);
}
