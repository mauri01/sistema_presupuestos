package com.example.model;

public class VentasMesModel {

    private float ventaTotalPrecio;
    private int cantidadArticulos;
    private String fechaVenta;
    private int idPedido;
    private String cliente;

    public float getVentaTotalPrecio() {
        return ventaTotalPrecio;
    }

    public void setVentaTotalPrecio(float ventaTotalPrecio) {
        this.ventaTotalPrecio = ventaTotalPrecio;
    }

    public int getCantidadArticulos() {
        return cantidadArticulos;
    }

    public void setCantidadArticulos(int cantidadArticulos) {
        this.cantidadArticulos = cantidadArticulos;
    }

    public String getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(String fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }
}
