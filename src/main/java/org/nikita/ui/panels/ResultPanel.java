package org.nikita.ui.panels;

import org.nikita.model.BalancingResult;
import org.nikita.model.Machine;
import org.nikita.model.Side;
import org.nikita.model.Stanchion;
import org.nikita.util.Utils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.stream.Collectors;

public class ResultPanel extends JPanel {

    private final Runnable onBackRequested;
    private final Runnable onExportPdfRequested;
    private final Runnable onRecalculateRequested;

    public ResultPanel(
            BalancingResult result,
            Runnable onBackRequested,
            Runnable onExportPdfRequested,
            Runnable onRecalculateRequested  // ← новое
    ) {
        this.onBackRequested = onBackRequested;
        this.onExportPdfRequested = onExportPdfRequested;
        this.onRecalculateRequested = onRecalculateRequested;
        initUI(result);
    }

    private void initUI(BalancingResult result) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /// Заголовок
        String summary = String.format(
                "📊 Итоги: %d пружин → %d %s | Стратегия компоновки %s",
                result.stats.totalSprings,
                result.machines.size(),
                Utils.pluralizeMachines(result.machines.size()),
                result.strategyName
        );
        JLabel summaryLabel = new JLabel(summary, SwingConstants.CENTER);
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(summaryLabel, BorderLayout.NORTH);

        // Основное содержимое — скролл с таблицей станков
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(createMachinesPanel(result));
        add(scrollPane, BorderLayout.CENTER);

        // Кнопки внизу
        JButton backButton = new JButton("← Назад");
        backButton.addActionListener(e -> onBackRequested.run());

        JButton recalculateButton = new JButton("🔄 Пересчитать");
        recalculateButton.addActionListener(e -> onRecalculateRequested.run());

        JButton pdfButton = new JButton("Сохранить PDF");
        pdfButton.addActionListener(e -> onExportPdfRequested.run());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(recalculateButton);
        buttonPanel.add(pdfButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createMachinesPanel(BalancingResult result) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);

