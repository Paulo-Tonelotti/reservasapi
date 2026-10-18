package com.paulotonelotti.reservasapi.dto.response;

import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.Cliente;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cliente cadastrado")
public record ClienteResponseDTO(
    @Schema(description = "Id do cliente", example = "b0f103f2-1528-4d41-8c0d-189fd5d84982")
    UUID id,

    @Schema(description = "Nome do cliente", example = "Ana Souza")
    String nome,

    @Schema(description = "E-mail do cliente", example = "ana.souza@example.com")
    String email
) {
    public ClienteResponseDTO(Cliente cliente) {
        this(
            cliente.getId(),
            cliente.getNome(),
            cliente.getEmail());
    }
}
