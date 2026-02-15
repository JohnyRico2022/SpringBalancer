package org.nikita.ui.panels;

import org.nikita.model.Spring;
import org.nikita.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DataTablePanel extends JPanel {


    private final Runnable onBackRequested;
    private final Runnable onNextRequested;

    private JScrollPane scrollPane;
    private JButton nextButton;

    public DataTablePanel(Runnable onBackRequested, Runnable onNextRequested) {
        this.onBackRequested = onBackRequested;
        this.onNextRequested = onNextRequested;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Таблица
        String[] columns = {"№ пружины", "Усилие, Н"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Consolas", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Загруженные пружины"));
        add(scrollPane, BorderLayout.CENTER);

        // Кнопки
        JButton backButton = new JButton("← Назад");
        backButton.addActionListener(e -> onBackRequested.run());

        nextButton = new JButton("Далее →");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> onNextRequested.run());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateData(ArrayList<org.nikita.model.Spring> springs) {
        DefaultTableModel model = (DefaultTableModel) ((JTable) ((JViewport) scrollPane.getComponent(0)).getView()).getModel();
        model.setRowCount(0);
        for (Spring s : springs) {
            model.addRow(new Object[]{s.getId(), s.getForce()});
        }

        // Обновляем заголовок у TitledBorder
        TitledBorder border = (TitledBorder) scrollPane.getBorder();
        int stations = springs.size() / 24;
        border.setTitle(String.format("Загружено %d пружин → %d %s",
                springs.size(),
                stations,
                Utils.pluralizeMachines(stations)));
        scrollPane.repaint(); // перерисовываем границу

        // Активируем кнопку
        nextButton.setEnabled(true);
    }
}