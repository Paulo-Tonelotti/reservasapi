package com.paulotonelotti.reservasapi.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paulotonelotti.reservasapi.entity.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, UUID>{
    
}
