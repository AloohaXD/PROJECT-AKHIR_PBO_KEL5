package main;

import entity.Entity;

/**
 * Camera — follows the player and clamps to map boundaries.
 *
 * The camera stores an offset (offsetX, offsetY) representing how many
 * pixels the world has been shifted to keep the player centred on screen.
 *
 * World-to-screen conversion:
 *   screenX = entity.worldX - camera.offsetX
 *   screenY = entity.worldY - camera.offsetY
 *
 * Clamping ensures the camera never shows empty space beyond the map edges.
 */
public class Camera {

    private final GamePanel gp;

    /** World-space pixel offset applied to all draw calls. */
    public int offsetX;
    public int offsetY;

    public Camera(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Recomputes the camera offset to centre on the given entity (the player).
     * Called once per game tick in GamePanel.update().
     *
     * @param target the entity the camera should follow
     */
    public void update(Entity target) {
        // Ideal offset: place the target's centre at the screen centre
        int idealX = target.worldX + (target.width  / 2) - (gp.SCREEN_WIDTH  / 2);
        int idealY = target.worldY + (target.height / 2) - (gp.SCREEN_HEIGHT / 2);

        // Clamp so the camera never scrolls past the map edges
        offsetX = clamp(idealX, 0, gp.WORLD_WIDTH  - gp.SCREEN_WIDTH);
        offsetY = clamp(idealY, 0, gp.WORLD_HEIGHT - gp.SCREEN_HEIGHT);
    }

    /**
     * Converts a world-space X coordinate to screen-space X.
     * Convenience method used in all draw() calls.
     */
    public int toScreenX(int worldX) {
        return worldX - offsetX;
    }

    /**
     * Converts a world-space Y coordinate to screen-space Y.
     */
    public int toScreenY(int worldY) {
        return worldY - offsetY;
    }

    /**
     * Returns true if a world-space rectangle is visible on screen.
     * Used to cull off-screen draw calls and improve performance.
     *
     * @param worldX  left edge of the object in world space
     * @param worldY  top edge of the object in world space
     * @param w       width of the object
     * @param h       height of the object
     */
    public boolean isVisible(int worldX, int worldY, int w, int h) {
        int sx = toScreenX(worldX);
        int sy = toScreenY(worldY);
        return sx + w > 0 && sx < gp.SCREEN_WIDTH
            && sy + h > 0 && sy < gp.SCREEN_HEIGHT;
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
