package entity;
import main.GamePanel;
import main.KeyHandler;
import main.GamePanel;
import ui.HudRenderer;
import java.awt.*;
import java.awt.image.*;

public class Player extends Entity {
    private final KeyHandler key;
    private final PlayerStats stats;
    private HudRenderer hudRenderer;
    public Entity nearbyEntity = null;

    private boolean isAttacking = false;
    private int     attackTimer = 0;
    private static final int ATK_DUR = 15;
    // BUG FIX: wider interaction range — 2.5 tiles so you don't need to be right next to enemy
    private static final int ATK_RANGE = (int)(GamePanel.TILE_SIZE * 2.5);

    private float slashAlpha = 0f;
    public combat.HeroClass.ClassType heroClass = combat.HeroClass.ClassType.WARRIOR;
    public combat.HeroClass.ClassType[] party = new combat.HeroClass.ClassType[5];

    private BufferedImage heroSprite = null;

    public Player(GamePanel gp, KeyHandler key) {
        super(gp);
        this.key   = key;
        this.stats = new PlayerStats();
        type       = EntityType.PLAYER;
        worldX     = GamePanel.TILE_SIZE * 5;
        worldY     = GamePanel.TILE_SIZE * 5;
        width      = GamePanel.TILE_SIZE;
        height     = GamePanel.TILE_SIZE;
        solidArea  = new Rectangle(10, 26, 28, 18);
        solidAreaDefault = new Rectangle(solidArea);
        speed      = 4;
        loadSprite();
        generateFallbackSprites();
    }

    private void loadSprite() {
        // FIX: use ImageCache — no per-call disk I/O, instant
        heroSprite = main.ImageCache.get().get(heroClass.spritePath);
    }

    public void reloadSprite() { loadSprite(); }

    private void generateFallbackSprites() {
        Color main = heroClass.themeColor, dark = main.darker();
        for (int f = 0; f < 4; f++) {
            BufferedImage img = new BufferedImage(GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(main);  g.fillRoundRect(8, 10, 32, 34, 8, 8);
            g.setColor(dark);  g.fillRoundRect(12, 8, 24, 18, 6, 6);
            g.setColor(new Color(255, 220, 180)); g.fillOval(14, 6 + (f%2==0?0:2), 20, 20);
            g.dispose();
            walkDown[f] = walkUp[f] = walkLeft[f] = walkRight[f] = img;
        }
        idleImage = walkDown[0];
    }

    @Override public void update() {
        boolean moving = key.upPressed || key.downPressed || key.leftPressed || key.rightPressed;
        if (moving) {
            int dx = 0, dy = 0;
            if (key.upPressed)    { direction="up";    dy=-speed; }
            if (key.downPressed)  { direction="down";  dy= speed; }
            if (key.leftPressed)  { direction="left";  dx=-speed; }
            if (key.rightPressed) { direction="right"; dx= speed; }
            collisionOn = false;
            gp.collision.checkTile(this, dx, dy);
            if (!collisionOn) { worldX += dx; worldY += dy; }
        }
        // Clamp
        worldX = Math.max(GamePanel.TILE_SIZE, Math.min(worldX, gp.WORLD_WIDTH - width - GamePanel.TILE_SIZE));
        worldY = Math.max(GamePanel.TILE_SIZE, Math.min(worldY, gp.WORLD_HEIGHT - height - GamePanel.TILE_SIZE));

        // Detect nearby entity (wider range = ATK_RANGE)
        int nearIdx = findNearbyEntity();
        nearbyEntity = (nearIdx >= 0) ? gp.entities.get(nearIdx) : null;

        // Attack key (X)
        if (key.attackJustPressed && !isAttacking) {
            isAttacking = true; attackTimer = 0; slashAlpha = 1f;
            main.AudioManager.get().playSfx(main.AudioManager.SFX_SWORD_SLASH);
            // Attack nearest enemy within ATK_RANGE
            if (nearbyEntity instanceof EnemyEntity ee && ee.isAlive)
                gp.startCombatTransition(nearIdx);
        }
        if (isAttacking) {
            attackTimer++;
            slashAlpha = Math.max(0f, 1f - (float)attackTimer / ATK_DUR);
            if (attackTimer >= ATK_DUR) isAttacking = false;
        }

        // NPC interact (Z)
        if (key.interactJustPressed && nearbyEntity instanceof NPC) {
            gp.setGameState(GamePanel.GameState.DIALOG);
        }

        advanceAnimation(moving);
        key.clearOneShots();
    }

    /** BUG FIX: wider interaction range — finds nearest entity within ATK_RANGE */
    private int findNearbyEntity() {
        int bestIdx = -1;
        double bestDist = ATK_RANGE;
        int px = worldX + width/2, py = worldY + height/2;
        for (int i = 0; i < gp.entities.size(); i++) {
            Entity e = gp.entities.get(i);
            if (e instanceof EnemyEntity ee && !ee.isAlive) continue;
            int ex = e.worldX + e.width/2, ey = e.worldY + e.height/2;
            double dist = Math.hypot(px - ex, py - ey);
            if (dist < bestDist) { bestDist = dist; bestIdx = i; }
        }
        return bestIdx;
    }

    @Override public void draw(Graphics2D g2) {
        if (!gp.camera.isVisible(worldX, worldY, width, height)) return;
        int sx = getScreenX(), sy = getScreenY();
        if (heroSprite != null)
            g2.drawImage(heroSprite, sx, sy, width, height, null);
        else
            g2.drawImage(getFrame(spriteFrame), sx, sy, width, height, null);
        if (slashAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, slashAlpha));
            g2.setColor(new Color(255, 255, 100));
            g2.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2.drawString("⚔", sx + 4, sy + 20);
            g2.setComposite(AlphaComposite.SrcOver);
        }
        if (hudRenderer != null)
            hudRenderer.drawEntityLabel(g2, "Lv" + stats.getLevel(), sx, sy, heroClass.themeColor);
    }

    public void gainExp(int amt) {
        int oldLv = stats.getLevel();
        stats.gainExp(amt);
        if (stats.getLevel() > oldLv && hudRenderer != null)
            hudRenderer.triggerLevelUp(stats.getLevel());
    }

    public PlayerStats  getStats()               { return stats; }
    public void         setHudRenderer(HudRenderer h) { hudRenderer = h; }
    public void         restoreStats()            { stats.restoreAll(); }

    /** Build a scaled CharacterProfile from current player level — FIX for combat stats */
    public combat.HeroClass.CharacterProfile buildScaledProfile(combat.HeroClass.ClassType cls) {
        int lvl = stats.getLevel() - 1;
        int scaledHp  = cls.baseHp  + lvl * 20;
        int scaledMp  = cls.baseMp  + lvl * 10;
        int scaledAtk = cls.baseAtk + lvl * 5;
        return new combat.HeroClass.CharacterProfile(cls, scaledHp, scaledMp, scaledAtk);
    }
}
