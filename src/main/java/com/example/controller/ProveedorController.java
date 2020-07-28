package com.example.controller;

import com.example.model.Proveedor;
import com.example.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @RequestMapping(value="/admin/proveedor", method = RequestMethod.GET)
    public ModelAndView proveedores(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/proveedor");

        Proveedor proveedor = new Proveedor();
        modelAndView.addObject("proveedor",proveedor);
        return modelAndView;
    }

    @RequestMapping(value="/admin/proveedor-list", method = RequestMethod.GET)
    public ModelAndView proveedorList(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/proveedorList");
        modelAndView.addObject("proveedor",proveedorService.findAll());
        return modelAndView;
    }

    @RequestMapping(value="/proveedor", method = RequestMethod.POST)
    public ModelAndView addProveedor(@Valid Proveedor proveedor){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/proveedor");
        String messageError = null;
        String message = null;

        Proveedor proveedorCreated = proveedorService.findbyDni(proveedor.getNumDocumento());

        if(proveedorCreated == null){
            proveedorService.saveProveedor(proveedor);
            message = "El Proveedor fue creado de forma correcta";
        }else if(proveedor.getId() != 0){
            proveedorService.saveProveedor(proveedor);
            message = "El Proveedor fue modificado de forma correcta";
        }

        else{
            messageError = "El proveedor ya existe.";
        }
        modelAndView.addObject("error", messageError);
        modelAndView.addObject("message", message);
        return modelAndView;
    }

    @GetMapping("/proveedor/{id}/remove")
    public ModelAndView removeUser(@PathVariable("id") String id){
        String message = null;
        try{
            Proveedor proveedor = proveedorService.findbyDni(id);
            proveedorService.remove(proveedor);
            message = "Proveedor Eliminado con Exito";
        }catch (Exception e){
            message = "Ocurrio un error, vuelva a intentar";
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/proveedorList");
        modelAndView.addObject("proveedor",proveedorService.findAll());
        modelAndView.addObject("message", message);

        return modelAndView;
    }

    @GetMapping("/proveedor/{id}/edit")
    public ModelAndView editUser(@PathVariable("id") String id){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/proveedor");

        Proveedor proveedor = proveedorService.findbyDni(id);
        modelAndView.addObject("proveedor", proveedor);
        return modelAndView;
    }
}
