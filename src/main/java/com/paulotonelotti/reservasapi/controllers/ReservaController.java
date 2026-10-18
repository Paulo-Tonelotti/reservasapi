package com.paulotonelotti.reservasapi.controllers;

import java.net.URI;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.paulotonelotti.reservasapi.dto.request.ReservaRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ErroResponse;
import com.paulotonelotti.reservasapi.dto.response.ErroValidacaoResponse;
import com.paulotonelotti.reservasapi.dto.response.ReservaResponseDTO;
import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;
import com.paulotonelotti.reservasapi.services.ReservaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservas")
@Tag(name = "Reservas", description = "Criação, consulta e mudança de status das reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @Operation(summary = "Criar reserva",
            description = """
                    Cria uma reserva `PENDENTE` se o horário estiver livre no espaço. \
                    O valor total é calculado pelo servidor (minutos reservados × valor por hora) \
                    e fica congelado na reserva. Uma reserva que termina às 10h não conflita com \
                    outra que começa às 10h.""")
    @ApiResponse(responseCode = "201", description = "Reserva criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (datas no passado, fim antes do início etc.)",
            content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Cliente ou espaço não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Horário já ocupado no espaço",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ReservaResponseDTO> criarReserva(@Valid @RequestBody ReservaRequestDTO reservaReqDto) {
        ReservaResponseDTO response = reservaService.criarReserva(reservaReqDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar reservas",
            description = "Lista paginada, ordenada por início por padrão. Os filtros são opcionais e combináveis.")
    @ApiResponse(responseCode = "200", description = "Página de reservas")
    @ApiResponse(responseCode = "400", description = "Filtro, paginação ou ordenação inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Page<ReservaResponseDTO>> listarReservas(
            @Parameter(description = "Filtra pelas reservas deste espaço")
            @RequestParam(required = false) UUID espacoId,
            @Parameter(description = "Filtra pelas reservas deste cliente")
            @RequestParam(required = false) UUID clienteId,
            @Parameter(description = "Filtra pelo status da reserva")
            @RequestParam(required = false) StatusReserva status,
            @ParameterObject @PageableDefault(sort = "inicio") Pageable pageable) {
        return ResponseEntity.ok(reservaService.listar(espacoId, clienteId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva por id")
    @ApiResponse(responseCode = "200", description = "Reserva encontrada")
    @ApiResponse(responseCode = "400", description = "Id em formato inválido",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Reserva não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ReservaResponseDTO> buscarReservaPorId(
            @Parameter(description = "Id da reserva") @PathVariable UUID id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar reserva", description = "Só reservas `PENDENTE` podem ser confirmadas.")
    @ApiResponse(responseCode = "200", description = "Reserva confirmada")
    @ApiResponse(responseCode = "404", description = "Reserva não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409",
            description = "Reserva não está pendente, ou foi alterada por outra operação ao mesmo tempo",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ReservaResponseDTO> confirmarReserva(
            @Parameter(description = "Id da reserva") @PathVariable UUID id) {
        ReservaResponseDTO response = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva",
            description = "Reservas `PENDENTE` ou `CONFIRMADA` podem ser canceladas. O cancelamento libera o horário.")
    @ApiResponse(responseCode = "200", description = "Reserva cancelada")
    @ApiResponse(responseCode = "404", description = "Reserva não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409",
            description = "Reserva já cancelada, ou alterada por outra operação ao mesmo tempo",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(
            @Parameter(description = "Id da reserva") @PathVariable UUID id) {
        return ResponseEntity.ok(reservaService.cancelarReserva(id));
    }
}
