package com.example.controller;

import com.example.model.*;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
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
    private UserService userService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private NegocioService negocioService;

    @Autowired
    private PriceListService priceListService;

    @RequestMapping("/ventas")
    public ModelAndView process(@RequestParam String sourceText, @RequestParam String clienteID) throws ParseException, IOException {
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
                String unidad = datosIndividual[4];
                try {
                    idPedido = registrarCompra(idArt, precio, cantidad, idPedido, fecha, clienteID, unidad);
                    //articleService.descontarStock(idArt, cantidad);
                    totalVenta = totalVenta + (Float.parseFloat(precio)*Integer.parseInt(cantidad));
                } catch (Exception e) {
                    messageVenta = "Surgio un error al Cargar los datos Intente de Nuevo";
                    modelAndView.addObject("messageVenta", messageVenta);
                    return modelAndView;
                }
            }
            //--- Fin de Registo Articulos
            messageVenta = "El presupuesto se registro con Exito";
        }else{
            messageVenta = "No es posible realizar el presupuesto.";
        }


        DateFormat hourdateFormat = new SimpleDateFormat("MMMM");
        String fechaMes = hourdateFormat.format(new Date());
        //countVentas = getCountVentasFecha(countVentas);
        //stockDisponible = getStockDisponible();
        List<Cliente> clientes = clienteService.findAll();
        modelAndView.addObject("fechaMes",fechaMes);
        modelAndView.addObject("clientes", clientes.stream()
                .filter(Cliente::isActive)
                .collect(Collectors.toList()));
        modelAndView.setViewName("admin/index");

        User user = getUserAuth();
        List<Price> priceList = priceListService.findExcelPrices((long) user.getId());
        modelAndView.addObject("messageVenta", messageVenta);
        modelAndView.addObject("countVentas", countVentas);
        modelAndView.addObject("stockDisponible", stockDisponible);
        modelAndView.addObject("totalVentaAmostrar",totalVenta);
        modelAndView.addObject("idPedido",idPedido);
        Optional<Negocio> negocio = negocioService.findAll().stream().findFirst();
        modelAndView.addObject("priceList", priceList);
        negocio.ifPresent(negocio1 -> modelAndView.addObject("negocio", negocio1));
        return modelAndView;
    }

    private int getCountVentasFecha(int countVentas) throws ParseException {
        DateFormat hourdateFormat = new SimpleDateFormat("MM");
        String fechaMes = hourdateFormat.format(new Date());
        DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");
        String fechaAnio = hourdateFormatAnio.format(new Date());

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

    private int registrarCompra(String idArt, String precio, String cantidad, int idPedido, String fecha, String clienteID, String unidad) {
        Venta venta = new Venta();
        venta.setArticle(Integer.parseInt(idArt));
        venta.setCantidad(Integer.parseInt(cantidad));
        venta.setUnidad(unidad);
        venta.setPrecio(Float.parseFloat(precio));
        if(!clienteID.equals("")){
            venta.setCliente(Integer.parseInt(clienteID));
        }

        if(("").equals(fecha)){
            DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            fecha = hourdateFormat.format(new Date());
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
        String fechaMes = hourdateFormat.format(new Date());
        DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");
        String fechaAnio = hourdateFormatAnio.format(new Date());

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

        Optional<Negocio> negocio = negocioService.findAll().stream().findFirst();
        String moneda = "$";
        if(negocio.isPresent() && negocio.get().getSimboloMoneda() != null){
            moneda = negocio.get().getSimboloMoneda();
        }

        modelAndView.addObject("moneda",moneda);
        modelAndView.addObject("listaVentasTotales", ventasTotales);
        return modelAndView;
    }

    private void obtenerVentaPorPedido(List<VentasMesModel> ventasTotales, int pedido, List<Venta> ventasMes) {
        float cantidadPrecioTotal = 0;
        String fechaVenta = "";
        int cantidadArticulos = 0;
        String nombreCliente = "";
        for (Venta ventaTotal : ventasMes){
            float cantidadPrecio = ventaTotal.getCantidad() * ventaTotal.getPrecio();
            cantidadPrecioTotal = cantidadPrecioTotal+cantidadPrecio;
            fechaVenta = ventaTotal.getFechaVenta();
            cantidadArticulos = cantidadArticulos+ventaTotal.getCantidad();
            Cliente cliente = clienteService.findbyId(ventaTotal.getCliente());
            nombreCliente = cliente == null ? "Sin registro" : cliente.getNombre()+"("+cliente.getNumDocumento()+")";
        }
        VentasMesModel ventasMesModel = new VentasMesModel();
        ventasMesModel.setVentaTotalPrecio(cantidadPrecioTotal);
        ventasMesModel.setIdPedido(pedido);
        ventasMesModel.setFechaVenta(fechaVenta);
        ventasMesModel.setCantidadArticulos(cantidadArticulos);
        ventasMesModel.setCliente(nombreCliente);
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
            float precioArticle = venta.getPrecio();
            int cantidad = venta.getCantidad();
            pedidoDetalle.setCantidad(cantidad);
            pedidoDetalle.setPrecioArticle(precioArticle);

            Cliente cliente = clienteService.findbyId(venta.getCliente());
            if(cliente != null){
                pedidoDetalle.setCliente(cliente.getNombre());
            }else{
                pedidoDetalle.setCliente("Sin registro");
            }

            ventaPedidos.add(pedidoDetalle);
        }
        return ventaPedidos;
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
