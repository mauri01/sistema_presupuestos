package com.example.repository;

import com.example.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("compraRepository")
public interface CompraRepository extends JpaRepository<Compra, Long> {
}
