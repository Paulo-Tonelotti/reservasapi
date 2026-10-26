package com.paulotonelotti.reservasapi.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.paulotonelotti.reservasapi.dto.request.AlterarSenhaRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.EsqueciSenhaRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.LoginRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.RedefinirSenhaRequestDTO;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
@SecurityRequirements // vazio: o cadastro e o login são públicos, sem o cadeado do Swagger
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
    public ResponseEntity<ClienteResponseDTO> registrarCliente(
            @Valid @RequestBody RegistroRequestDTO registroRequestDTO) {
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

    @PutMapping("/alterar-senha")
    // Sobrescreve o @SecurityRequirements vazio da classe: este endpoint exige o token
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Alterar a própria senha",
            description = "Troca a senha do usuário logado. É preciso informar a senha atual.")
    @ApiResponse(responseCode = "204", description = "Senha alterada")
    @ApiResponse(responseCode = "400",
            description = "Dados inválidos, senha atual incorreta ou nova senha igual à atual",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AlterarSenhaRequestDTO alterarSenhaRequestDTO) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        authService.alterarSenha(usuarioId, alterarSenhaRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/esqueci-senha")
    @Operation(summary = "Solicitar redefinição de senha",
            description = """
                    Se o e-mail estiver cadastrado, envia um link de redefinição válido por 30 minutos. \
                    A resposta é sempre a mesma, exista o e-mail ou não, para não revelar quais e-mails \
                    estão cadastrados. Um novo pedido invalida os links anteriores.""")
    @ApiResponse(responseCode = "204",
            description = "Pedido recebido (se o e-mail estiver cadastrado, as instruções foram enviadas)")
    @ApiResponse(responseCode = "400", description = "E-mail inválido",
            content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class)))
    public ResponseEntity<Void> esqueciSenha(
            @Valid @RequestBody EsqueciSenhaRequestDTO esqueciSenhaRequestDTO) {
        authService.solicitarRedefinicao(esqueciSenhaRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/redefinir-senha")
    @Operation(summary = "Redefinir a senha com o link recebido por e-mail",
            description = "Usa o token do link enviado pelo `esqueci-senha`. Cada link só pode ser usado uma vez.")
    @ApiResponse(responseCode = "204", description = "Senha redefinida")
    @ApiResponse(responseCode = "400",
            description = "Link inválido, expirado ou já usado, ou nova senha fora das regras",
            content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<Void> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequestDTO redefinirSenhaRequestDTO) {
        authService.redefinirSenha(redefinirSenhaRequestDTO);
        return ResponseEntity.noContent().build();
    }
}
