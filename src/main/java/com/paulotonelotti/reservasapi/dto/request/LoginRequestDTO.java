package com.paulotonelotti.reservasapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para um usuario logar")
public record LoginRequestDTO(
        @Schema(description = "Email do usuário que vai logar", example = "teste@emailteste.com")
        @NotBlank(message = "O email é obrigatório e deve ser preenchido")
        @Email
        @Size(max = 320)
        String email,

        @Schema(description = "Senha do usuário que vai logar", example = "senha-forte-123")
        @NotBlank(message = "A senha é obrigatória e deve ser preenchida")
        String senha
) {
}
