package com.paulotonelotti.reservasapi.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.Reserva;
import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;


public record ReservaResponseDTO(
    UUID id,
    UUID clienteId,
    UUID espacoId,
    LocalDateTime inicio,
    LocalDateTime fim,
    StatusReserva status
) {
    public ReservaResponseDTO(Reserva reserva) {
        this(reserva.getId(), 
            reserva.getCliente().getId(), 
            reserva.getEspaco().getId(), 
            reserva.getInicio(), 
            reserva.getFim(), 
            reserva.getStatus());
    }
}
