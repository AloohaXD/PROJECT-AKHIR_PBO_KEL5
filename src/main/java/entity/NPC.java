package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * NPC — karakter diam yang bisa diajak bicara.
 */
public class NPC extends Entity {

    public String[] dialogLines = {};
    private int currentDialogIndex = 0;

    private float indicatorAlpha = 0f;
    private boolean nearPlayer   = false;
    private int   indicatorBob   = 0;

    public NPC(GamePanel gp) {
        super(gp);
        this.type         = EntityType.NPC;
        this.interactable = true;
        this.speed        = 0;

        width  = GamePanel.TILE_SIZE;
        height = GamePanel.TILE_SIZE;

        solidArea        = new Rectangle(8, 16, 32, 28);
        solidAreaDefault = new Rectangle(solidArea);

        generatePlaceholderSprites();
    }

    @Override
    public void update() {
        int dx = Math.abs((gp.player.worldX + gp.player.width  / 2) - (worldX + width  / 2));
        int dy = Math.abs((gp.player.worldY + gp.player.height / 2) - (worldY + height / 2));
        nearPlayer = (dx < GamePanel.TILE_SIZE * 2 && dy < GamePanel.TILE_SIZE * 2);

        if (nearPlayer && indicatorAlpha < 1f)       indicatorAlpha = Math.min(1f, indicatorAlpha + 0.08f);
        else if (!nearPlayer && indicatorAlpha > 0f) indicatorAlpha = Math.max(0f, indicatorAlpha - 0.08f);

        indicatorBob++;
        animationSpeed = 30;
        advanceAnimation(false);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!gp.camera.isVisible(worldX, worldY, width, height)) return;

        int sx = gp.camera.toScreenX(worldX);
        int sy = gp.camera.toScreenY(worldY);

        BufferedImage frame = walkDown[0];
        if (frame != null) g2.drawImage(frame, sx, sy, width, height, null);

        if (indicatorAlpha > 0f) drawIndicator(g2, sx, sy);
    }

    private void drawIndicator(Graphics2D g2, int sx, int sy) {
        float bob = (float) Math.sin(indicatorBob * 0.1) * 3f;
        int bubbleX = sx + width / 2 - 12;
        int bubbleY = sy - 36 + (int) bob;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, indicatorAlpha));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(bubbleX, bubbleY, 24, 22, 8, 8);

        int[] tailX = { bubbleX + 8, bubbleX + 14, bubbleX + 6 };
        int[] tailY = { bubbleY + 22, bubbleY + 22, bubbleY + 30 };
        g2.fillPolygon(tailX, tailY, 3);

        g2.setColor(new Color(40, 40, 40));
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString("!", bubbleX + 8, bubbleY + 16);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public String getCurrentDialog() {
        if (dialogLines == null || dialogLines.length == 0) return "...";
        return dialogLines[currentDialogIndex];
    }

    public boolean advanceDialog() {
        currentDialogIndex++;
        return currentDialogIndex < dialogLines.length;
    }

    public void resetDialog() { currentDialogIndex = 0; }

    private void generatePlaceholderSprites() {
        Color bodyColor = new Color(220, 120, 120);
        Color darkColor = bodyColor.darker();
        Color shadow    = new Color(0, 0, 0, 60);

        for (int i = 0; i < 4; i++) {
            walkDown[i] = walkUp[i] = walkLeft[i] = walkRight[i] = createNPCSprite(bodyColor, darkColor, shadow);
        }
        idleImage = walkDown[0];
    }

    private BufferedImage createNPCSprite(Color body, Color dark, Color shadow) {
        int w = GamePanel.TILE_SIZE, h = GamePanel.TILE_SIZE;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(shadow);   g.fillOval(w / 2 - 10, h - 10, 20, 8);
        g.setColor(body);     g.fillRoundRect(w / 2 - 9, 16, 18, 20, 6, 6);
        g.setColor(new Color(255, 220, 180)); g.fillOval(w / 2 - 8, 2, 16, 16);
        g.setColor(dark);
        g.fillOval(w / 2 - 4, 8, 3, 4);
        g.fillOval(w / 2 + 1, 8, 3, 4);
        g.fillRoundRect(w/2 - 7, 32, 7, 10, 3, 3);
        g.fillRoundRect(w/2,     32, 7, 10, 3, 3);
        g.dispose();
        return img;
    }
}
