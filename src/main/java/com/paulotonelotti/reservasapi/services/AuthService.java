package com.paulotonelotti.reservasapi.services;

import com.paulotonelotti.reservasapi.dto.request.ClienteRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.LoginRequestDTO;
import com.paulotonelotti.reservasapi.dto.request.RegistroRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ClienteResponseDTO;
import com.paulotonelotti.reservasapi.dto.response.LoginResponseDTO;
import com.paulotonelotti.reservasapi.entity.Usuario;
import com.paulotonelotti.reservasapi.entity.enums.Papel;
import com.paulotonelotti.reservasapi.exceptions.ConflitoDuplicidadeException;
import com.paulotonelotti.reservasapi.helpers.HelperEmail;
import com.paulotonelotti.reservasapi.repositories.UsuarioRepository;
import com.paulotonelotti.reservasapi.security.TokenService;
import com.paulotonelotti.reservasapi.security.UsuarioAutenticado;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(ClienteService clienteService, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository,
                       AuthenticationManager authenticationManager, TokenService tokenService) {
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Transactional
    public ClienteResponseDTO registrar(RegistroRequestDTO registroRequestDTO) {
        String email = HelperEmail.normalizarEmail(registroRequestDTO.email());
        ClienteRequestDTO clienteRequestDTO = new ClienteRequestDTO(registroRequestDTO.nome(), registroRequestDTO.email());

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
        UsernamePasswordAuthenticationToken userAndPass = new UsernamePasswordAuthenticationToken(email, loginRequestDTO.senha());
        Authentication authentication = authenticationManager.authenticate(userAndPass);

        UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();
        return tokenService.gerarToken(usuarioAutenticado);
    }
}
