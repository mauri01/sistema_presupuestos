package com.example.controller;

import com.example.model.*;
import com.example.service.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DocumentController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CompraService compraService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    NegocioService negocioService;

    @Autowired
    ClienteService clienteService;

    public static int EXCEL = 0;
    public static int PDF = 1;

    @RequestMapping(value = "/admin/document/ticket/imprimir/{id}", method = RequestMethod.GET)
        private ModelAndView createTicketImprimir(HttpServletResponse response, @PathVariable("id") int id) {
        List<DetalleVentaPedido> detalleVentaPedidos = new ArrayList<>();
        float totalVenta= 0;
        DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String fecha = hourdateFormat.format(new Date());

        List<Venta> ventas = ventaService.findAll()
                .stream()
                .filter(venta -> venta.getPedido() == id)
                .collect(Collectors.toList());

        for(Venta venta : ventas){
            DetalleVentaPedido detalleVenta = new DetalleVentaPedido();
            detalleVenta.setPrecioArticle(venta.getPrecio());
            detalleVenta.setNameArticle(articleService.findbyId(venta.getArticle()).getNombre());
            detalleVenta.setCantidad(venta.getCantidad());
            detalleVentaPedidos.add(detalleVenta);
            totalVenta = totalVenta + venta.getCantidad() * venta.getPrecio();
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("articles", detalleVentaPedidos);
        modelAndView.addObject("totalVenta", totalVenta);
        modelAndView.addObject("fecha", fecha);

        Optional<Negocio> negocio = negocioService.findAll().stream().findFirst();
        if (negocio.isPresent()){
            modelAndView.addObject("negocio", negocio.get());
        }else{
            modelAndView.addObject("negocio", new Negocio());
        }

        String moneda = "$";
        if(negocio.isPresent() && negocio.get().getSimboloMoneda() != null){
            moneda = negocio.get().getSimboloMoneda();
        }
        modelAndView.addObject("moneda", moneda);
        modelAndView.setViewName("admin/imprimirTicket");
        return modelAndView;

    }

    @RequestMapping(value = "/admin/document/descargar/{id}", method = RequestMethod.GET)
    private void createDocument(HttpServletResponse response, @PathVariable("id") int id) {

        try {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            response.addHeader("Content-Type", "application/force-download");

            List<Venta> ventas = ventaService.findAll();
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = new Date();
            String nombreArchivo = "Reporte_" + dateFormat.format(date);

            if (id == EXCEL) {

                response.addHeader("Content-Disposition", "attachment; filename=\""+nombreArchivo+".xls\"");
                output = createExcel(ventas);

            } else if (id == PDF){
                response.addHeader("Content-Disposition", "attachment; filename=\""+nombreArchivo+".pdf\"");
                output = createPDF(ventas);
            }

            response.getOutputStream().write(output.toByteArray());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ByteArrayOutputStream createExcel(List<Venta> ventas) throws IOException {
        // Se crea el libro
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HSSFWorkbook libro = new HSSFWorkbook();

        // Se crea una hoja dentro del libro
        HSSFSheet hoja = libro.createSheet();
        libro.setSheetName(0, "Ventas Realizadas");

        Font headerFont = libro.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle headerStyle = libro.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle gananciaStyle1 = libro.createCellStyle();
        gananciaStyle1.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        gananciaStyle1.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle gananciaStyle2 = libro.createCellStyle();
        gananciaStyle2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        gananciaStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);


        createHoja1(ventas, hoja, headerStyle, gananciaStyle1, gananciaStyle2);

        createHoja2(libro, headerStyle);

        createHoja3(libro, headerStyle, gananciaStyle1);

        try {
            libro.write(output);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private void createHoja3(HSSFWorkbook libro, CellStyle headerStyle, CellStyle gananciaStyle1) {
        HSSFSheet hoja3 = libro.createSheet();
        libro.setSheetName(2, "Stock Registrado");

        HSSFRow filaHeader3 = hoja3.createRow(2);
        filaHeader3.createCell(0).setCellValue("NOMBRE ARTICULO");
        filaHeader3.createCell(1).setCellValue("MEDIDA");
        filaHeader3.createCell(2).setCellValue("STOCK DISPONIBLE");

        for (int i = 0; i < 3; i++) {
            Cell cel = filaHeader3.getCell(i);
            cel.setCellStyle(headerStyle);
            hoja3.autoSizeColumn(i);
        }

        List<Article> articles = articleService.findAllArticle();

        for (int i = 0; i < articles.size(); i++) {
            HSSFRow filaHeaderTabla = hoja3.createRow(3 + i);

            filaHeaderTabla.createCell(0).setCellValue(articles.get(i).getNombre());
            filaHeaderTabla.createCell(1).setCellValue(articles.get(i).getMedida());
            filaHeaderTabla.createCell(2).setCellValue(articles.get(i).getStock());
            Cell cel1 = filaHeaderTabla.getCell(2);
            if (articles.get(i).getStock() <= 0) {
                cel1.setCellStyle(gananciaStyle1);
            }
        }
    }

    private void createHoja2(HSSFWorkbook libro, CellStyle headerStyle) {
        HSSFSheet hoja2 = libro.createSheet();
        libro.setSheetName(1, "Compras Realizadas");

        HSSFRow filaHeader2 = hoja2.createRow(2);
        filaHeader2.createCell(0).setCellValue("N° COMPROBANTE");
        filaHeader2.createCell(1).setCellValue("FECHA Y HORA COMPRA");
        filaHeader2.createCell(2).setCellValue("NOMBRE ARTICULO");
        filaHeader2.createCell(3).setCellValue("CANTIDAD");
        filaHeader2.createCell(4).setCellValue("PRECIO");
        filaHeader2.createCell(5).setCellValue("TOTAL");
        filaHeader2.createCell(6).setCellValue("TIPO DE INGRESO");
        filaHeader2.createCell(7).setCellValue("PROVEEDOR");

        for (int i = 0; i < 8; i++) {
            Cell cel = filaHeader2.getCell(i);
            cel.setCellStyle(headerStyle);
            hoja2.autoSizeColumn(i);
        }

        List<Compra> compras = compraService.findAll();

        for (int i = 0; i < compras.size(); i++) {
            HSSFRow filaHeaderTabla = hoja2.createRow(3 + i);

            int articleID = compras.get(i).getArticleId();
            Article articulo = articleService.findbyId(articleID);

            Proveedor proveedor = proveedorService.findbyId(compras.get(i).getProvedor());

            filaHeaderTabla.createCell(0).setCellValue(compras.get(i).getNumeroComprobante());
            filaHeaderTabla.createCell(1).setCellValue(compras.get(i).getFechaIngreso());
            filaHeaderTabla.createCell(2).setCellValue(articulo.getNombre());
            filaHeaderTabla.createCell(3).setCellValue(compras.get(i).getCantidad());
            filaHeaderTabla.createCell(4).setCellValue(compras.get(i).getPrecio());
            filaHeaderTabla.createCell(5).setCellValue(compras.get(i).getPrecioTotal());
            filaHeaderTabla.createCell(6).setCellValue(compras.get(i).getTipoIngreso());
            filaHeaderTabla.createCell(7).setCellValue(proveedor.getRazonSocial() + "(" + proveedor.getNumDocumento() + ")");
        }
    }

    private void createHoja1(List<Venta> ventas, HSSFSheet hoja, CellStyle headerStyle, CellStyle gananciaStyle1, CellStyle gananciaStyle2) {
        HSSFRow filaHeader = hoja.createRow(2);
        filaHeader.createCell(0).setCellValue("ID PEDIDO");
        filaHeader.createCell(1).setCellValue("FECHA Y HORA VENTA");
        filaHeader.createCell(2).setCellValue("NOMBRE ARTICULO");
        filaHeader.createCell(3).setCellValue("CANTIDAD");
        filaHeader.createCell(4).setCellValue("PRECIO VENTA");
        filaHeader.createCell(5).setCellValue("VENTA TOTAL");
        filaHeader.createCell(6).setCellValue("PRECIO COMPRA");
        filaHeader.createCell(7).setCellValue("GANANCIA");
        filaHeader.createCell(8).setCellValue("NOMBRE CLIENTE");

        for (int i = 0; i < 9; i++) {
            Cell cel = filaHeader.getCell(i);
            cel.setCellStyle(headerStyle);
            hoja.autoSizeColumn(i);
        }


        for (int i = 0; i < ventas.size(); i++) {
            HSSFRow filaHeaderTabla = hoja.createRow(3 + i);
            int articleID = ventas.get(i).getArticle();
            Article articulo = articleService.findbyId(articleID);
            Compra compraArt = compraService.findAll()
                    .stream()
                    .filter(compra -> compra.getArticleId() == articleID)
                    .findFirst().get();
            filaHeaderTabla.createCell(0).setCellValue(ventas.get(i).getPedido());
            filaHeaderTabla.createCell(1).setCellValue(ventas.get(i).getFechaVenta());
            filaHeaderTabla.createCell(2).setCellValue(articulo.getNombre());
            filaHeaderTabla.createCell(3).setCellValue(ventas.get(i).getCantidad());
            filaHeaderTabla.createCell(4).setCellValue(ventas.get(i).getPrecio());
            float ventaTotal = ventas.get(i).getPrecio() * ventas.get(i).getCantidad();
            filaHeaderTabla.createCell(5).setCellValue(ventaTotal);
            filaHeaderTabla.createCell(6).setCellValue(compraArt.getPrecio());
            float compraTotal = ventas.get(i).getCantidad() * compraArt.getPrecio();
            float ganancia = ventaTotal - compraTotal;
            filaHeaderTabla.createCell(7).setCellValue(ganancia);
            Cell cel1 = filaHeaderTabla.getCell(7);
            if (ganancia < 0) {
                cel1.setCellStyle(gananciaStyle1);
            } else {
                cel1.setCellStyle(gananciaStyle2);
            }

            Cliente cliente = clienteService.findbyId(ventas.get(i).getCliente());
            if(cliente != null){
                filaHeaderTabla.createCell(8).setCellValue(cliente.getNombre()+"("+cliente.getNumDocumento()+")");
            }else{
                filaHeaderTabla.createCell(8).setCellValue("Sin registro");
            }

        }
    }


    public ByteArrayOutputStream createPDF(List<Venta> ventas) throws IOException {

        PDDocument document;
        PDPage page;
        PDFont font;
        PDPageContentStream contentStream;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Creating Document
        document = new PDDocument();

        // Creating Pages
        List<Compra> compras = compraService.findAll();
        List<Article> articles = articleService.findAllArticle();

        for(int function = 0 ; function < 4; function++){
            int cant = 0;
            int inicial = 0;
            int indice = 32;

            if(function == 0){
                cant = ventas.size()+1;
            }else if(function == 1){
                cant = compras.size()+1;
            }else if(function == 2){
                cant = articles.size()+1;
            }
            while ( cant > 0 ) {
                if (cant < 32){
                    indice = cant;
                }
                if(function == 0) {
                    crearPaginaVentaRealizada(ventas, document, inicial, indice);
                }else if(function == 1){
                    crearComprasRealizada(compras, document, inicial, indice);
                }else if(function == 2){
                    crearStock(articles, document, inicial, indice);
                }
                cant = cant-32;
                inicial = inicial+indice;
            }
        }

        // Finally Let's save the PDF
        document.save(output);
        document.close();

        return output;
    }

    private void crearStock(List<Article> articles, PDDocument document, int inicial, int indice) throws IOException {

        PDPage page;
        PDFont font;
        PDPageContentStream contentStream;
        page = new PDPage();

        // Adding page to document
        document.addPage(page);

        // Adding FONT to document
        font = PDType1Font.HELVETICA;

        // Next we start a new content stream which will "hold" the to be created content.
        contentStream = new PDPageContentStream(document, page);

        // Let's define the content stream
        contentStream.beginText();
        contentStream.setFont(font, 14);
        contentStream.moveTextPositionByAmount(10, 760);
        contentStream.drawString("STOCK REGISTRADO");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(10, 740);
        contentStream.drawString("Aqui encontrara el stock registrado hasta la fecha.");
        contentStream.endText();

        String matriz[][] = new String[indice][3];
        int numInicial = 0;

        ArrayList<String> headerTitulos = new ArrayList<>();
        headerTitulos.add("NOMBRE ARTICULO");
        headerTitulos.add("MEDIDA");
        headerTitulos.add("STOCK DISPONIBLE");

        numInicial = crearHeaderPDF(inicial, matriz, numInicial, headerTitulos);

        for (int i = numInicial; i < indice; i++) {
            int indiceUtilizar = getIndiceUtilizar(numInicial, i);

            matriz[i][0] = articles.get(indiceUtilizar).getNombre();
            matriz[i][1] = articles.get(indiceUtilizar).getMedida();
            matriz[i][2] = String.valueOf(articles.get(indiceUtilizar).getStock());
        }

        drawTable(page, contentStream, 700, 10, matriz);

        // Let's close the content stream
        contentStream.close();
    }

    private void crearPaginaVentaRealizada(List<Venta> ventas, PDDocument document, int inicial, int indice) throws IOException {
        PDPage page;
        PDFont font;
        PDPageContentStream contentStream;
        page = new PDPage();

        // Adding page to document
        document.addPage(page);

        // Adding FONT to document
        font = PDType1Font.HELVETICA;

        // Next we start a new content stream which will "hold" the to be created content.
        contentStream = new PDPageContentStream(document, page);

        // Let's define the content stream
        contentStream.beginText();
        contentStream.setFont(font, 14);
        contentStream.moveTextPositionByAmount(10, 760);
        contentStream.drawString("VENTAS REALIZADAS");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(10, 740);
        contentStream.drawString("Aqui encontrara todas las ventas que se realizaron hasta la Fecha.");
        contentStream.endText();

        String matriz[][] = new String[indice][8];
        int numInicial = 0;

        ArrayList<String> headerTitulos = new ArrayList<>();
        headerTitulos.add("ID PEDIDO");
        headerTitulos.add("FECHA VENTA");
        headerTitulos.add("ARTICULO");
        headerTitulos.add("CANTIDAD");
        headerTitulos.add("PRECIO VENTA");
        headerTitulos.add("VENTA TOTAL");
        headerTitulos.add("PRECIO COMPRA");
        headerTitulos.add("GANANCIA");

        numInicial = crearHeaderPDF(inicial, matriz, numInicial, headerTitulos);

        for (int i = numInicial; i < indice; i++) {
            int indiceUtilizar = getIndiceUtilizar(numInicial, i);

            int articleID = ventas.get(indiceUtilizar).getArticle();
            Article articulo = articleService.findbyId(articleID);
            Compra compraArt = compraService.findAll()
                    .stream()
                    .filter(compra -> compra.getArticleId() == articleID)
                    .findFirst().get();
            float ventaTotal = ventas.get(indiceUtilizar).getPrecio() * ventas.get(indiceUtilizar).getCantidad();
            float compraTotal = ventas.get(indiceUtilizar).getCantidad() * compraArt.getPrecio();
            float ganancia = ventaTotal - compraTotal;
            matriz[i][0] = String.valueOf(ventas.get(indiceUtilizar).getPedido());
            String fecha = ventas.get(indiceUtilizar).getFechaVenta();
            matriz[i][1] = fecha.substring(0, fecha.length()-2);
            matriz[i][2] = String.valueOf(articulo.getNombre());
            matriz[i][3] = String.valueOf(ventas.get(indiceUtilizar).getCantidad());
            matriz[i][4] = String.valueOf(ventas.get(indiceUtilizar).getPrecio());
            matriz[i][5] = String.valueOf(ventaTotal);
            matriz[i][6] = String.valueOf(compraArt.getPrecio());
            matriz[i][7] = String.valueOf(ganancia);
        }

        drawTable(page, contentStream, 700, 10, matriz);

        // Let's close the content stream
        contentStream.close();
    }

    private void crearComprasRealizada(List<Compra> compras, PDDocument document, int inicial, int indice) throws IOException {
        PDPage page;
        PDFont font;
        PDPageContentStream contentStream;
        page = new PDPage();
        // Adding page to document
        document.addPage(page);

        // Adding FONT to document
        font = PDType1Font.HELVETICA;

        // Next we start a new content stream which will "hold" the to be created content.
        contentStream = new PDPageContentStream(document, page);

        // Let's define the content stream
        contentStream.beginText();
        contentStream.setFont(font, 14);
        contentStream.moveTextPositionByAmount(10, 760);
        contentStream.drawString("COMPRAS REALIZADAS");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(10, 740);
        contentStream.drawString("Aqui encontrara todas las compras que se realizaron hasta la Fecha.");
        contentStream.endText();

        String matriz[][] = new String[indice][8];
        int numInicial = 0;

        ArrayList<String> headerTitulos = new ArrayList<>();
        headerTitulos.add("COMPROBANTE");
        headerTitulos.add("FECHA COMPRA");
        headerTitulos.add("ARTICULO");
        headerTitulos.add("CANTIDAD");
        headerTitulos.add("PRECIO COMPRA");
        headerTitulos.add("TOTAL");
        headerTitulos.add("TIPO INGRESO");
        headerTitulos.add("PROVEEDOR");

        numInicial = crearHeaderPDF(inicial, matriz, numInicial, headerTitulos);

        for (int i = numInicial; i < indice; i++) {

            int indiceUtilizar = getIndiceUtilizar(numInicial, i);

            int articleID = compras.get(indiceUtilizar).getArticleId();
            Article articulo = articleService.findbyId(articleID);

            Proveedor proveedor = proveedorService.findbyId(compras.get(indiceUtilizar).getProvedor());

            matriz[i][0] =String.valueOf(compras.get(indiceUtilizar).getNumeroComprobante());
            String fecha = compras.get(indiceUtilizar).getFechaIngreso();
            matriz[i][1] = fecha.substring(0, fecha.length()-2);
            matriz[i][2] =articulo.getNombre();
            matriz[i][3] =String.valueOf(compras.get(indiceUtilizar).getCantidad());
            matriz[i][4] =String.valueOf(compras.get(indiceUtilizar).getPrecio());
            matriz[i][5] =String.valueOf(compras.get(indiceUtilizar).getPrecioTotal());
            matriz[i][6] =compras.get(indiceUtilizar).getTipoIngreso();
            matriz[i][7] =proveedor.getRazonSocial() + "(" + proveedor.getNumDocumento() + ")";
        }

        drawTable(page, contentStream, 700, 10, matriz);

        // Let's close the content stream
        contentStream.close();
    }

    private int getIndiceUtilizar(int numInicial, int i) {
        int indiceUtilizar = 0;

        if(numInicial == 1){
            indiceUtilizar = i-1;
        }
        return indiceUtilizar;
    }

    private int crearHeaderPDF(int inicial, String[][] matriz, int numInicial, ArrayList<String> headerTitulos) {
        if(inicial == 0){
            numInicial = 1;
            for(int i = 0; i<headerTitulos.size(); i++)
            matriz[0][i] = headerTitulos.get(i);
        }
        return numInicial;
    }


    public static void drawTable(PDPage page, PDPageContentStream contentStream,
                                 float y, float margin,
                                 String[][] content) throws IOException {
        final int rows = content.length;
        final int cols = content[0].length;
        final float rowHeight = 20f;
        final float tableWidth = page.getMediaBox().getWidth() - margin - margin;
        final float tableHeight = rowHeight * rows;
        final float colWidth = tableWidth/(float)cols;
        final float cellMargin=5f;

        //draw the rows
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
            contentStream.drawLine(margin, nexty, margin+tableWidth, nexty);
            nexty-= rowHeight;
        }

        //draw the columns
        float nextx = margin;
        for (int i = 0; i <= cols; i++) {
            contentStream.drawLine(nextx, y, nextx, y-tableHeight);
            nextx += colWidth;
        }

        //now add the text
        contentStream.setFont( PDType1Font.HELVETICA_BOLD , 8 );

        float textx = margin+cellMargin;
        float texty = y-15;
        for(int i = 0; i < content.length; i++){
            for(int j = 0 ; j < content[i].length; j++){
                String text = content[i][j];
                contentStream.beginText();
                contentStream.moveTextPositionByAmount(textx,texty);
                contentStream.drawString(text);
                contentStream.endText();
                textx += colWidth;
            }
            texty-=rowHeight;
            textx = margin+cellMargin;
        }
    }
}
