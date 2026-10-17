package com.paulotonelotti.reservasapi.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paulotonelotti.reservasapi.entity.Espaco;

public interface EspacoRepository extends JpaRepository<Espaco, UUID> {
    
    @Query("SELECT COUNT(e) > 0 FROM Espaco e WHERE LOWER(e.nome) = LOWER(:nome)")
    boolean existsByNomeIgnoreCaseJpql(@Param("nome") String nome);

    @Query("""
       SELECT COUNT(e) > 0
       FROM Espaco e
       WHERE LOWER(e.nome) = LOWER(:nome)
         AND e.id <> :id
       """)
    boolean existsOutroComMesmoNome(@Param("nome") String nome, @Param("id") Long id);      
}
