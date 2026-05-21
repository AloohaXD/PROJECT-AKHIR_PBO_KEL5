package tile;
import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * TileManager — BUG FIX: Chapter 6 was using lava (solid) as ground making map unpassable.
 * All chapters now have a walkable ground tile; only obstacles/borders are solid.
 */
public class TileManager {
    private final GamePanel gp;
    public Tile[] tiles;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[10];
        mapTileNum = new int[GamePanel.WORLD_ROWS][GamePanel.WORLD_COLS];
        setupTiles();
        loadMap(1);
    }

    private void setupTiles() {
        tiles[0] = makeTile(genGrass(),     false); // walkable grass
        tiles[1] = makeTile(genWall(),      true);  // solid wall
        tiles[2] = makeTile(genPath(),      false); // walkable path
        tiles[3] = makeTile(genWater(),     true);  // solid water
        tiles[4] = makeTile(genDarkGrass(), false); // walkable dark grass
        tiles[5] = makeTile(genVolcanic(),  false); // walkable volcanic (NOT solid - ch6 fix!)
        tiles[6] = makeTile(genSnow(),      false); // walkable snow
        tiles[7] = makeTile(genSand(),      false); // walkable sand
        tiles[8] = makeTile(genLavaRock(),  true);  // solid lava rock (obstacle only)
        tiles[9] = makeTile(genIce(),       false); // walkable ice
    }

    private Tile makeTile(BufferedImage img, boolean col) {
        Tile t = new Tile(); t.image = img; t.collision = col; return t;
    }

    /** BUG FIX: Each chapter uses a walkable ground tile (0,2,4,5,6,7,9).
     *  Only borders and scattered rocks use solid tiles (1,3,8). */
    public void loadMap(int chapter) {
        // Ground tile (always walkable)
        int ground = switch (chapter) {
            case 1      -> 0; // grass
            case 2      -> 4; // dark grass
            case 3      -> 4; // dark grass (cave-like)
            case 4      -> 2; // stone path (ruins)
            case 5      -> 7; // sand/swamp
            case 6      -> 5; // volcanic rock (walkable!) — BUG FIX
            case 7      -> 1; // dark stone — but we need walkable, use path=2
            case 8      -> 9; // ice
            case 9      -> 6; // snow
            default     -> 4; // demon realm — dark grass
        };
        // For ch7 ground was tile 1 (solid) — fix to use path (2)
        if (chapter == 7) ground = 2;

        // Obstacle tile (solid)
        int obstacle = switch (chapter) {
            case 5  -> 3; // water
            case 6  -> 8; // lava rock (now only as obstacles, not ground)
            default -> 1; // wall
        };

        Random rng = new Random(chapter * 99991L);

        // Fill with walkable ground
        for (int r = 0; r < GamePanel.WORLD_ROWS; r++)
            for (int c = 0; c < GamePanel.WORLD_COLS; c++)
                mapTileNum[r][c] = ground;

        // Solid border
        for (int c = 0; c < GamePanel.WORLD_COLS; c++) {
            mapTileNum[0][c] = obstacle;
            mapTileNum[GamePanel.WORLD_ROWS-1][c] = obstacle;
        }
        for (int r = 0; r < GamePanel.WORLD_ROWS; r++) {
            mapTileNum[r][0] = obstacle;
            mapTileNum[r][GamePanel.WORLD_COLS-1] = obstacle;
        }

        // Scattered obstacle clusters
        int clusters = 20 + chapter * 2;
        for (int i = 0; i < clusters; i++) {
            int r = rng.nextInt(GamePanel.WORLD_ROWS - 6) + 3;
            int c = rng.nextInt(GamePanel.WORLD_COLS - 6) + 3;
            int size = rng.nextInt(2) + 1;
            for (int dr = 0; dr <= size; dr++)
                for (int dc = 0; dc <= size; dc++)
                    if (r+dr < GamePanel.WORLD_ROWS-1 && c+dc < GamePanel.WORLD_COLS-1)
                        mapTileNum[r+dr][c+dc] = obstacle;
        }

        // Path cross through center
        int mr = GamePanel.WORLD_ROWS / 2;
        int mc = GamePanel.WORLD_COLS / 2;
        for (int c = 1; c < GamePanel.WORLD_COLS-1; c++) mapTileNum[mr][c] = 2;
        for (int r = 1; r < GamePanel.WORLD_ROWS-1; r++) mapTileNum[r][mc] = 2;

        // Always clear spawn area (top-left)
        for (int r = 1; r < 10; r++)
            for (int c = 1; c < 15; c++)
                mapTileNum[r][c] = ground;
    }

    public void draw(Graphics2D g2) {
        int startCol = Math.max(0, gp.camera.offsetX / gp.TILE_SIZE);
        int startRow = Math.max(0, gp.camera.offsetY / gp.TILE_SIZE);
        int endCol = Math.min(gp.WORLD_COLS-1, (gp.camera.offsetX + gp.SCREEN_WIDTH)  / gp.TILE_SIZE + 1);
        int endRow = Math.min(gp.WORLD_ROWS-1, (gp.camera.offsetY + gp.SCREEN_HEIGHT) / gp.TILE_SIZE + 1);
        for (int r = startRow; r <= endRow; r++)
            for (int c = startCol; c <= endCol; c++) {
                int idx = mapTileNum[r][c];
                if (idx < 0 || idx >= tiles.length || tiles[idx] == null) continue;
                int sx = gp.camera.toScreenX(c * gp.TILE_SIZE);
                int sy = gp.camera.toScreenY(r * gp.TILE_SIZE);
                g2.drawImage(tiles[idx].image, sx, sy, gp.TILE_SIZE, gp.TILE_SIZE, null);
            }
    }

    // ── Tile generators ────────────────────────────────────────
    private BufferedImage genGrass()     { return gen(new Color(68,120,40), new Color(55,100,32), new Color(80,140,50)); }
    private BufferedImage genWall()      { return genSolid(new Color(90,85,80), new Color(70,65,60)); }
    private BufferedImage genPath()      { return gen(new Color(150,130,90), new Color(140,120,80), new Color(160,140,100)); }
    private BufferedImage genWater()     { return gen(new Color(30,80,160),  new Color(20,60,130), new Color(40,100,190)); }
    private BufferedImage genDarkGrass() { return gen(new Color(40,80,30),   new Color(30,60,22),  new Color(50,95,38)); }
    private BufferedImage genVolcanic()  { return gen(new Color(100,60,40),  new Color(80,45,30),  new Color(120,75,50)); } // BUG FIX: walkable dark rock
    private BufferedImage genSnow()      { return gen(new Color(230,240,255),new Color(210,225,245),new Color(245,250,255)); }
    private BufferedImage genSand()      { return gen(new Color(210,185,110),new Color(200,175,100),new Color(220,195,120)); }
    private BufferedImage genLavaRock()  { return gen(new Color(180,50,10),  new Color(150,30,5),  new Color(200,70,20)); } // solid obstacle
    private BufferedImage genIce()       { return gen(new Color(180,220,240),new Color(160,200,225),new Color(200,235,255)); }

    private BufferedImage gen(Color base, Color dark, Color light) {
        int ts = GamePanel.TILE_SIZE;
        BufferedImage img = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(base);  g.fillRect(0, 0, ts, ts);
        g.setColor(dark);  g.fillRect(0, 0, ts/2, ts/2); g.fillRect(ts/2, ts/2, ts/2, ts/2);
        g.setColor(light); g.fillRect(ts/2, 0, ts/2, ts/2); g.fillRect(0, ts/2, ts/2, ts/2);
        g.setColor(base.darker()); g.drawRect(0, 0, ts-1, ts-1);
        g.dispose(); return img;
    }
    private BufferedImage genSolid(Color base, Color dark) {
        int ts = GamePanel.TILE_SIZE;
        BufferedImage img = new BufferedImage(ts, ts, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(base); g.fillRect(0, 0, ts, ts);
        g.setColor(dark); g.fillRect(0, 0, ts/2, ts/2); g.fillRect(ts/2, ts/2, ts/2, ts/2);
        g.setColor(new Color(110,105,100)); g.drawRect(0, 0, ts-1, ts-1);
        g.dispose(); return img;
    }
}
