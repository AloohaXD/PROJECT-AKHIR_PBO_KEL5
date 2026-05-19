package entity;

import main.GamePanel;
import ui.HudRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * EnemyEntity.java  (DIPERBARUI)
 * ============================================================
 * Perubahan dari versi sebelumnya:
 *
 *  1. Memiliki level sendiri → ditampilkan di bawah sprite
 *  2. Memberikan EXP reward ke player saat dikalahkan
 *  3. Label level di bawah sprite menggunakan HudRenderer
 *  4. EXP reward dihitung berdasarkan level musuh
 * ============================================================
 */
public class EnemyEntity extends Entity {

    // ─── State ───────────────────────────────────────────────
    public boolean isAlive = true;
    public boolean isEnemy = true;

    // ─── Level & Reward ──────────────────────────────────────
    private final int enemyLevel;       // Level musuh ini
    private final int expReward;        // EXP yang diberikan saat dikalahkan

    // ─── Visual ──────────────────────────────────────────────
    private float   pulseTimer  = 0f;
    private float   alertAlpha  = 0f;
    private boolean nearPlayer  = false;

    // Referensi HudRenderer (di-set oleh GamePanel)
    private HudRenderer hudRenderer;

    // Warna musuh
    private static final Color BODY_COLOR = new Color(180, 40,  40);
    private static final Color DARK_COLOR = new Color(100, 20,  20);
    private static final Color GLOW_COLOR = new Color(255, 80,  80, 60);

    // Warna label level musuh: merah
    private static final Color LEVEL_LABEL_COLOR = new Color(255, 120, 100);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public EnemyEntity(GamePanel gp) {
        this(gp, 1); // Default: Level 1
    }

    /**
     * Constructor dengan level tertentu.
     * EXP reward dihitung otomatis berdasarkan level.
     *
     * @param gp    Referensi GamePanel
     * @param level Level musuh (menentukan kekuatan & EXP reward)
     */
    public EnemyEntity(GamePanel gp, int level) {
        super(gp);
        this.type         = EntityType.NPC; // Masuk ke list entities
        this.interactable = false;
        this.speed        = 0;
        this.enemyLevel   = Math.max(1, level);

        // EXP reward: makin tinggi level, makin banyak EXP
        // Formula: 20 * level + 10
        this.expReward = 20 * this.enemyLevel + 10;

        width  = GamePanel.TILE_SIZE;
        height = GamePanel.TILE_SIZE;
        solidArea        = new Rectangle(8, 16, 32, 28);
        solidAreaDefault = new Rectangle(solidArea);

        generateSprites();
    }

    /** Set referensi HudRenderer untuk menggambar label level. */
    public void setHudRenderer(HudRenderer hud) {
        this.hudRenderer = hud;
    }

    // =========================================================
    // GETTER
    // =========================================================
    public int getEnemyLevel() { return enemyLevel; }
    public int getExpReward()  { return expReward; }

    // =========================================================
    // UPDATE
    // =========================================================
    @Override
    public void update() {
        if (!isAlive) return;

        pulseTimer += 0.05f;

        // Cek jarak ke player
        int dx = Math.abs((gp.player.worldX + gp.player.width  / 2) - (worldX + width  / 2));
        int dy = Math.abs((gp.player.worldY + gp.player.height / 2) - (worldY + height / 2));
        nearPlayer = (dx < GamePanel.TILE_SIZE * 3 && dy < GamePanel.TILE_SIZE * 3);

        // Fade in/out alert
        if (nearPlayer && alertAlpha < 1f)       alertAlpha = Math.min(1f, alertAlpha + 0.06f);
        else if (!nearPlayer && alertAlpha > 0f) alertAlpha = Math.max(0f, alertAlpha - 0.06f);
    }

    // =========================================================
    // DRAW
    // =========================================================
    @Override
    public void draw(Graphics2D g2) {
        if (!isAlive) return;
        if (!gp.camera.isVisible(worldX, worldY, width, height)) return;

        int sx = gp.camera.toScreenX(worldX);
        int sy = gp.camera.toScreenY(worldY);

        // Glow pulsing saat dekat player
        if (alertAlpha > 0f) {
            float pulse = (float)(0.5 + 0.5 * Math.sin(pulseTimer * 3));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alertAlpha * pulse * 0.5f));
            g2.setColor(GLOW_COLOR);
            int glowR = (int)(20 * alertAlpha);
            g2.fillOval(sx - glowR, sy - glowR, width + glowR * 2, height + glowR * 2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Gambar sprite musuh
        BufferedImage frame = walkDown[0];
        if (frame != null) g2.drawImage(frame, sx, sy, width, height, null);

        // Label "[ X ] SERANG" saat player dekat
        if (alertAlpha > 0f) drawEnemyLabel(g2, sx, sy);

        // ─── Label Level di bawah sprite ──────────────────────
        if (hudRenderer != null) {
            hudRenderer.drawEntityLevelLabel(
                g2, sx, sy, width, height,
                enemyLevel, LEVEL_LABEL_COLOR
            );
        }
    }

    private void drawEnemyLabel(Graphics2D g2, int sx, int sy) {
        float bob = (float) Math.sin(pulseTimer * 2) * 3f;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alertAlpha));
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        String text = "[ X ] SERANG";
        int tw = fm.stringWidth(text);
        int tx = sx + width / 2 - tw / 2;
        int ty = (int)(sy - 10 + bob);

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(tx - 4, ty - fm.getAscent() - 2, tw + 8, fm.getHeight() + 4, 6, 6);
        g2.setColor(new Color(255, 100, 100));
        g2.drawString(text, tx, ty);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // =========================================================
    // SPRITE GENERATION
    // =========================================================
    private void generateSprites() {
        for (int i = 0; i < 4; i++) {
            walkDown[i] = walkUp[i] = walkLeft[i] = walkRight[i] = createEnemySprite(i);
        }
        idleImage = walkDown[0];
    }

    private BufferedImage createEnemySprite(int frame) {
        int w = GamePanel.TILE_SIZE;
        int h = GamePanel.TILE_SIZE;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Warna sedikit berbeda berdasarkan level untuk variasi visual
        Color bodyVariant = enemyLevel >= 3
            ? new Color(140, 30, 160)   // Ungu untuk musuh high-level
            : BODY_COLOR;               // Merah untuk musuh biasa

        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(w / 2 - 10, h - 10, 20, 8);

        // Body
        g.setColor(bodyVariant);
        g.fillRoundRect(w / 2 - 11, 14, 22, 22, 4, 4);

        // Bahu / tanduk
        g.setColor(DARK_COLOR);
        g.fillRect(w / 2 - 14, 16, 6, 8);
        g.fillRect(w / 2 + 8,  16, 6, 8);

        // Kepala
        g.setColor(new Color(200, 70, 70));
        g.fillOval(w / 2 - 9, 1, 18, 17);

        // Mata kuning
        g.setColor(new Color(255, 220, 50));
        g.fillOval(w / 2 - 6, 6, 5, 5);
        g.fillOval(w / 2 + 1, 6, 5, 5);

        // Pupil merah
        g.setColor(new Color(255, 50, 50));
        g.fillOval(w / 2 - 5, 7, 3, 3);
        g.fillOval(w / 2 + 2, 7, 3, 3);

        // Kaki
        g.setColor(DARK_COLOR);
        g.fillRoundRect(w/2 - 8, 32, 7, 10, 3, 3);
        g.fillRoundRect(w/2 + 1, 32, 7, 10, 3, 3);

        g.dispose();
        return img;
    }
}