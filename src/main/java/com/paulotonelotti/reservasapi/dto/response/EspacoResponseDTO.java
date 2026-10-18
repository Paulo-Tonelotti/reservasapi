package com.paulotonelotti.reservasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.Espaco;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Espaço cadastrado")
public record EspacoResponseDTO(
    @Schema(description = "Id do espaço", example = "210161ef-ace5-44c0-866e-4364831c72be")
    UUID id,

    @Schema(description = "Nome do espaço", example = "Auditório")
    String nome,

    @Schema(description = "Quantidade máxima de pessoas", example = "150")
    Integer capacidade,

    @Schema(description = "Valor cobrado por hora", example = "600.00")
    BigDecimal valorPorHora
) {
    public EspacoResponseDTO(Espaco espaco) {
        this(
            espaco.getId(),
            espaco.getNome(),
            espaco.getCapacidade(),
            espaco.getValorPorHora());
    }
}
