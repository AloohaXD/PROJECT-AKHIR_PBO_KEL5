package ui;
import main.AudioManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingsPanel extends JPanel {
    private final Runnable onBack;
    private int animFrame=0;
    private final Timer timer;
    private final Rectangle backR=new Rectangle();
    private boolean backHov=false;
    private float bgmVol, sfxVol;
    // Slider rects
    private final Rectangle bgmSlider=new Rectangle(), sfxSlider=new Rectangle();
    private boolean draggingBgm=false, draggingSfx=false;

    public SettingsPanel(int w, int h, Runnable onBack) {
        this.onBack=onBack;
        bgmVol=AudioManager.get().getBgmVolume();
        sfxVol=AudioManager.get().getSfxVolume();
        setOpaque(true); setBackground(Color.BLACK);
        setPreferredSize(new Dimension(w,h));
        timer=new Timer(16,e->{animFrame++;repaint();});
        timer.start();

        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ if(backR.contains(e.getPoint())){ timer.stop(); onBack.run(); } }
            public void mousePressed(MouseEvent e){
                if(bgmSlider.contains(e.getPoint())) draggingBgm=true;
                if(sfxSlider.contains(e.getPoint())) draggingSfx=true;
                handleDrag(e);
            }
            public void mouseReleased(MouseEvent e){ draggingBgm=false; draggingSfx=false; }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){ backHov=backR.contains(e.getPoint()); repaint(); }
            public void mouseDragged(MouseEvent e){ handleDrag(e); }
        });
    }

    private void handleDrag(MouseEvent e) {
        if(draggingBgm){ bgmVol=clampSlider(e.getX(),bgmSlider); AudioManager.get().setBgmVolume(bgmVol); }
        if(draggingSfx){ sfxVol=clampSlider(e.getX(),sfxSlider); AudioManager.get().setSfxVolume(sfxVol); }
        repaint();
    }
    private float clampSlider(int mx, Rectangle r){
        return Math.max(0f,Math.min(1f,(float)(mx-r.x)/r.width));
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int W=getWidth(), H=getHeight();
        GradientPaint bg=new GradientPaint(0,0,new Color(5,3,18),0,H,new Color(12,6,30));
        g2.setPaint(bg); g2.fillRect(0,0,W,H);

        g2.setFont(new Font("Serif",Font.BOLD,40));
        g2.setColor(new Color(200,180,255));
        String title="⚙ Pengaturan";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(title,(W-fm.stringWidth(title))/2,100);

        drawVolumeSlider(g2,W/2-150,200,300,"Volume BGM (Musik)",bgmVol,bgmSlider);
        drawVolumeSlider(g2,W/2-150,290,300,"Volume SFX (Efek Suara)",sfxVol,sfxSlider);

        // Tip
        g2.setFont(new Font("Monospaced",Font.ITALIC,13));
        g2.setColor(new Color(120,110,150));
        g2.drawString("Klik & seret slider untuk mengatur volume",W/2-160,380);

        // Back button
        drawBtn(g2,"← Kembali ke Menu",W/2,450,backR,backHov,new Color(100,160,255));
    }

    private void drawVolumeSlider(Graphics2D g2, int x, int y, int w, String label, float vol, Rectangle r) {
        g2.setFont(new Font("Serif",Font.BOLD,16));
        g2.setColor(new Color(200,190,230));
        g2.drawString(label,x,y);
        int sy=y+12, sh=18;
        r.setBounds(x,sy,w,sh);
        g2.setColor(new Color(40,35,60)); g2.fillRoundRect(x,sy,w,sh,8,8);
        int fw=(int)(w*vol);
        GradientPaint gp=new GradientPaint(x,sy,new Color(80,60,200),x+fw,sy,new Color(160,100,255));
        g2.setPaint(gp); if(fw>0) g2.fillRoundRect(x,sy,fw,sh,8,8);
        g2.setColor(new Color(180,160,255)); g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x,sy,w,sh,8,8); g2.setStroke(new BasicStroke(1));
        // Thumb
        int tx=x+fw-6;
        g2.setColor(Color.WHITE); g2.fillOval(tx,sy+1,16,sh-2);
        g2.setColor(new Color(200,180,255)); g2.drawOval(tx,sy+1,16,sh-2);
        // Percent
        g2.setColor(new Color(180,170,210)); g2.setFont(new Font("Monospaced",Font.PLAIN,12));
        g2.drawString(String.format("%d%%",(int)(vol*100)),x+w+10,sy+sh-4);
    }

    private void drawBtn(Graphics2D g2, String label, int cx, int cy, Rectangle bounds, boolean hov, Color accent) {
        g2.setFont(new Font("Serif",Font.BOLD,20));
        FontMetrics fm=g2.getFontMetrics();
        int bw=fm.stringWidth(label)+60,bh=42,bx=cx-bw/2,by=cy-bh/2;
        bounds.setBounds(bx,by,bw,bh);
        if(hov){g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40));g2.fillRoundRect(bx,by,bw,bh,12,12);}
        g2.setColor(hov?accent:accent.darker());
        g2.setStroke(new BasicStroke(hov?2.5f:1.5f)); g2.drawRoundRect(bx,by,bw,bh,12,12); g2.setStroke(new BasicStroke(1));
        g2.setColor(hov?Color.WHITE:new Color(200,195,215));
        g2.drawString(label,bx+30,by+bh/2+7);
    }

    public void stopAnimation() { timer.stop(); }
}
