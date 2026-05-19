package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Entity — base class untuk semua objek yang ada di dunia.
 * Subclass: Player, NPC, EnemyEntity, dll.
 */
public abstract class Entity {

    // ─── World Position ───────────────────────────────────────────────────────
    public int worldX, worldY;
    public int width, height;

    // ─── Movement ────────────────────────────────────────────────────────────
    public int     speed;
    public String  direction = "down";
    public boolean collisionOn = false;

    // ─── Hitbox ───────────────────────────────────────────────────────────────
    public Rectangle solidArea        = new Rectangle(8, 16, 32, 32);
    public Rectangle solidAreaDefault = new Rectangle(solidArea);

    // ─── Sprites ─────────────────────────────────────────────────────────────
    public BufferedImage[] walkUp    = new BufferedImage[4];
    public BufferedImage[] walkDown  = new BufferedImage[4];
    public BufferedImage[] walkLeft  = new BufferedImage[4];
    public BufferedImage[] walkRight = new BufferedImage[4];
    public BufferedImage   idleImage;

    protected int spriteCounter  = 0;
    protected int spriteFrame    = 0;
    protected int animationSpeed = 12;

    // ─── Type ─────────────────────────────────────────────────────────────────
    public enum EntityType { PLAYER, NPC, ITEM, OBSTACLE }
    public EntityType type = EntityType.NPC;
    public boolean interactable = false;

    // ─── Reference ───────────────────────────────────────────────────────────
    protected final GamePanel gp;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2);

    /** Majukan animasi satu tick. Kembalikan frame yang harus digambar. */
    protected BufferedImage advanceAnimation(boolean isMoving) {
        if (!isMoving) {
            spriteCounter = 0;
            spriteFrame   = 0;
            return idleImage != null ? idleImage : getDirectionFrame(0);
        }
        spriteCounter++;
        if (spriteCounter >= animationSpeed) {
            spriteFrame   = (spriteFrame + 1) % 4;
            spriteCounter = 0;
        }
        return getDirectionFrame(spriteFrame);
    }

    private BufferedImage getDirectionFrame(int frame) {
        return switch (direction) {
            case "up"    -> walkUp[frame];
            case "left"  -> walkLeft[frame];
            case "right" -> walkRight[frame];
            default      -> walkDown[frame];
        };
    }

    protected int getScreenX() { return gp.camera.toScreenX(worldX); }
    protected int getScreenY() { return gp.camera.toScreenY(worldY); }

    public void resetSolidArea() { solidArea.setBounds(solidAreaDefault); }
}
