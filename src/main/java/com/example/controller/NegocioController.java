package com.example.controller;

import com.example.model.Negocio;
import com.example.service.NegocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Optional;

@Controller
public class NegocioController {

    @Autowired
    private NegocioService negocioService;

    @RequestMapping(value="/admin/minegocio", method = RequestMethod.GET)
    public ModelAndView negocio(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/negocio");


        Optional<Negocio> negocioCreated = negocioService.findAll().stream().findFirst();
        if(negocioCreated.isPresent()){
            Negocio negocio = negocioCreated.get();
            modelAndView.addObject("negocio",negocio);
        }else{
            Negocio negocio = new Negocio();
            modelAndView.addObject("negocio",negocio);
        }



        return modelAndView;
    }

    @RequestMapping(value="/minegocio", method = RequestMethod.POST)
    public ModelAndView addNegocio(@Valid Negocio negocio){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/negocio");
        String messageError = null;
        String message = null;
        
        try{
            negocioService.saveNegocio(negocio);
            message = "El Negocio fue creado de forma correcta";

        }catch (Exception e){
            messageError = "Ocurrio un error al crear el Negocio";
        }


        modelAndView.addObject("error", messageError);
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
