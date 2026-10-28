package vibe.launcher.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/** Левая половина окна: логотип на градиентном фоне. */
final class HeroPanel extends JPanel {
    HeroPanel() {
        setOpaque(true);
        setBackground(Theme.BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        float radius = Math.max(w, h) * 0.8f;
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.2f, h * 0.15f), radius,
                new float[]{0f, 1f}, new Color[]{new Color(139, 92, 246, 110), new Color(0, 0, 0, 0)}));
        g2.fillRect(0, 0, w, h);
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.9f, h * 0.95f), radius * 0.7f,
                new float[]{0f, 1f}, new Color[]{new Color(236, 72, 153, 90), new Color(0, 0, 0, 0)}));
        g2.fillRect(0, 0, w, h);

        int x = 48;
        int y = h / 2 - 20;
        g2.setFont(Theme.font(Font.BOLD, 56));
        g2.setPaint(new GradientPaint(x, 0, Theme.TEXT, x + 260, 0, new Color(0xC4B5FD)));
        g2.drawString("VIBE", x, y);
        g2.setFont(Theme.font(Font.PLAIN, 28));
        g2.setColor(Theme.TEXT);
        g2.drawString("CLIENT", x + 3, y + 38);

        g2.setFont(Theme.font(Font.PLAIN, 14));
        g2.setColor(Theme.MUTED);
        g2.drawString("Свой клиент Minecraft. Один клик до игры.", x + 3, y + 74);
        g2.dispose();
    }
}
