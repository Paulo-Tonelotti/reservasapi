package com.paulotonelotti.reservasapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(

    @NotBlank(message = "O nome é obrigatório e deve ser preenchido")
    @Size(max = 100)
    String nome,

    @NotBlank(message = "O email é obrigatório e deve ser preenchido")
    @Email
    @Size(max = 320)
    String email
) {
}
