package com.example.service;

import com.example.model.Article;
import com.example.model.Proveedor;

import java.util.List;

public interface ProveedorService {
    public Proveedor findbyDni(String numDocumento);
    void saveProveedor(Proveedor proveedor);

    List<Proveedor> findAll();

    void remove(Proveedor proveedor);
}
