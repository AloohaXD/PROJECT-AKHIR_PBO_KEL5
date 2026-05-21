package entity;
import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;

public class NPC extends Entity {
    public String[] dialogLines={};
    private int dialogIdx=0;
    private float indicatorAlpha=0f;
    private int indicatorBob=0;
    private boolean nearPlayer=false;

    public NPC(GamePanel gp) {
        super(gp); type=EntityType.NPC; interactable=true; speed=0;
        width=GamePanel.TILE_SIZE; height=GamePanel.TILE_SIZE;
        solidArea=new Rectangle(8,16,32,28); solidAreaDefault=new Rectangle(solidArea);
        generateSprites();
    }
    private void generateSprites() {
        for(int f=0;f<4;f++) {
            BufferedImage img=new BufferedImage(GamePanel.TILE_SIZE,GamePanel.TILE_SIZE,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=img.createGraphics();
            g.setColor(new Color(80,160,80)); g.fillRoundRect(8,10,32,34,8,8);
            g.setColor(new Color(255,220,180)); g.fillOval(14,8,20,20);
            g.setColor(new Color(200,160,60)); g.fillRect(14,6,20,8);
            g.dispose(); walkDown[f]=walkUp[f]=walkLeft[f]=walkRight[f]=img;
        }
        idleImage=walkDown[0];
    }
    @Override public void update() {
        int dx=Math.abs((gp.player.worldX+gp.player.width/2)-(worldX+width/2));
        int dy=Math.abs((gp.player.worldY+gp.player.height/2)-(worldY+height/2));
        nearPlayer=(dx<GamePanel.TILE_SIZE*2&&dy<GamePanel.TILE_SIZE*2);
        if(nearPlayer&&indicatorAlpha<1f)     indicatorAlpha=Math.min(1f,indicatorAlpha+0.07f);
        else if(!nearPlayer&&indicatorAlpha>0f) indicatorAlpha=Math.max(0f,indicatorAlpha-0.07f);
        indicatorBob++;
    }
    @Override public void draw(Graphics2D g2) {
        if(!gp.camera.isVisible(worldX,worldY,width,height)) return;
        int sx=getScreenX(), sy=getScreenY();
        g2.drawImage(idleImage,sx,sy,width,height,null);
        if(indicatorAlpha>0f) {
            float bob=(float)Math.sin(indicatorBob*0.1)*3f;
            int bx=sx+width/2-10, by=sy-30+(int)bob;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,indicatorAlpha));
            g2.setColor(Color.WHITE); g2.fillRoundRect(bx,by,20,18,6,6);
            g2.setColor(new Color(40,40,40)); g2.setFont(new Font("Monospaced",Font.BOLD,14));
            g2.drawString("!",bx+6,by+13);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
    public String getCurrentDialog() {
        return dialogLines.length>0?dialogLines[dialogIdx%dialogLines.length]:"...";
    }
    public void advanceDialog() { if(dialogLines.length>0) dialogIdx=(dialogIdx+1)%dialogLines.length; }
    public void resetDialog()   { dialogIdx=0; }
    /** Returns true after all dialog lines shown (cycled back to 0 after last) */
    public boolean isDialogDone() { return dialogLines.length>0 && dialogIdx==0; }
}
