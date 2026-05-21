package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ImageCache — loads every game image ONCE at startup and caches in RAM.
 * FIX: eliminates per-frame ImageIO.read() calls that caused combat lag.
 */
public class ImageCache {
    private static final ImageCache INSTANCE = new ImageCache();
    public static ImageCache get() { return INSTANCE; }

    private final Map<String, BufferedImage> cache = new HashMap<>();

    private ImageCache() {}

    /** Load all game images at startup (call once from GamePanel). */
    public void preloadAll() {
        // Characters
        for (combat.HeroClass.ClassType cls : combat.HeroClass.ClassType.values())
            load(cls.spritePath);

        // Boss sprites
        for (int i = 1; i <= 10; i++)
            load("/assets/images/boss/boss_ch" + i + ".png");

        // Kroco sprites
        String[] krocos = {"goblin","skeleton","orc","slime","lizard","ogre","chimera","vampire","undead","minotaur"};
        for (String k : krocos)
            load("/assets/images/kroco/" + k + ".png");

        // Combat backgrounds (JPG)
        for (int i = 1; i <= 10; i++)
            load("/assets/images/combat_bg/ch" + i + ".jpg");

        // Main menu
        load("/assets/images/main_menu/background.png");
        load("/assets/images/main_menu/btn_start.png");
        load("/assets/images/main_menu/btn_setting.png");
        load("/assets/images/main_menu/btn_exit.png");

        System.out.println("[ImageCache] Preloaded " + cache.size() + " images");
    }

    private void load(String path) {
        if (cache.containsKey(path)) return;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) cache.put(path, img);
            }
        } catch (Exception ignored) {}
    }

    /** Get cached image; returns fallback if not found. */
    public BufferedImage get(String path) {
        return cache.getOrDefault(path, null);
    }

    /** Get image scaled to target size (cached). */
    public BufferedImage getScaled(String path, int w, int h) {
        String key = path + "@" + w + "x" + h;
        if (cache.containsKey(key)) return cache.get(key);
        BufferedImage src = get(path);
        if (src == null) return null;
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        cache.put(key, scaled);
        return scaled;
    }

    /** Draw image fit-to-box (cover entire box, may crop), centred. */
    public static void drawFit(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, x, y, x+w, y+h, 0, 0, img.getWidth(), img.getHeight(), null);
    }

    /** Draw image scaled to fill entire box (stretch). */
    public static void drawStretch(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, x, y, w, h, null);
    }

    /** Draw image keeping aspect ratio, fitting inside box (letterbox). */
    public static void drawContain(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;
        float scaleX = (float)w / img.getWidth();
        float scaleY = (float)h / img.getHeight();
        float scale  = Math.min(scaleX, scaleY);
        int dw = (int)(img.getWidth()  * scale);
        int dh = (int)(img.getHeight() * scale);
        int dx = x + (w - dw) / 2;
        int dy = y + (h - dh) / 2;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, dx, dy, dw, dh, null);
    }
}
