package com.example.service;


import com.example.model.Cliente;

import java.util.List;

public interface ClienteService {

    public Cliente findbyDni(String numDocumento);
    public Cliente findbyId(int id);
    void saveCliente(Cliente proveedor);

    List<Cliente> findAll();

    void remove(Cliente cliente);
}
