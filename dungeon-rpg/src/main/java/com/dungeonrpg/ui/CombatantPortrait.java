package com.dungeonrpg.ui;

import com.dungeonrpg.ui.sprite.CombatantSprite;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * CombatantPortrait owns exactly one CombatantSprite and animates it: a
 * gentle idle bob at all times, a lunge-forward-and-back motion on
 * playAttackAnimation(), a brief color flash plus a rising, fading
 * damage number on flashHit(). All animation state (bob phase, lunge
 * progress, flash alpha, floating numbers) lives here — the sprite
 * classes themselves stay simple, stateless "how do I look" strategies,
 * same separation CombatEngine/CombatPanel already established between
 * rules and rendering.
 *
 * Driven by a single javax.swing.Timer ticking at ~33fps. dispose() MUST
 * be called when a portrait is discarded (a new CombatPanel is built for
 * every encounter) or the Timer keeps firing in the background forever —
 * a real resource leak, not a theoretical one, over a long play session.
 */
public class CombatantPortrait extends JComponent {

    private static final int TICK_MS = 30;
    private static final double BOB_SPEED = 0.10;
    private static final double BOB_AMPLITUDE = 4.0;
    private static final int LUNGE_TICKS = 14;
    private static final double LUNGE_DISTANCE = 34;
    private static final int FLOAT_TEXT_LIFE_TICKS = 30;

    private final CombatantSprite sprite;
    private final boolean facesLeft;
    private final Timer timer;

    private double bobPhase = 0;
    private int lungeTicksElapsed = -1; // -1 means "not lunging"
    private double flashAlpha = 0;
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    public CombatantPortrait(CombatantSprite sprite, boolean facesLeft) {
        this.sprite = sprite;
        this.facesLeft = facesLeft;
        setOpaque(false);

        timer = new Timer(TICK_MS, e -> tick());
        timer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(140, 130);
    }

    public void playAttackAnimation() {
        lungeTicksElapsed = 0;
    }

    public void flashHit(int damage) {
        flashAlpha = 1.0;
        floatingTexts.add(new FloatingText(damage));
    }

    /** Must be called when this portrait is discarded, to stop its Timer. */
    public void dispose() {
        timer.stop();
    }

    private void tick() {
        bobPhase += BOB_SPEED;

        if (lungeTicksElapsed >= 0) {
            lungeTicksElapsed++;
            if (lungeTicksElapsed > LUNGE_TICKS) {
                lungeTicksElapsed = -1;
            }
        }

        if (flashAlpha > 0) {
            flashAlpha = Math.max(0, flashAlpha - 0.07);
        }

        floatingTexts.forEach(FloatingText::age);
        floatingTexts.removeIf(FloatingText::isExpired);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() * 2 / 3;
        int scale = Math.min(getWidth(), getHeight()) - 20;

        double bob = Math.sin(bobPhase) * BOB_AMPLITUDE;

        int lungeOffset = 0;
        if (lungeTicksElapsed >= 0) {
            double progress = lungeTicksElapsed / (double) LUNGE_TICKS;
            double magnitude = Math.sin(progress * Math.PI) * LUNGE_DISTANCE;
            lungeOffset = (int) (facesLeft ? -magnitude : magnitude);
        }

        sprite.paint(g2, cx + lungeOffset, cy, scale, bob, facesLeft);

        if (flashAlpha > 0) {
            g2.setColor(new Color(255, 255, 255, (int) (flashAlpha * 160)));
            g2.fillOval(cx - scale / 2, cy - scale, scale, scale);
        }

        for (FloatingText text : floatingTexts) {
            text.paint(g2, cx, cy - scale / 2);
        }

        g2.dispose();
    }

    /**
     * A small piece of "-N" text that rises and fades over its lifetime.
     * Kept as a private nested class rather than a separate file since it
     * has no meaning or use outside a CombatantPortrait's animation state.
     */
    private static class FloatingText {
        private final int damage;
        private int age = 0;

        FloatingText(int damage) {
            this.damage = damage;
        }

        void age() {
            age++;
        }

        boolean isExpired() {
            return age >= FLOAT_TEXT_LIFE_TICKS;
        }

        void paint(Graphics2D g2, int x, int baseY) {
            double progress = age / (double) FLOAT_TEXT_LIFE_TICKS;
            int y = baseY - (int) (progress * 40);
            int alpha = (int) (255 * (1 - progress));

            String text = "-" + damage;
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.setColor(new Color(0, 0, 0, Math.max(0, alpha - 60)));
            g2.drawString(text, x - g2.getFontMetrics().stringWidth(text) / 2 + 1, y + 1);
            g2.setColor(new Color(230, 60, 60, alpha));
            g2.drawString(text, x - g2.getFontMetrics().stringWidth(text) / 2, y);
        }
    }
}
