package com.paulotonelotti.reservasapi.controllers;

import java.net.URI;
import java.util.UUID;

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
import com.paulotonelotti.reservasapi.dto.response.EspacoResponseDTO;
import com.paulotonelotti.reservasapi.services.EspacoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/espacos")
public class EspacoController {

    private final EspacoService espacoService;

    public EspacoController(EspacoService espacoService) {
        this.espacoService = espacoService;
    }

    @PostMapping
    public ResponseEntity<EspacoResponseDTO> salvarEspaco(@Valid @RequestBody EspacoRequestDTO espacoReqDto) {
        EspacoResponseDTO response = espacoService.salvar(espacoReqDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EspacoResponseDTO>> listarEspacosPagina(
            @PageableDefault(sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(espacoService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacoResponseDTO> buscarEspacoPorId(@PathVariable UUID id) {
        EspacoResponseDTO response = espacoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacoResponseDTO> atualizarEspaco(
            @PathVariable UUID id, @Valid @RequestBody EspacoRequestDTO espacoReqDto) {
        EspacoResponseDTO response = espacoService.atualizar(id, espacoReqDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEspaco(@PathVariable UUID id) {
        espacoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
