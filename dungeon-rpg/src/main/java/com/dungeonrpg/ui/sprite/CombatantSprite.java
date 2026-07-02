package com.dungeonrpg.ui.sprite;

import java.awt.Graphics2D;

/**
 * CombatantSprite is a STRATEGY, the exact same pattern already used for
 * Ability and AIStrategy — one interface, one method, many interchangeable
 * implementations. CombatantPortrait (the animated component that hosts a
 * sprite) doesn't know or care whether it's drawing a Warrior or a Dragon;
 * it just calls paint() and trusts the sprite to know its own shape.
 *
 * All sprites are pure vector drawing (Java2D shapes only, no image
 * files) — this avoids any external asset/licensing question entirely
 * and keeps the whole game a single, self-contained Java project with
 * nothing to download or bundle.
 */
public interface CombatantSprite {

    /**
     * @param g2      graphics context to draw into
     * @param cx      horizontal center of the sprite
     * @param cy      vertical center of the sprite (roughly torso height)
     * @param scale   overall size in pixels (sprites should scale their
     *                internal proportions off this, not hardcode pixels)
     * @param bob     a small vertical offset driven by the idle animation
     *                (breathing/hovering motion), already computed by
     *                the caller — the sprite just applies it
     * @param facesLeft true if this sprite should face left (used so
     *                  hero and enemy sprites face each other rather
     *                  than both facing the same direction)
     */
    void paint(Graphics2D g2, int cx, int cy, int scale, double bob, boolean facesLeft);
}
