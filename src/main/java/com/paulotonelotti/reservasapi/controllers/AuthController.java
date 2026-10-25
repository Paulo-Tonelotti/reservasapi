package com.paulotonelotti.reservasapi.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.paulotonelotti.reservasapi.dto.request.LoginRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.RegistroRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ClienteResponseDTO;
import com.paulotonelotti.reservasapi.dto.response.ErroResponse;
import com.paulotonelotti.reservasapi.dto.response.ErroValidacaoResponse;
import com.paulotonelotti.reservasapi.dto.response.LoginResponseDTO;
import com.paulotonelotti.reservasapi.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrar")
    @Operation(summary = "Cadastrar novo usuário e cliente")
    @ApiResponse(responseCode = "201", description = "Cliente criado")
    @ApiResponse(responseCode = "400", description = "Validação de dados",
            content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ClienteResponseDTO> registrarCliente(@Valid @RequestBody RegistroRequestDTO registroRequestDTO) {
        ClienteResponseDTO response = authService.registrar(registroRequestDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/clientes/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login",
            description = "Devolve um JWT para enviar nas próximas requisições no cabeçalho "
                    + "`Authorization: Bearer <token>`.")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class)))
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(authService.login(loginRequestDTO));
    }
}
