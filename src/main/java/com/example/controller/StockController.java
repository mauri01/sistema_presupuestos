package com.example.controller;

import com.example.model.Negocio;
import com.example.service.NegocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class StockController {

    @Autowired
    private NegocioService negocioService;

    @RequestMapping(value="/admin/stock", method = RequestMethod.GET)
    public ModelAndView stock(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stock");
        return modelAndView;
    }

    @RequestMapping(value="/admin/stock-list", method = RequestMethod.GET)
    public ModelAndView stockList(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stockList");

        Optional<Negocio> negocio = negocioService.findAll().stream().findFirst();
        String moneda = "$";
        if(negocio.isPresent() && negocio.get().getSimboloMoneda() != null){
            moneda = negocio.get().getSimboloMoneda();
        }

        modelAndView.addObject("moneda",moneda);
        return modelAndView;
    }
}
