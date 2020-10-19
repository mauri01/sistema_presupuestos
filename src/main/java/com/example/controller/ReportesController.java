package com.example.controller;

import com.example.model.Article;
import com.example.model.Compra;
import com.example.model.Venta;
import com.example.service.ArticleService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ReportesController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ArticleService articleService;

    public static int MES = 0;
    public static int ANIO = 1;
    public static int CANTIDAD = 2;
    public static int SINCANTIDAD = 3;

    DateFormat hourdateFormat = new SimpleDateFormat("MM");
    DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");

    @RequestMapping(value = "/admin/reportes", method = RequestMethod.GET)
    public ModelAndView reportes() throws ParseException {
        String fechaMes = hourdateFormat.format(new Date());
        String fechaAnio = hourdateFormatAnio.format(new Date());

        float gastosTotales = reportCompras(Integer.parseInt(fechaMes) , Integer.parseInt(fechaAnio));
        float gananciasTotales = reportVentas(Integer.parseInt(fechaMes) , Integer.parseInt(fechaAnio));
        float balance = gananciasTotales - gastosTotales;

        //GANANCIAS TOTALES ANIO
        float gastosTotalesAnio = reportCompras(-99, Integer.parseInt(fechaAnio));
        float gananciasTotalesAnio = reportVentas(-99, Integer.parseInt(fechaAnio));
        float balanceAnio = gananciasTotalesAnio - gastosTotalesAnio;
        //-----

        int articuloMasVendidoDelMes = getArticuloMasVendido(fechaMes, fechaAnio, MES, SINCANTIDAD);
        int cantMasVendidoDelMes = getArticuloMasVendido(fechaMes, fechaAnio, MES, CANTIDAD);
        Article articuloMes = articleService.findbyId(articuloMasVendidoDelMes);
        String articuloNombre = articuloMes == null ? "Sin Ventas" : articuloMes.getNombre();

        int articuloMasVendidoDelAnio = getArticuloMasVendido(fechaMes, fechaAnio, ANIO, SINCANTIDAD);
        int cantMasVendidoDelAnio = getArticuloMasVendido(fechaMes, fechaAnio, ANIO, CANTIDAD);
        Article articuloAnio = articleService.findbyId(articuloMasVendidoDelAnio);
        String articuloAnioNombre = articuloAnio == null ? "Sin Ventas" : articuloAnio.getNombre();


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("gastosTotales", gastosTotales);
        modelAndView.addObject("gananciasTotales", gananciasTotales);
        modelAndView.addObject("balance", balance);
        modelAndView.addObject("articuloNombre", articuloNombre);
        modelAndView.addObject("cantArticuloNombre", cantMasVendidoDelMes);
        modelAndView.addObject("articuloAnioNombre", articuloAnioNombre);
        modelAndView.addObject("cantAnioNombre", cantMasVendidoDelAnio);
        modelAndView.addObject("gananciasTotalesAnio",gananciasTotalesAnio);
        modelAndView.addObject("balanceAnio",balanceAnio);
        modelAndView.addObject("gastosTotalesAnio",gastosTotalesAnio);
        modelAndView.setViewName("admin/report");
        return modelAndView;
    }

    @RequestMapping(value = "/report/compras/{mes}/{ano}", method = RequestMethod.GET)
    @ResponseBody
    public float reportCompras(@PathVariable("mes") int mes, @PathVariable("ano") int anio) {
        List<Compra> listaCompras = compraService.findAll();
        float total = 0;
        try {
            for (Compra compra : listaCompras) {
                Date hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(compra.getFechaIngreso());
                int mesCompra = hourdateFormat.getMonth() + 1;
                int anioCompra = Integer.parseInt(hourdateFormatAnio.format(hourdateFormat));
                if (mes == -99) {
                    if (anioCompra == anio) {
                        total = total + compra.getPrecioTotal();
                    }
                } else {
                    if (mes == mesCompra && anioCompra == anio) {
                        total = total + compra.getPrecioTotal();
                    }
                }
            }
        } catch (Exception e) {
            return 0;
        }

        return total;

    }

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
