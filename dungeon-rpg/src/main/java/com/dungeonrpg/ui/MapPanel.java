package com.dungeonrpg.ui;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.map.GameMap;
import com.dungeonrpg.map.Tile;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.Polygon;

/**
 * MapPanel renders the dungeon grid. Same responsibility as before (pure
 * view over GameMap, no game logic) but tokens are now class-aware
 * faces instead of plain colored circles — a Warrior, Mage, and Rogue
 * (and every enemy type) each render as a visually distinct badge, not
 * an interchangeable dot. MapPanel now needs a Hero reference (not just
 * the GameMap) specifically to know which face to draw for the hero's
 * token — the GameMap itself only tracks position, not identity.
 */
public class MapPanel extends JPanel {

    private static final int TILE_SIZE = 44;

    private final GameMap map;
    private final Hero hero;

    public MapPanel(GameMap map, Hero hero) {
        this.map = map;
        this.hero = hero;
        setPreferredSize(new Dimension(map.getWidth() * TILE_SIZE, map.getHeight() * TILE_SIZE));
        setBackground(UiTheme.BG_DARKEST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                drawTile(g2, map.getTile(row, col));
            }
        }

        map.getEnemyPlacements().forEach(placement ->
            drawFaceToken(g2, placement.getRow(), placement.getCol(), placement.getEnemy(), false));

        drawFaceToken(g2, map.getHeroRow(), map.getHeroCol(), hero, true);

