package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.Polygon;

/** A robed spellcaster with a pointed hat and a glowing staff orb. */
public class MageSprite implements CombatantSprite {

    private static final Color ROBE = new Color(90, 70, 150);
    private static final Color ROBE_DARK = new Color(60, 46, 108);
    private static final Color SKIN = new Color(224, 186, 150);
    private static final Color STAFF = new Color(120, 84, 50);
    private static final Color GLOW = new Color(140, 200, 255);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        int y = cy + (int) bob;

        // Robe: wide triangular base for a flowing look
        Polygon robe = new Polygon();
        robe.addPoint(cx, (int) (y - scale * 0.20));
        robe.addPoint((int) (cx - scale * 0.28), (int) (y + scale * 0.32));
        robe.addPoint((int) (cx + scale * 0.28), (int) (y + scale * 0.32));
        g2.setColor(ROBE);
        g2.fillPolygon(robe);
        g2.setColor(ROBE_DARK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(robe);

        // Staff with a glowing orb, held on the facing side
        int staffX = cx + dir * scale / 3;
        g2.setColor(STAFF);
        g2.setStroke(new BasicStroke(Math.max(2f, scale * 0.04f)));
        g2.drawLine(staffX, y + scale / 4, staffX, y - scale / 3);

        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(staffX, y - scale / 3), scale * 0.12f,
            new float[] { 0f, 1f },
            new Color[] { GLOW, new Color(GLOW.getRed(), GLOW.getGreen(), GLOW.getBlue(), 0) });
        g2.setPaint(glow);
        g2.fillOval((int) (staffX - scale * 0.09), (int) (y - scale / 3 - scale * 0.09),
            (int) (scale * 0.18), (int) (scale * 0.18));

        // Head + pointed hat
        g2.setColor(SKIN);
        g2.fill(new Ellipse2D.Double(cx - scale * 0.10, y - scale * 0.42, scale * 0.20, scale * 0.20));
        Polygon hat = new Polygon();
        hat.addPoint(cx, (int) (y - scale * 0.68));
        hat.addPoint((int) (cx - scale * 0.15), (int) (y - scale * 0.36));
        hat.addPoint((int) (cx + scale * 0.15), (int) (y - scale * 0.36));
        g2.setColor(ROBE);
        g2.fillPolygon(hat);
    }
}
