package entity;

import main.GamePanel;
import main.KeyHandler;
import ui.HudRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Player.java  (DIPERBARUI)
 * ============================================================
 * Perubahan dari versi sebelumnya:
 *
 *  1. Integrasi PlayerStats  → HP, Mana, Attack, Level, EXP
 *     sekarang dikelola oleh objek PlayerStats (bukan field biasa)
 *
 *  2. Sistem EXP & Level Up  → gainExp(amount) memicu level up
 *     jika EXP cukup dan menampilkan notifikasi lewat HudRenderer
 *
 *  3. Label Level di bawah sprite → drawLevelLabel() dipanggil di draw()
 *
 *  4. Tombol [I] untuk membuka Inventory → KeyHandler sudah mendukung
 *
 *  5. Tombol [Q] untuk ganti quest yang ditampilkan di HUD
 * ============================================================
 */
public class Player extends Entity {

    // ─── Dependencies ─────────────────────────────────────────
    private final KeyHandler keyHandler;

    // ─── Statistik Pemain ─────────────────────────────────────
    // PlayerStats menyimpan semua stat: HP, Mana, Attack, Level, EXP
    private final PlayerStats stats;

    // ─── Referensi HUD ────────────────────────────────────────
    // HudRenderer dibutuhkan untuk memicu notifikasi Level Up
    private HudRenderer hudRenderer;

    // ─── Posisi di Layar (selalu di tengah) ───────────────────
    public final int screenX;
    public final int screenY;

    // ─── Entity Terdekat untuk Interaksi ─────────────────────
    public Entity nearbyEntity = null;

    // ─── State Serangan ───────────────────────────────────────
    private boolean isAttacking     = false;
    private int     attackTimer     = 0;
    private static final int ATTACK_DURATION = 15; // frame
    private static final int ATTACK_RANGE    = (int)(GamePanel.TILE_SIZE * 1.4);

    // Efek visual slash
    private float slashAngle = 0f;
    private float slashAlpha = 0f;

    // ─── Warna Label Level ────────────────────────────────────
    // Warna label level player: biru langit
    private static final Color LEVEL_LABEL_COLOR = new Color(100, 180, 255);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public Player(GamePanel gp, KeyHandler keyHandler) {
        super(gp);
        this.keyHandler = keyHandler;
        this.stats      = new PlayerStats(); // Level 1, stat default
        this.type       = EntityType.PLAYER;

        // Posisi awal di dunia
        worldX = GamePanel.TILE_SIZE * 5;
        worldY = GamePanel.TILE_SIZE * 5;
        width  = GamePanel.TILE_SIZE;
        height = GamePanel.TILE_SIZE;

        // Hitbox solid (sedikit lebih kecil dari sprite agar terasa pas)
        solidArea        = new Rectangle(10, 26, 28, 18);
        solidAreaDefault = new Rectangle(solidArea);

        speed = 4;

        // Selalu berada di tengah layar
        screenX = GamePanel.SCREEN_WIDTH  / 2 - (width  / 2);
        screenY = GamePanel.SCREEN_HEIGHT / 2 - (height / 2);

        generatePlaceholderSprites();
    }

    /**
     * Set referensi ke HudRenderer.
     * Dipanggil oleh GamePanel setelah HudRenderer dibuat.
     */
    public void setHudRenderer(HudRenderer hud) {
        this.hudRenderer = hud;
    }

    // =========================================================
    // SISTEM EXP & LEVEL UP
    // =========================================================

    /**
     * Memberikan EXP kepada pemain.
     * Jika EXP cukup, otomatis naik level dan tampilkan notifikasi.
     *
     * @param amount Jumlah EXP yang diterima
     */
    public void gainExp(int amount) {
        boolean leveledUp = stats.addExp(amount);
        if (leveledUp && hudRenderer != null) {
            // Tampilkan notifikasi level up di layar
            hudRenderer.triggerLevelUpNotification(stats.getLevel());
        }
    }

