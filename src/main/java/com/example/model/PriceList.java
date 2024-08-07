package com.example.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "PriceList")
@Data
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_list_id")
    private int id;

    @Column(name = "user_id")
    private Long userId;

    @Lob
    @Column(name = "data")
    private byte[] data;

    @Column(name = "name_file")
    private String nameFile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
