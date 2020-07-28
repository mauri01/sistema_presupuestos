package com.example.repository;

import com.example.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("ventaRepository")
public interface VentaRepository extends JpaRepository<Venta, Long> {
}
