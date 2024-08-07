package com.example.controller;

import com.example.model.Venta;
import com.example.service.NegocioService;
import com.example.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReportesController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private NegocioService negocioService;

    public static int MES = 0;
    public static int ANIO = 1;
    public static int CANTIDAD = 2;
    public static int SINCANTIDAD = 3;

    DateFormat hourdateFormat = new SimpleDateFormat("MM");
    DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");

    @RequestMapping(value = "/report/ventas/{mes}/{ano}", method = RequestMethod.GET)
    @ResponseBody
    public float reportVentas(@PathVariable("mes") int mes, @PathVariable("ano") int anio) {
        List<Venta> listaVentas = ventaService.findAll();
        float total = 0;
        try {
            for (Venta venta : listaVentas) {
                Date hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
                int mesCompra = hourdateFormat.getMonth() + 1 ;
                int anioCompra = Integer.parseInt(hourdateFormatAnio.format(hourdateFormat));
                if (mes == -99) {
                    if (anioCompra == anio) {
                        total = total + (venta.getCantidad() * venta.getPrecio());
                    }
                } else {
                    if (mes == mesCompra && anioCompra == anio) {
                        total = total + (venta.getCantidad() * venta.getPrecio());
                    }
                }

            }
        } catch (Exception e) {
            return 0;
        }

        return total;

    }

    public int getArticuloMasVendido(String fechaMes, String fechaAnio, int calculo, int cantidad) throws ParseException {
        List<Venta> listaVentas = ventaService.findAll();
        List<Venta> listaVentasMes = new ArrayList<>();

        int mayorArticulo = 0;
        int mayorArticuloId = 0;

        if (calculo == MES) {
            for (Venta venta : listaVentas) {
                Date hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
                int mesCompra = hourdateFormat.getMonth() + 1;
                int anioCompra = Integer.parseInt(hourdateFormatAnio.format(hourdateFormat));
                if (Integer.parseInt(fechaMes) == mesCompra && anioCompra == Integer.parseInt(fechaAnio)) {
                    listaVentasMes.add(venta);
                }
            }
        } else if (calculo == ANIO) {
            for (Venta venta : listaVentas) {
                Date hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
                int anioCompra = Integer.parseInt(hourdateFormatAnio.format(hourdateFormat));
                if (anioCompra == Integer.parseInt(fechaAnio)) {
                    listaVentasMes.add(venta);
                }
            }
        }


        for (Venta ventaMes : listaVentasMes) {

            List<Venta> articulos = listaVentas.stream().filter(venta -> venta.getArticle() == ventaMes.getArticle())
                    .collect(Collectors.toList());

            if (articulos.size() > mayorArticulo) {
                mayorArticulo = articulos.size();
                mayorArticuloId = articulos.get(0).getArticle();
            }
        }

        if (cantidad == CANTIDAD) {
            return mayorArticulo;
        } else {
            return mayorArticuloId;
        }

    }
}
