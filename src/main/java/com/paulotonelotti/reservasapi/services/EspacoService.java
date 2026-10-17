package com.paulotonelotti.reservasapi.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.paulotonelotti.reservasapi.dto.request.EspacoRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.EspacoResponseDTO;
import com.paulotonelotti.reservasapi.entity.Espaco;
import com.paulotonelotti.reservasapi.exceptions.ConflitoDuplicidadeException;
import com.paulotonelotti.reservasapi.exceptions.RecursoNaoEncontradoException;
import com.paulotonelotti.reservasapi.exceptions.RegraNegocioException;
import com.paulotonelotti.reservasapi.repositories.EspacoRepository;
import com.paulotonelotti.reservasapi.repositories.ReservaRepository;

@Service
@Transactional(readOnly = true)
public class EspacoService {

    private final EspacoRepository espacoRepository;
    private final ReservaRepository reservaRepository;

    public EspacoService(EspacoRepository espacoRepository, ReservaRepository reservaRepository) {
        this.espacoRepository = espacoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public EspacoResponseDTO salvar(EspacoRequestDTO espacoDto) {
        String nome = espacoDto.nome().trim();

        if (espacoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoDuplicidadeException("Já existe espaço com esse nome");
        }

        Espaco espaco = new Espaco();
        espaco.setNome(nome);
        espaco.setValorPorHora(espacoDto.valorPorHora());
        espaco.setCapacidade(espacoDto.capacidade());

        Espaco espacoSalvo = espacoRepository.save(espaco);

        return new EspacoResponseDTO(espacoSalvo);
    }

    public Page<EspacoResponseDTO> listar(Pageable pageable) {
        Page<Espaco> listaEspacos = espacoRepository.findAll(pageable);

        return listaEspacos.map(EspacoResponseDTO::new);
    }

    public Espaco buscarEntidadePorId(UUID id) {
        return espacoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Espaço não encontrado: %s", id)));
    }

    public EspacoResponseDTO buscarPorId(UUID id) {
        Espaco espaco = buscarEntidadePorId(id);
        return new EspacoResponseDTO(espaco);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Espaco buscarEntidadePorIdComLock(UUID id) {
        return espacoRepository.findComLockById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Espaço não encontrado: %s", id)));
    }

    @Transactional
    public EspacoResponseDTO atualizar(UUID id, EspacoRequestDTO espacoDto) {
        Espaco espaco = buscarEntidadePorId(id);

        String nome = espacoDto.nome().trim();

        if (espacoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoDuplicidadeException("Já existe espaço com esse nome");
        }

        espaco.setNome(nome);
        espaco.setValorPorHora(espacoDto.valorPorHora());
        espaco.setCapacidade(espacoDto.capacidade());

        return new EspacoResponseDTO(espaco);
    }

    @Transactional
    public void excluir(UUID id) {
        Espaco espaco = buscarEntidadePorId(id);

        if (reservaRepository.existsByEspacoId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir o espaço: existem reservas vinculadas a ele");
        }

        espacoRepository.delete(espaco);
    }
}
