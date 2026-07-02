package com.dungeonrpg.ui.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** A bone-white figure with a visible ribcage and hollow eyes. */
public class SkeletonSprite implements CombatantSprite {

    private static final Color BONE = new Color(226, 220, 200);
    private static final Color BONE_SHADOW = new Color(170, 164, 146);
    private static final Color EYE_SOCKET = new Color(20, 18, 16);

    @Override
    public void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft) {
        int y = cy + (int) bob;

        // Rib cage: an oval outline with horizontal rib lines
        g2.setColor(BONE);
        g2.fill(new Ellipse2D.Double(cx - scale * 0.20, y - scale * 0.18, scale * 0.40, scale * 0.38));
        g2.setColor(BONE_SHADOW);
        g2.setStroke(new BasicStroke(Math.max(1.5f, scale * 0.025f)));
        for (int i = 0; i < 3; i++) {
            int ribY = (int) (y - scale * 0.10 + i * scale * 0.10);
            g2.drawLine((int) (cx - scale * 0.14), ribY, (int) (cx + scale * 0.14), ribY);
        }

        // Limb bones (thin lines)
        g2.setColor(BONE);
        g2.setStroke(new BasicStroke(Math.max(2f, scale * 0.035f)));
        g2.drawLine(cx - scale / 5, y + scale / 5, cx - scale / 8, y + scale * 4 / 10);
        g2.drawLine(cx + scale / 5, y + scale / 5, cx + scale / 8, y + scale * 4 / 10);

        // Skull with dark hollow eye sockets
        g2.setColor(BONE);
        g2.fill(new Ellipse2D.Double(cx - scale * 0.13, y - scale * 0.42, scale * 0.26, scale * 0.24));
        g2.setColor(EYE_SOCKET);
        g2.fillOval((int) (cx - scale * 0.08), (int) (y - scale * 0.34), 6, 7);
        g2.fillOval((int) (cx + scale * 0.02), (int) (y - scale * 0.34), 6, 7);
    }
}
