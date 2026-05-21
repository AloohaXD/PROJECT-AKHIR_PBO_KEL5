package entity;
import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {
    public int worldX, worldY, width, height, speed;
    public String direction = "down";
    public boolean collisionOn = false;
    public Rectangle solidArea        = new Rectangle(8,16,32,32);
    public Rectangle solidAreaDefault = new Rectangle(solidArea);
    public BufferedImage[] walkUp=new BufferedImage[4], walkDown=new BufferedImage[4],
                           walkLeft=new BufferedImage[4], walkRight=new BufferedImage[4];
    public BufferedImage idleImage;
    protected int spriteCounter=0, spriteFrame=0, animationSpeed=12;
    public enum EntityType { PLAYER, NPC, ENEMY, OBSTACLE }
    public EntityType type = EntityType.NPC;
    public boolean interactable = false;
    protected final GamePanel gp;
    public Entity(GamePanel gp) { this.gp=gp; }
    public abstract void update();
    public abstract void draw(Graphics2D g2);
    protected BufferedImage advanceAnimation(boolean moving) {
        if (!moving) { spriteCounter=0; spriteFrame=0; return idleImage!=null?idleImage:getFrame(0); }
        if (++spriteCounter>=animationSpeed) { spriteFrame=(spriteFrame+1)%4; spriteCounter=0; }
        return getFrame(spriteFrame);
    }
    protected BufferedImage getFrame(int f) {
        return switch(direction){case "up"->walkUp[f];case "left"->walkLeft[f];case "right"->walkRight[f];default->walkDown[f];};
    }
    protected int getScreenX() { return gp.camera.toScreenX(worldX); }
    protected int getScreenY() { return gp.camera.toScreenY(worldY); }
    public void resetSolidArea() { solidArea.setBounds(solidAreaDefault); }
}
