package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;

/** The final boss: a bulky, spiked, Bowser-esque dinosaur silhouette. */
public class DinosaurSprite implements CombatantSprite {

    private static final Color HIDE = new Color(70, 130, 60);
    private static final Color HIDE_DARK = new Color(44, 88, 38);
    private static final Color BELLY = new Color(214, 196, 140);
    private static final Color SPIKE = new Color(230, 220, 200);
    private static final Color EYE = new Color(255, 60, 40);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        int s = (int) (scale * 1.35); // the boss reads as clearly larger than anything else
        int y = cy + (int) bob;

        // Tail
        g2.setColor(HIDE_DARK);
        g2.setStroke(new BasicStroke(Math.max(4f, s * 0.09f)));
        g2.drawLine(cx - dir * (int) (s * 0.20), (int) (y + s * 0.10),
            cx - dir * (int) (s * 0.55), (int) (y + s * 0.28));

        // Big bulky body
        g2.setColor(HIDE);
        g2.fill(new Ellipse2D.Double(cx - s * 0.28, y - s * 0.16, s * 0.56, s * 0.42));
        g2.setColor(BELLY);
        g2.fill(new Ellipse2D.Double(cx - s * 0.16, y - s * 0.02, s * 0.32, s * 0.26));

        // Spiked ridge along the back
        g2.setColor(SPIKE);
        for (int i = 0; i < 4; i++) {
            int spikeX = cx - dir * (int) (s * 0.18) + dir * i * (int) (s * 0.12);
            Polygon spike = new Polygon();
            spike.addPoint(spikeX, (int) (y - s * 0.16));
            spike.addPoint(spikeX - (int) (s * 0.05), (int) (y - s * 0.02));
            spike.addPoint(spikeX + (int) (s * 0.05), (int) (y - s * 0.02));
            g2.fillPolygon(spike);
        }

        // Short stubby legs
        g2.setColor(HIDE_DARK);
        g2.fillRoundRect(cx - (int) (s * 0.20), (int) (y + s * 0.20), (int) (s * 0.14), (int) (s * 0.16), 6, 6);
        g2.fillRoundRect(cx + (int) (s * 0.06), (int) (y + s * 0.20), (int) (s * 0.14), (int) (s * 0.16), 6, 6);

        // Thick neck and head
        int headX = cx + dir * (int) (s * 0.32);
        int headY = (int) (y - s * 0.30);
        g2.setColor(HIDE);
        g2.setStroke(new BasicStroke(Math.max(6f, s * 0.16f)));
        g2.drawLine(cx + dir * (int) (s * 0.12), (int) (y - s * 0.06), headX, headY);
        g2.fill(new Ellipse2D.Double(headX - s * 0.16, headY - s * 0.12, s * 0.32, s * 0.24));

        // Small horns
        g2.setColor(SPIKE);
        Polygon horn = new Polygon();
        horn.addPoint(headX, (int) (headY - s * 0.12));
        horn.addPoint((int) (headX - s * 0.04), (int) (headY - s * 0.24));
        horn.addPoint((int) (headX + s * 0.03), (int) (headY - s * 0.11));
        g2.fillPolygon(horn);

        // Eye and jaw
        g2.setColor(EYE);
        g2.fillOval((int) (headX + dir * s * 0.03), (int) (headY - s * 0.03), 7, 7);
        g2.setColor(HIDE_DARK);
        g2.fillRect((int) (headX - s * 0.02), (int) (headY + s * 0.08), (int) (s * 0.18), (int) (s * 0.05));
    }
}
