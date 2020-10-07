package com.example.service;

import com.example.model.Cliente;
import com.example.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("clienteService")
public class ClienteServiceImpl implements ClienteService{

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public Cliente findbyDni(String numDocumento){
        return clienteRepository.findByNumDocumento(numDocumento);
    }

    @Override
    public Cliente findbyId(int id){
        return clienteRepository.findById(id);
    }

    @Override
    public void saveCliente(Cliente cliente){
        clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> findAll(){
        return clienteRepository.findAll();
    }

    @Override
    public void remove(Cliente cliente){
        clienteRepository.delete(cliente);
    }
}
