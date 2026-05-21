package main;
import entity.Entity;
import java.awt.Rectangle;

public class CollisionChecker {
    private final GamePanel gp;
    public CollisionChecker(GamePanel gp) { this.gp = gp; }

    public void checkTile(Entity entity, int dx, int dy) {
        int L = entity.worldX + entity.solidArea.x;
        int R = L + entity.solidArea.width  - 1;
        int T = entity.worldY + entity.solidArea.y;
        int B = T + entity.solidArea.height - 1;
        int lc = (L+dx)/gp.TILE_SIZE, rc = (R+dx)/gp.TILE_SIZE;
        int tr = (T+dy)/gp.TILE_SIZE, br = (B+dy)/gp.TILE_SIZE;
        if (isSolid(lc,tr)||isSolid(rc,tr)||isSolid(lc,br)||isSolid(rc,br))
            entity.collisionOn = true;
    }
    public void checkTile(Entity entity) {
        int dx=0, dy=0;
        switch(entity.direction){
            case "up"    -> dy=-entity.speed;
            case "down"  -> dy= entity.speed;
            case "left"  -> dx=-entity.speed;
            case "right" -> dx= entity.speed;
        }
        checkTile(entity, dx, dy);
    }
    private boolean isSolid(int col, int row) {
        if (col<0||col>=gp.WORLD_COLS||row<0||row>=gp.WORLD_ROWS) return true;
        int t = gp.tileManager.mapTileNum[row][col];
        if (t<0||t>=gp.tileManager.tiles.length||gp.tileManager.tiles[t]==null) return false;
        return gp.tileManager.tiles[t].collision;
    }
    public int checkEntity(Entity entity, java.util.List<Entity> list) {
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            Entity e = list.get(i);
            if (e == entity) continue;
            entity.solidArea.x += entity.worldX;
            entity.solidArea.y += entity.worldY;
            e.solidArea.x += e.worldX;
            e.solidArea.y += e.worldY;
            if (entity.solidArea.intersects(e.solidArea)) idx = i;
            entity.solidArea.x -= entity.worldX;
            entity.solidArea.y -= entity.worldY;
            e.solidArea.x -= e.worldX;
            e.solidArea.y -= e.worldY;
            if (idx >= 0) break;
        }
        return idx;
    }
}
