package ui;

import combat.HeroClass;
import main.ImageCache;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * HeroSelectPanel — 5 Chromatic Warriors with proper sprites from ImageCache.
 * FIX: uses preloaded images for smooth rendering.
 */
public class HeroSelectPanel extends JPanel {
    public interface OnSelectDone { void done(HeroClass.ClassType[] party, HeroClass.ClassType leader); }
    private final OnSelectDone callback;
    private final int W, H;
    private final Timer timer;
    private int animFrame = 0;
    private static final HeroClass.ClassType[] CLASSES = HeroClass.ClassType.values();
    private int leaderIdx = 0, hov = -1;
    private final Rectangle[] cardR   = new Rectangle[CLASSES.length];
    private final Rectangle  confirmR = new Rectangle();
    private boolean confirmHov = false;

    private static final String[] HP_STR  = {"250","300","200","200","150"};
    private static final String[] ATK_STR = {"Sedang","Rendah","Tinggi","Sangat Tinggi","Tertinggi"};
    private static final String[] DEF_STR = {"Tinggi","Tertinggi","Rendah","Rendah","Terendah"};
    private static final String[] KGD_STR = {"Kerajaan Merah","Kerajaan Biru","Kerajaan Hijau","Kerajaan Ungu","Kerajaan Hitam"};

