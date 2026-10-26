package com.paulotonelotti.reservasapi.repositories;

import com.paulotonelotti.reservasapi.entity.RedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RedefinicaoSenhaRepository extends JpaRepository<RedefinicaoSenha, UUID> {
    Optional<RedefinicaoSenha> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
    UPDATE RedefinicaoSenha r SET r.usadoEm = :agora
    WHERE r.usuario.id = :usuarioId AND r.usadoEm IS NULL
    """)
    void invalidarPendentes(@Param("usuarioId") UUID usuarioId, @Param("agora") Instant agora);

    @Modifying
    @Query("DELETE FROM RedefinicaoSenha r WHERE r.usuario.id = :usuarioId")
    void apagarPorUsuario(@Param("usuarioId") UUID usuarioId);
}
