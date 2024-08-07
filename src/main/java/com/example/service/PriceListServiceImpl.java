package com.example.service;

import com.example.model.Price;
import com.example.model.PriceList;
import com.example.repository.PriceListRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service("priceListService")
public class PriceListServiceImpl implements PriceListService {

    @Autowired
    private PriceListRepository priceListRepository;

    @Override
    public PriceList findByName(String nameFile) {
        return priceListRepository.findByNameFile(nameFile);
    }

    @Override
    public void save(PriceList priceList) {
        priceListRepository.save(priceList);
    }

    @Override
    public PriceList findByUserId(Long id) {
        return priceListRepository.findByUserId(id);
    }

    @Override
    public List<Price> findExcelPrices(String name) throws IOException {
        PriceList fileStorage = findByName(name);

        ByteArrayInputStream bis = new ByteArrayInputStream(fileStorage.getData());
        Workbook workbook = new XSSFWorkbook(bis);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        List<Price> priceList = new ArrayList<>();

        // Skip the first row (header)
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getPhysicalNumberOfCells() > 1) {
                double id = row.getCell(0).getNumericCellValue();
                String nombre = row.getCell(1).getStringCellValue();
                double precio = row.getCell(2).getNumericCellValue();
                String unidad = row.getCell(3).getStringCellValue();
                Price price = new Price();
                price.setId((long) id);
                price.setNombre(nombre);
                price.setPrecioVenta((float) precio);
                price.setUnidad(unidad);
                priceList.add(price);
            }
        }

        workbook.close();
        return priceList;
    }

}
