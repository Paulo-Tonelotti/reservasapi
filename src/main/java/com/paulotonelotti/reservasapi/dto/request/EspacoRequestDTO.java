package com.paulotonelotti.reservasapi.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EspacoRequestDTO(

    @NotBlank(message = "O nome é obrigatório e deve ser preenchido")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    String nome,

    @NotNull(message = "A capacidade é obrigatória e deve ser preenchida")
    @Positive(message = "A capacidade deve ser positiva")
    Integer capacidade,

    @NotNull(message = "O valor por hora é obrigatório e deve ser preenchido")
    @Positive(message = "O valor por hora deve ser positivo")
    @Digits(
        integer = 8,
        fraction = 2,
        message = "O valor por hora deve ter no máximo 8 dígitos inteiros e 2 casas decimais")
    BigDecimal valorPorHora
) {
}
