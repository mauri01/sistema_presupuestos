package com.example.model;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePresupuesto {
    private float total;
    private Long iva;
    private Long descuento;
    private String nombreCliente;
    private String tel;
    private String fechaVenta;
}
