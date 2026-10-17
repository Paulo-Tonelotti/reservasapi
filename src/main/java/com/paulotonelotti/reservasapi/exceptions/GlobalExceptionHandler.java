package com.paulotonelotti.reservasapi.exceptions;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                request
        );
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> handleValidacaoDadosRequest(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<CampoErro> erros = ex
                                .getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map((FieldError field) -> new CampoErro(field.getField(), field.getDefaultMessage()))
                                .toList();
            
        return montarErroValidacaoResponse(
                HttpStatus.NOT_FOUND, 
                "Erro de validação", 
                "Um ou mais campos estão inválidos.", 
                erros, 
                request);
           
        }

    private ResponseEntity<ErroResponse> montarErroResponse(
            HttpStatus statusHttp, String tituloErro, String mensagem, HttpServletRequest request) {

        ErroResponse erro = new ErroResponse(
            Instant.now(),
            statusHttp.value(),
            tituloErro,
            mensagem,
            request.getRequestURI()
        );

        return ResponseEntity.status(statusHttp).body(erro);
    }

        private ResponseEntity<ErroValidacaoResponse> montarErroValidacaoResponse(
            HttpStatus statusHttp, String tituloErro, String mensagem, List<CampoErro> erros, HttpServletRequest request) {

        ErroValidacaoResponse erro = new ErroValidacaoResponse(
            Instant.now(),
            statusHttp.value(),
            tituloErro,
            mensagem,
            request.getRequestURI(),
            erros
        );

        return ResponseEntity.status(statusHttp).body(erro);
    }
}
