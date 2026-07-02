package com.dungeonrpg.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A JButton subclass that paints itself instead of relying on the
 * platform's default look and feel. Swing's stock JButton is the single
 * biggest giveaway of an unpolished Java app — flat gray, sharp corners,
 * no hover feedback. Overriding paintComponent() and turning off the
 * default content/border painting is the standard technique for a
 * custom-styled Swing button; everything else about JButton (click
 * handling, focus, enabled/disabled state) still works normally, we're
 * only replacing how it draws itself.
 */
public class RoundedButton extends JButton {

    private boolean hovering = false;

    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(UiTheme.TEXT_PRIMARY);
        setFont(UiTheme.FONT_BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = !isEnabled() ? UiTheme.BG_PANEL_RAISED
            : hovering ? UiTheme.ACCENT_HOVER
            : UiTheme.ACCENT;

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();

        setForeground(isEnabled() ? UiTheme.BG_DARKEST : UiTheme.TEXT_MUTED);
        super.paintComponent(g);
    }
}
