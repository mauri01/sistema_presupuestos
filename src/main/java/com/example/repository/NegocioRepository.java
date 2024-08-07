package com.example.repository;

import com.example.model.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("negocioRepository")
public interface NegocioRepository extends JpaRepository<Negocio, Long> {
}
