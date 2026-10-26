package com.paulotonelotti.reservasapi.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class GeradorTokenRecuperacao {

    private GeradorTokenRecuperacao() {
        throw new UnsupportedOperationException("Classe utilitária: não deve ser instanciada");
    }

    public static String gerarCodigo() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String gerarTokenHashCodigo(String codigo) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // O SHA-256 faz parte de toda JVM; se faltar, é um problema do ambiente, não da requisição
            throw new IllegalStateException(e);
        }
    }
}
