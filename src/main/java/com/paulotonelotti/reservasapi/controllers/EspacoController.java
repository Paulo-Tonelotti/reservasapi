package com.paulotonelotti.reservasapi.controllers;

import java.net.URI;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.paulotonelotti.reservasapi.dto.request.EspacoRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ErroResponse;
import com.paulotonelotti.reservasapi.dto.response.ErroValidacaoResponse;
import com.paulotonelotti.reservasapi.dto.response.EspacoResponseDTO;
import com.paulotonelotti.reservasapi.services.EspacoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/espacos")
@Tag(name = "Espaços", description = "Cadastro e consulta dos espaços que podem ser reservados")
public class EspacoController {

    private final EspacoService espacoService;

    public EspacoController(EspacoService espacoService) {
        this.espacoService = espacoService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar espaço",
            description = "O nome deve ser único, sem diferenciar maiúsculas e minúsculas.")
    @ApiResponse(responseCode = "201", description = "Espaço cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class)))
    @ApiResponse(responseCode = "409", description = "Já existe um espaço com esse nome",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<EspacoResponseDTO> salvarEspaco(@Valid @RequestBody EspacoRequestDTO espacoReqDto) {
        EspacoResponseDTO response = espacoService.salvar(espacoReqDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar espaços", description = "Lista paginada, ordenada por nome por padrão.")
    @ApiResponse(responseCode = "200", description = "Página de espaços")
    @ApiResponse(responseCode = "400", description = "Parâmetro de paginação ou ordenação inválido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Page<EspacoResponseDTO>> listarEspacosPagina(
            @ParameterObject @PageableDefault(sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(espacoService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar espaço por id")
    @ApiResponse(responseCode = "200", description = "Espaço encontrado")
    @ApiResponse(responseCode = "400", description = "Id em formato inválido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Espaço não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<EspacoResponseDTO> buscarEspacoPorId(
            @Parameter(description = "Id do espaço") @PathVariable UUID id) {
        EspacoResponseDTO response = espacoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar espaço",
            description = "Reajustes no valor por hora não alteram o valor de reservas já feitas.")
    @ApiResponse(responseCode = "200", description = "Espaço atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Espaço não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Já existe outro espaço com esse nome",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<EspacoResponseDTO> atualizarEspaco(
            @Parameter(description = "Id do espaço") @PathVariable UUID id,
            @Valid @RequestBody EspacoRequestDTO espacoReqDto) {
        EspacoResponseDTO response = espacoService.atualizar(id, espacoReqDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir espaço", description = "Só é permitido para espaços sem reservas vinculadas.")
    @ApiResponse(responseCode = "204", description = "Espaço excluído")
    @ApiResponse(responseCode = "404", description = "Espaço não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Espaço possui reservas vinculadas",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> excluirEspaco(@Parameter(description = "Id do espaço") @PathVariable UUID id) {
        espacoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
