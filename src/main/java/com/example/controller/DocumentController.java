package com.example.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class DocumentController {

    /*@RequestMapping(value="/sd", method = RequestMethod.GET)
    private void createHelloDocument(HttpServletResponse response)
    {

        try {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output = createPDF();

            response.addHeader("Content-Type", "application/force-download");
            response.addHeader("Content-Disposition", "attachment; filename=\"yourFile.pdf\"");
            response.getOutputStream().write(output.toByteArray());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    public ByteArrayOutputStream createPDF() throws IOException {

        PDDocument document;
        PDPage page;
        PDFont font;
        PDPageContentStream contentStream;

        InputStream inputFront;
        InputStream inputBack;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Creating Document
        document = new PDDocument();

        // Creating Pages
        for(int i=0; i<2; i++) {

            page = new PDPage();

            // Adding page to document
            document.addPage(page);

            // Adding FONT to document
            font = PDType1Font.HELVETICA;

            // Next we start a new content stream which will "hold" the to be created content.
            contentStream = new PDPageContentStream(document, page);

            // Let's define the content stream
            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.moveTextPositionByAmount(10, 770);
            contentStream.drawString("Amount: $1.00");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.moveTextPositionByAmount(200, 770);
            contentStream.drawString("Sequence Number: 123456789");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.moveTextPositionByAmount(10, 760);
            contentStream.drawString("Account: 123456789");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.moveTextPositionByAmount(200, 760);
            contentStream.drawString("Captura Date: 04/25/2011");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.moveTextPositionByAmount(10, 750);
            contentStream.drawString("Bank Number: 123456789");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 8);
            contentStream.moveTextPositionByAmount(200, 750);
            contentStream.drawString("Check Number: 123456789");
            contentStream.endText();

            // Let's close the content stream
            contentStream.close();

        }

        // Finally Let's save the PDF
        document.save(output);
        document.close();

        return output;
    }
}
