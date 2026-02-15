package org.nikita.ui;

import org.nikita.domain.SpringBalancer;
import org.nikita.io.CsvLoader;
import org.nikita.io.PdfExporter;
import org.nikita.model.BalancingResult;
import org.nikita.model.Spring;
import org.nikita.ui.panels.DataTablePanel;
import org.nikita.ui.panels.ResultPanel;
import org.nikita.ui.panels.WelcomePanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Main extends JFrame {

    private WelcomePanel welcomePanel;
    private DataTablePanel dataTablePanel;
    private ArrayList<org.nikita.model.Spring> springs;
    private BalancingResult currentResult;

    private int attempt = 0;

    public Main() {
        setTitle("SpringsBalancer — Сортировщик пружин");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        welcomePanel = new WelcomePanel(this::onCsvLoadRequested);
        dataTablePanel = new DataTablePanel(this::onBackRequested, this::onNextRequested);

        showWelcomePanel();
        setVisible(true);
    }

    /// Точка входа
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    public void showWelcomePanel() {
        setContentPane(welcomePanel);
        revalidate();
        repaint();
    }

    public void showDataTablePanel(ArrayList<org.nikita.model.Spring> springs) {
        this.springs = springs;
        dataTablePanel.updateData(springs);
        setContentPane(dataTablePanel);
        revalidate();
        repaint();
    }

    /// Обработчики событий из панелей
    private void onCsvLoadRequested() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV файлы", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                ArrayList<Spring> loaded = CsvLoader.loadCsv(file.getAbsolutePath());
                int count = loaded.size();
                if (count % 24 != 0) {
                    JOptionPane.showMessageDialog(this,
                            "Количество пружин (" + count + ") не кратно 24.\nПроверьте файл.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                showDataTablePanel(loaded);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки:\n" + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void onBackRequested() {
        showWelcomePanel();
    }

    private void onNextRequested() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<BalancingResult, Void> worker = new SwingWorker<>() {
            @Override
            protected BalancingResult doInBackground() throws Exception {
                return SpringBalancer.balance(springs, attempt);
            }

            @Override
            protected void done() {
                Main.this.setCursor(Cursor.getDefaultCursor());
                try {
                    BalancingResult result = get();
                    currentResult = result;
                    showResultPanel(result);
                    attempt++; // следующий клик — другая стратегия
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Main.this,
                            "Не удалось сбалансировать пружины:\n" + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showResultPanel(BalancingResult result) {
        this.currentResult = result;
        ResultPanel resultPanel = new ResultPanel(
                result,
                this::showWelcomePanel,
                this::onExportPdfRequested,
                this::onRecalculateRequested
        );
        setContentPane(resultPanel);
        revalidate();
        repaint();
    }

    private void onRecalculateRequested() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<BalancingResult, Void> worker = new SwingWorker<>() {
            @Override
            protected BalancingResult doInBackground() throws Exception {
                // Используем тот же список пружин, но новая попытка → новая стратегия
                return SpringBalancer.balance(springs, attempt++);
            }

            @Override
            protected void done() {
                Main.this.setCursor(Cursor.getDefaultCursor());
                try {
                    BalancingResult result = get();
                    showResultPanel(result); // ← обновляет весь экран
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Ошибка пересчёта:\n" + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }


    private void onExportPdfRequested() {

        JFileChooser chooser = new JFileChooser();
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        chooser.setSelectedFile(new File("SpringsBalancer_Report_" +
                timestamp + ".pdf"));
        chooser.setDialogTitle("Сохранить отчёт в PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF файлы (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String path = file.getPath();
            if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

            try {
                PdfExporter.exportToPdf(currentResult, path);
                JOptionPane.showMessageDialog(this,
                        "Отчёт сохранён:\n" + path,
                        "Готово", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта:\n" + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}