package org.nikita.ui.panels;

import org.nikita.ui.components.InfoButton;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {

    private final Runnable onCsvLoadRequested;

    public WelcomePanel(Runnable onCsvLoadRequested) {
        this.onCsvLoadRequested = onCsvLoadRequested;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        /// Заголовок
        JLabel titleLabel = new JLabel("Добро пожаловать в SpringsBalancer!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(titleLabel, BorderLayout.CENTER);

        /// Кнопка загрузки
        JButton loadButton = new JButton("Загрузить CSV");
        loadButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadButton.setPreferredSize(new Dimension(200, 40));
        loadButton.addActionListener(e -> onCsvLoadRequested.run());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        //todo может кнопкой?
        /// Кнопка "О программе" — в правом верхнем углу
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        topPanel.add(new InfoButton(), BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }
}