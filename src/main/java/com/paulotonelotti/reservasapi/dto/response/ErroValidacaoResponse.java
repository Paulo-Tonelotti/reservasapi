package com.paulotonelotti.reservasapi.dto.response;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de erro de validação, com a lista de campos inválidos")
public record ErroValidacaoResponse(
    @Schema(description = "Momento do erro (UTC)", example = "2026-10-17T17:12:05.385Z")
    Instant timestamp,

    @Schema(description = "Status HTTP", example = "400")
    int status,

    @Schema(description = "Categoria do erro", example = "Erro de validação")
    String erro,

    @Schema(description = "Descrição do erro", example = "Um ou mais campos estão inválidos.")
    String mensagem,

    @Schema(description = "Caminho da requisição", example = "/clientes")
    String caminho,

    @Schema(description = "Campos inválidos e suas mensagens")
    List<CampoErro> erros
) {
    @Schema(description = "Campo inválido")
    public record CampoErro(
        @Schema(description = "Nome do campo", example = "email")
        String campo,

        @Schema(description = "Mensagem de validação", example = "deve ser um endereço de e-mail bem formado")
        String mensagem
    ) {}
}
