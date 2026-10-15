package com.paulotonelotti.reservasapi.dto.response;

import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.Cliente;

public record ClienteResponseDTO(
    UUID id,
    String nome,
    String email
) {
    public ClienteResponseDTO(Cliente cliente) {
        this(cliente.getId(), 
            cliente.getNome(), 
            cliente.getEmail());
    }
}