        drawImminentThreatWarnings(g2);
        drawTorchlight(g2);
    }

    /**
     * Highlights any enemy tile exactly one step from the hero with a
     * pulsing red ring — a beat of tension before a collision actually
     * happens, similar to a "vision cone" cue in stealth games: the
     * player gets a clear visual signal that the NEXT move could start
     * a fight, without the game silently deciding for them.
     */
    private void drawImminentThreatWarnings(Graphics2D g2) {
        int heroRow = map.getHeroRow();
        int heroCol = map.getHeroCol();
        long pulse = System.currentTimeMillis() % 900;
        float alpha = 0.35f + 0.35f * (float) Math.abs(Math.sin(pulse / 900.0 * Math.PI));

        for (var placement : map.getEnemyPlacements()) {
            int rowDist = Math.abs(placement.getRow() - heroRow);
            int colDist = Math.abs(placement.getCol() - heroCol);
            if (Math.max(rowDist, colDist) != 1) {
                continue; // only warn one step away, not the whole aggro radius
            }
            int x = placement.getCol() * TILE_SIZE;
            int y = placement.getRow() * TILE_SIZE;
            g2.setColor(new Color(220, 40, 40, (int) (alpha * 255)));
            g2.setStroke(new java.awt.BasicStroke(3f));
            g2.drawRoundRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4, 10, 10);
        }
    }

    private void drawTile(Graphics2D g2, Tile tile) {
        int x = tile.getCol() * TILE_SIZE;
        int y = tile.getRow() * TILE_SIZE;
        boolean checker = (tile.getRow() + tile.getCol()) % 2 == 0;

        switch (tile.getType()) {
            case WALL -> drawWall(g2, x, y);
            case FLOOR -> drawFloor(g2, x, y, checker);
            case STAIRS -> {
                drawFloor(g2, x, y, checker);
                drawStairsIcon(g2, x, y);
            }
            case CHEST -> {
                drawFloor(g2, x, y, checker);
                drawChestIcon(g2, x, y);
            }
        }
    }

    private void drawFloor(Graphics2D g2, int x, int y, boolean checker) {
        Color base = checker ? UiTheme.FLOOR_LIGHT : UiTheme.FLOOR_DARK;
        GradientPaint gradient = new GradientPaint(
            x, y, base.brighter(), x, y + TILE_SIZE, base.darker());
        g2.setPaint(gradient);
        g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        g2.setColor(new Color(0, 0, 0, 18));
        g2.drawRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawWall(Graphics2D g2, int x, int y) {
        GradientPaint gradient = new GradientPaint(
            x, y, UiTheme.WALL, x, y + TILE_SIZE, UiTheme.WALL_SHADOW);
        g2.setPaint(gradient);
        g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        // Beveled edge: a light line top-left, dark line bottom-right,
        // to give the block a slight embossed, load-bearing look instead
        // of reading as a flat void.
        g2.setColor(new Color(255, 255, 255, 20));
        g2.drawLine(x, y, x + TILE_SIZE, y);
        g2.drawLine(x, y, x, y + TILE_SIZE);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawLine(x + TILE_SIZE, y, x + TILE_SIZE, y + TILE_SIZE);
        g2.drawLine(x, y + TILE_SIZE, x + TILE_SIZE, y + TILE_SIZE);
    }

    private void drawStairsIcon(Graphics2D g2, int x, int y) {
        int steps = 4;
        int stepH = TILE_SIZE / steps;

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8, 6, 6);

        for (int i = 0; i < steps; i++) {
            int stepY = y + TILE_SIZE - (i + 1) * stepH;
            int stepW = TILE_SIZE - (i * TILE_SIZE / (steps * 2));
            int stepX = x + (TILE_SIZE - stepW) / 2;
            g2.setColor(i % 2 == 0 ? UiTheme.STAIRS : UiTheme.STAIRS_DARK);
            g2.fillRect(stepX, stepY, stepW, stepH - 1);
        }
    }

    private void drawChestIcon(Graphics2D g2, int x, int y) {
        int pad = 9;
        int bodyX = x + pad;
        int bodyY = y + pad + 4;
        int bodyW = TILE_SIZE - pad * 2;
        int bodyH = TILE_SIZE - pad * 2 - 4;

        g2.setColor(UiTheme.CHEST_WOOD);
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 4, 4);
        g2.setColor(UiTheme.CHEST_WOOD.darker());
        g2.fillRect(bodyX, bodyY, bodyW, 5);

        g2.setColor(UiTheme.CHEST_METAL);
        g2.fillRect(bodyX + bodyW / 2 - 2, bodyY, 4, bodyH);
        g2.fillOval(bodyX + bodyW / 2 - 3, bodyY + bodyH / 2 - 3, 6, 6);
    }

    /**
     * Draws one character's map token: a rounded-square colored badge
     * (blue family for the hero, red/warm family for enemies — same
     * faction-color language as the reference image) with a small,
     * bold face on top whose FEATURES differ per class. This is what
     * makes a Warrior, Mage, and Rogue readable apart on the map even
     * though they're all "the hero" — same badge family, different
     * face — and the same principle separates Goblin/Skeleton/Troll/
     * Dinosaur from each other.
     */
    private void drawFaceToken(Graphics2D g2, int row, int col, Character character, boolean isHero) {
        int pad = 6;
        int x = col * TILE_SIZE + pad;
        int y = row * TILE_SIZE + pad;
        int size = TILE_SIZE - (pad * 2);

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(x + 2, y + size - 5, size - 4, 7, 6, 6);

        String type = character.getClass().getSimpleName();
        Color badge = badgeColorFor(type, isHero);
        GradientPaint gradient = new GradientPaint(x, y, badge.brighter(), x + size, y + size, badge.darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(x, y, size, size, 12, 12);
        g2.setColor(badge.darker().darker());
        g2.drawRoundRect(x, y, size, size, 12, 12);

        drawFace(g2, type, x, y, size);
    }

    private Color badgeColorFor(String type, boolean isHero) {
        if (isHero) {
            return switch (type) {
                case "Mage" -> new Color(90, 70, 150);
                case "Rogue" -> new Color(50, 110, 100);
                default -> UiTheme.HERO; // Warrior
            };
        }
        return switch (type) {
            case "Goblin" -> new Color(150, 60, 40);
            case "Skeleton" -> new Color(120, 110, 100);
            case "Troll" -> new Color(120, 50, 40);
            case "Dinosaur" -> new Color(160, 30, 30);
            default -> UiTheme.ENEMY;
        };
    }

    /** Dispatches to a per-class face — this is the actual visual differentiator between otherwise same-colored badges. */
    private void drawFace(Graphics2D g2, String type, int x, int y, int size) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        Color white = new Color(250, 248, 240);
        Color dark = new Color(30, 26, 22);

        switch (type) {
            case "Warrior" -> {
                eyes(g2, cx, cy, size, white, dark, false);
                g2.setColor(dark);
                g2.drawLine(cx - size / 5, cy + size / 6, cx + size / 5, cy + size / 6); // stern flat mouth
                g2.setColor(new Color(200, 206, 214));
                g2.drawArc(cx - size / 3, y + 2, size * 2 / 3, size / 3, 0, 180); // helmet brow
            }
            case "Mage" -> {
                eyes(g2, cx, cy, size, white, new Color(140, 200, 255), true);
                g2.setColor(dark);
                g2.fillOval(cx - size / 14, cy + size / 8, size / 7, size / 7); // small "o" mouth
                g2.setColor(new Color(230, 230, 255));
                Polygon hat = new Polygon();
                hat.addPoint(cx, y - 2);
                hat.addPoint(cx - size / 5, y + size / 4);
                hat.addPoint(cx + size / 5, y + size / 4);
                g2.fillPolygon(hat);
            }
            case "Rogue" -> {
                g2.setColor(dark);
                g2.fillRect(cx - size / 3, cy - size / 10, size * 2 / 3, size / 8); // eye mask band
                g2.setColor(white);
                g2.fillOval(cx - size / 6, cy - size / 12, size / 10, size / 10);
                g2.fillOval(cx + size / 16, cy - size / 12, size / 10, size / 10);
                g2.setColor(dark);
                g2.drawArc(cx - size / 6, cy + size / 8, size / 3, size / 6, 200, 140); // smirk
            }
            case "Goblin" -> {
                eyes(g2, cx, cy, size, new Color(230, 230, 80), dark, false);
                g2.setColor(white);
                Polygon fangs = new Polygon();
                fangs.addPoint(cx - size / 8, cy + size / 10);
                fangs.addPoint(cx - size / 16, cy + size / 4);
                fangs.addPoint(cx, cy + size / 10);
                g2.fillPolygon(fangs);
                Polygon fangs2 = new Polygon();
                fangs2.addPoint(cx + size / 8, cy + size / 10);
                fangs2.addPoint(cx + size / 16, cy + size / 4);
                fangs2.addPoint(cx, cy + size / 10);
                g2.fillPolygon(fangs2);
            }
            case "Skeleton" -> {
                g2.setColor(dark);
                g2.fillOval(cx - size / 4, cy - size / 8, size / 6, size / 5);
                g2.fillOval(cx + size / 14, cy - size / 8, size / 6, size / 5);
                g2.drawLine(cx - size / 6, cy + size / 6, cx + size / 6, cy + size / 6);
                for (int i = -1; i <= 1; i++) {
                    g2.drawLine(cx + i * size / 10, cy + size / 6, cx + i * size / 10, cy + size / 4);
                }
            }
            case "Troll" -> {
                eyes(g2, cx, cy, size, white, dark, false);
                g2.setColor(white);
                Polygon tuskL = new Polygon();
                tuskL.addPoint(cx - size / 6, cy + size / 8);
                tuskL.addPoint(cx - size / 4, cy + size / 3);
                tuskL.addPoint(cx - size / 10, cy + size / 6);
                g2.fillPolygon(tuskL);
                Polygon tuskR = new Polygon();
                tuskR.addPoint(cx + size / 6, cy + size / 8);
                tuskR.addPoint(cx + size / 4, cy + size / 3);
                tuskR.addPoint(cx + size / 10, cy + size / 6);
                g2.fillPolygon(tuskR);
            }
            case "Dinosaur" -> {
                eyes(g2, cx, cy, size, new Color(255, 210, 60), dark, false);
                g2.setColor(new Color(230, 220, 200));
                Polygon horn = new Polygon();
                horn.addPoint(cx, y - 3);
                horn.addPoint(cx - size / 7, y + size / 6);
                horn.addPoint(cx + size / 7, y + size / 6);
                g2.fillPolygon(horn);
                g2.setColor(dark);
                Polygon jaw = new Polygon();
                for (int i = 0; i < 4; i++) {
                    int tx = cx - size / 4 + i * size / 6;
                    jaw.addPoint(tx, cy + size / 8);
                    jaw.addPoint(tx + size / 14, cy + size / 4);
                    jaw.addPoint(tx + size / 8, cy + size / 8);
                }
                g2.fillPolygon(jaw);
            }
            default -> eyes(g2, cx, cy, size, white, dark, false);
        }
    }

    private void eyes(Graphics2D g2, int cx, int cy, int size, Color scleraColor, Color pupilColor, boolean big) {
        int eyeSize = big ? size / 4 : size / 5;
        int offsetX = size / 6;
        int eyeY = cy - size / 10;

        g2.setColor(scleraColor);
        g2.fillOval(cx - offsetX - eyeSize / 2, eyeY - eyeSize / 2, eyeSize, eyeSize);
        g2.fillOval(cx + offsetX - eyeSize / 2, eyeY - eyeSize / 2, eyeSize, eyeSize);
        g2.setColor(pupilColor);
        int pupilSize = eyeSize / 2;
        g2.fillOval(cx - offsetX - pupilSize / 2, eyeY - pupilSize / 2, pupilSize, pupilSize);
        g2.fillOval(cx + offsetX - pupilSize / 2, eyeY - pupilSize / 2, pupilSize, pupilSize);
    }

    /**
     * A soft radial gradient of translucent black, centered on the hero,
     * that darkens the far edges of the map. Purely a lighting effect —
     * it doesn't hide anything gameplay-relevant, it just gives the room
     * depth instead of looking like a flat, evenly-lit grid.
     */
    private void drawTorchlight(Graphics2D g2) {
        int centerX = map.getHeroCol() * TILE_SIZE + TILE_SIZE / 2;
        int centerY = map.getHeroRow() * TILE_SIZE + TILE_SIZE / 2;
        float radius = TILE_SIZE * 9f;

        if (radius <= 0) {
            return;
        }

        RadialGradientPaint vignette = new RadialGradientPaint(
            new Point2D.Float(centerX, centerY), radius,
            new float[] { 0f, 0.65f, 1f },
            new Color[] {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 65)
            });
        g2.setPaint(vignette);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}

