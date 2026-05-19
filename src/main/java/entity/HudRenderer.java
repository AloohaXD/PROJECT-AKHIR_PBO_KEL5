package ui;

import entity.Player;
import entity.PlayerStats;
import main.GamePanel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * HudRenderer.java
 * ============================================================
 * Renderer HUD (Head-Up Display) untuk mode eksplorasi.
 *
 * Bertanggung jawab menggambar:
 *  1. Panel Profil Pemain  → pojok kiri atas (nama, level, avatar)
 *  2. HP Bar               → berwarna merah/hijau
 *  3. Mana Bar             → berwarna biru
 *  4. EXP Bar              → berwarna kuning, di bawah Mana
 *  5. Ikon Inventory       → bisa diklik, pojok kanan bawah
 *  6. Quest Tracker        → pojok kanan atas
 *  7. Level Label Entity   → tepat di bawah sprite player/musuh
 *  8. Notifikasi Level Up  → muncul sementara saat naik level
 *
 * Semua rendering menggunakan Graphics2D murni (tanpa Swing component)
 * agar kompatibel dengan game loop berbasis Canvas/JPanel.
 * ============================================================
 */
public class HudRenderer {

    // ─── Referensi ────────────────────────────────────────────
    private final GamePanel gp;

    // ─── Konstanta Warna & Font ───────────────────────────────
    // Panel profil
    private static final Color PANEL_BG      = new Color(10, 8, 25, 210);
    private static final Color PANEL_BORDER  = new Color(120, 100, 180, 180);
    private static final Color GOLD          = new Color(255, 200, 50);
    private static final Color TEXT_WHITE    = new Color(240, 235, 255);
    private static final Color TEXT_DIM      = new Color(150, 140, 170);

    // Bar warna
    private static final Color HP_BG         = new Color(50, 20, 20);
    private static final Color HP_FG_HIGH    = new Color(60, 210, 80);
    private static final Color HP_FG_MED     = new Color(220, 180, 0);
    private static final Color HP_FG_LOW     = new Color(220, 50, 50);
    private static final Color MANA_BG       = new Color(15, 20, 50);
    private static final Color MANA_FG       = new Color(60, 130, 255);
    private static final Color EXP_BG        = new Color(30, 25, 10);
    private static final Color EXP_FG        = new Color(255, 200, 30);

    // Quest tracker
    private static final Color QUEST_BG      = new Color(8, 6, 20, 200);
    private static final Color QUEST_BORDER  = new Color(80, 70, 130, 160);
    private static final Color QUEST_TITLE   = new Color(200, 180, 255);
    private static final Color QUEST_DONE    = new Color(80, 220, 100);

    // Inventory
    private static final Color INV_BG        = new Color(20, 15, 40, 200);
    private static final Color INV_BORDER    = new Color(140, 110, 60, 200);
    private static final Color INV_HOVER     = new Color(255, 200, 80, 80);

    // Level up notif
    private static final Color LEVELUP_COLOR = new Color(255, 220, 50);

    // ─── State Notifikasi Level Up ────────────────────────────
    private int   levelUpTimer  = 0;          // Frame timer notifikasi
    private static final int LEVELUP_DURATION = 180; // 3 detik @ 60fps
    private int   lastShownLevel = 1;

    // ─── State Hover Inventory ────────────────────────────────
    private boolean inventoryHovered = false;

    // ─── Area Klik Inventory (untuk MouseListener) ────────────
    public Rectangle inventoryClickArea;

