package com.example.service;

import com.example.model.Article;
import com.example.model.Proveedor;
import com.example.repository.ArticleRepository;
import com.example.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("proveedorService")
public class ProveedorServiceImpl implements ProveedorService{

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Override
    public Proveedor findbyDni(String numDocumento){
        return proveedorRepository.findByNumDocumento(numDocumento);
    }

    @Override
    public void saveProveedor(Proveedor proveedor){
        proveedorRepository.save(proveedor);
    }

    @Override
    public List<Proveedor> findAll(){
        return proveedorRepository.findAll();
    }

    @Override
    public void remove(Proveedor proveedor){
        proveedorRepository.delete(proveedor);
    }
}
