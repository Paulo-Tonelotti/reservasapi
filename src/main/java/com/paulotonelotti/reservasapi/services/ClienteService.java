package com.paulotonelotti.reservasapi.services;

import java.util.Locale;
import java.util.UUID;

import com.paulotonelotti.reservasapi.helpers.HelperEmail;
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

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;

    public ClienteService(ClienteRepository clienteRepository, ReservaRepository reservaRepository) {
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
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
            boolean emailExistente = clienteRepository.existsByEmail(email);

            if (emailExistente) {
                throw new ConflitoDuplicidadeException("Email de atualização já cadastrado");
            }
        }

        cliente.setNome(nome);
        cliente.setEmail(email);

        return new ClienteResponseDTO(cliente);
    }

    @Transactional
    public void excluir(UUID id) {
        Cliente cliente = buscarEntidadePorId(id);

        if (reservaRepository.existsByClienteId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir o cliente: existem reservas vinculadas a ele");
        }

        clienteRepository.delete(cliente);
    }

    private String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
    }
}
