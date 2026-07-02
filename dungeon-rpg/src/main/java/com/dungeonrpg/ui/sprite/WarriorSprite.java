package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/** A stocky armored figure with a shield and a raised sword. */
public class WarriorSprite implements CombatantSprite {

    private static final Color ARMOR = new Color(120, 130, 145);
    private static final Color ARMOR_DARK = new Color(80, 88, 100);
    private static final Color SKIN = new Color(224, 186, 150);
    private static final Color STEEL = new Color(206, 212, 220);
    private static final Color SHIELD = new Color(150, 60, 50);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int dir = facesLeft ? -1 : 1;
        int y = cy + (int) bob;

        // Legs
        g2.setColor(ARMOR_DARK);
        g2.fillRoundRect(cx - scale / 6, y + scale / 8, scale / 8, scale / 4, 4, 4);
        g2.fillRoundRect(cx + scale / 12, y + scale / 8, scale / 8, scale / 4, 4, 4);

        // Torso
        g2.setColor(ARMOR);
        g2.fill(new RoundRectangle2D.Double(cx - scale * 0.22, y - scale * 0.18,
            scale * 0.44, scale * 0.38, scale * 0.12, scale * 0.12));

        // Shield (off-hand side)
        g2.setColor(SHIELD);
        g2.fillOval(cx - dir * (int) (scale * 0.34) - scale / 10, y - scale / 10, scale / 5, scale / 3);
        g2.setColor(ARMOR_DARK);
        g2.drawOval(cx - dir * (int) (scale * 0.34) - scale / 10, y - scale / 10, scale / 5, scale / 3);

        // Sword arm raised on the facing side
        g2.setColor(STEEL);
        g2.setStroke(new BasicStroke(Math.max(2f, scale * 0.05f)));
        int handX = cx + dir * scale / 3;
        int handY = y - scale / 4;
        g2.drawLine(cx + dir * scale / 8, y - scale / 10, handX, handY);
        g2.drawLine(handX, handY, handX + dir * scale / 10, handY - scale / 3);

        // Head
        g2.setColor(SKIN);
        g2.fill(new Ellipse2D.Double(cx - scale * 0.11, y - scale * 0.40, scale * 0.22, scale * 0.22));
        g2.setColor(STEEL);
        g2.fillArc((int) (cx - scale * 0.13), (int) (y - scale * 0.42), (int) (scale * 0.26), (int) (scale * 0.18), 0, 180);
    }
}
