package com.example.controller;

import com.example.model.*;
import com.example.service.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DocumentController {

    @Autowired
    private UserService userService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    NegocioService negocioService;

    @Autowired
    ClienteService clienteService;

    @Autowired
    private PriceListService priceListService;

    public static int EXCEL = 0;
    public static int PDF = 1;

    @PostMapping("/admin/document/ticket/imprimir")
    private ModelAndView imprimirTicket(@RequestParam Long formIdPedido, @RequestParam Long formIva, @RequestParam Long formdescuento, @RequestParam String formvalidation) throws IOException {
        List<DetalleVentaPedido> detalleVentaPedidos = new ArrayList<>();
        float totalVenta = 0;
        DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String fecha = hourdateFormat.format(new Date());

        List<Venta> ventas = ventaService.findAll()
                .stream()
                .filter(venta -> venta.getPedido() == formIdPedido)
                .collect(Collectors.toList());

        for (Venta venta : ventas) {
            DetalleVentaPedido detalleVenta = new DetalleVentaPedido();
            detalleVenta.setPrecioArticle(venta.getPrecio());
            List<Price> priceList = priceListService.findExcelPrices("1.xlsx");
            Price list = priceList.stream().filter(price -> price.getId() == venta.getArticle()).findFirst().orElse(null);
            detalleVenta.setNameArticle(list.getNombre());
            detalleVenta.setCantidad(venta.getCantidad());
            detalleVenta.setUnidad(venta.getUnidad());
            detalleVentaPedidos.add(detalleVenta);
            totalVenta = totalVenta + venta.getCantidad() * venta.getPrecio();
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("articles", detalleVentaPedidos);
        modelAndView.addObject("totalVenta", totalVenta);
        modelAndView.addObject("fecha", fecha);

        User user = getUserAuth();

        Optional<Negocio> negocio = negocioService.findAll().stream()
                .filter(negocio1 -> negocio1.getUserId() == user.getId()).findFirst();
        if (negocio.isPresent()) {
            if(negocio.get().getLogo() != null) {
                String base64Image = Base64.getEncoder().encodeToString(negocio.get().getLogo());
                modelAndView.addObject("image", base64Image);
                modelAndView.addObject("imageType", "image/jpeg");
            }
            modelAndView.addObject("negocio", negocio.get());
        } else {
            Negocio my_negocio = new Negocio();
            my_negocio.setDomicilio("***");
            my_negocio.setNombre("***");
            my_negocio.setTel("***");
            modelAndView.addObject("negocio", my_negocio);
        }

        Optional<Venta> ventaForClient = ventas.stream().findFirst();
        if (ventaForClient.isPresent()) {
            int cliente = ventaForClient.get().getCliente();
            if (cliente > 0) {
                Cliente clienteData = clienteService.findbyId(cliente);
                modelAndView.addObject("cliente", clienteData);
            } else {
                Cliente cliente1 = new Cliente();
                cliente1.setNombre("***");
                cliente1.setDomicilio("***");
                cliente1.setTel("***");
                modelAndView.addObject("cliente", cliente1);
            }
        }

        String moneda = "$";
        if (negocio.isPresent() && negocio.get().getSimboloMoneda() != null) {
            moneda = negocio.get().getSimboloMoneda();
        }
        modelAndView.addObject("moneda", moneda);
        modelAndView.addObject("valido_hasta", formvalidation);
        long descuento = formdescuento != null ? formdescuento : 0L;
        if (formIva != null){
            double precioIva = calculateIVA(totalVenta, formIva);
            modelAndView.addObject("iva", formIva);
            modelAndView.addObject("total_iva", precioIva - descuento);
            modelAndView.addObject("total_precio", precioIva - descuento);
            modelAndView.addObject("total_descuento", descuento);
        } else {
            modelAndView.addObject("iva", 0);
            modelAndView.addObject("total_iva", 0);
            modelAndView.addObject("total_descuento", descuento);
            modelAndView.addObject("total_precio", totalVenta - descuento);
        }

        modelAndView.setViewName("admin/imprimirTicket");
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

    public static double calculateIVA(double total, double percentage) {
        return total * (percentage / 100);
    }

}
