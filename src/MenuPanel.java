import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * MenuPanel.java
 * ============================================================
 * Layar 1: Main Menu
 * Menampilkan judul game, tombol Start dan Exit.
 * Menggunakan paintComponent untuk background animasi.
 * ============================================================
 */
public class MenuPanel extends JPanel {

    // Callback untuk transisi ke panel lain
    private final Runnable onStartGame;

    // ── Warna & Font Tema ──────────────────────────────────
    private static final Color BG_TOP    = new Color(5, 5, 20);
    private static final Color BG_BOT    = new Color(20, 5, 40);
    private static final Color GOLD      = new Color(255, 200, 50);
    private static final Color GOLD_DIM  = new Color(180, 130, 30);
    private static final Color RED_GLOW  = new Color(200, 40, 40);
    private static final Color TEXT_DIM  = new Color(160, 140, 180);

    // Bintang-bintang dekorasi (posisi x, y, ukuran)
    private final int[][] stars;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public MenuPanel(Runnable onStartGame) {
        this.onStartGame = onStartGame;
        setLayout(null); // Absolute positioning untuk desain bebas
        setPreferredSize(new Dimension(780, 650));

        // Generate posisi bintang acak
        stars = new int[80][3];
        java.util.Random r = new java.util.Random(42);
        for (int i = 0; i < stars.length; i++) {
            stars[i][0] = r.nextInt(780);
            stars[i][1] = r.nextInt(400);
            stars[i][2] = r.nextInt(3) + 1;
        }

        buildUI();
    }

