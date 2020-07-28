package com.example.service;

import com.example.model.Compra;

import java.util.List;

public interface CompraService {

    public Compra saveCompra(Compra compra);
    List<Compra> findAll();
}
