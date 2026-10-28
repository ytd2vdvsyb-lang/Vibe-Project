package vibe.launcher.ui;

import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Большая кнопка "Играть" с градиентом. */
final class GradientButton extends JButton {
    private boolean hover;

    GradientButton(String text) {
        super(text);
        setFont(Theme.font(Font.BOLD, 18));
        setForeground(Theme.TEXT);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isEnabled()) {
            g2.setPaint(new GradientPaint(0, 0, hover ? Theme.ACCENT.brighter() : Theme.ACCENT,
                    getWidth(), getHeight(), hover ? Theme.ACCENT_2.brighter() : Theme.ACCENT_2));
        } else {
            g2.setColor(Theme.BORDER);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}