    // =========================================================
    // UPDATE (dipanggil setiap frame oleh game loop)
    // =========================================================
    @Override
    public void update() {
        // Jangan proses input saat dialog atau combat
        if (gp.gameState == GamePanel.GameState.DIALOG) {
            handleDialogInput();
            keyHandler.clearOneShots();
            return;
        }
        if (gp.gameState == GamePanel.GameState.COMBAT
         || gp.gameState == GamePanel.GameState.TRANSITION) {
            keyHandler.clearOneShots();
            return;
        }

        // ─── Tombol Inventory [I] ─────────────────────────────
        if (keyHandler.inventoryJustPressed) {
            // TODO: Buka panel inventory
            // Saat ini hanya log ke console — ganti dengan UI inventory nanti
            System.out.println("[INFO] Inventory dibuka oleh pemain.");
            // Contoh untuk nanti: gp.gameState = GamePanel.GameState.INVENTORY;
        }

        // ─── Tombol Ganti Quest [Q] ───────────────────────────
        if (keyHandler.questJustPressed) {
            if (gp.getQuestTracker() != null) {
                gp.getQuestTracker().nextQuest();
            }
        }

        // ─── Attack State ─────────────────────────────────────
        if (isAttacking) {
            attackTimer++;
            slashAlpha = 1f - (float) attackTimer / ATTACK_DURATION;
            if (attackTimer >= ATTACK_DURATION) {
                isAttacking = false;
                attackTimer = 0;
                slashAlpha  = 0f;
            }
        }

        // Trigger serangan
        if (keyHandler.attackJustPressed && !isAttacking) {
            isAttacking = true;
            attackTimer = 0;

            // Sudut slash sesuai arah hadap
            slashAngle = switch (direction) {
                case "up"    -> -90f;
                case "left"  -> 180f;
                case "right" -> 0f;
                default      ->  90f;
            };

            // Cek musuh dalam jangkauan
            int hitIndex = gp.collision.checkEntityAttackRange(this, ATTACK_RANGE);
            if (hitIndex >= 0 && gp.entities.get(hitIndex) instanceof EnemyEntity enemy) {
                if (enemy.isAlive) {
                    gp.startCombatTransition(hitIndex);
                }
            }
        }

        // ─── Gerakan 360° ─────────────────────────────────────
        boolean moving = false;
        float vx = 0, vy = 0;

        if (keyHandler.upPressed)    vy -= 1;
        if (keyHandler.downPressed)  vy += 1;
        if (keyHandler.leftPressed)  vx -= 1;
        if (keyHandler.rightPressed) vx += 1;

        if (vx != 0 || vy != 0) {
            moving = true;

            // Normalisasi agar kecepatan diagonal tetap konsisten
            float len = (float) Math.sqrt(vx * vx + vy * vy);
            vx = vx / len * speed;
            vy = vy / len * speed;

            // Update arah hadap untuk sprite & interaksi
            if (Math.abs(vx) >= Math.abs(vy)) {
                direction = (vx > 0) ? "right" : "left";
            } else {
                direction = (vy > 0) ? "down" : "up";
            }

            // Collision check X
            collisionOn = false;
            gp.collision.checkTile(this, (int) vx, 0);
            gp.collision.checkEntity(this, false);
            if (!collisionOn) worldX += (int) vx;

            // Collision check Y
            collisionOn = false;
            gp.collision.checkTile(this, 0, (int) vy);
            gp.collision.checkEntity(this, false);
            if (!collisionOn) worldY += (int) vy;
        }

        // Deteksi entity terdekat
        int entityIndex = gp.collision.checkEntityInteraction(this);
        nearbyEntity = (entityIndex >= 0) ? gp.entities.get(entityIndex) : null;

        // Trigger dialog
        if (keyHandler.interactJustPressed && nearbyEntity instanceof NPC) {
            gp.gameState = GamePanel.GameState.DIALOG;
        }

        advanceAnimation(moving);
        keyHandler.clearOneShots();
    }

    private void handleDialogInput() {
        if (keyHandler.interactJustPressed) {
            if (nearbyEntity instanceof NPC npc) {
                if (!npc.advanceDialog()) {
                    gp.gameState = GamePanel.GameState.EXPLORATION;
                    npc.resetDialog();
                }
            }
        }
        if (keyHandler.escJustPressed) {
            gp.gameState = GamePanel.GameState.EXPLORATION;
            if (nearbyEntity instanceof NPC npc) npc.resetDialog();
        }
    }

