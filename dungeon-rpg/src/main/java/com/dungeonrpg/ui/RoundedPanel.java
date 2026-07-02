package com.dungeonrpg.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A JPanel that paints a rounded-rectangle card background instead of the
 * default sharp-cornered fill. Used anywhere the UI needs to visually
 * group content (the hero/enemy stat cards in combat, the log area) —
 * one class, reused everywhere a "card" is needed, rather than repeating
 * the same custom-paint code in every panel that wants rounded corners.
 */
public class RoundedPanel extends JPanel {

    private final Color background;
    private final int arc;

    public RoundedPanel(Color background, int arc) {
        this.background = background;
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
