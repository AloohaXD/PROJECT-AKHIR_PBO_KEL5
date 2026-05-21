package entity;
import main.GamePanel;
import ui.HudRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;

public class EnemyEntity extends Entity {
    public boolean isAlive = true;
    public boolean isBoss  = false;
    private final int enemyLevel;
    private final int expReward;
    private final int chapter;          // BUG FIX: store chapter so it doesn't change on retry
    private float pulseTimer = 0f;
    private float alertAlpha = 0f;
    private boolean nearPlayer = false;
    private HudRenderer hudRenderer;

    // Sprite paths per chapter
    private static final String[] KROCO_SPRITES = {
        "/assets/images/kroco/goblin.png",   // ch1
        "/assets/images/kroco/skeleton.png", // ch2
        "/assets/images/kroco/orc.png",      // ch3
        "/assets/images/kroco/slime.png",    // ch4
        "/assets/images/kroco/lizard.png",   // ch5
        "/assets/images/kroco/ogre.png",     // ch6
        "/assets/images/kroco/chimera.png",  // ch7
        "/assets/images/kroco/vampire.png",  // ch8
        "/assets/images/kroco/undead.png",   // ch9
        "/assets/images/kroco/minotaur.png", // ch10
    };
    private static final String[] BOSS_SPRITES = {
        "/assets/images/boss/boss_ch1.png",
        "/assets/images/boss/boss_ch2.png",
        "/assets/images/boss/boss_ch3.png",
        "/assets/images/boss/boss_ch4.png",
        "/assets/images/boss/boss_ch5.png",
        "/assets/images/boss/boss_ch6.png",
        "/assets/images/boss/boss_ch7.png",
        "/assets/images/boss/boss_ch8.png",
        "/assets/images/boss/boss_ch9.png",
        "/assets/images/boss/boss_ch10.png",
    };

    public EnemyEntity(GamePanel gp, int level, boolean boss, int chapter) {
        super(gp);
        this.chapter    = Math.max(1, Math.min(10, chapter));
        this.type       = EntityType.ENEMY;
        this.interactable = false;
        this.speed      = 0;
        this.enemyLevel = Math.max(1, level);
        this.isBoss     = boss;
        this.expReward  = boss ? level * 80 + 100 : level * 20 + 10;
        width  = GamePanel.TILE_SIZE;
        height = GamePanel.TILE_SIZE;
        solidArea        = new Rectangle(8, 16, 32, 28);
        solidAreaDefault = new Rectangle(solidArea);
        loadSprite();
    }

    private void loadSprite() {
        // FIX: use ImageCache — no disk I/O per enemy creation
        String[] arr = isBoss ? BOSS_SPRITES : KROCO_SPRITES;
        int idx = chapter - 1;
        if (idx < 0 || idx >= arr.length) { generateFallback(); return; }
        idleImage = main.ImageCache.get().get(arr[idx]);
        if (idleImage != null) { walkDown[0]=idleImage; }
        else generateFallback();
    }

    private void generateFallback() {
        Color body = isBoss ? new Color(120,0,160) : new Color(180,40,40);
        BufferedImage img = new BufferedImage(GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(body);          g.fillRoundRect(6,10,36,34,10,10);
        g.setColor(body.darker()); g.fillRoundRect(10,8,28,18,6,6);
        g.setColor(new Color(255,60,60)); g.fillOval(12,12,8,8); g.fillOval(28,12,8,8);
        if (isBoss) { g.setColor(new Color(255,200,0)); g.fillPolygon(new int[]{16,24,32}, new int[]{8,2,8}, 3); }
        g.dispose();
        idleImage = img; walkDown[0] = img;
    }

    @Override public void update() {
        if (!isAlive) return;
        pulseTimer += 0.08f;
        int px = gp.player.worldX + gp.player.width/2;
        int py = gp.player.worldY + gp.player.height/2;
        int dx = Math.abs(px - (worldX + width/2));
        int dy = Math.abs(py - (worldY + height/2));
        nearPlayer = (dx < GamePanel.TILE_SIZE * 3 && dy < GamePanel.TILE_SIZE * 3);
        if (nearPlayer && alertAlpha < 1f)      alertAlpha = Math.min(1f, alertAlpha + 0.06f);
        else if (!nearPlayer && alertAlpha > 0f) alertAlpha = Math.max(0f, alertAlpha - 0.04f);
    }

    @Override public void draw(Graphics2D g2) {
        if (!isAlive || !gp.camera.isVisible(worldX, worldY, width, height)) return;
        int sx = getScreenX(), sy = getScreenY();
        float glow = (float)(0.6 + 0.4 * Math.sin(pulseTimer));
        Color gc = isBoss ? new Color(180,0,255,60) : new Color(255,80,80,50);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.4f));
        g2.setColor(gc); g2.fillOval(sx-4, sy-4, width+8, height+8);
        g2.setComposite(AlphaComposite.SrcOver);
        if (idleImage != null)
            g2.drawImage(idleImage, sx, sy, width, height, null);
        Color lc = isBoss ? new Color(200,100,255) : new Color(255,120,100);
        if (hudRenderer != null)
            hudRenderer.drawEntityLabel(g2, (isBoss?"BOSS ":"") + "Lv" + enemyLevel, sx, sy, lc);
        if (alertAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alertAlpha));
            g2.setColor(Color.RED); g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString("!", sx + width/2 - 4, sy - 8);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }

    public int getEnemyLevel() { return enemyLevel; }
    public int getExpReward()  { return expReward; }
    public int getChapter()    { return chapter; }
    public void setHudRenderer(HudRenderer h) { hudRenderer = h; }
}
