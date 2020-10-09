package com.example.controller;

import com.example.model.Article;
import com.example.model.Compra;
import com.example.service.ArticleService;
import com.example.service.CompraService;
import com.example.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class StockController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private CompraService compraService;

    Date date = new Date();

    @RequestMapping(value="/admin/stock", method = RequestMethod.GET)
    public ModelAndView stock(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stock");

        modelAndView.addObject("articles",articleService.findAllArticleActive());
        modelAndView.addObject("proveedores",proveedorService.findAll());
        modelAndView.addObject("compra",new Compra());
        return modelAndView;
    }

    @RequestMapping(value="/admin/stock-list", method = RequestMethod.GET)
    public ModelAndView stockList(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stockList");

        modelAndView.addObject("articles",articleService.findAllArticleActive());
        return modelAndView;
    }

    @RequestMapping(value="/admin/deleteArticle/{id}", method = RequestMethod.GET)
    public ModelAndView deleteArticle(@PathVariable("id") int id){

        String messageCarga = "";
        try{
            Article article = articleService.findbyId(id);
            article.setActive(false);
            articleService.saveArticle(article);
            messageCarga = "Articulo eliminado de forma correcta.";
        }catch (Exception e){
            messageCarga = "Ocurrio un error, vuelva a intentar.";
        }


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stockList");
        modelAndView.addObject("messageCarga",messageCarga);
        modelAndView.addObject("articles",articleService.findAllArticleActive());
        return modelAndView;
    }

    @RequestMapping(value="/admin/cargarstock/{id}", method = RequestMethod.GET)
    public ModelAndView cargarstocks(@PathVariable("id") int id){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stock");

        modelAndView.addObject("articles",articleService.findAllArticle());
        modelAndView.addObject("proveedores",proveedorService.findAll());
        modelAndView.addObject("compra",new Compra());
        modelAndView.addObject("idSelected",id);
        return modelAndView;
    }

    @RequestMapping(value="/stock", method = RequestMethod.POST)
    public ModelAndView addStock(@Valid Compra compra){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/stock");
        DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String fecha = hourdateFormat.format(date);

        try{
            float total = compra.getCantidad() * compra.getPrecio();
            if (total > 0){
                compra.setPrecioTotal(total);
                compra.setFechaIngreso(fecha);
                Compra compraCreated = compraService.saveCompra(compra);

                Article article = articleService.findbyId(compraCreated.getArticleId());
                article.setStock(article.getStock() + compraCreated.getCantidad());
                article.setPrecioVenta(compra.getPrecioVenta());
                articleService.saveArticle(article);
                modelAndView.addObject("messageResult","Se registro de forma Correcta");
            }else{
                modelAndView.addObject("messageResult","Es necesario que el precio o cantidad sean mayor a cero");
            }

        }catch (Exception e){
            modelAndView.addObject("messageResult","Ocurrio un error al registrar");
        }

        modelAndView.addObject("articles",articleService.findAllArticle());
        modelAndView.addObject("proveedores",proveedorService.findAll());
        modelAndView.addObject("compra",new Compra());
        return modelAndView;
    }
}
