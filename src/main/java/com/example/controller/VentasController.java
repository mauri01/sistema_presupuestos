package com.example.controller;

import com.example.model.*;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;
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

    private int registrarCompra(String idArt, String precio, String cantidad, int idPedido, String fecha, String clienteID, String unidad) {
        Venta venta = new Venta();
        venta.setArticle(Integer.parseInt(idArt));
        venta.setCantidad(Integer.parseInt(cantidad));
        venta.setUnidad(unidad);
        venta.setPrecio(Float.parseFloat(precio));
        User user = getUserAuth();
        venta.setUserId((long) user.getId());
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

    @RequestMapping(value="/admin/presupuestos", method = RequestMethod.GET)
    public ModelAndView listaPresupuestos(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/presupuestos");
        User user = getUserAuth();

        Map<Integer, List<Venta>> presupuestosByPedido = ventaService.findAll().stream()
                .filter(venta -> venta.getUserId() == user.getId())
                .collect(Collectors.groupingBy(Venta::getPedido));

        List<DetallePresupuesto> detallePresupuestos = new ArrayList<>();
        for (Map.Entry<Integer, List<Venta>> entry : presupuestosByPedido.entrySet()) {
            Integer idVenta = entry.getKey();
            List<Venta> ventas = entry.getValue();
            float sumaTotal = 0;
            for (Venta venta : ventas){
                int cantidad = venta.getCantidad();
                float precio = venta.getPrecio();
                sumaTotal = sumaTotal + (cantidad * precio);
            }
            
            Optional<Venta> datosVenta = ventas.stream().findFirst();
            if (datosVenta.isPresent()) {
                Long descuento = datosVenta.get().getDescuento();
                if (descuento != null) {
                    sumaTotal = sumaTotal - descuento;
                }
                Long iva = datosVenta.get().getIva();
                if (iva != null) {
                    sumaTotal = sumaTotal * (iva / 100);
                }
                int clienteID = datosVenta.get().getCliente();
                String nombreCliente = "-";
                String telefono = "-";
                if (clienteID > 0) {
                    Cliente datosCliente = clienteService.findbyId(clienteID);
                    nombreCliente = datosCliente.getNombre();
                    telefono = datosCliente.getTel();
                }

                String fecha = datosVenta.get().getFechaVenta();
                DetallePresupuesto detalle = DetallePresupuesto.builder()
                        .iva(iva != null ? iva : 0)
                        .descuento(descuento != null ? descuento : 0)
                        .nombreCliente(nombreCliente)
                        .total(sumaTotal)
                        .tel(telefono)
                        .fechaVenta(fecha)
                        .build();
                detallePresupuestos.add(detalle);
            }
        }


        modelAndView.addObject("presupuestos", detallePresupuestos);
        return modelAndView;
    }
}
