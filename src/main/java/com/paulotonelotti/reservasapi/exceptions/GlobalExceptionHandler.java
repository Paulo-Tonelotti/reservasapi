package com.paulotonelotti.reservasapi.exceptions;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.paulotonelotti.reservasapi.dto.response.ErroResponse;
import com.paulotonelotti.reservasapi.dto.response.ErroValidacaoResponse;
import com.paulotonelotti.reservasapi.dto.response.ErroValidacaoResponse.CampoErro;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConflitoDuplicidadeException.class)
    public ResponseEntity<ErroResponse> handleConflitoDuplicidade(
            ConflitoDuplicidadeException ex, HttpServletRequest request) {

        return montarErroResponse(
                HttpStatus.CONFLICT,
                "Dados em conflito ou duplicados",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleViolacaoDeDados(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Violação de integridade em {}: {}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return montarErroResponse(
                HttpStatus.CONFLICT,
                "Violação de integridade dos dados",
                "A operação conflita com dados já existentes ou viola uma restrição do banco de dados",
                request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {

        return montarErroResponse(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> handleValidacaoDadosRequest(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<CampoErro> erros = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map((FieldError field) -> new CampoErro(field.getField(), field.getDefaultMessage()))
                .toList();

        return montarErroValidacaoResponse(
                HttpStatus.BAD_REQUEST,
                "Erro de validação",
                "Um ou mais campos estão inválidos.",
                erros,
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTipoDeArgumentoIncompativel(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        return montarErroResponse(
                HttpStatus.BAD_REQUEST,
                "Argumento inválido",
                String.format("O valor informado para '%s' não corresponde ao tipo esperado", ex.getName()),
                request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraNegocio(
            RegraNegocioException ex, HttpServletRequest request) {

        return montarErroResponse(
                HttpStatus.CONFLICT,
                "Operação não permitida",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Corpo de requisição ilegível em {}: {}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return montarErroResponse(
                HttpStatus.BAD_REQUEST,
                "Corpo de requisição inválido",
                "Corpo da requisição inválido ou mal formatado",
                request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        return montarErroResponse(
                HttpStatus.UNAUTHORIZED,
                "Não autorizado",
                "Email ou senha inválidos",
                request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> handleObjectOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {

        log.warn("Conflito de atualização {}: {}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return montarErroResponse(
                HttpStatus.CONFLICT,
                "Conflito de atualização",
                "A reserva foi alterada por outra operação. Consulte novamente e tente de novo",
                request);
    }

    // Ordenação por um campo que não existe (ex.: ?sort=telefone)
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErroResponse> handlePropriedadeInexistente(
            PropertyReferenceException ex, HttpServletRequest request) {

        log.warn("Propriedade inexistente em {}: {}", request.getRequestURI(), ex.getMessage());

        return montarErroResponse(
                HttpStatus.BAD_REQUEST,
                "Parâmetro de ordenação inválido",
                String.format("Não é possível ordenar por '%s'", ex.getPropertyName()),
                request);
    }

    // Ordenação com expressão inválida (ex.: ?sort=["nome"]). Essa exceção também aparece em erros
    // de uso do JPA que são bugs do código; por isso só vira 400 quando a requisição tem "sort"
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErroResponse> handleUsoInvalidoDeAcessoADados(
            InvalidDataAccessApiUsageException ex, HttpServletRequest request) {

        if (request.getParameter("sort") != null) {
            log.warn("Ordenação inválida em {}: {}", request.getRequestURI(), ex.getMessage());

            return montarErroResponse(
                    HttpStatus.BAD_REQUEST,
                    "Parâmetro de ordenação inválido",
                    "Use o formato sort=campo,asc ou sort=campo,desc com um campo existente",
                    request);
        }

        log.error("Erro de acesso a dados em {}", request.getRequestURI(), ex);

        return montarErroResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro inesperado",
                request);
    }

    private ResponseEntity<ErroResponse> montarErroResponse(
            HttpStatus statusHttp, String tituloErro, String mensagem, HttpServletRequest request) {

        ErroResponse erro = new ErroResponse(
                Instant.now(),
                statusHttp.value(),
                tituloErro,
                mensagem,
                request.getRequestURI());

        return ResponseEntity.status(statusHttp).body(erro);
    }

    private ResponseEntity<ErroValidacaoResponse> montarErroValidacaoResponse(
            HttpStatus statusHttp, String tituloErro, String mensagem,
            List<CampoErro> erros, HttpServletRequest request) {

        ErroValidacaoResponse erro = new ErroValidacaoResponse(
                Instant.now(),
                statusHttp.value(),
                tituloErro,
                mensagem,
                request.getRequestURI(),
                erros);

        return ResponseEntity.status(statusHttp).body(erro);
    }
}