        int row = 0;
        for (int i = 0; i < result.machines.size(); i++) {
            Machine m = result.machines.get(i);
            int machineSum = m.sum;
            int deviation = Math.abs(m.leftSide.sum - m.rightSide.sum);
            int absDev = Math.abs(deviation);

            // === Заголовок станка (занимает обе колонки) ===
            String machineText = String.format(
                    "Станок %d (суммарное усилие: %d Н, разница сторон: %d Н)",
                    i + 1, machineSum, deviation
            );
            JLabel machineHeader = new JLabel(machineText);
            machineHeader.setFont(machineHeader.getFont().deriveFont(Font.BOLD, 14f));
            machineHeader.setOpaque(true);
            machineHeader.setBackground(getDeviationColor(absDev));
            machineHeader.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            panel.add(machineHeader, gbc);
            row++;

            // === Левая сторона (колонка 0) ===
            addSideColumn(panel, "Левая сторона", m.leftSide, 0, row);

            // === Правая сторона (колонка 1) ===
            addSideColumn(panel, "Правая сторона", m.rightSide, 1, row);

            row++; // переходим к следующему станку
        }
        return panel;
    }

    private void addSideColumn(JPanel panel, String sideName, Side side, int col, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Вертикальная панель для всей стороны
        JPanel sidePanel = new JPanel(new BorderLayout(0, 6));
        sidePanel.setBorder(BorderFactory.createEtchedBorder());

        // 1. Заголовок
        JLabel header = new JLabel(sideName, SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setOpaque(true);
        header.setBackground(new Color(240, 240, 240));

        // 2. Сумма и разница
        int diff = Math.abs(side.left.sum - side.right.sum);
        String infoText = String.format("усилие = %d Н, разница стоек = %d Н", side.sum, diff);
        JLabel infoLabel = new JLabel(infoText, SwingConstants.CENTER);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 11f));

        // 3. Две стойки — горизонтально
        JPanel stanchionsPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        stanchionsPanel.add(createStanchionBox(side.left));
        stanchionsPanel.add(createStanchionBox(side.right));

        // Собираем всё вместе
        sidePanel.add(header, BorderLayout.NORTH);
        sidePanel.add(infoLabel, BorderLayout.CENTER);
        sidePanel.add(stanchionsPanel, BorderLayout.SOUTH);

        panel.add(sidePanel, gbc);
    }

    private JPanel createStanchionBox(Stanchion s) {
        JPanel box = new JPanel(new BorderLayout(4, 2));
        box.setBorder(BorderFactory.createLoweredBevelBorder());

        // Номер стойки
        JLabel idLabel = new JLabel("Стойка #" + s.id, SwingConstants.CENTER);
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 11f));

        // Пружины
        String springs = s.springs.stream()
                .map(sp -> String.format("%d (%d)", sp.getId(), sp.getForce()))
                .collect(Collectors.joining(", "));
        JTextArea springsArea = new JTextArea(springs);
        springsArea.setFont(new Font("Consolas", Font.PLAIN, 10));
        springsArea.setLineWrap(true);
        springsArea.setWrapStyleWord(true);
        springsArea.setEditable(false);
        springsArea.setFocusable(false);
        springsArea.setOpaque(false);

        // Сумма
        JLabel sumLabel = new JLabel(String.format("%d Н", s.sum), SwingConstants.CENTER);
        sumLabel.setFont(sumLabel.getFont().deriveFont(Font.BOLD, 10f));

        box.add(idLabel, BorderLayout.NORTH);
        box.add(springsArea, BorderLayout.CENTER);
        box.add(sumLabel, BorderLayout.SOUTH);

        return box;
    }

    private void addSideHeader(JPanel panel, String sideName, Side side, int col, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);

        int diff = Math.abs(side.left.sum - side.right.sum);
        String text = String.format("<html><b>%s</b><br>Σ=%d Н<br>Δ=%d Н</html>",
                sideName, side.sum, diff);

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEtchedBorder());
        label.setOpaque(true);
        label.setBackground(new Color(245, 245, 245));

        panel.add(label, gbc);
    }

    private void addStanchionCellWithSideInfo(JPanel panel, Side side, String sideName, int col, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0.45;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Разница между стойками на стороне
        int diff = Math.abs(side.left.sum - side.right.sum);
        String sideInfo = String.format("%s (%d Н, Δ=%d Н)", sideName, side.sum, diff);

        JLabel sideLabel = new JLabel(sideInfo);
        sideLabel.setFont(sideLabel.getFont().deriveFont(Font.BOLD, 10f));
        sideLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Пружины первой стойки
        String springsText = side.left.springs.stream()
                .map(sp -> String.format("%d (%d)", sp.getId(), sp.getForce()))
                .collect(Collectors.joining(", "));
        JLabel springsLabel = new JLabel(springsText);
        springsLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        springsLabel.setToolTipText("Номер (усилие Н)");

        // Сумма стойки
        JLabel sumLabel = new JLabel(String.format("%d Н", side.left.sum));
        sumLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        sumLabel.setFont(sumLabel.getFont().deriveFont(Font.BOLD, 11f));

        // Сборка ячейки
        JPanel cell = new JPanel(new BorderLayout(6, 2));
        cell.add(sideLabel, BorderLayout.NORTH);      // ← информация о стороне сверху
        cell.add(springsLabel, BorderLayout.CENTER);
        cell.add(sumLabel, BorderLayout.EAST);
        cell.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        panel.add(cell, gbc);
    }

    private JPanel createSidePanel(String title, int sum) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                String.format("%s (%d Н)", title, sum),
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)
        ));
        panel.setPreferredSize(new Dimension(250, 40)); // минимальная высота
        return panel;
    }

    private void addStanchionCell(JPanel panel, Stanchion s, int col, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0.45;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Номер стойки: ищем индекс в исходном списке (чтобы не было "?")
        // Для простоты — временно ставим "–", позже можно добавить Stanchion.id
        JLabel idLabel = new JLabel("Стойка №" + s.id);
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 11f));

        // Пружины: номер (усилие), через запятую
        String springsText = s.springs.stream()
                .map(sp -> String.format("%d (%d)", sp.getId(), sp.getForce()))
                .collect(Collectors.joining(", "));
        JLabel springsLabel = new JLabel(springsText);
        springsLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        springsLabel.setToolTipText("Номер (усилие Н)");

        // Сумма стойки
        JLabel sumLabel = new JLabel(String.format("%d Н", s.sum));
        sumLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        sumLabel.setFont(sumLabel.getFont().deriveFont(Font.BOLD, 11f));

        // Сборка ячейки
        JPanel cell = new JPanel(new BorderLayout(6, 2));
        cell.add(idLabel, BorderLayout.NORTH);
        cell.add(springsLabel, BorderLayout.CENTER);
        cell.add(sumLabel, BorderLayout.EAST);
        cell.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        panel.add(cell, gbc);
    }

    private Color getDeviationColor(int absDev) {
        if (absDev < 100) return new Color(200, 255, 200); // светло-зелёный
        if (absDev < 300) return new Color(255, 255, 200); // светло-жёлтый
        return new Color(255, 220, 220); // светло-розовый
    }
}