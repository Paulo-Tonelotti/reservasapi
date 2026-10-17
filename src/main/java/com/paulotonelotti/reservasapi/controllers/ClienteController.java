package com.paulotonelotti.reservasapi.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.paulotonelotti.reservasapi.dto.request.ClienteRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ClienteResponseDTO;
import com.paulotonelotti.reservasapi.services.ClienteService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/clientes")
public class ClienteController {
    
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> salvarCliente(@Valid @RequestBody ClienteRequestDTO clienteDto) {
        ClienteResponseDTO response = clienteService.salvar(clienteDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(response.id())
                    .toUri();

        return ResponseEntity.created(uri).body(response);
    }
    

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<ClienteResponseDTO> listaClientes = clienteService.listar();
        return ResponseEntity.ok().body(listaClientes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> listarClientePorId(@Valid @PathVariable UUID id) {
        ClienteResponseDTO response = clienteService.buscarPorId(id);
        return ResponseEntity.ok().body(response);
    }
    
}
