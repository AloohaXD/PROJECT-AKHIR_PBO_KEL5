package ui;

import main.GamePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;

/**
 * MainMenuPanel.java
 * ============================================================
 * Layar Main Menu yang ditampilkan saat game pertama kali dijalankan.
 *
 * Fitur:
 *  - Memuat background dari assets/bg_menu.png (dengan fallback jika tidak ada)
 *  - Judul game "The Last Chromatic Warrior" dengan efek warna chromatic
 *  - Tombol Start → masuk ke mode eksplorasi
 *  - Tombol Exit  → tutup program
 *  - Efek bintang berkedip di background (procedural)
 *  - Seluruh UI digambar dengan Graphics2D (bukan komponen Swing)
 *    agar tetap konsisten dengan game loop
 *
 * Navigasi: Menu → GamePanel (EXPLORATION state)
 * ============================================================
 */
public class MainMenuPanel extends JPanel {

    // ─── Callback ke GamePanel ────────────────────────────────
    private final Runnable onStartGame; // Dipanggil saat tombol Start diklik

    // ─── Background ───────────────────────────────────────────
    private BufferedImage bgImage;       // Gambar bg_menu.png (jika ada)
    private boolean bgLoaded = false;

    // ─── Bintang-bintang dekoratif (procedural) ───────────────
    private final int[][] stars;         // [i][0]=x, [i][1]=y, [i][2]=size, [i][3]=alphaOffset

    // ─── State Tombol ─────────────────────────────────────────
    private boolean startHovered = false;
    private boolean exitHovered  = false;

    // Area klik tombol (diperbarui setiap paint)
    private final Rectangle startBounds = new Rectangle();
    private final Rectangle exitBounds  = new Rectangle();

    // ─── Animasi ─────────────────────────────────────────────
    private int animFrame = 0; // Counter frame untuk efek bergerak

    // ─── Timer Animasi ────────────────────────────────────────
    private final Timer animTimer;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public MainMenuPanel(int width, int height, Runnable onStartGame) {
        this.onStartGame = onStartGame;

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Coba muat background dari resources
        loadBackgroundImage();

        // Generate posisi bintang acak (seed tetap agar konsisten)
        Random rng = new Random(7777L);
        stars = new int[100][4];
        for (int i = 0; i < stars.length; i++) {
            stars[i][0] = rng.nextInt(width);
            stars[i][1] = rng.nextInt(height * 3 / 4);     // Hanya di 3/4 atas
            stars[i][2] = rng.nextInt(3) + 1;               // Ukuran 1-3 px
            stars[i][3] = rng.nextInt(100);                  // Offset kedipan
        }

        // Daftarkan event listener mouse
        setupMouseListeners();

        // Timer animasi: repaint 60fps (untuk efek kedipan bintang, dsb.)
        animTimer = new Timer(16, e -> {
            animFrame++;
            repaint();
        });
        animTimer.start();

        // ─── Mulai Main Menu BGM ──────────────────────────────
        main.AudioManager.get().playBgm(main.AudioManager.BGM_MAIN_MENU, true);
    }

    // =========================================================
    // MEMUAT GAMBAR BACKGROUND
    // =========================================================
    /**
     * Mencoba memuat bg_menu.png dari folder assets.
     * Jika gagal (file tidak ditemukan), gunakan background procedural.
     *
     * Untuk menambahkan gambar:
     *   Simpan file sebagai: src/main/resources/assets/bg_menu.png
     *   Format yang didukung: PNG, JPEG, GIF
     */
    private void loadBackgroundImage() {
        try {
            // Coba muat dari classpath resources
            InputStream is = getClass().getResourceAsStream("/assets/bg_menu.png");
            if (is != null) {
                bgImage  = ImageIO.read(is);
                bgLoaded = true;
                is.close();
            }
        } catch (Exception e) {
            // File tidak ditemukan — gunakan background procedural (tidak masalah)
            bgLoaded = false;
        }
    }

    // =========================================================
    // EVENT LISTENERS MOUSE
    // =========================================================
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                startHovered = startBounds.contains(p);
                exitHovered  = exitBounds.contains(p);