    // ─── Font ─────────────────────────────────────────────────
    private final Font fontName    = new Font("Serif", Font.BOLD, 14);
    private final Font fontLevel   = new Font("Monospaced", Font.BOLD, 11);
    private final Font fontBar     = new Font("Monospaced", Font.PLAIN, 10);
    private final Font fontQuest   = new Font("SansSerif", Font.BOLD, 11);
    private final Font fontInv     = new Font("Serif", Font.BOLD, 20);
    private final Font fontLevelUp = new Font("Serif", Font.BOLD, 28);
    private final Font fontLvLabel = new Font("Monospaced", Font.BOLD, 10);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public HudRenderer(GamePanel gp) {
        this.gp = gp;
        // Inisialisasi area klik inventory (posisi awal, diperbarui tiap draw)
        inventoryClickArea = new Rectangle(
            GamePanel.SCREEN_WIDTH - 70, GamePanel.SCREEN_HEIGHT - 70, 56, 56
        );
    }

    // =========================================================
    // ENTRY POINT: Gambar seluruh HUD
    // =========================================================
    /**
     * Dipanggil dari GamePanel.paintComponent() setiap frame.
     * Menggambar semua elemen HUD di atas layar eksplorasi.
     *
     * @param g2    Graphics2D dari game panel
     * @param player Entitas player untuk data stat dan posisi
     */
    public void draw(Graphics2D g2, Player player) {
        PlayerStats stats = player.getStats();

        drawProfilePanel(g2, stats);
        drawQuestTracker(g2);
        drawInventoryButton(g2);
        drawLevelUpNotification(g2, stats);
    }

    /**
     * Gambar label Level tepat di bawah sprite entitas.
     * Dipanggil dari Player.draw() dan EnemyEntity.draw().
     *
     * @param g2     Graphics2D
     * @param screenX posisi X screen dari entitas
     * @param screenY posisi Y screen dari entitas
     * @param width  lebar sprite
     * @param height tinggi sprite
     * @param level  level yang ingin ditampilkan
     * @param color  warna teks level (biru untuk player, merah untuk musuh)
     */
    public void drawEntityLevelLabel(Graphics2D g2, int screenX, int screenY,
                                      int width, int height, int level, Color color) {
        String text = "Lv." + level;
        g2.setFont(fontLvLabel);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);

        // Posisi: di bawah sprite, tengah secara horizontal
        int tx = screenX + width / 2 - textW / 2;
        int ty = screenY + height + fm.getAscent() + 2;

        // Background hitam semi-transparan agar teks terbaca di atas tile apapun
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(tx - 3, ty - fm.getAscent() - 1, textW + 6, fm.getHeight() + 2, 4, 4);

