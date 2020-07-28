package com.example.model;

import javax.persistence.*;

@Entity
@Table(name = "Compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "compra_id")
    private int id;

    @Column(name = "article_id")
    private int articleId;

    @Column(name = "cantidad")
    private int cantidad;

    @Column(name = "precio")
    private float precio;

    @Column(name = "precio_venta", nullable = false)
    private float precioVenta;

    @Column(name = "precio_total")
    private float precioTotal;

    @Column(name = "comprobante")
    private String comprobante;

    @Column(name = "numero_comprobante")
    private int numeroComprobante;

    @Column(name = "tipo_ingreso")
    private String tipoIngreso;

    @Column(name = "proveedor_id")
    private int provedor;

    @Column(name = "fecha_ingreso")
    private String fechaIngreso;

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public float getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(float precioTotal) {
        this.precioTotal = precioTotal;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public int getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(int numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getTipoIngreso() {
        return tipoIngreso;
    }

    public void setTipoIngreso(String tipoIngreso) {
        this.tipoIngreso = tipoIngreso;
    }

    public int getProvedor() {
        return provedor;
    }

    public void setProvedor(int provedor) {
        this.provedor = provedor;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(float precioVenta) {
        this.precioVenta = precioVenta;
    }
}
