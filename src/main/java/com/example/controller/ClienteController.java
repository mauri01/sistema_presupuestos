package com.example.controller;

import com.example.model.Cliente;
import com.example.model.Role;
import com.example.model.User;
import com.example.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @RequestMapping(value="/admin/cliente", method = RequestMethod.GET)
    public ModelAndView clientes(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/cliente");

        Cliente cliente = new Cliente();
        modelAndView.addObject("cliente",cliente);
        return modelAndView;
    }

    @RequestMapping(value="/cliente", method = RequestMethod.POST)
    public ModelAndView addProveedor(@Valid Cliente cliente){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/cliente");
        String messageError = null;
        String message = null;

        Cliente clienteCreated = clienteService.findbyDni(cliente.getNumDocumento());

        if(clienteCreated == null){
            clienteService.saveCliente(cliente);
            message = "El Cliente fue creado de forma correcta";
        }else if(cliente.getId() != 0){
            clienteService.saveCliente(cliente);
            message = "El Cliente fue modificado de forma correcta";
        }

        else{
            messageError = "El cliente ya existe.";
        }

        Cliente clienteNuevo = new Cliente();
        modelAndView.addObject("cliente",clienteNuevo);
        modelAndView.addObject("error", messageError);
        modelAndView.addObject("message", message);
        return modelAndView;
    }

    @RequestMapping(value="/admin/clientes", method = RequestMethod.GET)
    public ModelAndView listaClientes(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/clientes");


        modelAndView.addObject("clientes",clienteService.findAll());
        return modelAndView;
    }

    @GetMapping("/cliente/{id}/edit")
    public ModelAndView editCliente(@PathVariable("id") int id){
        ModelAndView modelAndView = new ModelAndView();
        Cliente clienteEdit = clienteService.findbyId(id);
        modelAndView.addObject("cliente", clienteEdit);
        modelAndView.setViewName("admin/cliente");
        return modelAndView;
    }

    @GetMapping("/cliente/{id}/remove")
    public ModelAndView removeCliente(@PathVariable("id") int id){
        ModelAndView modelAndView = new ModelAndView();
        try{
            Cliente cliente = clienteService.findbyId(id);
            clienteService.remove(cliente);
            modelAndView.addObject("message", "Se eliminó el cliente de forma correcta.");
        }catch (Exception e){
            modelAndView.addObject("error", "Ocurrió un error, vuelva a intentar.");
        }

        modelAndView.addObject("clientes",clienteService.findAll());
        modelAndView.setViewName("admin/clientes");
        return modelAndView;
    }
}