        // Teks level
        g2.setColor(color);
        g2.drawString(text, tx, ty);
    }

    // =========================================================
    // PANEL PROFIL (pojok kiri atas)
    // =========================================================
    private void drawProfilePanel(Graphics2D g2, PlayerStats stats) {
        // Dimensi panel
        int panelX = 10, panelY = 10;
        int panelW = 210, panelH = 110;

        // Background panel
        drawPanel(g2, panelX, panelY, panelW, panelH, PANEL_BG, PANEL_BORDER);

        // ─── Avatar Player ────────────────────────────────────
        int avatarSize = 44;
        int avatarX    = panelX + 8;
        int avatarY    = panelY + 8;
        drawPlayerAvatar(g2, avatarX, avatarY, avatarSize, stats.getLevel());

        // ─── Nama & Level ─────────────────────────────────────
        int textX = avatarX + avatarSize + 8;
        int textY = panelY + 22;

        g2.setFont(fontName);
        g2.setColor(GOLD);
        g2.drawString("Chromatic Hero", textX, textY);

        g2.setFont(fontLevel);
        g2.setColor(TEXT_WHITE);
        g2.drawString("Level " + stats.getLevel(), textX, textY + 14);

        // ─── HP Bar ───────────────────────────────────────────
        int barX = textX;
        int barW = panelW - textX - panelX - 8;

        drawBar(g2, barX, textY + 22, barW, 10,
                stats.getHpPercent(), HP_BG, getHpColor(stats.getHpPercent()),
                "HP " + stats.getCurrentHp() + "/" + stats.getMaxHp());

        // ─── Mana Bar ─────────────────────────────────────────
        drawBar(g2, barX, textY + 36, barW, 10,
                stats.getManaPercent(), MANA_BG, MANA_FG,
                "MP " + stats.getCurrentMana() + "/" + stats.getMaxMana());

        // ─── EXP Bar ──────────────────────────────────────────
        drawBar(g2, barX, textY + 50, barW, 8,
                stats.getExpPercent(), EXP_BG, EXP_FG,
                "EXP " + stats.getCurrentExp() + "/" + stats.getExpToNextLevel());

        // ─── ATK Stat ─────────────────────────────────────────
        g2.setFont(fontBar);
        g2.setColor(new Color(255, 140, 80));
        g2.drawString("ATK: " + stats.getAttack(), textX, textY + 76);
    }

    /**
     * Menggambar avatar pixel art sederhana untuk profile panel.
     * Lingkaran dengan warna yang berubah seiring level.
     */
    private void drawPlayerAvatar(Graphics2D g2, int x, int y, int size, int level) {
        // Border avatar (warna berubah berdasarkan level)
        Color borderColor = switch (level) {
            case 1, 2     -> new Color(100, 180, 120);
            case 3, 4     -> new Color(80, 150, 255);
            case 5, 6, 7  -> new Color(180, 80, 255);
            default        -> new Color(255, 180, 50);
        };

        // Background avatar
        g2.setColor(new Color(30, 25, 60));
        g2.fillRoundRect(x, y, size, size, 8, 8);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, size, size, 8, 8);
        g2.setStroke(new BasicStroke(1f));

        // Gambar wajah karakter pixel art kecil
        int cx = x + size / 2;
        int cy = y + size / 2 - 2;

        // Tubuh
        g2.setColor(new Color(100, 149, 237));
        g2.fillRoundRect(cx - 9, cy + 6, 18, 14, 4, 4);

        // Wajah
        g2.setColor(new Color(255, 220, 180));
        g2.fillOval(cx - 7, cy - 6, 14, 14);

        // Mata
        g2.setColor(new Color(40, 40, 80));
        g2.fillOval(cx - 4, cy - 2, 3, 3);
        g2.fillOval(cx + 1, cy - 2, 3, 3);

        // Bintang level jika level tinggi
        if (level >= 5) {
            g2.setColor(GOLD);
            g2.setFont(new Font("Monospaced", Font.BOLD, 8));
            g2.drawString("★", x + size - 13, y + 10);
        }
    }

    // =========================================================
    // QUEST TRACKER (pojok kanan atas)
    // =========================================================
    private void drawQuestTracker(Graphics2D g2) {
        ui.QuestTracker qt = gp.getQuestTracker();
        if (qt == null || qt.getActiveQuest() == null) return;

        ui.QuestTracker.Quest quest = qt.getActiveQuest();
        int progress  = qt.getActiveProgress();
        boolean done  = progress >= quest.targetCount();

        // Dimensi panel
        int panelW = 220, panelH = 78;
        int panelX = GamePanel.SCREEN_WIDTH - panelW - 10;
        int panelY = 10;

        drawPanel(g2, panelX, panelY, panelW, panelH, QUEST_BG, QUEST_BORDER);

        // Header "📋 QUEST AKTIF"
        g2.setFont(fontQuest);
        g2.setColor(QUEST_TITLE);
        g2.drawString("📋  QUEST AKTIF", panelX + 10, panelY + 18);

        // Garis pemisah
        g2.setColor(new Color(80, 70, 130, 120));
        g2.drawLine(panelX + 8, panelY + 24, panelX + panelW - 8, panelY + 24);

        // Judul quest
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(done ? QUEST_DONE : TEXT_WHITE);
        String titleText = (done ? "✓ " : "") + quest.title();
        g2.drawString(titleText, panelX + 10, panelY + 40);

        // Deskripsi singkat
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(TEXT_DIM);
        // Potong teks jika terlalu panjang
        String desc = quest.description();
        if (desc.length() > 32) desc = desc.substring(0, 29) + "...";
        g2.drawString(desc, panelX + 10, panelY + 54);

        // Progress bar quest
        int pbarX = panelX + 10;
        int pbarY = panelY + 60;
        int pbarW = panelW - 20;
        float pct  = quest.targetCount() > 0 ? (float) progress / quest.targetCount() : 0f;
        drawBar(g2, pbarX, pbarY, pbarW, 8,
                pct, new Color(20, 15, 40), done ? QUEST_DONE : new Color(160, 100, 255),
                progress + "/" + quest.targetCount());

        // Navigasi quest (jika ada lebih dari 1)
        if (qt.getQuestCount() > 1) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2.setColor(TEXT_DIM);
            g2.drawString("[Q] ganti quest  " + (qt.getActiveQuestIndex() + 1) + "/" + qt.getQuestCount(),
                panelX + 10, panelY + panelH - 3);
        }
    }

    // =========================================================
    // TOMBOL INVENTORY (pojok kanan bawah)
    // =========================================================
    private void drawInventoryButton(Graphics2D g2) {
        int btnSize = 56;
        int btnX    = GamePanel.SCREEN_WIDTH  - btnSize - 10;
        int btnY    = GamePanel.SCREEN_HEIGHT - btnSize - 10;

        // Update area klik
        inventoryClickArea.setBounds(btnX, btnY, btnSize, btnSize);

        // Background
        g2.setColor(inventoryHovered ? new Color(40, 32, 80, 220) : INV_BG);
        g2.fillRoundRect(btnX, btnY, btnSize, btnSize, 12, 12);

        // Border emas jika hover
        g2.setColor(inventoryHovered ? GOLD : INV_BORDER);
        g2.setStroke(new BasicStroke(inventoryHovered ? 2.5f : 1.5f));
        g2.drawRoundRect(btnX, btnY, btnSize, btnSize, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        // Ikon tas (emoji — diganti pixel art jika ada aset)
        g2.setFont(fontInv);
        g2.setColor(inventoryHovered ? GOLD : new Color(200, 170, 100));
        FontMetrics fm = g2.getFontMetrics();
        String icon = "🎒";
        g2.drawString(icon, btnX + (btnSize - fm.stringWidth(icon)) / 2, btnY + btnSize / 2 + 8);

        // Label "INV" di bawah ikon
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        g2.setColor(TEXT_DIM);
        g2.drawString("INV", btnX + btnSize / 2 - 9, btnY + btnSize - 5);

        // Tooltip saat hover
        if (inventoryHovered) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String tip = "[ I ] Inventory";
            fm = g2.getFontMetrics();
            int tipW = fm.stringWidth(tip) + 12;
            g2.setColor(new Color(10, 8, 25, 200));
            g2.fillRoundRect(btnX - tipW - 4, btnY + 16, tipW, 20, 6, 6);
            g2.setColor(GOLD);
            g2.drawString(tip, btnX - tipW + 2, btnY + 30);
        }
    }

    // =========================================================
    // NOTIFIKASI LEVEL UP (muncul di tengah layar)
    // =========================================================

    /**
     * Memicu animasi notifikasi Level Up.
     * Dipanggil oleh Player saat stat.addExp() mengembalikan true.
     */
    public void triggerLevelUpNotification(int newLevel) {
        lastShownLevel = newLevel;
        levelUpTimer   = LEVELUP_DURATION;
    }

    private void drawLevelUpNotification(Graphics2D g2, PlayerStats stats) {
        if (levelUpTimer <= 0) return;
        levelUpTimer--;

        // Hitung alpha: fade in cepat, hold, fade out di akhir
        float alpha;
        if (levelUpTimer > LEVELUP_DURATION - 20) {
            // Fade in (20 frame pertama)
            alpha = 1f - (levelUpTimer - (LEVELUP_DURATION - 20)) / 20f;
        } else if (levelUpTimer < 40) {
            // Fade out (40 frame terakhir)
            alpha = levelUpTimer / 40f;
        } else {
            alpha = 1f;
        }

        int cx = GamePanel.SCREEN_WIDTH / 2;
        int cy = GamePanel.SCREEN_HEIGHT / 2 - 60;

        // Panel background
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.85f));
        g2.setColor(new Color(5, 3, 15));
        g2.fillRoundRect(cx - 180, cy - 40, 360, 100, 16, 16);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(cx - 180, cy - 40, 360, 100, 16, 16);
        g2.setStroke(new BasicStroke(1f));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Teks utama "LEVEL UP!"
        g2.setFont(fontLevelUp);
        g2.setColor(GOLD);
        FontMetrics fm = g2.getFontMetrics();
        String main = "✦  LEVEL UP!  ✦";
        g2.drawString(main, cx - fm.stringWidth(main) / 2, cy);

        // Teks level baru
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.setColor(TEXT_WHITE);
        String sub = "Sekarang Level " + lastShownLevel
                   + "  |  HP +" + 15 + "  MP +" + 8 + "  ATK +" + 3;
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 26);

        // Teks kecil
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXT_DIM);
        String tip = "Kamu semakin kuat!";
        fm = g2.getFontMetrics();
        g2.drawString(tip, cx - fm.stringWidth(tip) / 2, cy + 46);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // =========================================================
    // HELPER: Menggambar Bar (HP/Mana/EXP/Quest)
    // =========================================================
    /**
     * Menggambar satu bar dengan label teks di dalamnya.
     *
     * @param g2        Graphics2D
     * @param x, y      Posisi kiri atas bar
     * @param w, h      Ukuran bar
     * @param fillPct   Nilai 0.0 - 1.0 untuk isi bar
     * @param bgColor   Warna background bar
     * @param fillColor Warna isi bar
     * @param label     Teks yang ditampilkan di atas bar (opsional)
     */
    private void drawBar(Graphics2D g2, int x, int y, int w, int h,
                         float fillPct, Color bgColor, Color fillColor, String label) {
        fillPct = Math.max(0f, Math.min(1f, fillPct)); // Clamp 0-1

        // Background
        g2.setColor(bgColor);
        g2.fillRoundRect(x, y, w, h, 4, 4);

        // Isi bar
        if (fillPct > 0f) {
            int fillW = (int)(w * fillPct);
            g2.setColor(fillColor);
            g2.fillRoundRect(x, y, fillW, h, 4, 4);

            // Highlight putih kecil di atas bar
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRect(x + 1, y + 1, fillW - 2, h / 3);
        }

        // Border tipis
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(x, y, w, h, 4, 4);

        // Label teks (jika bar cukup tinggi)
        if (label != null && h >= 8) {
            g2.setFont(fontBar);
            g2.setColor(new Color(255, 255, 255, 200));
            FontMetrics fm = g2.getFontMetrics();
            // Hanya gambar label jika muat
            if (fm.stringWidth(label) < w - 4) {
                g2.drawString(label, x + 3, y + h - 1);
            }
        }
    }

    /** Pilih warna HP berdasarkan persentase (hijau → kuning → merah). */
    private Color getHpColor(float pct) {
        if (pct > 0.5f) return HP_FG_HIGH;
        if (pct > 0.25f) return HP_FG_MED;
        return HP_FG_LOW;
    }

    // =========================================================
    // HELPER: Menggambar Panel Background
    // =========================================================
    private void drawPanel(Graphics2D g2, int x, int y, int w, int h,
                            Color bg, Color border) {
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setStroke(new BasicStroke(1f));
    }

    // =========================================================
    // SETTER untuk state interaktif
    // =========================================================
    public void setInventoryHovered(boolean hovered) {
        this.inventoryHovered = hovered;
    }
}