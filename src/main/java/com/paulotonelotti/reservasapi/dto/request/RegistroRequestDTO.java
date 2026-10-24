package com.paulotonelotti.reservasapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registrar um cliente e usuario")
public record RegistroRequestDTO(
        @Schema(description = "Nome do usuário a ser registrado", example = "Fulano de Tal")
        @NotBlank(message = "O nome é obrigatório e deve ser preenchido")
        @Size(max = 100)
        String nome,
        @Schema(description = "Email do usuário a ser registrado", example = "teste@emailteste.com")
        @NotBlank(message = "O email é obrigatório e deve ser preenchido")
        @Email
        @Size(max = 320)
        String email,
        @Schema(description = "Senha do usuário a ser registrado", example = "senha-forte-123")
        @NotBlank(message = "A senha é obrigatória e deve ser preenchida")
        @Size(min = 8, max = 72)
        String senha
) {
}
