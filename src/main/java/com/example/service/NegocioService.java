package com.example.service;

import com.example.model.Negocio;

import java.util.List;

public interface NegocioService {
    void saveNegocio(Negocio negocio);

    List<Negocio> findAll();
}
