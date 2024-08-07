package com.example.controller;

import com.example.model.Cliente;
import com.example.model.Role;
import com.example.model.User;
import com.example.service.ClienteService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UserService userService;

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
        User user = getUserAuth();
        cliente.setFromUser(user.getId());
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

        List<Cliente> clientes = clienteService.findAll();
        User user = getUserAuth();
        modelAndView.addObject("clientes",clientes.stream()
                .filter(cliente -> cliente.getFromUser() == user.getId() && cliente.isActive())
                .collect(Collectors.toList()));
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
            cliente.setActive(false);
            clienteService.saveCliente(cliente);
            modelAndView.addObject("message", "Se eliminó el cliente de forma correcta.");
        }catch (Exception e){
            modelAndView.addObject("error", "Ocurrió un error, vuelva a intentar.");
        }

        List<Cliente> clientes = clienteService.findAll();
        modelAndView.addObject("clientes",clientes.stream()
                .filter(Cliente::isActive)
                .collect(Collectors.toList()));
        modelAndView.setViewName("admin/clientes");
        return modelAndView;
    }

    private User getUserAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();

        } else if (authentication != null) {
            username = authentication.getPrincipal().toString();
        }
        return userService.findUserByEmail(username);
    }
}
