package com.paulotonelotti.reservasapi.helpers;

import java.util.Locale;

public final class HelperEmail {

    private HelperEmail() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
