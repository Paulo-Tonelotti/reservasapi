package com.paulotonelotti.reservasapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequestDTO(
        @NotBlank(message = "O token é obrigatório e deve ser preenchido")
        String token,

        @NotBlank(message = "A nova senha é obrigatória e deve ser preenchida")
        @Size(message = "A nova senha deve ter entre 8 e 72 caracteres", min = 8, max = 72)
        String novaSenha
) {
}
