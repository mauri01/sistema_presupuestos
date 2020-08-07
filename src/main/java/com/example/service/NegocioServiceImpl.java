package com.example.service;

import com.example.model.Negocio;
import com.example.repository.NegocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("negocioService")
public class NegocioServiceImpl implements NegocioService{

    @Autowired
    private NegocioRepository negocioRepository;

    @Override
    public void saveNegocio(Negocio negocio) {
        negocioRepository.save(negocio);
    }

    @Override
    public List<Negocio> findAll() {
        return negocioRepository.findAll();
    }
}
