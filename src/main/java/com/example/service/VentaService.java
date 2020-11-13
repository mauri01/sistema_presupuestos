package com.example.service;

import com.example.model.Proveedor;
import com.example.model.Venta;

import java.util.List;

public interface VentaService {

    public Venta saveVenta(Venta venta);
    void remove(Venta venta);

    List<Venta> findAll();
}
