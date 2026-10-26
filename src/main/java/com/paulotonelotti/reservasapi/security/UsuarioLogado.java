package com.paulotonelotti.reservasapi.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

public record UsuarioLogado(UUID idCliente, boolean admin) {

    // Monta o usuário logado a partir do token. O administrador não tem clienteId, então o idCliente fica nulo
    public static UsuarioLogado doToken(Jwt jwt) {
        boolean ehAdmin = jwt.getClaimAsStringList("roles").contains("ADMIN");

        String clienteIdToken = jwt.getClaimAsString("clienteId");
        UUID idCliente = clienteIdToken != null ? UUID.fromString(clienteIdToken) : null;

        return new UsuarioLogado(idCliente, ehAdmin);
    }

    // O ADMIN acessa qualquer cliente; os demais, só o próprio
    public boolean podeAcessarCliente(UUID clienteId) {
        return admin || clienteId.equals(idCliente);
    }
}
