package main;
import entity.Entity;

public class Camera {
    private final GamePanel gp;
    public int offsetX, offsetY;
    public Camera(GamePanel gp) { this.gp = gp; }

    public void update(Entity target) {
        int idealX = target.worldX + target.width/2  - gp.SCREEN_WIDTH/2;
        int idealY = target.worldY + target.height/2 - gp.SCREEN_HEIGHT/2;
        offsetX = clamp(idealX, 0, gp.WORLD_WIDTH  - gp.SCREEN_WIDTH);
        offsetY = clamp(idealY, 0, gp.WORLD_HEIGHT - gp.SCREEN_HEIGHT);
    }
    public int toScreenX(int worldX) { return worldX - offsetX; }
    public int toScreenY(int worldY) { return worldY - offsetY; }
    public boolean isVisible(int wx, int wy, int w, int h) {
        int sx = toScreenX(wx), sy = toScreenY(wy);
        return sx+w>0 && sx<gp.SCREEN_WIDTH && sy+h>0 && sy<gp.SCREEN_HEIGHT;
    }
    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
