package main;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * TransitionEffect — efek transisi distorsi saat memasuki / keluar dari combat.
 *
 * Fase:
 *  FADE_IN   → layar membeku lalu terdistorsi (ripple + chromatic aberration)
 *  HOLD      → layar penuh hitam sebentar
 *  DONE      → transisi selesai, callback dipanggil
 */
public class TransitionEffect {

    public enum Phase { INACTIVE, DISTORT, HOLD, FADE_OUT, DONE }

    private Phase  phase     = Phase.INACTIVE;
    private int    timer     = 0;

    // Durasi tiap fase (frames @ 60 FPS)
    private static final int DISTORT_FRAMES  = 40;
    private static final int HOLD_FRAMES     = 20;
    private static final int FADE_OUT_FRAMES = 30;

    // Snapshot layar saat transisi dimulai
    private BufferedImage snapshot;

    // Noise untuk efek distorsi
    private final Random rng = new Random();
    private float[] noiseX;
    private float[] noiseY;
    private static final int STRIPS = 60;

    // Callback dipanggil saat layar penuh hitam (HOLD phase)
    private Runnable onMidpoint;

    // Warna overlay
    private float overlayAlpha = 0f;

    public TransitionEffect() {}

    /**
     * Mulai transisi.
     * @param screenSnapshot screenshot layar saat ini untuk efek distorsi
     * @param onMidpoint     callback yang dipanggil saat layar hitam penuh
     */
    public void start(BufferedImage screenSnapshot, Runnable onMidpoint) {
        this.snapshot    = screenSnapshot;
        this.onMidpoint  = onMidpoint;
        this.phase       = Phase.DISTORT;
        this.timer       = 0;
        this.overlayAlpha = 0f;

        // Buat noise acak untuk distorsi horizontal
        noiseX = new float[STRIPS];
        noiseY = new float[STRIPS];
        for (int i = 0; i < STRIPS; i++) {
            noiseX[i] = (rng.nextFloat() - 0.5f) * 30;
            noiseY[i] = (rng.nextFloat() - 0.5f) * 8;
        }
    }

    public boolean isActive() {
        return phase != Phase.INACTIVE && phase != Phase.DONE;
    }

    public Phase getPhase() { return phase; }

    /** Dipanggil setiap game tick */
    public void update() {
        if (!isActive()) return;

        timer++;

        switch (phase) {
            case DISTORT -> {
                overlayAlpha = (float) timer / DISTORT_FRAMES;
                if (timer >= DISTORT_FRAMES) {
                    phase = Phase.HOLD;
                    timer = 0;
                    overlayAlpha = 1f;
                    if (onMidpoint != null) onMidpoint.run();
                }
            }
            case HOLD -> {
                if (timer >= HOLD_FRAMES) {
                    phase = Phase.FADE_OUT;
                    timer = 0;
                }
            }
            case FADE_OUT -> {
                overlayAlpha = 1f - (float) timer / FADE_OUT_FRAMES;
                if (timer >= FADE_OUT_FRAMES) {
                    phase = Phase.DONE;
                    overlayAlpha = 0f;
                }
            }
        }
    }

    /**
     * Gambar efek transisi di atas semua konten layar.
     * Dipanggil di akhir paintComponent.
     */
    public void draw(Graphics2D g2, int screenW, int screenH) {
        if (!isActive()) return;

        switch (phase) {
            case DISTORT -> drawDistortion(g2, screenW, screenH);
            case HOLD    -> {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, screenW, screenH);
            }
            case FADE_OUT -> {
                // Fade dari hitam ke normal
                g2.setColor(Color.BLACK);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));
                g2.fillRect(0, 0, screenW, screenH);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }
    }

    /** Gambar efek distorsi + chromatic aberration */
    private void drawDistortion(Graphics2D g2, int screenW, int screenH) {
        if (snapshot == null) return;

        float progress = (float) timer / DISTORT_FRAMES;

        // Gambar strips terdistorsi
        int stripH = screenH / STRIPS;
        for (int i = 0; i < STRIPS; i++) {
            int sy = i * stripH;
            int sh = Math.min(stripH, screenH - sy);

            // Intensitas distorsi meningkat seiring waktu
            float intensity = progress * 3f;
            int offsetX = (int)(noiseX[i] * intensity);
            int offsetY = (int)(noiseY[i] * intensity * 0.3f);

            // Gambar strip snapshot dengan offset
            g2.drawImage(snapshot,
                offsetX, sy + offsetY, offsetX + screenW, sy + offsetY + sh,
                0, sy, screenW, sy + sh,
                null
            );
        }

        // Chromatic aberration — tiga layer RGB bergeser
        if (snapshot != null && progress > 0.3f) {
            float caIntensity = (progress - 0.3f) * 12f;

            // Channel merah (geser kiri)
            drawColorChannel(g2, snapshot, screenW, screenH, -(int)caIntensity, 0, new Color(255, 0, 0, 80));
            // Channel biru (geser kanan)
            drawColorChannel(g2, snapshot, screenW, screenH,  (int)caIntensity, 0, new Color(0, 0, 255, 80));
        }

        // Overlay hitam yang makin gelap
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha * 0.9f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenW, screenH);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Vignette merah
        float vigAlpha = progress * 0.6f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, vigAlpha));
        RadialGradientPaint vignette = new RadialGradientPaint(
            screenW / 2f, screenH / 2f,
            Math.max(screenW, screenH) / 1.5f,
            new float[]{ 0f, 0.7f, 1f },
            new Color[]{ new Color(0,0,0,0), new Color(150,0,0,100), new Color(200,0,0,200) }
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, screenW, screenH);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Teks "!!!" bergetar
        if (progress > 0.5f) {
            int shake = (int)((progress - 0.5f) * 10 * (rng.nextFloat() - 0.5f));
            g2.setFont(new Font("Monospaced", Font.BOLD, 48));
            g2.setColor(new Color(255, 80, 80, (int)(255 * (progress - 0.5f) * 2)));
            String txt = "！！！";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, (screenW - fm.stringWidth(txt)) / 2 + shake, screenH / 2 + shake);
        }
    }

    private void drawColorChannel(Graphics2D g2, BufferedImage src,
                                   int w, int h, int dx, int dy, Color tint) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
        g2.drawRenderedImage(src, at);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
