package com.example.controller;

import com.example.model.Article;
import com.example.model.Venta;
import com.example.model.VentasMesModel;
import com.example.model.DetalleVentaPedido;
import com.example.service.ArticleService;
import com.example.service.ProveedorService;
import com.example.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class VentasController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProveedorService proveedorService;

    Date date = new Date();

    @RequestMapping("/ventas")
    public ModelAndView process(@RequestParam String sourceText) throws ParseException {
        int idPedido = 0;
        String fecha = "";
        String messageVenta = "";
        int countVentas = 0;
        String stockDisponible = "";
        float totalVenta = 0;


        ModelAndView modelAndView = new ModelAndView();

        if(!"".equals(sourceText)){
            String[] valores = sourceText.split("]");

            //--- Recorrer cada venta individual
            for (String valor : valores) {
                String[] datosIndividual = valor.split(",");
                String idArt = datosIndividual[0];
                String precio = datosIndividual[1];
                String cantidad = datosIndividual[2];
                try {
                    idPedido = registrarCompra(idArt, precio, cantidad, idPedido, fecha);
                    articleService.descontarStock(idArt, cantidad);
                    totalVenta = totalVenta + (Float.parseFloat(precio)*Integer.parseInt(cantidad));
                } catch (Exception e) {
                    messageVenta = "Surgio un error al Cargar la venta Intente de Nuevo";
                    modelAndView.addObject("messageVenta", messageVenta);
                    return modelAndView;
                }
            }
            //--- Fin de Registo Articulos
            messageVenta = "La venta se registro con Exito";
        }else{
            messageVenta = "No es posible realizar la venta.";
        }


        DateFormat hourdateFormat = new SimpleDateFormat("MMMM");
        String fechaMes = hourdateFormat.format(date);
        countVentas = getCountVentasFecha(countVentas);
        stockDisponible = getStockDisponible();

        List<Article> allArticles = articleService.findAllArticle();
        modelAndView.addObject("provTotal",proveedorService.findAll());
        modelAndView.addObject("fechaMes",fechaMes);
        modelAndView.addObject("articles", allArticles);
        modelAndView.setViewName("admin/index");


        modelAndView.addObject("messageVenta", messageVenta);
        modelAndView.addObject("countVentas", countVentas);
        modelAndView.addObject("stockDisponible", stockDisponible);
        modelAndView.addObject("totalVentaAmostrar",totalVenta);
        modelAndView.addObject("idPedido",idPedido);
        return modelAndView;
    }

    private int getCountVentasFecha(int countVentas) throws ParseException {
        DateFormat hourdateFormat = new SimpleDateFormat("MM");
        String fechaMes = hourdateFormat.format(date);
        DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");
        String fechaAnio = hourdateFormatAnio.format(date);

        int idPedido = 0;
        List<Venta> listVentas = ventaService.findAll();

        for (Venta venta : listVentas){
            if(venta.getPedido() != idPedido){
                Date dateVenta = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
                String fechaMesVenta = hourdateFormat.format(dateVenta);
                String fechaAnioVenta = hourdateFormatAnio.format(dateVenta);

                if(fechaMes.equals(fechaMesVenta) && fechaAnio.equals(fechaAnioVenta)){
                    countVentas++;
                }
                idPedido = venta.getPedido();
            }
        }
        return countVentas;
    }

    public String getStockDisponible() {
        String stockMessage = "-";
        List<Article> stockDisponible = articleService.findAllArticle().stream()
                .filter(article -> article.getStock() > 0).collect(Collectors.toList());
        if(stockDisponible.size() != 0){
            stockMessage = "+";
        }
        return stockMessage;
    }

    private int registrarCompra(String idArt, String precio, String cantidad, int idPedido, String fecha) {
        Venta venta = new Venta();
        venta.setArticle(Integer.parseInt(idArt));
        venta.setCantidad(Integer.parseInt(cantidad));
        venta.setPrecio(Float.parseFloat(precio));

        if(("").equals(fecha)){
            DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            fecha = hourdateFormat.format(date);
        }

        venta.setFechaVenta(fecha);

        if (idPedido != 0) {
            venta.setPedido(idPedido);
        } else {
            ventaService.saveVenta(venta);
            idPedido = venta.getId();
            venta.setPedido(idPedido);
        }

        ventaService.saveVenta(venta);
        return venta.getPedido();
    }

    @RequestMapping(value="/admin/ventas-list", method = RequestMethod.GET)
    public ModelAndView stockList() throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/ventaList");

        DateFormat hourdateFormat = new SimpleDateFormat("MM");
        String fechaMes = hourdateFormat.format(date);
        DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");
        String fechaAnio = hourdateFormatAnio.format(date);

        List<Venta> listVentas = ventaService.findAll();
        List<VentasMesModel> ventasTotales = new ArrayList<>();

        for (Venta venta : listVentas){
            Date dateVenta = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
            String fechaMesVenta = hourdateFormat.format(dateVenta);
            String fechaAnioVenta = hourdateFormatAnio.format(dateVenta);
            if(fechaMes.equals(fechaMesVenta) && fechaAnio.equals(fechaAnioVenta)){
                int pedido = venta.getPedido();
                Optional<VentasMesModel> existePedido = ventasTotales.stream()
                        .filter(ventasMesModel -> ventasMesModel.getIdPedido() == pedido).findAny();

                if(!existePedido.isPresent()){
                    List<Venta> ventasMes = listVentas.stream()
                                                .filter(ven -> (pedido == ven.getPedido())).collect(Collectors.toList());

                    obtenerVentaPorPedido(ventasTotales, pedido, ventasMes);
                }

            }
        }
        modelAndView.addObject("listaVentasTotales", ventasTotales);
        return modelAndView;
    }

    private void obtenerVentaPorPedido(List<VentasMesModel> ventasTotales, int pedido, List<Venta> ventasMes) {
        float cantidadPrecioTotal = 0;
        String fechaVenta = "";
        int cantidadArticulos = 0;
        for (Venta ventaTotal : ventasMes){
            float cantidadPrecio = ventaTotal.getCantidad() * ventaTotal.getPrecio();
            cantidadPrecioTotal = cantidadPrecioTotal+cantidadPrecio;
            fechaVenta = ventaTotal.getFechaVenta();
            cantidadArticulos = cantidadArticulos+ventaTotal.getCantidad();
        }
        VentasMesModel ventasMesModel = new VentasMesModel();
        ventasMesModel.setVentaTotalPrecio(cantidadPrecioTotal);
        ventasMesModel.setIdPedido(pedido);
        ventasMesModel.setFechaVenta(fechaVenta);
        ventasMesModel.setCantidadArticulos(cantidadArticulos);
        ventasTotales.add(ventasMesModel);
    }

    @RequestMapping(value="/venta/pedido/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<DetalleVentaPedido> getArticlesByPedido(@PathVariable("id") String id){
        List<Venta> ventaPedido = ventaService.findAll().stream()
                .filter(venta -> venta.getPedido() == Integer.valueOf(id))
                .collect(Collectors.toList());

        List<DetalleVentaPedido> ventaPedidos = new ArrayList<>();

        for(Venta venta : ventaPedido){
            DetalleVentaPedido pedidoDetalle = new DetalleVentaPedido();
            Article article = articleService.findbyId(venta.getArticle());
            float precioArticle = venta.getPrecio();
            int cantidad = venta.getCantidad();
            pedidoDetalle.setCantidad(cantidad);
            pedidoDetalle.setNameArticle(article.getNombre());
            pedidoDetalle.setPrecioArticle(precioArticle);
            ventaPedidos.add(pedidoDetalle);
        }
        return ventaPedidos;
    }
}
