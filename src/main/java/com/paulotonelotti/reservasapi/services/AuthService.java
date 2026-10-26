package com.paulotonelotti.reservasapi.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paulotonelotti.reservasapi.dto.request.AlterarSenhaRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.ClienteRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.EsqueciSenhaRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.LoginRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.RedefinirSenhaRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.RegistroRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ClienteResponseDTO;
import com.paulotonelotti.reservasapi.dto.response.LoginResponseDTO;
import com.paulotonelotti.reservasapi.entity.RedefinicaoSenha;
import com.paulotonelotti.reservasapi.entity.Usuario;
import com.paulotonelotti.reservasapi.entity.enums.Papel;
import com.paulotonelotti.reservasapi.exceptions.ConflitoDuplicidadeException;
import com.paulotonelotti.reservasapi.exceptions.DadosInvalidosException;
import com.paulotonelotti.reservasapi.exceptions.RecursoNaoEncontradoException;
import com.paulotonelotti.reservasapi.helpers.HelperEmail;
import com.paulotonelotti.reservasapi.repositories.RedefinicaoSenhaRepository;
import com.paulotonelotti.reservasapi.repositories.UsuarioRepository;
import com.paulotonelotti.reservasapi.security.GeradorTokenRecuperacao;
import com.paulotonelotti.reservasapi.security.TokenService;
import com.paulotonelotti.reservasapi.security.UsuarioAutenticado;

@Service
public class AuthService {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedefinicaoSenhaRepository redefinicaoSenhaRepository;
    private final EmailService emailService;

    public AuthService(
            ClienteService clienteService,
            PasswordEncoder passwordEncoder,
            UsuarioRepository usuarioRepository,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            RedefinicaoSenhaRepository redefinicaoSenhaRepository,
            EmailService emailService) {
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.redefinicaoSenhaRepository = redefinicaoSenhaRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ClienteResponseDTO registrar(RegistroRequestDTO registroRequestDTO) {
        String email = HelperEmail.normalizarEmail(registroRequestDTO.email());
        ClienteRequestDTO clienteRequestDTO =
                new ClienteRequestDTO(registroRequestDTO.nome(), registroRequestDTO.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoDuplicidadeException("Já existe um cliente cadastrado com esse e-mail");
        }

        ClienteResponseDTO responseDTO = clienteService.salvar(clienteRequestDTO);

        Usuario usuario = new Usuario();
        usuario.setEmail(responseDTO.email());
        usuario.setSenha(passwordEncoder.encode(registroRequestDTO.senha()));
        usuario.setPapel(Papel.CLIENTE);
        usuario.setCliente(clienteService.buscarEntidadePorId(responseDTO.id()));
        usuarioRepository.save(usuario);

        return responseDTO;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        String email = HelperEmail.normalizarEmail(loginRequestDTO.email());
        UsernamePasswordAuthenticationToken userAndPass =
                new UsernamePasswordAuthenticationToken(email, loginRequestDTO.senha());
        Authentication authentication = authenticationManager.authenticate(userAndPass);

        UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();
        return tokenService.gerarToken(usuarioAutenticado);
    }

    @Transactional
    public void alterarSenha(UUID usuarioId, AlterarSenhaRequestDTO alterarSenhaRequestDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!passwordEncoder.matches(alterarSenhaRequestDTO.senhaAtual(), usuario.getSenha())) {
            throw new DadosInvalidosException("A senha atual está incorreta");
        }

        if (passwordEncoder.matches(alterarSenhaRequestDTO.senhaNova(), usuario.getSenha())) {
            throw new DadosInvalidosException("A nova senha deve ser diferente da atual");
        }

        usuario.setSenha(passwordEncoder.encode(alterarSenhaRequestDTO.senhaNova()));
    }

    @Transactional
    public void solicitarRedefinicao(EsqueciSenhaRequestDTO esqueciSenhaRequestDTO) {
        String email = HelperEmail.normalizarEmail(esqueciSenhaRequestDTO.email());
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isPresent()) {
            redefinicaoSenhaRepository.invalidarPendentes(usuario.get().getId(), Instant.now());
            String codigo = GeradorTokenRecuperacao.gerarCodigo();
            String tokenHash = GeradorTokenRecuperacao.gerarTokenHashCodigo(codigo);

            RedefinicaoSenha redefinicaoSenha = new RedefinicaoSenha();
            redefinicaoSenha.setUsuario(usuario.get());
            redefinicaoSenha.setTokenHash(tokenHash);
            redefinicaoSenha.setExpiraEm(Instant.now().plus(30, ChronoUnit.MINUTES));

            redefinicaoSenhaRepository.save(redefinicaoSenha);

            String link = frontendUrl + "/redefinir-senha?token=" + codigo;
            emailService.enviarRedefinicaoSenha(usuario.get().getEmail(), link);
        }
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequestDTO redefinirSenhaRequestDTO) {
        String codigoToken = redefinirSenhaRequestDTO.token();
        String tokenHash = GeradorTokenRecuperacao.gerarTokenHashCodigo(codigoToken);

        RedefinicaoSenha pedido = redefinicaoSenhaRepository
                .findByTokenHash(tokenHash).orElseThrow(() -> new DadosInvalidosException("Link inválido ou expirado"));

        if (pedido.getExpiraEm().isAfter(Instant.now()) && pedido.getUsadoEm() == null) {
            Usuario usuario = pedido.getUsuario();
            usuario.setSenha(passwordEncoder.encode(redefinirSenhaRequestDTO.novaSenha()));
            pedido.setUsadoEm(Instant.now());
        } else {
            throw new DadosInvalidosException("Link inválido ou expirado");
        }
    }
}
