package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.Polygon;

/** The boss: winged, taller than every other combatant, with a long neck and tail. */
public class DragonSprite implements CombatantSprite {

    private static final Color SCALE = new Color(140, 40, 40);
    private static final Color SCALE_DARK = new Color(90, 22, 22);
    private static final Color WING = new Color(70, 20, 24);
    private static final Color HORN = new Color(224, 214, 190);
    private static final Color EYE = new Color(255, 210, 60);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        int s = (int) (scale * 1.4); // the boss reads as clearly larger than anything else
        int y = cy + (int) bob;

        // Wings, drawn behind the body
        g2.setColor(WING);
        Polygon wingBack = new Polygon();
        wingBack.addPoint(cx - dir * (int) (s * 0.05), (int) (y - s * 0.10));
        wingBack.addPoint(cx - dir * (int) (s * 0.55), (int) (y - s * 0.40));
        wingBack.addPoint(cx - dir * (int) (s * 0.40), (int) (y + s * 0.05));
        g2.fillPolygon(wingBack);

        // Tail (curved)
        g2.setColor(SCALE_DARK);
        CubicCurve2D tail = new CubicCurve2D.Double(
            cx - dir * s * 0.20, y + s * 0.15,
            cx - dir * s * 0.40, y + s * 0.30,
            cx - dir * s * 0.55, y + s * 0.10,
            cx - dir * s * 0.65, y + s * 0.25);
        g2.setStroke(new BasicStroke(Math.max(3f, s * 0.05f)));
        g2.draw(tail);

        // Body
        g2.setColor(SCALE);
        g2.fill(new Ellipse2D.Double(cx - s * 0.26, y - s * 0.18, s * 0.52, s * 0.40));

        // Front wing, on top of the body for depth
        Polygon wingFront = new Polygon();
        wingFront.addPoint(cx + dir * (int) (s * 0.05), (int) (y - s * 0.14));
        wingFront.addPoint(cx + dir * (int) (s * 0.50), (int) (y - s * 0.42));
        wingFront.addPoint(cx + dir * (int) (s * 0.36), (int) (y + s * 0.02));
        g2.setColor(WING.brighter());
        g2.fillPolygon(wingFront);

        // Long neck and head
        int headX = cx + dir * (int) (s * 0.30);
        int headY = (int) (y - s * 0.44);
        g2.setColor(SCALE);
        g2.setStroke(new BasicStroke(Math.max(4f, s * 0.10f)));
        g2.drawLine(cx + dir * (int) (s * 0.10), (int) (y - s * 0.08), headX, headY);

        g2.fill(new Ellipse2D.Double(headX - s * 0.13, headY - s * 0.10, s * 0.26, s * 0.20));

        // Horns
        g2.setColor(HORN);
        Polygon horn = new Polygon();
        horn.addPoint(headX, (int) (headY - s * 0.10));
        horn.addPoint((int) (headX - s * 0.05), (int) (headY - s * 0.24));
        horn.addPoint((int) (headX + s * 0.03), (int) (headY - s * 0.09));
        g2.fillPolygon(horn);

        // Eye
        g2.setColor(EYE);
        g2.fillOval((int) (headX + dir * s * 0.02), (int) (headY - s * 0.02), 6, 6);
    }
}
