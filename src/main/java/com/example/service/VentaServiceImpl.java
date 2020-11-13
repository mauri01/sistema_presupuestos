package com.example.service;

import com.example.model.Venta;
import com.example.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ventaService")
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Override
    public Venta saveVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findAll(){
        return ventaRepository.findAll();
    }

    @Override
    public void remove (Venta venta){
        ventaRepository.delete(venta);
    }
}
