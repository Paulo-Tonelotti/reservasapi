package com.paulotonelotti.reservasapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequestDTO (
        @Email
        @NotBlank(message = "O email é obrigatório e deve ser preenchido")
        String email
) {
}
