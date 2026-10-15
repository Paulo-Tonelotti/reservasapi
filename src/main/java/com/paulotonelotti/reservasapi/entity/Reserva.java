package com.paulotonelotti.reservasapi.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter 
@Getter 
@Entity 
@Table(name = "tb_reserva")
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_espaco", nullable = false)
    private Espaco espaco;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime fim;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusReserva status;
}