    // =========================================================
    // DRAW (dipanggil setiap frame oleh game loop)
    // =========================================================
    @Override
    public void draw(Graphics2D g2) {
        if (!gp.camera.isVisible(worldX, worldY, width, height)) return;

        int drawX = gp.camera.toScreenX(worldX);
        int drawY = gp.camera.toScreenY(worldY);

        boolean isMoving = keyHandler.upPressed || keyHandler.downPressed
                        || keyHandler.leftPressed || keyHandler.rightPressed;
        BufferedImage frame = advanceAnimation(isMoving);

        if (frame != null) g2.drawImage(frame, drawX, drawY, width, height, null);

        // Gambar efek slash saat menyerang
        if (isAttacking && slashAlpha > 0f) {
            drawSlash(g2, drawX, drawY);
        }

        // ─── Label Level di bawah sprite ──────────────────────
        // Dipanggil lewat HudRenderer agar konsisten dengan musuh
        if (hudRenderer != null) {
            hudRenderer.drawEntityLevelLabel(
                g2, drawX, drawY, width, height,
                stats.getLevel(), LEVEL_LABEL_COLOR
            );
        }
    }

    /** Gambar efek slash 3 garis saat menyerang. */
    private void drawSlash(Graphics2D g2, int drawX, int drawY) {
        int cx = drawX + width  / 2;
        int cy = drawY + height / 2;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, slashAlpha));
        float rad = (float) Math.toRadians(slashAngle);

        for (int i = -1; i <= 1; i++) {
            float offset = i * 6;
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);

            int x1 = (int)(cx + cos * 10 - sin * (offset - 14));
            int y1 = (int)(cy + sin * 10 + cos * (offset - 14));
            int x2 = (int)(cx + cos * 38 - sin * (offset + 14));
            int y2 = (int)(cy + sin * 38 + cos * (offset + 14));

            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 255, 100));
            g2.drawLine(x1, y1, x2, y2);
            g2.setStroke(old);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // =========================================================
    // GETTER
    // =========================================================

    /** Akses ke PlayerStats untuk HUD, combat sync, dll. */
    public PlayerStats getStats() { return stats; }

    // =========================================================
    // SPRITE GENERATION (placeholder pixel art)
    // =========================================================
    private void generatePlaceholderSprites() {
        Color bodyColor   = new Color(100, 149, 237); // Biru langit
        Color darkColor   = bodyColor.darker();
        Color shadowColor = new Color(0, 0, 0, 60);

        for (int i = 0; i < 4; i++) {
            float bob = (i % 2 == 0) ? 0 : -3;
            walkDown[i]  = createPlayerSprite(bodyColor, darkColor, shadowColor, "down",  bob, i);
            walkUp[i]    = createPlayerSprite(bodyColor, darkColor, shadowColor, "up",    bob, i);
            walkLeft[i]  = createPlayerSprite(bodyColor, darkColor, shadowColor, "left",  bob, i);
            walkRight[i] = createPlayerSprite(bodyColor, darkColor, shadowColor, "right", bob, i);
        }
        idleImage = walkDown[0];
    }

    private BufferedImage createPlayerSprite(Color body, Color dark, Color shadow,
                                              String dir, float bob, int frame) {
        int w = GamePanel.TILE_SIZE, h = GamePanel.TILE_SIZE;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int baseY = (int)(h * 0.1f + bob);

        g.setColor(shadow);
        g.fillOval(w / 2 - 10, h - 10, 20, 8);

        g.setColor(body);
        g.fillRoundRect(w / 2 - 9, baseY + 14, 18, 20, 6, 6);

        g.setColor(new Color(255, 220, 180));
        g.fillOval(w / 2 - 8, baseY, 16, 16);

        g.setColor(dark);
        switch (dir) {
            case "down"  -> { g.fillOval(w/2-5, baseY+5, 3, 4); g.fillOval(w/2+2, baseY+5, 3, 4); }
            case "left"  -> g.fillOval(w/2-7, baseY+5, 3, 4);
            case "right" -> g.fillOval(w/2+4, baseY+5, 3, 4);
            default      -> {}
        }

        int legSwing = (frame % 2 == 0) ? 3 : -3;
        g.fillRoundRect(w/2 - 7 + legSwing, baseY + 30, 7, 12, 3, 3);
        g.fillRoundRect(w/2      - legSwing, baseY + 30, 7, 12, 3, 3);

        g.dispose();
        return img;
    }
}