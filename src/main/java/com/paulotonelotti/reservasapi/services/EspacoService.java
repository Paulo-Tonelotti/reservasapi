package com.paulotonelotti.reservasapi.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paulotonelotti.reservasapi.dto.request.EspacoRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.EspacoResponseDTO;
import com.paulotonelotti.reservasapi.entity.Espaco;
import com.paulotonelotti.reservasapi.exceptions.ConflitoDuplicidadeException;
import com.paulotonelotti.reservasapi.exceptions.RecursoNaoEncontradoException;
import com.paulotonelotti.reservasapi.repositories.EspacoRepository;

@Service
public class EspacoService {

    private final EspacoRepository espacoRepository;

    public EspacoService(EspacoRepository espacoRepository) {
        this.espacoRepository = espacoRepository;
    }

    @Transactional
    public EspacoResponseDTO salvar(EspacoRequestDTO espacoDto) {
        String nome = espacoDto.nome().trim();

        if (espacoRepository.existsByNomeIgnoreCaseJpql(nome)) {
            throw new ConflitoDuplicidadeException("Já existe espaço com esse nome");
        }

        Espaco espaco = new Espaco();
        espaco.setNome(nome);
        espaco.setValorPorHora(espacoDto.valorPorHora());
        espaco.setCapacidade(espacoDto.capacidade());

        Espaco espacoSalvo = espacoRepository.save(espaco);

        return new EspacoResponseDTO(espacoSalvo);
    }

    @Transactional(readOnly = true)
    public List<EspacoResponseDTO> listar() {
        List<Espaco> listaEspacos = espacoRepository.findAll();

        return listaEspacos
                .stream()
                .map(EspacoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Espaco buscarEntidadePorId(UUID id) {
        return espacoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Espaço não encontrado: %s", id)));
    }

    @Transactional(readOnly = true)
    public EspacoResponseDTO buscarPorId(UUID id) {
        Espaco espaco = buscarEntidadePorId(id);

        return new EspacoResponseDTO(espaco);
    }
}
