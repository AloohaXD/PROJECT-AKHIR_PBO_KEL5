package tile;

import java.awt.image.BufferedImage;

/**
 * Tile — data container for a single tile type.
 *
 * Fields:
 *  - image:     the rendered appearance of the tile
 *  - collision: if true, entities cannot walk through this tile
 *
 * To add animated tiles (e.g., water): replace `image` with `BufferedImage[]`
 * and add an animation frame counter in TileManager.
 */
public class Tile {
    public BufferedImage image;
    public boolean       collision = false; // Solid by default: false (walkable)
}
