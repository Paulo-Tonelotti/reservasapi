package com.paulotonelotti.reservasapi.security;

import java.util.UUID;

public record UsuarioLogado(UUID idCliente, boolean admin) {
}
