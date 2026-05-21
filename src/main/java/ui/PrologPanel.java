package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PrologPanel — typewriter backstory.
 * BUG FIX: text was doubling because completed lines were drawn in both
 * revealedLines AND as the "currently typing" partial text.
 * Fix: don't draw partial text while in holding state (line already in revealedLines).
 */
public class PrologPanel extends JPanel {
    private final Runnable onDone;
    private final int W, H;
    private Timer timer;

    private static final String[] LINES = {
        "",
        "✦  P R O L O G  ✦",
        "",
        "Pada suatu ketika, ada sebuah kerajaan yang diperintah",
        "oleh 5 raja berbeda di 5 kastil yang masing-masing",
        "mewakili warna Merah, Biru, Hijau, Ungu, dan Hitam.",
        "",
        "Segalanya berjalan damai seperti biasa...",
        "",
        "Hingga suatu hari — 5 kerajaan diserang oleh ras iblis.",
        "",
        "Akhirnya, 5 kerajaan jatuh ke dalam cengkeraman",
        "Raja Iblis yang kejam dan bertangan besi.",
        "",
        "Dalam situasi di mana harapan terasa mustahil,",
        "keajaiban pun terjadi.",
        "",
        "5 pahlawan muncul dari setiap kerajaan —",
        "membawa kembali harapan yang hampir padam.",
        "",
        "Mereka berdoa kepada para dewa untuk diberi kekuatan.",
        "Para dewa mendengar, dan memberikan mereka kekuatan",
        "untuk mengalahkan Raja Iblis beserta 9 bawahannya.",
        "",
        "Kini — takdir dunia ada di tanganmu.",
        "",
        "  ✦  The Last Chromatic Warrior  ✦",
        "",
    };

    private int  lineIndex = 0;
    private int  charIndex = 0;
    private int  frame     = 0;
    private boolean finished = false;
    private boolean holding  = false;   // BUG FIX: separate holding flag
    private int  holdTimer   = 0;
    private int  skipCooldown= 60;
    private int  autoDoneTimer = 0;

    private static final int CHARS_PER_TICK = 3;
    private static final int HOLD_FRAMES    = 35;

    private final List<String> revealedLines = new ArrayList<>();

    public PrologPanel(int w, int h, Runnable onDone) {
        this.W = w; this.H = h; this.onDone = onDone;
        setOpaque(true); setBackground(Color.BLACK);
        timer = new Timer(16, e -> tick());
        timer.start();
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (skipCooldown <= 0) { stop(); onDone.run(); }
            }
        });
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                if (k==KeyEvent.VK_ENTER||k==KeyEvent.VK_SPACE||k==KeyEvent.VK_ESCAPE)
                    if (skipCooldown <= 0) { stop(); onDone.run(); }
            }
        });
        setFocusable(true);
    }

    private void tick() {
        frame++;
        if (skipCooldown > 0) skipCooldown--;

        if (finished) {
            autoDoneTimer++;
            if (autoDoneTimer > 100) { stop(); onDone.run(); }
            repaint(); return;
        }

        if (holding) {
            holdTimer++;
            if (holdTimer >= HOLD_FRAMES) {
                holding   = false;
                holdTimer = 0;
                lineIndex++;
                charIndex = 0;
                if (lineIndex >= LINES.length) finished = true;
            }
            repaint(); return;
        }

        // Typewrite current line
        String line = LINES[lineIndex];
        charIndex = Math.min(charIndex + CHARS_PER_TICK, line.length());

        if (charIndex >= line.length()) {
            // BUG FIX: add to revealedLines FIRST, then start holding
            revealedLines.add(line);
            holding   = true;
            holdTimer = 0;
            // Don't advance lineIndex here — it advances when hold completes
        }
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK); g2.fillRect(0, 0, W, H);

        final int fontH = 24;
        final int maxVisible = 14;

        List<String> toShow = revealedLines;
        if (toShow.size() > maxVisible) toShow = toShow.subList(toShow.size() - maxVisible, toShow.size());

        int startY = H/2 - (Math.min(toShow.size() + 1, maxVisible) * fontH) / 2;
        int y = startY;

        for (String line : toShow) {
            drawStyledLine(g2, line, y, 1.0f);
            y += fontH;
        }

        // BUG FIX: only draw partial text when NOT holding (line not yet in revealedLines)
        if (!finished && !holding && lineIndex < LINES.length) {
            String partial = LINES[lineIndex].substring(0, charIndex);
            drawStyledLine(g2, partial, y, 0.85f);
            // Cursor blink
            if ((frame / 10) % 2 == 0) {
                g2.setFont(new Font("Serif", Font.PLAIN, 16));
                g2.setColor(new Color(255, 255, 255, 180));
                FontMetrics fm = g2.getFontMetrics();
                int cx = (W - fm.stringWidth(LINES[lineIndex])) / 2 + fm.stringWidth(partial) + 2;
                g2.drawString("|", cx, y);
            }
        }

        // Skip hint
        if (skipCooldown <= 0) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.setColor(new Color(120, 110, 140, (int)(128 + 80 * Math.sin(frame * 0.07))));
            String hint = "[ Enter / Klik ] Lewati";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, W - fm.stringWidth(hint) - 16, H - 16);
        }
    }

    private void drawStyledLine(Graphics2D g2, String line, int y, float alpha) {
        if (line.isBlank()) return;
        boolean isTitle    = line.contains("P R O L O G") || line.contains("Chromatic Warrior");
        boolean isEmphasis = line.startsWith("Hingga") || line.startsWith("Kini") || line.startsWith("5 pahlawan");

        Font f; Color c;
        if (isTitle) {
            f = new Font("Serif", Font.BOLD, 26);
            c = new Color(255, 200, 50, (int)(alpha * 255));
        } else if (isEmphasis) {
            f = new Font("Serif", Font.BOLD, 17);
            c = new Color(220, 180, 255, (int)(alpha * 255));
        } else {
            f = new Font("Serif", Font.PLAIN, 16);
            c = new Color(210, 205, 220, (int)(alpha * 255));
        }
        g2.setFont(f);
        g2.setColor(c);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(line, (W - fm.stringWidth(line)) / 2, y);
    }

    private void stop() {
        if (timer != null) { timer.stop(); timer = null; }
    }

    public void stopAnimation() { stop(); }
}
