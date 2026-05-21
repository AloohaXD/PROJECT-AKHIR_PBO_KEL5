package ui;
import entity.Player;
import entity.PlayerStats;
import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HudRenderer {
    private final GamePanel gp;
    private static final Color GOLD    = new Color(255,200,50);
    private static final Color WHITE   = new Color(240,235,255);
    private int levelUpTimer = 0;
    private int lastLevel    = 1;
    private static final int LU_DUR   = 180;
    public  Rectangle inventoryClickArea = new Rectangle();
    private boolean inventoryHovered = false;

    public HudRenderer(GamePanel gp) { this.gp = gp; }

    public void draw(Graphics2D g2, Player player) {
        gp.getQuestTracker().updateNotif();
        drawProfile(g2, player);
        drawQuestPanel(g2);
        drawChapterBar(g2);
        drawQuestNotif(g2);
        if (levelUpTimer > 0) { drawLevelUp(g2, lastLevel); levelUpTimer--; }
    }

    private void drawProfile(Graphics2D g2, Player player) {
        PlayerStats s = player.getStats();
        int x=12, y=12, w=230, h=120;
        drawPanel(g2, x, y, w, h, new Color(10,8,25,220), player.heroClass.themeColor);
        // Hero icon
        g2.setColor(player.heroClass.themeColor.darker()); g2.fillRoundRect(x+8,y+8,44,44,8,8);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Serif",Font.BOLD,22));
        g2.drawString(String.valueOf(player.heroClass.displayName.charAt(0)), x+20, y+36);
        // Name & Level
        g2.setColor(GOLD);     g2.setFont(new Font("Serif",Font.BOLD,14));
        g2.drawString(player.heroClass.displayName + " — " + player.heroClass.kingdom, x+58, y+22);
        g2.setColor(WHITE);    g2.setFont(new Font("Monospaced",Font.BOLD,11));
        g2.drawString("Level " + s.getLevel() + "  ATK:" + s.getBaseAtk(), x+58, y+37);
        // Bars
        drawBar(g2,x+8,y+58,214,13,s.getHp(),s.getMaxHp(),new Color(50,20,20),hpColor(s),"HP "+s.getHp()+"/"+s.getMaxHp());
        drawBar(g2,x+8,y+75,214,13,s.getMp(),s.getMaxMp(),new Color(15,20,50),new Color(60,130,255),"MP "+s.getMp()+"/"+s.getMaxMp());
        drawBar(g2,x+8,y+92,214,13,s.getExp(),s.getExpToNext(),new Color(30,25,10),new Color(255,200,30),"EXP "+s.getExp()+"/"+s.getExpToNext());
    }

    private Color hpColor(PlayerStats s) {
        float r = (float)s.getHp()/s.getMaxHp();
        return r>0.5f ? new Color(60,210,80) : r>0.25f ? new Color(220,180,0) : new Color(220,50,50);
    }

    private void drawBar(Graphics2D g2,int x,int y,int w,int h,int cur,int max,Color bg,Color fg,String label){
        g2.setColor(bg); g2.fillRoundRect(x,y,w,h,4,4);
        if(max>0){int fw=(int)((float)cur/max*w);if(fw>0){g2.setColor(fg);g2.fillRoundRect(x,y,fw,h,4,4);}}
        g2.setColor(WHITE); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
        g2.drawString(label,x+3,y+h-2);
    }

    private void drawQuestPanel(Graphics2D g2) {
        QuestTracker qt = gp.getQuestTracker();
        QuestTracker.Quest q = qt.getActiveQuest(); if(q==null) return;
        int x=gp.SCREEN_WIDTH-215, y=12, w=205, h=80;
        drawPanel(g2, x, y, w, h, new Color(8,6,20,210), new Color(80,70,130,200));
        g2.setColor(new Color(200,180,255)); g2.setFont(new Font("Serif",Font.BOLD,11));
        g2.drawString("Quest Aktif  [Q:ganti]",x+8,y+18);
        g2.setColor(WHITE); g2.setFont(new Font("Monospaced",Font.PLAIN,11));
        g2.drawString(q.title(),x+8,y+34);
        int prog = qt.getActiveProgress();
        boolean done = prog >= q.targetCount();
        g2.setColor(done ? new Color(80,220,100) : new Color(180,160,220));
        g2.drawString(q.description()+" ["+prog+"/"+q.targetCount()+"]",x+8,y+50);
        g2.setColor(new Color(100,90,130)); g2.setFont(new Font("Monospaced",Font.PLAIN,9));
        g2.drawString("Reward: "+q.rewardText(),x+8,y+66);
    }

    private void drawChapterBar(Graphics2D g2) {
        main.ChapterManager cm = gp.getChapterManager();
        int x=gp.SCREEN_WIDTH/2-130, y=8, w=260, h=26;
        drawPanel(g2, x, y, w, h, new Color(10,5,30,210), new Color(180,100,255,200));
        g2.setColor(GOLD); g2.setFont(new Font("Serif",Font.BOLD,13));
        String txt = "Chapter " + cm.getCurrentChapter() + "/10 — " + cm.getChapterTitle();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, x+(w-fm.stringWidth(txt))/2, y+17);
        g2.setColor(new Color(180,180,220)); g2.setFont(new Font("Monospaced",Font.PLAIN,11));
        g2.drawString("⏱ "+cm.getElapsedString(), x+w+6, y+17);
    }

    private void drawQuestNotif(Graphics2D g2) {
        QuestTracker qt = gp.getQuestTracker();
        if (!qt.isNotifActive()) return;
        float alpha = qt.getNotifAlpha();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        String text = qt.getNotifText();
        g2.setFont(new Font("Serif",Font.BOLD,20));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int bx = (gp.SCREEN_WIDTH-tw)/2-16, by = gp.SCREEN_HEIGHT/2+80;
        g2.setColor(new Color(255,180,0,200)); g2.fillRoundRect(bx,by,tw+32,36,12,12);
        g2.setColor(new Color(0,0,0,80)); g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx,by,tw+32,36,12,12); g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.BLACK); g2.drawString(text,bx+16,by+25);
        g2.setComposite(AlphaComposite.SrcOver);
    }

    private void drawLevelUp(Graphics2D g2, int level) {
        float alpha = Math.min(1f,(float)levelUpTimer/30f)*Math.min(1f,(float)levelUpTimer/LU_DUR*5f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        String msg = "✦ LEVEL UP!  Lv " + level + " ✦";
        g2.setFont(new Font("Serif",Font.BOLD,32));
        FontMetrics fm = g2.getFontMetrics();
        int mx = (gp.SCREEN_WIDTH-fm.stringWidth(msg))/2, my = gp.SCREEN_HEIGHT/2-80;
        g2.setColor(new Color(0,0,0,130)); g2.fillRoundRect(mx-12,my-30,fm.stringWidth(msg)+24,44,12,12);
        g2.setColor(GOLD); g2.drawString(msg,mx,my);
        g2.setComposite(AlphaComposite.SrcOver);
    }

    public void drawEntityLabel(Graphics2D g2,String label,int sx,int sy,Color c){
        g2.setFont(new Font("Monospaced",Font.BOLD,9));
        FontMetrics fm = g2.getFontMetrics();
        int lx = sx + (GamePanel.TILE_SIZE-fm.stringWidth(label))/2;
        int ly = sy + GamePanel.TILE_SIZE + 12;
        g2.setColor(new Color(0,0,0,160)); g2.fillRoundRect(lx-2,ly-10,fm.stringWidth(label)+4,12,4,4);
        g2.setColor(c); g2.drawString(label,lx,ly);
    }

    private void drawPanel(Graphics2D g2,int x,int y,int w,int h,Color bg,Color border){
        g2.setColor(bg); g2.fillRoundRect(x,y,w,h,12,12);
        g2.setColor(border); g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x,y,w,h,12,12); g2.setStroke(new BasicStroke(1f));
    }

    public void triggerLevelUp(int newLevel){ levelUpTimer=LU_DUR; lastLevel=newLevel; }
    public void setInventoryHovered(boolean h){ inventoryHovered=h; }
}