    public HeroSelectPanel(int w, int h, OnSelectDone callback) {
        this.W=w; this.H=h; this.callback=callback;
        setOpaque(true); setBackground(Color.BLACK);
        for (int i=0; i<cardR.length; i++) cardR[i]=new Rectangle();
        timer = new Timer(16, e -> { animFrame++; repaint(); });
        timer.start();
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){
                hov=-1;
                for(int i=0;i<cardR.length;i++) if(cardR[i].contains(e.getPoint())) hov=i;
                confirmHov=confirmR.contains(e.getPoint()); repaint();
            }
        });
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                for(int i=0;i<cardR.length;i++){
                    if(cardR[i].contains(e.getPoint())){ leaderIdx=i; repaint(); return; }
                }
                if(confirmR.contains(e.getPoint())) confirm();
            }
        });
    }

    private void confirm() {
        java.util.List<HeroClass.ClassType> party = new ArrayList<>();
        party.add(CLASSES[leaderIdx]);
        for(int i=0;i<CLASSES.length;i++) if(i!=leaderIdx) party.add(CLASSES[i]);
        timer.stop();
        callback.done(party.toArray(new HeroClass.ClassType[0]), CLASSES[leaderIdx]);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        GradientPaint bg=new GradientPaint(0,0,new Color(5,3,18),0,H,new Color(12,6,30));
        g2.setPaint(bg); g2.fillRect(0,0,W,H);

        g2.setFont(new Font("Serif",Font.BOLD,36));
        g2.setColor(new Color(255,200,50));
        String t="✦ Pilih Hero Chromatikmu ✦";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(t,(W-fm.stringWidth(t))/2,52);

        g2.setFont(new Font("Serif",Font.ITALIC,14));
        g2.setColor(new Color(160,150,200));
        String sub="Klik kartu untuk jadikan Leader · Semua 5 hero ikut dalam party";
        fm=g2.getFontMetrics();
        g2.drawString(sub,(W-fm.stringWidth(sub))/2,76);

        int cardW=152,cardH=330,gap=8;
        int totalW=CLASSES.length*(cardW+gap)-gap;
        int sx=(W-totalW)/2, cy=95;
        for(int i=0;i<CLASSES.length;i++){
            cardR[i].setBounds(sx+i*(cardW+gap),cy,cardW,cardH);
            drawCard(g2,sx+i*(cardW+gap),cy,cardW,cardH,i);
        }
        drawConfirmBtn(g2,W/2,cy+cardH+48,confirmR,confirmHov);
    }

    private void drawCard(Graphics2D g2,int x,int y,int w,int h,int i){
        HeroClass.ClassType cls=CLASSES[i];
        boolean leader=(i==leaderIdx), isHov=(i==hov);
        Color theme=cls.themeColor;
        // Background
        Color bg=new Color(theme.getRed()/7+8,theme.getGreen()/7+8,theme.getBlue()/7+8,250);
        g2.setColor(bg); g2.fillRoundRect(x,y,w,h,16,16);
        // Border
        float p=(float)(0.7+0.3*Math.sin(animFrame*0.08));
        Color border=leader?new Color(255,200,50):isHov?theme.brighter():
            new Color(theme.getRed(),theme.getGreen(),theme.getBlue(),(int)(200*p));
        g2.setColor(border); g2.setStroke(new BasicStroke(leader?3f:isHov?2.5f:1.5f));
        g2.drawRoundRect(x,y,w,h,16,16); g2.setStroke(new BasicStroke(1f));

        // Hero sprite — FIX: use ImageCache, draw contained (aspect ratio preserved)
        BufferedImage sprite=ImageCache.get().get(cls.spritePath);
        int imgAreaX=x+8,imgAreaY=y+10,imgAreaW=w-16,imgAreaH=120;
        if(sprite!=null){
            // Contain: fit inside area preserving aspect ratio
            float scaleX=(float)imgAreaW/sprite.getWidth();
            float scaleY=(float)imgAreaH/sprite.getHeight();
            float scale=Math.min(scaleX,scaleY);
            int dw=(int)(sprite.getWidth()*scale), dh=(int)(sprite.getHeight()*scale);
            int dx=imgAreaX+(imgAreaW-dw)/2, dy=imgAreaY+(imgAreaH-dh)/2;
            g2.drawImage(sprite,dx,dy,dw,dh,null);
        } else {
            g2.setColor(theme); g2.setFont(new Font("Serif",Font.PLAIN,44));
            g2.drawString("⚔",x+w/2-16,y+70);
        }

        // Name
        g2.setFont(new Font("Serif",Font.BOLD,15)); g2.setColor(Color.WHITE);
        fm_center(g2,cls.displayName,x,y+imgAreaH+24,w);
        // Kingdom
        g2.setFont(new Font("Monospaced",Font.PLAIN,9)); g2.setColor(theme.brighter());
        fm_center(g2,KGD_STR[i],x,y+imgAreaH+38,w);

        // Stats box
        int bx=x+6,by=y+imgAreaH+45,bw=w-12,bh=110;
        g2.setColor(new Color(0,0,0,140)); g2.fillRoundRect(bx,by,bw,bh,8,8);
        g2.setColor(new Color(theme.getRed(),theme.getGreen(),theme.getBlue(),90));
        g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(bx,by,bw,bh,8,8);

        g2.setFont(new Font("Monospaced",Font.BOLD,9));
        g2.setColor(new Color(255,210,100)); g2.drawString("STATS",bx+5,by+12);
        g2.setFont(new Font("Monospaced",Font.PLAIN,9));
        g2.setColor(new Color(200,190,230));
        g2.drawString("❤ HP  : "+HP_STR[i],  bx+5,by+26);
        g2.drawString("⚔ ATK : "+ATK_STR[i],bx+5,by+39);
        g2.drawString("🛡 DEF : "+DEF_STR[i],bx+5,by+52);
        g2.drawString("💧 MP  : "+cls.baseMp, bx+5,by+65);
        // Lore short
        g2.setFont(new Font("Serif",Font.ITALIC,10)); g2.setColor(new Color(160,150,190));
        String[] lore=getLore(i);
        for(int li=0;li<lore.length;li++) g2.drawString(lore[li],bx+5,by+80+li*12);

        // Leader badge
        if(leader){
            g2.setFont(new Font("Serif",Font.BOLD,11));
            g2.setColor(new Color(0,0,0,180));
            g2.fillRoundRect(x+w/2-34,y+h-22,68,16,6,6);
            g2.setColor(new Color(255,200,50));
            g2.drawString("★ LEADER",x+w/2-30,y+h-10);
        }
    }

    private void fm_center(Graphics2D g2,String s,int x,int y,int w){
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(s,x+(w-fm.stringWidth(s))/2,y);
    }
    private String[] getLore(int i){
        return switch(i){
            case 0->new String[]{"Pertahanan tinggi,","damage sedang"};
            case 1->new String[]{"Pertahanan tertinggi,","damage terendah"};
            case 2->new String[]{"Damage tinggi,","jarak jauh"};
            case 3->new String[]{"Damage sihir besar,","pertahanan rendah"};
            default->new String[]{"Damage tertinggi,","HP terendah"};
        };
    }
    private void drawConfirmBtn(Graphics2D g2,int cx,int cy,Rectangle bounds,boolean hov){
        String label="▶ MULAI PETUALANGAN!";
        g2.setFont(new Font("Serif",Font.BOLD,22));
        FontMetrics fm=g2.getFontMetrics();
        int bw=fm.stringWidth(label)+80,bh=50,bx=cx-bw/2,by=cy-bh/2;
        bounds.setBounds(bx,by,bw,bh);
        float p=(float)(0.85+0.15*Math.sin(animFrame*0.1));
        Color ac=new Color(80,200,80);
        g2.setColor(hov?new Color(80,200,80,70):new Color(60,160,60,35));
        g2.fillRoundRect(bx,by,bw,bh,14,14);
        g2.setColor(hov?ac:new Color((int)(ac.getRed()*p),(int)(ac.getGreen()*p),(int)(ac.getBlue()*p)));
        g2.setStroke(new BasicStroke(hov?3f:2f)); g2.drawRoundRect(bx,by,bw,bh,14,14); g2.setStroke(new BasicStroke(1f));
        g2.setColor(hov?Color.WHITE:new Color(200,240,200));
        g2.drawString(label,bx+40,by+bh/2+8);
    }
    public void stopAnimation(){ timer.stop(); }
}
