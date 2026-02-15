package org.nikita.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class InfoButton extends JButton {

    public InfoButton() {
        super("О программе");
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(this::showAboutDialog);
    }

    private void showAboutDialog(ActionEvent e) {
        String html = """
        <html>
        <head>
          <style>
            body { font-family: 'Segoe UI'; font-size: 13px; margin: 12px; }
            h2 { margin-top: 0; color: #2c3e50; }
            hr { border: 0; border-top: 1px solid #eee; margin: 12px 0; }
            a { color: #3498db; text-decoration: none; }
            a:hover { text-decoration: underline; }
          </style>
        </head>
        <body>
          <h2>Spring_Balancer v0.2</h2>
          <p>Сортировщик пружин по усилию сжатия</p>
          <hr>
          <p><b>Автор:</b> Константинов Никита <br>
             <b>Лицензия:</b> MIT (свободное ПО)</p>
          <p><b>📩 Обратная связь:</b><br>
             <a href="https://t.me/Nikita_Konstantinov_spb">Telegram: @Nikita_Konstantinov_spb</a></p>
          <p style="color: #7f8c8d; margin-top: 20px;">
            made on Java with ❤️
          </p>
        </body>
        </html>
        """;

        // ✅ Кликабельная HTML-панель
        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        /*// ✅ Обработчик кликов по ссылкам
        editorPane.addHyperlinkListener(e2 -> {
            if (e2.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e2.getURL().toURI());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(this),
                            "Не удалось открыть ссылку:\n" + e2.getURL(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });*/

        JLabel label = new JLabel(html);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                label,
                "О программе",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
