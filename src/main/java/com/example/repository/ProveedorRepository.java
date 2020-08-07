package com.example.repository;

import com.example.model.Article;
import com.example.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("proveedorRepository")
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Proveedor findByNumDocumento(String dni);

    Proveedor findById(int dni);
}
