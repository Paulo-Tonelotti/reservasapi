package com.paulotonelotti.reservasapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para o usuário logado trocar a própria senha")
public record AlterarSenhaRequestDTO(

        @Schema(description = "Senha atual do usuário", example = "senha-forte-123")
        @NotBlank(message = "A senha atual é obrigatória e deve ser preenchida")
        String senhaAtual,

        @Schema(description = "Nova senha (de 8 a 72 caracteres)", example = "nova-senha-456")
        @NotBlank(message = "A senha nova é obrigatória e deve ser preenchida")
        @Size(min = 8, max = 72, message = "A senha nova deve ter entre 8 e 72 caracteres")
        String senhaNova
) {
}
