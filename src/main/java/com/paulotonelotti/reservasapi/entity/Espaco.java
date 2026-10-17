package com.paulotonelotti.reservasapi.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter 
@Getter 
@Entity 
@Table (name = "tb_espaco")
@NoArgsConstructor 
public class Espaco {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "capacidade", nullable = false)
    private Integer capacidade;

    @Column(name = "valor_por_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPorHora;
}
