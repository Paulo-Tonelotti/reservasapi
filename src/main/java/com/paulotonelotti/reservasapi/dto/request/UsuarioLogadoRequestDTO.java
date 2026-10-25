package com.paulotonelotti.reservasapi.dto.request;

import java.util.UUID;

public record UsuarioLogadoRequestDTO(UUID id, boolean admin) {
}
