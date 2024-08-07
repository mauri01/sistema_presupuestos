package com.example.repository;

import com.example.model.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("priceListRepository")
public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    PriceList findByNameFile(String nameFile);
    PriceList findByUserId(Long userId);
}