                // Ubah cursor saat hover tombol
                setCursor(Cursor.getPredefinedCursor(
                    (startHovered || exitHovered) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR
                ));
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (startBounds.contains(p)) {
                    // Hentikan timer animasi lalu panggil callback Start
                    animTimer.stop();
                    onStartGame.run();
                } else if (exitBounds.contains(p)) {
                    // Konfirmasi keluar
                    int confirm = JOptionPane.showConfirmDialog(
                        MainMenuPanel.this,
                        "Yakin ingin keluar dari game?",
                        "Konfirmasi Keluar",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        animTimer.stop();
                        System.exit(0);
                    }
                }
            }
        });
    }

    // =========================================================
    // RENDERING UTAMA
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Aktifkan antialiasing untuk teks
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth();
        int H = getHeight();

        // ─── 1. Gambar Background ─────────────────────────────
        if (bgLoaded && bgImage != null) {
            // Gambar background PNG yang dimuat, scale ke ukuran layar
            g2.drawImage(bgImage, 0, 0, W, H, null);
            // Overlay gelap agar teks terbaca
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(0, 0, W, H);
        } else {
            // Background procedural jika PNG tidak ada
            drawProceduralBackground(g2, W, H);
        }

        // ─── 2. Bintang berkedip ──────────────────────────────
        drawStars(g2, W);

        // ─── 3. Silhouette gunung ─────────────────────────────
        drawMountainSilhouette(g2, W, H);

        // ─── 4. Panel judul semi-transparan ───────────────────
        drawTitlePanel(g2, W, H);

        // ─── 5. Judul "THE LAST CHROMATIC WARRIOR" ────────────
        drawChromaticTitle(g2, W, H);

        // ─── 6. Tombol Start & Exit ───────────────────────────
        drawButtons(g2, W, H);

        // ─── 7. Footer (versi & kontrol) ─────────────────────
        drawFooter(g2, W, H);

        g2.dispose();
    }

    // ─── Background Procedural ────────────────────────────────

    private void drawProceduralBackground(Graphics2D g2, int W, int H) {
        // Gradient langit malam dari atas ke bawah
        GradientPaint bg = new GradientPaint(
            0, 0, new Color(5, 3, 18),
            0, H, new Color(25, 8, 45)
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);
    }

    // ─── Bintang-bintang ─────────────────────────────────────

    private void drawStars(Graphics2D g2, int W) {
        for (int[] star : stars) {
            // Efek kedipan menggunakan sin wave dengan phase berbeda tiap bintang
            float blink = (float)(0.4 + 0.6 * Math.sin((animFrame + star[3]) * 0.04));
            int alpha   = (int)(blink * 200) + 55;

            g2.setColor(new Color(255, 255, 255, Math.min(255, alpha)));
            int s = star[2];
            g2.fillOval(star[0], star[1], s, s);
        }
    }

    // ─── Gunung Silhouette ────────────────────────────────────

    private void drawMountainSilhouette(Graphics2D g2, int W, int H) {
        // Gunung belakang (lebih gelap)
        g2.setColor(new Color(12, 8, 28));
        int[] xB = {0, 100, 200, 350, 480, 600, 720, 850, W, W, 0};
        int[] yB = {H, H-200, H-320, H-260, H-380, H-290, H-350, H-220, H-180, H, H};
        g2.fillPolygon(xB, yB, xB.length);

        // Gunung depan (lebih terang sedikit)
        g2.setColor(new Color(18, 12, 38));
        int[] xF = {0, 80, 180, 300, 420, 540, 660, 780, W, W, 0};
        int[] yF = {H, H-120, H-220, H-170, H-280, H-200, H-250, H-140, H-110, H, H};
        g2.fillPolygon(xF, yF, xF.length);
    }

    // ─── Panel Judul ──────────────────────────────────────────

    private void drawTitlePanel(Graphics2D g2, int W, int H) {
        int panelW = W - 120;
        int panelH = 220;
        int panelX = 60;
        int panelY = H / 2 - 210;

        // Background panel
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Border emas
        g2.setColor(new Color(255, 200, 50, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // Hiasan kristal chromatic di pojok
        drawChromaticCrystalRow(g2, W / 2, panelY + panelH + 12, 5);
    }

    // ─── Judul Chromatic ──────────────────────────────────────

    /**
     * Menggambar judul dengan warna pelangi per-karakter.
     * "THE LAST" di atas, "CHROMATIC WARRIOR" di bawahnya lebih besar.
     */
    private void drawChromaticTitle(Graphics2D g2, int W, int H) {
        // Warna pelangi untuk tiap karakter
        Color[] rainbow = {
            new Color(255, 80,  80),   // Merah
            new Color(255, 140, 0),    // Oranye
            new Color(255, 220, 0),    // Kuning
            new Color(80,  220, 80),   // Hijau
            new Color(60,  180, 255),  // Biru
            new Color(120, 80,  255),  // Ungu
            new Color(200, 80,  255),  // Violet
            new Color(255, 100, 200),  // Pink
        };

        int titleCenterX = W / 2;
        int titleBaseY   = H / 2 - 130;

        // ── "— THE LAST —" ────────────────────────────────────
        g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 24));
        g2.setColor(new Color(180, 160, 200));
        FontMetrics fm = g2.getFontMetrics();
        String topLine  = "—   T H E   L A S T   —";
        g2.drawString(topLine, titleCenterX - fm.stringWidth(topLine) / 2, titleBaseY);

        // ── "CHROMATIC" ───────────────────────────────────────
        String word1 = "CHROMATIC";
        g2.setFont(new Font("Serif", Font.BOLD, 52));
        fm = g2.getFontMetrics();
        drawChromaticWord(g2, word1, titleCenterX, titleBaseY + 60, rainbow, 0, fm);

        // ── "WARRIOR" (lebih besar) ───────────────────────────
        String word2 = "WARRIOR";
        g2.setFont(new Font("Serif", Font.BOLD, 70));
        fm = g2.getFontMetrics();
        drawChromaticWord(g2, word2, titleCenterX, titleBaseY + 130, rainbow, 2, fm);

        // Tagline
        g2.setFont(new Font("Serif", Font.ITALIC, 14));
        g2.setColor(new Color(160, 140, 190));
        String tagline = "\"Satu Pejuang. Satu Takdir. Satu Dunia.\"";
        fm = g2.getFontMetrics();
        g2.drawString(tagline, titleCenterX - fm.stringWidth(tagline) / 2, titleBaseY + 155);
    }

    /**
     * Helper: Menggambar satu kata dengan warna pelangi per-karakter.
     * Setiap karakter mendapat warna dari array rainbow secara bergilir.
     *
     * @param colorOffset Geser index warna awal agar variasi antar kata
     */
    private void drawChromaticWord(Graphics2D g2, String word, int centerX, int y,
                                    Color[] rainbow, int colorOffset, FontMetrics fm) {
        int totalW = fm.stringWidth(word);
        int startX = centerX - totalW / 2;

        for (int i = 0; i < word.length(); i++) {
            String ch     = String.valueOf(word.charAt(i));
            Color  color  = rainbow[(i + colorOffset) % rainbow.length];
            int    charX  = startX + fm.stringWidth(word.substring(0, i));

            // Shadow
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(ch, charX + 2, y + 2);

            // Glow tipis (gambar lebih terang di bawah)
            g2.setColor(color.darker());
            g2.drawString(ch, charX - 1, y - 1);

            // Karakter utama
            g2.setColor(color);
            g2.drawString(ch, charX, y);
        }
    }

    // ─── Tombol Start & Exit ──────────────────────────────────

    private void drawButtons(Graphics2D g2, int W, int H) {
        int btnW = 240;
        int btnH = 50;
        int btnX = W / 2 - btnW / 2;

        // Tombol START
        int startY = H / 2 + 30;
        drawMenuButton(g2, btnX, startY, btnW, btnH,
                       "▶   MULAI PETUALANGAN",
                       new Color(255, 200, 50),
                       startHovered ? new Color(50, 35, 5) : new Color(30, 22, 3));
        startBounds.setBounds(btnX, startY, btnW, btnH);

        // Tombol EXIT
        int exitY = H / 2 + 96;
        drawMenuButton(g2, btnX + 40, exitY, btnW - 80, btnH - 10,
                       "✕   KELUAR",
                       new Color(200, 100, 100),
                       exitHovered ? new Color(50, 8, 8) : new Color(30, 5, 5));
        exitBounds.setBounds(btnX + 40, exitY, btnW - 80, btnH - 10);
    }

    /**
     * Menggambar satu tombol menu dengan efek hover.
     */
    private void drawMenuButton(Graphics2D g2, int x, int y, int w, int h,
                                 String text, Color fgColor, Color bgColor) {
        // Background tombol
        g2.setColor(bgColor);
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // Border tombol
        g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 180));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        // Glow tipis pada border atas (efek depth)
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawLine(x + 6, y + 1, x + w - 6, y + 1);

        // Teks tombol
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.setColor(fgColor);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, tx, ty);
    }

    // ─── Kristal Dekoratif ────────────────────────────────────

    private void drawChromaticCrystalRow(Graphics2D g2, int centerX, int y, int count) {
        Color[] crystalColors = {
            new Color(255, 80,  80,  160),
            new Color(255, 180, 50,  160),
            new Color(80,  220, 100, 160),
            new Color(60,  150, 255, 160),
            new Color(180, 80,  255, 160),
        };
        int[] sizes = {12, 16, 20, 16, 12};
        int totalW  = 0;
        for (int s : sizes) totalW += s + 8;

        int cx = centerX - totalW / 2;
        for (int i = 0; i < Math.min(count, sizes.length); i++) {
            Color c = crystalColors[i % crystalColors.length];
            int   s = sizes[i];
            int   h = s;

            // Bentuk diamond
            int[] px = {cx + s/2, cx + s, cx + s/2, cx};
            int[] py = {y,        y + h/2, y + h,   y + h/2};
            g2.setColor(c);
            g2.fillPolygon(px, py, 4);
            g2.setColor(c.brighter());
            g2.drawLine(cx + s/2, y, cx + s, y + h/2);

            cx += s + 8;
        }
    }

    // ─── Footer ───────────────────────────────────────────────

    private void drawFooter(Graphics2D g2, int W, int H) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(120, 110, 140));

        String left  = "WASD: Gerak  |  X: Serang  |  Z: Bicara  |  I: Inventory";
        String right = "v2.0  —  The Last Chromatic Warrior";

        g2.drawString(left,  20, H - 12);

        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(right, W - fm.stringWidth(right) - 20, H - 12);
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    /** Hentikan timer saat panel dilepas dari layar. */
    public void stopAnimation() {
        if (animTimer != null) animTimer.stop();
    }
}