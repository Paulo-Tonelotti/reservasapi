package com.paulotonelotti.reservasapi.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.paulotonelotti.reservasapi.dto.response.ErroResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/*
 * Os erros 401 e 403 nascem nos filtros do Spring Security, antes de a requisição chegar
 * aos controllers, por isso não passam pelo GlobalExceptionHandler. Esta classe escreve a
 * resposta no mesmo formato do ErroResponse que o resto da API usa.
 */
@Component
public class TratadorErrosSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorErrosSeguranca.class);

    private final JsonMapper jsonMapper;

    public TratadorErrosSeguranca(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    // 401: sem token, token inválido, adulterado ou expirado
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {

        log.debug("Requisição não autenticada em {}: {}", request.getRequestURI(), ex.getMessage());

        // Cabeçalho padrão que indica ao cliente qual tipo de autenticação a API espera
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");

        escreverErro(request, response, HttpStatus.UNAUTHORIZED,
                "Não autorizado",
                "Autenticação necessária: envie um token válido no cabeçalho Authorization");
    }

    // 403: o usuário está autenticado, mas o papel dele não permite acessar a rota
    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {

        log.debug("Acesso negado em {}: {}", request.getRequestURI(), ex.getMessage());

        escreverErro(request, response, HttpStatus.FORBIDDEN,
                "Acesso negado",
                "Você não tem permissão para acessar este recurso");
    }

    private void escreverErro(
            HttpServletRequest request, HttpServletResponse response,
            HttpStatus status, String titulo, String mensagem) throws IOException {

        ErroResponse erro = new ErroResponse(
                Instant.now(),
                status.value(),
                titulo,
                mensagem,
                request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), erro);
    }
}
