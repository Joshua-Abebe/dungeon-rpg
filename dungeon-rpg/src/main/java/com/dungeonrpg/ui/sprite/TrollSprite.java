package com.dungeonrpg.ui.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/** A bulky, heavy figure — bigger than every other combatant except Dragon. */
public class TrollSprite implements CombatantSprite {

    private static final Color SKIN = new Color(120, 110, 90);
    private static final Color SKIN_DARK = new Color(84, 76, 60);
    private static final Color CLUB = new Color(90, 66, 40);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        int s = (int) (scale * 1.15); // trolls read as noticeably larger
        int y = cy + (int) bob;

        // Big torso
        g2.setColor(SKIN);
        g2.fill(new RoundRectangle2D.Double(cx - s * 0.30, y - s * 0.16,
            s * 0.60, s * 0.42, s * 0.14, s * 0.14));

        // Thick arm holding a big club
        g2.setColor(SKIN_DARK);
        g2.fillRoundRect(cx + dir * (int) (s * 0.20), y - s / 10, (int) (s * 0.18), (int) (s * 0.30), 8, 8);
        g2.setColor(CLUB);
        int clubX = cx + dir * (int) (s * 0.36);
        g2.fillRoundRect(clubX - (int) (s * 0.07), y - (int) (s * 0.44),
            (int) (s * 0.14), (int) (s * 0.30), 6, 6);

        // Small head relative to the body (emphasizes bulk)
        g2.setColor(SKIN);
        g2.fill(new Ellipse2D.Double(cx - s * 0.11, y - s * 0.38, s * 0.22, s * 0.20));
        g2.setColor(SKIN_DARK);
        g2.fillOval((int) (cx - s * 0.06), (int) (y - s * 0.30), 5, 5);
        g2.fillOval((int) (cx + s * 0.02), (int) (y - s * 0.30), 5, 5);
    }
}
