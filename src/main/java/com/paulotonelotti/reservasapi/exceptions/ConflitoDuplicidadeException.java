package com.paulotonelotti.reservasapi.exceptions;

public class ConflitoDuplicidadeException extends RuntimeException {

    public ConflitoDuplicidadeException(String mensagem) {
        super(mensagem);
    }
}
