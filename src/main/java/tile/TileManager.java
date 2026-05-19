package tile;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * TileManager — manages the tile palette and the world map.
 *
 * Tile Types (extend this list as needed):
 *   0 = Grass       (walkable)
 *   1 = Wall/Stone  (solid, blocks movement)
 *   2 = Path        (walkable, different colour)
 *   3 = Water       (solid, visual variety)
 *   4 = Dark Grass  (walkable, encounter zone — future use)
 *
 * The world map is a 2D integer array where each cell references a tile type.
 * This makes the map easy to edit and scales well to larger worlds.
 *
 * Loading external tilesets:
 *   Replace the generated BufferedImages in setupTiles() with:
 *     tiles[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/grass.png"));
 */
public class TileManager {

    private final GamePanel gp;

    /** The tile palette — index maps to tile type number. */
    public Tile[] tiles;

    /** The world map — mapTileNum[row][col] = tile type at that position. */
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;

        tiles       = new Tile[16]; // Space for 16 tile types
        mapTileNum  = new int[GamePanel.WORLD_ROWS][GamePanel.WORLD_COLS];

        setupTiles();
        loadMap();
    }

    // ─── Tile Definitions ─────────────────────────────────────────────────────

    private void setupTiles() {
        // Tile 0: Grass (walkable)
        tiles[0]           = new Tile();
        tiles[0].image     = generateGrassTile();
        tiles[0].collision = false;

        // Tile 1: Stone Wall (solid)
        tiles[1]           = new Tile();
        tiles[1].image     = generateWallTile();
        tiles[1].collision = true;

        // Tile 2: Stone Path (walkable)
        tiles[2]           = new Tile();
        tiles[2].image     = generatePathTile();
        tiles[2].collision = false;

        // Tile 3: Water (solid)
        tiles[3]           = new Tile();
        tiles[3].image     = generateWaterTile();
        tiles[3].collision = true;

        // Tile 4: Dark Grass / Encounter Zone (walkable)
        tiles[4]           = new Tile();
        tiles[4].image     = generateDarkGrassTile();
        tiles[4].collision = false;
    }

    // ─── Map Layout ───────────────────────────────────────────────────────────

    /**
     * Defines the world map using a 2D integer array.
     *
     * Key:
     *   0 = Grass
     *   1 = Wall
     *   2 = Path
     *   3 = Water
     *   4 = Dark Grass
     *
     * The map is 40×40 tiles. Tiles outside the defined area default to 0.
     * Edit this array to design your world layout.
     */
    private void loadMap() {
        // Abbreviated map definition — 40×40 (0 = grass, 1 = wall, 2 = path, 3 = water, 4 = dark grass)
        // fmt:off
        int[][] layout = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,0,0,0,0,0,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,0,0,0,0,0,2,0,0,0,0,0,2,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,2,0,0,0,0,0,2,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,2,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,2,2,2,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,3,3,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,3,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        };
        // fmt:on

        // Copy layout into mapTileNum (handles size mismatches safely)
        for (int row = 0; row < Math.min(layout.length, GamePanel.WORLD_ROWS); row++) {
            for (int col = 0; col < Math.min(layout[row].length, GamePanel.WORLD_COLS); col++) {
                mapTileNum[row][col] = layout[row][col];
            }
        }
    }

    // ─── Rendering ────────────────────────────────────────────────────────────

    /**
     * Draws all visible tiles to the screen.
     *
     * Optimisation: Only tiles within the camera's visible range are drawn.
     * This culls up to 75% of draw calls on a 40×40 map viewed through a
     * 20×15 viewport.
     */
    public void draw(Graphics2D g2) {
        // Determine the range of tile columns and rows visible through the camera
        int startCol = Math.max(0, gp.camera.offsetX / gp.TILE_SIZE);
        int startRow = Math.max(0, gp.camera.offsetY / gp.TILE_SIZE);
        int endCol   = Math.min(gp.WORLD_COLS - 1, startCol + gp.SCREEN_COLS + 1);
        int endRow   = Math.min(gp.WORLD_ROWS - 1, startRow + gp.SCREEN_ROWS + 1);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                int tileNum = mapTileNum[row][col];

                // World position of this tile's top-left corner
                int worldX = col * gp.TILE_SIZE;
                int worldY = row * gp.TILE_SIZE;

                // Convert to screen space
                int screenX = gp.camera.toScreenX(worldX);
                int screenY = gp.camera.toScreenY(worldY);

                // Draw the tile image (if defined)
                if (tiles[tileNum] != null && tiles[tileNum].image != null) {
                    g2.drawImage(tiles[tileNum].image, screenX, screenY,
                        gp.TILE_SIZE, gp.TILE_SIZE, null);
                }
            }
        }
    }

    // ─── Procedural Tile Art ─────────────────────────────────────────────────
    // These methods generate pixel-art-style tile textures programmatically.
    // Replace with ImageIO.read() calls to use external tilesets.

    private BufferedImage generateGrassTile() {
        return generateTile(g -> {
            // Base green
            g.setColor(new Color(80, 150, 70));
            g.fillRect(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);

            // Subtle texture — lighter patches
            g.setColor(new Color(95, 168, 84, 120));
            g.fillRect(4, 6, 8, 6);
            g.fillRect(20, 2, 6, 8);
            g.fillRect(36, 14, 7, 5);
            g.fillRect(12, 30, 10, 6);
            g.fillRect(30, 36, 8, 8);

            // Tiny grass blades
            g.setColor(new Color(60, 180, 50, 180));
            for (int[] blade : new int[][]{{8,10},{22,8},{38,18},{16,34},{32,40}}) {
                g.fillRect(blade[0], blade[1], 2, 4);
            }
        });
    }

    private BufferedImage generateWallTile() {
        return generateTile(g -> {
            // Dark grey stone base
            g.setColor(new Color(80, 80, 90));
            g.fillRect(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);

            // Brick rows
            g.setColor(new Color(60, 60, 70));
            int brickH = 12;
            for (int row = 0; row < gp.TILE_SIZE; row += brickH) {
                int offset = ((row / brickH) % 2 == 0) ? 0 : gp.TILE_SIZE / 2;
                int brickW = 22;
                for (int col = -brickW; col < gp.TILE_SIZE; col += brickW) {
                    g.drawRect(col + offset, row, brickW, brickH);
                }
            }

            // Top highlight for depth
            g.setColor(new Color(120, 120, 130));
            g.fillRect(0, 0, gp.TILE_SIZE, 3);
        });
    }

    private BufferedImage generatePathTile() {
        return generateTile(g -> {
            // Sandy beige base
            g.setColor(new Color(180, 160, 120));
            g.fillRect(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);

            // Stone slab lines
            g.setColor(new Color(140, 120, 90, 150));
            g.drawLine(0, 16, gp.TILE_SIZE, 16);
            g.drawLine(0, 32, gp.TILE_SIZE, 32);
            g.drawLine(24, 0, 24, 16);
            g.drawLine(12, 16, 12, 32);
            g.drawLine(30, 32, 30, gp.TILE_SIZE);

            // Slight grunge
            g.setColor(new Color(100, 90, 70, 60));
            g.fillRect(5, 5, 3, 3);
            g.fillRect(28, 20, 3, 3);
            g.fillRect(40, 38, 4, 4);
        });
    }

    private BufferedImage generateWaterTile() {
        return generateTile(g -> {
            // Deep blue base
            g.setColor(new Color(30, 80, 180));
            g.fillRect(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);

            // Wave highlights
            g.setColor(new Color(60, 120, 220, 200));
            for (int y = 6; y < gp.TILE_SIZE; y += 10) {
                for (int x = 0; x < gp.TILE_SIZE - 8; x += 14) {
                    g.fillArc(x, y, 12, 5, 0, 180);
                }
            }

            // Surface glint
            g.setColor(new Color(180, 220, 255, 120));
            g.fillOval(8, 4, 12, 4);
            g.fillOval(30, 18, 10, 3);
        });
    }

    private BufferedImage generateDarkGrassTile() {
        return generateTile(g -> {
            // Darker, moody green
            g.setColor(new Color(50, 100, 45));
            g.fillRect(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);

            // Denser blades
            g.setColor(new Color(35, 140, 35, 200));
            for (int[] blade : new int[][]{{4,8},{12,14},{22,6},{30,20},{40,12},{8,32},{18,26},{36,38},{44,30}}) {
                if (blade[0] < gp.TILE_SIZE && blade[1] < gp.TILE_SIZE) {
                    g.fillRect(blade[0], blade[1], 2, 6);
                }
            }

            // Subtle purple tint (hint of mystery)
            g.setColor(new Color(80, 40, 120, 40));
            g.fillRect(0, 0, gp.TILE_SIZE, gp.TILE_SIZE);
        });
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /** Creates a TILE_SIZE × TILE_SIZE BufferedImage and runs the painter lambda. */
    private BufferedImage generateTile(java.util.function.Consumer<Graphics2D> painter) {
        int size = gp.TILE_SIZE;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        painter.accept(g);
        g.dispose();
        return img;
    }
}
