package com.paulotonelotti.reservasapi.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar ou atualizar um espaço")
public record EspacoRequestDTO(

    @Schema(description = "Nome do espaço (único, sem diferenciar maiúsculas)", example = "Auditório")
    @NotBlank(message = "O nome é obrigatório e deve ser preenchido")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    String nome,

    @Schema(description = "Quantidade máxima de pessoas", example = "150")
    @NotNull(message = "A capacidade é obrigatória e deve ser preenchida")
    @Positive(message = "A capacidade deve ser positiva")
    Integer capacidade,

    @Schema(description = "Valor cobrado por hora de reserva", example = "600.00")
    @NotNull(message = "O valor por hora é obrigatório e deve ser preenchido")
    @Positive(message = "O valor por hora deve ser positivo")
    @Digits(
        integer = 8,
        fraction = 2,
        message = "O valor por hora deve ter no máximo 8 dígitos inteiros e 2 casas decimais")
    BigDecimal valorPorHora
) {
}