    // =========================================================
    // MEMBANGUN KOMPONEN UI
    // =========================================================
    private void buildUI() {
        // --- Tombol START ---
        JButton btnStart = createMenuButton("▶  MULAI PETUALANGAN", GOLD, new Color(60, 40, 0));
        btnStart.setBounds(240, 420, 300, 55);
        btnStart.addActionListener(e -> onStartGame.run());
        add(btnStart);

        // --- Tombol EXIT ---
        JButton btnExit = createMenuButton("✕  KELUAR", new Color(200, 100, 100), new Color(60, 10, 10));
        btnExit.setBounds(290, 490, 200, 45);
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?",
                    "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) System.exit(0);
        });
        add(btnExit);

        // --- Label versi ---
        JLabel lblVersion = new JLabel("v1.0  |  A Turn-Based RPG Adventure", SwingConstants.CENTER);
        lblVersion.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblVersion.setForeground(TEXT_DIM);
        lblVersion.setBounds(0, 600, 780, 20);
        add(lblVersion);
    }

    /** Helper: membuat tombol menu dengan styling konsisten */
    private JButton createMenuButton(String text, Color fg, Color bgDark) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 16));
        btn.setForeground(fg);
        btn.setBackground(new Color(bgDark.getRed(), bgDark.getGreen(), bgDark.getBlue(), 200));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg, 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Efek hover
        btn.addMouseListener(new MouseAdapter() {
            Color original = btn.getBackground();
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.min(255, bgDark.getRed()   + 40),
                    Math.min(255, bgDark.getGreen() + 40),
                    Math.min(255, bgDark.getBlue()  + 40), 220));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });

        return btn;
    }

    // =========================================================
    // CUSTOM PAINTING — Background + Judul + Dekorasi
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth();
        int H = getHeight();

        // --- Gradient background langit malam ---
        GradientPaint bgGrad = new GradientPaint(0, 0, BG_TOP, 0, H, BG_BOT);
        g2d.setPaint(bgGrad);
        g2d.fillRect(0, 0, W, H);

        // --- Bintang-bintang ---
        for (int[] star : stars) {
            int alpha = 120 + (star[2] * 40);
            g2d.setColor(new Color(255, 255, 255, Math.min(255, alpha)));
            g2d.fillOval(star[0], star[1], star[2], star[2]);
        }

        // --- Silhouette pegunungan (background) ---
        drawMountains(g2d, W, H);

        // --- Panel judul semi-transparan ---
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(80, 100, W - 160, 270, 20, 20);
        g2d.setColor(new Color(255, 200, 50, 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(80, 100, W - 160, 270, 20, 20);

        // --- Teks "THE LAST" (atas) ---
        g2d.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
        drawTextWithShadow(g2d, "— THE LAST —", W / 2, 155, TEXT_DIM, new Color(0,0,0,100));

        // --- Judul Utama "CHROMATIC WARRIOR" ---
        drawChromaticTitle(g2d, W / 2, 240);

        // --- Subtitle garis bawah ---
        g2d.setFont(new Font("Serif", Font.ITALIC, 15));
        drawTextWithShadow(g2d, "\"Satu Pejuang, Satu Takdir, Satu Dunia\"", W / 2, 320, TEXT_DIM, Color.BLACK);

        // --- Dekorasi garis emas ---
        g2d.setColor(GOLD_DIM);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawLine(100, 345, W - 100, 345);

        // --- Pixel Art dekorasi kristal warna (chromatic) ---
        drawChromaticCrystals(g2d, W, 370);

        g2d.dispose();
    }

    /** Menggambar judul "CHROMATIC WARRIOR" dengan warna pelangi per-karakter */
    private void drawChromaticTitle(Graphics2D g2d, int centerX, int baseY) {
        String word1 = "CHROMATIC";
        String word2 = "WARRIOR";
        Color[] colors = {
            new Color(255, 80, 80),   // R
            new Color(255, 140, 0),   // O
            new Color(255, 220, 0),   // Y
            new Color(80, 220, 80),   // G
            new Color(60, 180, 255),  // B
            new Color(120, 80, 255),  // I
            new Color(200, 80, 255),  // V
            new Color(255, 100, 200), // P
            GOLD,                     // Extra
        };

        Font titleFont = new Font("Serif", Font.BOLD, 46);
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();

        // Gambar kata pertama
        int w1Width = fm.stringWidth(word1);
        int x1 = centerX - w1Width / 2;
        for (int i = 0; i < word1.length(); i++) {
            String ch = String.valueOf(word1.charAt(i));
            Color c = colors[i % colors.length];
            // Shadow
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.drawString(ch, x1 + fm.stringWidth(word1.substring(0, i)) + 2, baseY - 45 + 2);
            // Karakter berwarna
            g2d.setColor(c);
            g2d.drawString(ch, x1 + fm.stringWidth(word1.substring(0, i)), baseY - 45);
        }

        // Gambar kata kedua (lebih besar)
        Font bigFont = new Font("Serif", Font.BOLD, 58);
        g2d.setFont(bigFont);
        fm = g2d.getFontMetrics();
        int w2Width = fm.stringWidth(word2);
        int x2 = centerX - w2Width / 2;
        for (int i = 0; i < word2.length(); i++) {
            String ch = String.valueOf(word2.charAt(i));
            Color c = colors[(i + 2) % colors.length];
            g2d.setColor(new Color(0, 0, 0, 140));
            g2d.drawString(ch, x2 + fm.stringWidth(word2.substring(0, i)) + 2, baseY + 2);
            // Outer glow effect (gambar karakter lebih terang di atas)
            g2d.setColor(c.darker());
            g2d.drawString(ch, x2 + fm.stringWidth(word2.substring(0, i)) - 1, baseY - 1);
            g2d.setColor(c);
            g2d.drawString(ch, x2 + fm.stringWidth(word2.substring(0, i)), baseY);
        }
    }

    /** Helper: teks dengan bayangan */
    private void drawTextWithShadow(Graphics2D g2d, String text, int cx, int y, Color fg, Color shadow) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        g2d.setColor(shadow);
        g2d.drawString(text, x + 2, y + 2);
        g2d.setColor(fg);
        g2d.drawString(text, x, y);
    }

    /** Silhouette pegunungan pixel-art di background */
    private void drawMountains(Graphics2D g2d, int W, int H) {
        Color mountain1 = new Color(15, 10, 35);
        Color mountain2 = new Color(25, 15, 50);

        // Gunung belakang (lebih gelap)
        g2d.setColor(mountain1);
        int[] xBg = {0,   80, 180, 300, 420, 520, 650, 780, 780, 0};
        int[] yBg = {H, 480, 350, 420, 310, 400, 320, 460, H,   H};
        g2d.fillPolygon(xBg, yBg, xBg.length);

        // Gunung depan (lebih terang)
        g2d.setColor(mountain2);
        int[] xFg = {0,  50, 150, 250, 380, 480, 600, 700, 780, 780, 0};
        int[] yFg = {H, 550, 420, 500, 380, 460, 400, 510, 520, H,   H};
        g2d.fillPolygon(xFg, yFg, xFg.length);
    }

    /** Kristal dekoratif berwarna di bawah judul */
    private void drawChromaticCrystals(Graphics2D g2d, int W, int baseY) {
        Color[] crystalColors = {
            new Color(255, 80, 80, 180),
            new Color(80, 200, 255, 180),
            new Color(200, 80, 255, 180),
            new Color(80, 220, 80, 180),
            new Color(255, 200, 50, 180),
        };
        int[] sizes = {18, 24, 30, 24, 18};
        int totalW = 0;
        for (int s : sizes) totalW += s + 10;

        int startX = W / 2 - totalW / 2;
        int cx = startX;
        for (int i = 0; i < crystalColors.length; i++) {
            drawCrystal(g2d, cx, baseY, sizes[i], crystalColors[i]);
            cx += sizes[i] + 16;
        }
    }

    /** Menggambar satu kristal pixel art (diamond shape) */
    private void drawCrystal(Graphics2D g2d, int x, int y, int size, Color c) {
        int half = size / 2;
        int[] xPts = {x + half, x + size, x + half, x};
        int[] yPts = {y,        y + half,  y + size,  y + half};
        g2d.setColor(c);
        g2d.fillPolygon(xPts, yPts, 4);
        g2d.setColor(c.brighter());
        g2d.drawLine(x + half, y, x + size, y + half);
        g2d.drawLine(x + half, y, x, y + half);
    }
}