package com.example.service;

import com.example.model.Price;
import com.example.model.PriceList;
import com.example.model.Role;
import com.example.model.User;

import java.io.IOException;
import java.util.List;

public interface PriceListService {
    PriceList findByName(String name);
    PriceList findByUserId(Long id);
    void save(PriceList priceList);
    List<Price> findExcelPrices(String name) throws IOException;
}
