package com.example.repository;

import com.example.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("clienteRepository")
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente findByNumDocumento(String dni);

    Cliente findById(int dni);
}
