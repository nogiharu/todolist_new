package com.example.todolist.view;

import java.time.LocalDateTime;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.web.servlet.view.document.AbstractPdfView;

public class SamplePdf extends AbstractPdfView {
    @Override
    protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        //テキスト
        String currentTime = ((LocalDateTime)model.get("ldt")).toString();
        document.add(new Paragraph("LoalDateTimeだよ(>_<)"));

        // フォント
        Font font = new Font(BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H",BaseFont.NOT_EMBEDDED), 16);

        //表
        Table table = new Table(1);
        Cell cell = new Cell(new Phrase("いまの時間だよ(^-^)",font));
        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.addCell(cell);
        table.addCell(currentTime);
        document.add(table);

    }
}