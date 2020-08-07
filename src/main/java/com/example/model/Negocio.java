package com.example.model;

import javax.persistence.*;

@Entity
@Table(name = "Negocio")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "negocio_id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "domicilio")
    private String domicilio;

    @Column(name = "cuit")
    private String cuit;

    @Column(name = "tel")
    private String tel;

    @Column(name = "web")
    private String web;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }
}
