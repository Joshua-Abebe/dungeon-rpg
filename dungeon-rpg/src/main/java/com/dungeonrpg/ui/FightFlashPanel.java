package com.dungeonrpg.ui;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A brief, punchy "FIGHT!" flash shown for a fraction of a second
 * before the real combat screen appears — the classic arcade-fighter
 * beat of announcing an encounter with a burst of text before dropping
 * the player into it. Purely a timed animation: the text scales up
 * quickly and fades, then onComplete fires so GameWindow can switch to
 * the actual CombatPanel underneath.
 */
public class FightFlashPanel extends JComponent {

    private static final int DURATION_MS = 650;
    private static final int TICK_MS = 20;

    private Timer timer;
    private final Color backgroundTint;
    private int elapsedMs = 0;

    /**
     * backgroundTint varies by floor — a lightweight stand-in for the
     * "dynamic battle background per tile environment" suggestion. This
     * game's tiles don't yet have distinct environment types (lava,
     * water, grass) to key art off of, so floor number is the most
     * honest signal available: floor 1 reads as a mundane stone
     * dungeon, floor 2 as something colder and more dangerous, and
     * floor 3 (the boss floor) as fire-lit and threatening.
     */
    public FightFlashPanel(Runnable onComplete, int floorNumber) {
        setOpaque(true);
        this.backgroundTint = tintForFloor(floorNumber);
        setBackground(Color.BLACK);

        timer = new Timer(TICK_MS, e -> {
            elapsedMs += TICK_MS;
            repaint();
            if (elapsedMs >= DURATION_MS) {
                timer.stop();
                onComplete.run();
            }
        });
        timer.start();
    }

    private static Color tintForFloor(int floorNumber) {
        return switch (floorNumber) {
            case 2 -> new Color(20, 26, 40); // colder, deeper dungeon
            case 3 -> new Color(48, 14, 10); // fire-lit boss floor
            default -> Color.BLACK; // floor 1: plain stone dungeon
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(backgroundTint);
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double progress = Math.min(1.0, elapsedMs / (double) DURATION_MS);
        // Fast punch-in (first 30%), hold, then fade out (last 25%).
        double scale = progress < 0.3 ? 0.4 + (progress / 0.3) * 0.8 : 1.2;
        int alpha = progress > 0.75 ? (int) (255 * (1 - (progress - 0.75) / 0.25)) : 255;
        alpha = Math.max(0, Math.min(255, alpha));

        String text = "FIGHT!";
        int baseSize = 64;
        Font font = new Font("SansSerif", Font.BOLD, (int) (baseSize * scale));
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        int y = (getHeight() + metrics.getAscent()) / 2;

        g2.setColor(new Color(0, 0, 0, alpha));
        g2.drawString(text, x + 4, y + 4);
        g2.setColor(new Color(220, 40, 40, alpha));
        g2.drawString(text, x, y);
    }
}
