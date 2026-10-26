package com.paulotonelotti.reservasapi.services;

import java.util.Locale;
import java.util.UUID;

import com.paulotonelotti.reservasapi.helpers.HelperEmail;
import com.paulotonelotti.reservasapi.repositories.RedefinicaoSenhaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paulotonelotti.reservasapi.dto.request.ClienteRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ClienteResponseDTO;
import com.paulotonelotti.reservasapi.entity.Cliente;
import com.paulotonelotti.reservasapi.exceptions.ConflitoDuplicidadeException;
import com.paulotonelotti.reservasapi.exceptions.RecursoNaoEncontradoException;
import com.paulotonelotti.reservasapi.exceptions.RegraNegocioException;
import com.paulotonelotti.reservasapi.repositories.ClienteRepository;
import com.paulotonelotti.reservasapi.repositories.ReservaRepository;
import com.paulotonelotti.reservasapi.repositories.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RedefinicaoSenhaRepository redefinicaoSenhaRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            RedefinicaoSenhaRepository redefinicaoSenhaRepository) {
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.redefinicaoSenhaRepository = redefinicaoSenhaRepository;
    }

    @Transactional
    public ClienteResponseDTO salvar(ClienteRequestDTO clienteDto) {
        String email = HelperEmail.normalizarEmail(clienteDto.email());
        String nome = normalizarNome(clienteDto.nome());

        if (clienteRepository.existsByEmail(email)) {
            throw new ConflitoDuplicidadeException("Email já cadastrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(email);

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return new ClienteResponseDTO(clienteSalvo);
    }

    public Cliente buscarEntidadePorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Cliente não encontrado: %s", id)));
    }

    public ClienteResponseDTO buscarPorId(UUID id) {
        Cliente cliente = buscarEntidadePorId(id);

        return new ClienteResponseDTO(cliente);
    }

    public Page<ClienteResponseDTO> listar(Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findAll(pageable);

        return clientes.map(ClienteResponseDTO::new);
    }

    @Transactional
    public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO clienteDto) {
        Cliente cliente = buscarEntidadePorId(id);

        String email = HelperEmail.normalizarEmail(clienteDto.email());
        String nome = normalizarNome(clienteDto.nome());

        if (!cliente.getEmail().equals(email)) {
            // O e-mail novo não pode estar em uso por outro cliente nem como login de outro usuário
            // (o login deste cliente ainda está com o e-mail antigo, então não entra nessa checagem)
            boolean emailExistente = clienteRepository.existsByEmail(email)
                    || usuarioRepository.existsByEmail(email);

            if (emailExistente) {
                throw new ConflitoDuplicidadeException("Email de atualização já cadastrado");
            }
        }

        cliente.setNome(nome);
        cliente.setEmail(email);

        // Mantém o e-mail de login igual ao do cliente. O usuário vem gerenciado pela transação,
        // então o dirty checking grava a alteração no commit, sem precisar de save()
        usuarioRepository.findByClienteId(id)
                .ifPresent(usuario -> usuario.setEmail(email));

        return new ClienteResponseDTO(cliente);
    }

    @Transactional
    public void excluir(UUID id) {
        Cliente cliente = buscarEntidadePorId(id);

        if (reservaRepository.existsByClienteId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir o cliente: existem reservas vinculadas a ele");
        }

        // O usuário (login) aponta para o cliente pela chave estrangeira, então sai primeiro.
        // Os dois deletes estão na mesma transação: se um falhar, nenhum é aplicado
        usuarioRepository.findByClienteId(id).ifPresent(usuario -> {
            redefinicaoSenhaRepository.apagarPorUsuario(usuario.getId());  // 1. os pedidos (apontam para o usuário)
            usuarioRepository.delete(usuario);                              // 2. o usuário (aponta para o cliente)
        });

        clienteRepository.delete(cliente);                                  // 3. o cliente
    }

    private String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
    }
}
