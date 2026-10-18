package com.paulotonelotti.reservasapi.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paulotonelotti.reservasapi.entity.Reserva;
import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    boolean existsByClienteId(UUID clienteId);

    boolean existsByEspacoId(UUID espacoId);

    // Igual ao findById, mas já traz o cliente e o espaço na mesma consulta (para os nomes no response)
    @EntityGraph(attributePaths = {"cliente", "espaco"})
    Optional<Reserva> findComDetalhesById(UUID id);

    @Query("""
        SELECT COUNT(r) > 0 FROM Reserva r
        WHERE r.espaco.id = :espacoId
          AND r.status <> com.paulotonelotti.reservasapi.entity.enums.StatusReserva.CANCELADA
          AND r.inicio < :fim
          AND r.fim > :inicio
        """)
    boolean existeConflito(
            @Param("espacoId") UUID espacoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @EntityGraph(attributePaths = {"cliente", "espaco"})
    @Query("""
        SELECT r FROM Reserva r
        WHERE (:espacoId IS NULL OR r.espaco.id = :espacoId)
          AND (:clienteId IS NULL OR r.cliente.id = :clienteId)
          AND (:status IS NULL OR r.status = :status)
        """)
    Page<Reserva> buscarComFiltros(
            @Param("espacoId") UUID espacoId,
            @Param("clienteId") UUID clienteId,
            @Param("status") StatusReserva status,
            Pageable pageable);
}
