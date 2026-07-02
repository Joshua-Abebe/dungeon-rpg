package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Polygon;

/** A small, hunched, aggressive figure — the weakest but scrappiest enemy. */
public class GoblinSprite implements CombatantSprite {

    private static final Color SKIN = new Color(110, 150, 70);
    private static final Color SKIN_DARK = new Color(78, 110, 46);
    private static final Color CLUB = new Color(110, 80, 50);
    private static final Color EYE = new Color(230, 40, 40);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        // Goblins are small — scale everything down and hunch the body low.
        int s = (int) (scale * 0.78);
        int y = cy + (int) bob + scale / 8;

        g2.setColor(SKIN);
        g2.fill(new Ellipse2D.Double(cx - s * 0.24, y - s * 0.10, s * 0.48, s * 0.34));

        // Crude club
        g2.setColor(CLUB);
        g2.setStroke(new BasicStroke(Math.max(2f, s * 0.06f)));
        g2.drawLine(cx + dir * s / 6, y, cx + dir * s * 4 / 10, y - s / 3);
        g2.fillOval(cx + dir * s * 4 / 10 - 4, y - s / 3 - 4, 9, 9);

        // Head with pointed ears
        g2.setColor(SKIN);
        g2.fill(new Ellipse2D.Double(cx - s * 0.16, y - s * 0.36, s * 0.32, s * 0.28));
        g2.setColor(SKIN_DARK);
        Polygon earL = new Polygon();
        earL.addPoint((int) (cx - s * 0.16), (int) (y - s * 0.28));
        earL.addPoint((int) (cx - s * 0.30), (int) (y - s * 0.34));
        earL.addPoint((int) (cx - s * 0.14), (int) (y - s * 0.18));
        g2.fillPolygon(earL);
        Polygon earR = new Polygon();
        earR.addPoint((int) (cx + s * 0.16), (int) (y - s * 0.28));
        earR.addPoint((int) (cx + s * 0.30), (int) (y - s * 0.34));
        earR.addPoint((int) (cx + s * 0.14), (int) (y - s * 0.18));
        g2.fillPolygon(earR);

        g2.setColor(EYE);
        g2.fillOval((int) (cx - s * 0.07), (int) (y - s * 0.26), 5, 5);
        g2.fillOval((int) (cx + s * 0.02), (int) (y - s * 0.26), 5, 5);
    }
}
