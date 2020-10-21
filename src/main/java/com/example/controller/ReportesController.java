package com.example.controller;

import com.example.model.Compra;
import com.example.model.Venta;
import com.example.service.CompraService;
import com.example.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ReportesController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private VentaService ventaService;

    DateFormat hourdateFormat = new SimpleDateFormat("MM");
    DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");

    @RequestMapping(value="/admin/reportes", method = RequestMethod.GET)
    public ModelAndView reportes() {
        String fechaMes = hourdateFormat.format(new Date());
        String fechaAnio = hourdateFormatAnio.format(new Date());

        float gastosTotales = reportCompras(Integer.parseInt(fechaMes) - 1, Integer.parseInt(fechaAnio));
        float gananciasTotales = reportVentas(Integer.parseInt(fechaMes) - 1, Integer.parseInt(fechaAnio));
        float balance = gananciasTotales - gastosTotales;

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("gastosTotales", gastosTotales);
        modelAndView.addObject("gananciasTotales", gananciasTotales);
        modelAndView.addObject("balance", balance);
        modelAndView.setViewName("admin/report");
        return modelAndView;
    }

    @RequestMapping(value="/report/compras/{mes}/{ano}", method = RequestMethod.GET)
    @ResponseBody
    public float reportCompras(@PathVariable("mes") int mes, @PathVariable("ano") int anio){
        List<Compra> listaCompras = compraService.findAll();
        float total = 0;
        try{
            for (Compra compra : listaCompras){
                Date hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(compra.getFechaIngreso());
                int mesCompra = hourdateFormat.getMonth();
                int anioCompra = Integer.parseInt(hourdateFormatAnio.format(hourdateFormat));
                if(mes == mesCompra && anioCompra == anio){
                    total = total + compra.getPrecioTotal();
                }
            }
        }catch (Exception e){
            return 0;
        }

        return total;
        
    }

    @RequestMapping(value="/report/ventas/{mes}/{ano}", method = RequestMethod.GET)
    @ResponseBody
    public float reportVentas(@PathVariable("mes") int mes, @PathVariable("ano") int anio){
        List<Venta> listaVentas = ventaService.findAll();
        float total = 0;
        try{
            for (Venta venta : listaVentas){
                Date hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
                int mesCompra = hourdateFormat.getMonth();
                int anioCompra = Integer.parseInt(hourdateFormatAnio.format(hourdateFormat));
                if(mes == mesCompra && anioCompra == anio){
                    total = total + (venta.getCantidad() * venta.getPrecio());
                }
            }
        }catch (Exception e){
            return 0;
        }

        return total;

    }
}
