package com.example.model;


public class DetalleVentaPedido {
    private String nameArticle;
    private float precioArticle;
    private int cantidad;

    public String getNameArticle() {
        return nameArticle;
    }

    public void setNameArticle(String nameArticle) {
        this.nameArticle = nameArticle;
    }

    public float getPrecioArticle() {
        return precioArticle;
    }

    public void setPrecioArticle(float precioArticle) {
        this.precioArticle = precioArticle;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
