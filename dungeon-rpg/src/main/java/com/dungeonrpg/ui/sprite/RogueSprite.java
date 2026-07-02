package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Polygon;
import java.awt.geom.RoundRectangle2D;

/** A slim hooded figure with twin daggers, built for speed over bulk. */
public class RogueSprite implements CombatantSprite {

    private static final Color CLOAK = new Color(48, 62, 52);
    private static final Color CLOAK_DARK = new Color(30, 40, 34);
    private static final Color SKIN = new Color(206, 168, 132);
    private static final Color BLADE = new Color(210, 214, 220);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        int y = cy + (int) bob;

        // Narrow torso, slimmer than Warrior's
        g2.setColor(CLOAK);
        g2.fill(new RoundRectangle2D.Double(cx - scale * 0.16, y - scale * 0.16,
            scale * 0.32, scale * 0.36, scale * 0.10, scale * 0.10));

        // Twin daggers, one forward one back
        g2.setColor(BLADE);
        g2.setStroke(new BasicStroke(Math.max(1.5f, scale * 0.03f)));
        g2.drawLine(cx + dir * scale / 6, y - scale / 12, cx + dir * scale * 4 / 10, y - scale / 4);
        g2.drawLine(cx - dir * scale / 8, y, cx - dir * scale / 3, y + scale / 10);

        // Hooded head: a dark triangle over an ellipse of shadowed face
        g2.setColor(CLOAK_DARK);
        Polygon hood = new Polygon();
        hood.addPoint(cx, (int) (y - scale * 0.62));
        hood.addPoint((int) (cx - scale * 0.14), (int) (y - scale * 0.30));
        hood.addPoint((int) (cx + scale * 0.14), (int) (y - scale * 0.30));
        g2.fillPolygon(hood);

        g2.setColor(SKIN.darker());
        g2.fill(new Ellipse2D.Double(cx - scale * 0.09, y - scale * 0.40, scale * 0.18, scale * 0.16));
    }
}
