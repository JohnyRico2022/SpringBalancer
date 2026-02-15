package org.nikita.io;

import com.lowagie.text.Rectangle;
import org.nikita.model.*;

import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.BaseFont;
import org.nikita.util.Utils;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfExporter {

    // Шрифты с поддержкой кириллицы (встроены в JAR)
    private static final Font TITLE_FONT;
    private static final Font HEADER_FONT;
    private static final Font TEXT_FONT;
    private static final Font SMALL_FONT;

    static {
        try {
            // Загружаем DejaVuSans.ttf из ресурсов
            BaseFont baseFont = BaseFont.createFont(
                    "/fonts/DejaVuSans.ttf",
                    BaseFont.IDENTITY_H,   // ← UTF-8
                    BaseFont.EMBEDDED      // ← встраиваем в PDF
            );
            TITLE_FONT = new Font(baseFont, 18, Font.BOLD);
            HEADER_FONT = new Font(baseFont, 12, Font.BOLD);
            TEXT_FONT = new Font(baseFont, 10);
            SMALL_FONT = new Font(baseFont, 8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки шрифта DejaVuSans.ttf", e);
        }
    }

    public static void exportToPdf(BalancingResult result, String outputPath)
            throws IOException, DocumentException {

        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        addTitle(doc);
        addSummary(doc, result);

        for (int i = 0; i < result.machines.size(); i++) {
            if (i > 0 && i % 3 == 0) {
                doc.newPage();
            }
            addMachine(doc, result.machines.get(i), i + 1);
            doc.add(Chunk.NEWLINE);
        }

        doc.close();
    }

    private static void addTitle(Document doc) throws DocumentException {
        Paragraph title = new Paragraph("SpringBalancer — Отчёт балансировки пружин", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Paragraph subtitle = new Paragraph("Дата формирования: " + dateTime, TEXT_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitle);
        doc.add(Chunk.NEWLINE);
    }

    private static void addSummary(Document doc, BalancingResult result) throws DocumentException {
        String summary = String.format(
                "Итоги: %d пружин → %d %s | Стратегия компановки %s",
                result.stats.totalSprings,
                result.machines.size(),
                Utils.pluralizeMachines(result.machines.size()),
                result.strategyName
        );
        Paragraph p = new Paragraph(summary, HEADER_FONT);
        doc.add(p);
        doc.add(Chunk.NEWLINE);
    }

    private static void addMachine(Document doc, Machine machine, int machineNum) throws DocumentException {

        int deviation = Math.abs(machine.leftSide.sum - machine.rightSide.sum);

        Paragraph header = new Paragraph(String.format(
                "Станок %d (сумма: %d Н, разница сторон: %d Н)",
                machineNum, machine.sum, deviation
        ), HEADER_FONT);
        header.setSpacingBefore(6f);
        header.setSpacingAfter(4f);
        doc.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(2f);
        table.setSpacingAfter(6f);

        PdfPCell leftCell = createSideCell("Левая сторона", machine.leftSide);
        table.addCell(leftCell);

        PdfPCell rightCell = createSideCell("Правая сторона", machine.rightSide);
        table.addCell(rightCell);

        doc.add(table);
    }

    private static PdfPCell createSideCell(String title, Side side) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setPadding(6f);

        int dev = Math.abs(side.left.sum - side.right.sum);

        Paragraph sideHeader = new Paragraph(String.format("%s (%d Н, разница стоек: %d H) ", title, side.sum, dev), TEXT_FONT);
        cell.addElement(sideHeader);

        addStanchionToCell(cell, side.left);
        addStanchionToCell(cell, side.right);

        return cell;
    }

    private static void addStanchionToCell(PdfPCell cell, Stanchion s) {
        /// Номер стойки
        Paragraph idLine = new Paragraph(String.format("Стойка #%d", s.id), TEXT_FONT);
        cell.addElement(idLine);

        /// Пружины: 23 (6868), 120 (6869), ...
        String springs = s.springs.stream()
                .map(sp -> String.format("%d (%d)", sp.getId(), sp.getForce()))
                .collect(java.util.stream.Collectors.joining(", "));
        Paragraph springsLine = new Paragraph(springs, SMALL_FONT);
        springsLine.setLeading(10f);
        cell.addElement(springsLine);

        /// Сумма
        Paragraph sumLine = new Paragraph(String.format("%d Н", s.sum), TEXT_FONT);
        sumLine.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(sumLine);

        /// Разделитель
        cell.addElement(new Paragraph("──────────────────────────────────────────────────", SMALL_FONT));
    }
}