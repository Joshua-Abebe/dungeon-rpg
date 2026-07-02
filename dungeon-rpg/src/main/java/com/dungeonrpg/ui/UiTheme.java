package com.dungeonrpg.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * UiTheme centralizes every color and font used across the game's Swing
 * screens. Before this class existed, MapPanel and CombatPanel each
 * defined their own Color constants independently — which is exactly how
 * a UI ends up with three slightly different shades of "dark brown"
 * scattered across files. Pulling it into one class is REUSE applied to
 * design, not just code: change a color here and every screen updates
 * consistently.
 */
public final class UiTheme {

    private UiTheme() {
        // Never instantiated — this class is a namespace for constants.
    }

    public static final Color BG_DARKEST = new Color(18, 16, 15);
    public static final Color BG_PANEL = new Color(28, 25, 22);
    public static final Color BG_PANEL_RAISED = new Color(38, 34, 30);

    public static final Color WALL = new Color(40, 36, 32);
    public static final Color WALL_SHADOW = new Color(26, 23, 20);
    public static final Color FLOOR_LIGHT = new Color(196, 181, 154);
    public static final Color FLOOR_DARK = new Color(180, 164, 137);
    public static final Color STAIRS = new Color(219, 172, 52);
    public static final Color STAIRS_DARK = new Color(163, 125, 32);
    public static final Color CHEST_WOOD = new Color(122, 78, 42);
    public static final Color CHEST_METAL = new Color(200, 172, 92);

    public static final Color HERO = new Color(70, 130, 210);
    public static final Color HERO_DARK = new Color(38, 82, 148);
    public static final Color ENEMY = new Color(196, 62, 62);
    public static final Color ENEMY_DARK = new Color(132, 34, 34);

    public static final Color HP_HIGH = new Color(96, 178, 92);
    public static final Color HP_MID = new Color(219, 172, 52);
    public static final Color HP_LOW = new Color(196, 62, 62);

    public static final Color TEXT_PRIMARY = new Color(238, 232, 220);
    public static final Color TEXT_MUTED = new Color(168, 158, 142);

    public static final Color ACCENT = new Color(200, 150, 60);
    public static final Color ACCENT_HOVER = new Color(220, 170, 80);

    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_LOG = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);
}
