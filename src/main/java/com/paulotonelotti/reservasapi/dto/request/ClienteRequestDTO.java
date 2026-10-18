package com.paulotonelotti.reservasapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar ou atualizar um cliente")
public record ClienteRequestDTO(

    @Schema(description = "Nome do cliente", example = "Ana Souza", maxLength = 100)
    @NotBlank(message = "O nome é obrigatório e deve ser preenchido")
    @Size(max = 100)
    String nome,

    @Schema(description = "E-mail do cliente (único; gravado em minúsculas)", example = "ana.souza@example.com")
    @NotBlank(message = "O email é obrigatório e deve ser preenchido")
    @Email
    @Size(max = 320)
    String email
) {
}
