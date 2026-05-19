package main;

import entity.Entity;

import java.awt.*;

/**
 * CollisionChecker — deteksi tabrakan tile dan entity.
 * Diperbarui untuk mendukung gerakan 360 derajat (dirX/dirY float).
 */
public class CollisionChecker {

    private final GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    // ─── Tile Collision (360° aware) ──────────────────────────────────────────

    /**
     * Memeriksa apakah bergerak dengan vektor (dx, dy) akan menabrak tile solid.
     * Mengisi entity.collisionOn = true jika terhalang.
     */
    public void checkTile(Entity entity, int dx, int dy) {
        int entityLeft   = entity.worldX + entity.solidArea.x;
        int entityRight  = entityLeft    + entity.solidArea.width  - 1;
        int entityTop    = entity.worldY + entity.solidArea.y;
        int entityBottom = entityTop     + entity.solidArea.height - 1;

        // Proyeksikan ke posisi berikutnya
        int leftCol   = (entityLeft  + dx) / gp.TILE_SIZE;
        int rightCol  = (entityRight + dx) / gp.TILE_SIZE;
        int topRow    = (entityTop   + dy) / gp.TILE_SIZE;
        int bottomRow = (entityBottom+ dy) / gp.TILE_SIZE;

        // Cek semua sudut hitbox
        if (isSolid(leftCol, topRow) || isSolid(rightCol, topRow)
         || isSolid(leftCol, bottomRow) || isSolid(rightCol, bottomRow)) {
            entity.collisionOn = true;
        }
    }

    /** Overload lama untuk kompatibilitas (4-arah) */
    public void checkTile(Entity entity) {
        int dx = 0, dy = 0;
        switch (entity.direction) {
            case "up"    -> dy = -entity.speed;
            case "down"  -> dy =  entity.speed;
            case "left"  -> dx = -entity.speed;
            case "right" -> dx =  entity.speed;
        }
        checkTile(entity, dx, dy);
    }

    private boolean isSolid(int col, int row) {
        if (col < 0 || col >= gp.WORLD_COLS || row < 0 || row >= gp.WORLD_ROWS) return true;
        int tileNum = gp.tileManager.mapTileNum[row][col];
        if (tileNum < 0 || tileNum >= gp.tileManager.tiles.length) return true;
        return gp.tileManager.tiles[tileNum].collision;
    }

    // ─── Entity Interaction Detection ────────────────────────────────────────

    /**
     * Cek apakah player berada dekat dengan entity untuk interaksi.
     * Menggunakan jangkauan radius melingkar (bukan 4 arah).
     * @return index entity di gp.entities, atau -1 jika tidak ada
     */
    public int checkEntityInteraction(Entity player) {
        int reachPx = gp.TILE_SIZE;

        Rectangle playerSolid = new Rectangle(
            player.worldX + player.solidArea.x,
            player.worldY + player.solidArea.y,
            player.solidArea.width,
            player.solidArea.height
        );

        // Expand ke arah yang sedang dihadapi (menggunakan direction string)
        Rectangle reach = new Rectangle(playerSolid);
        switch (player.direction) {
            case "up"    -> { reach.y -= reachPx;      reach.height += reachPx; }
            case "down"  -> { reach.height += reachPx; }
            case "left"  -> { reach.x -= reachPx;      reach.width  += reachPx; }
            case "right" -> { reach.width  += reachPx; }
        }

        for (int i = 0; i < gp.entities.size(); i++) {
            Entity e = gp.entities.get(i);
            Rectangle targetSolid = new Rectangle(
                e.worldX + e.solidArea.x,
                e.worldY + e.solidArea.y,
                e.solidArea.width,
                e.solidArea.height
            );
            if (reach.intersects(targetSolid)) return i;
        }
        return -1;
    }

    /**
     * Cek apakah player berada dalam jangkauan serangan dari posisi mana pun
     * (digunakan oleh basic attack 360°).
     * @return index entity yang kena, atau -1
     */
    public int checkEntityAttackRange(Entity player, int rangePx) {
        // Pusat player
        int px = player.worldX + player.width  / 2;
        int py = player.worldY + player.height / 2;

        for (int i = 0; i < gp.entities.size(); i++) {
            Entity e = gp.entities.get(i);
            int ex = e.worldX + e.width  / 2;
            int ey = e.worldY + e.height / 2;

            double dist = Math.sqrt((px - ex) * (double)(px - ex) + (py - ey) * (double)(py - ey));
            if (dist <= rangePx) return i;
        }
        return -1;
    }

    /** Full entity-to-entity solid collision. */
    public void checkEntity(Entity entity, boolean checkPlayer) {
        Rectangle movingBox = getProjectedBox(entity);

        if (checkPlayer) {
            Rectangle playerBox = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
            );
            if (movingBox.intersects(playerBox)) entity.collisionOn = true;
        }

        for (Entity e : gp.entities) {
            if (e == entity) continue;
            Rectangle otherBox = new Rectangle(
                e.worldX + e.solidArea.x,
                e.worldY + e.solidArea.y,
                e.solidArea.width,
                e.solidArea.height
            );
            if (movingBox.intersects(otherBox)) {
                entity.collisionOn = true;
                break;
            }
        }
    }

    private Rectangle getProjectedBox(Entity entity) {
        int x = entity.worldX + entity.solidArea.x;
        int y = entity.worldY + entity.solidArea.y;
        int w = entity.solidArea.width;
        int h = entity.solidArea.height;

        return switch (entity.direction) {
            case "up"    -> new Rectangle(x, y - entity.speed, w, h);
            case "down"  -> new Rectangle(x, y + entity.speed, w, h);
            case "left"  -> new Rectangle(x - entity.speed, y, w, h);
            case "right" -> new Rectangle(x + entity.speed, y, w, h);
            default      -> new Rectangle(x, y, w, h);
        };
    }
}
