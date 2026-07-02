package com.dungeonrpg.ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

/**
 * A self-contained HP bar: rounded track, gradient fill, and a color that
 * shifts from healthy green through amber to danger red as the value
 * drops. This is its own small class (not just inline painting code
 * inside CombatPanel) because both the hero's and the enemy's bars need
 * identical behavior — instantiate this twice rather than duplicating the
 * painting logic, which is REUSE at the component level.
 */
public class HealthBar extends JComponent {

    private int current;
    private int max = 1;

    public void setValue(int current, int max) {
        this.current = current;
        this.max = Math.max(1, max);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(220, 22);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        double ratio = (double) current / max;

        g2.setColor(UiTheme.BG_DARKEST);
        g2.fillRoundRect(0, 0, w, h, h, h);

        int fillWidth = (int) Math.round(w * ratio);
        if (fillWidth > 0) {
            Color barColor = ratio > 0.5 ? UiTheme.HP_HIGH : ratio > 0.25 ? UiTheme.HP_MID : UiTheme.HP_LOW;
            GradientPaint gradient = new GradientPaint(
                0, 0, barColor.brighter(), 0, h, barColor.darker());
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, fillWidth, h, h, h);
        }

        g2.setColor(new Color(0, 0, 0, 90));
        g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

        String label = current + " / " + max;
        g2.setFont(UiTheme.FONT_BODY_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(label)) / 2;
        int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(label, textX, textY + 1);
        g2.setColor(UiTheme.TEXT_PRIMARY);
        g2.drawString(label, textX, textY);

        g2.dispose();
    }
}
