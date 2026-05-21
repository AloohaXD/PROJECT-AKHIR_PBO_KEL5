package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOverScreen extends JPanel {
    private final int W, H;
    private int frame=0;
    private final Timer timer;
    private final Runnable onRestart, onMenu;
    private final Rectangle restartR=new Rectangle(), menuR=new Rectangle();
    private boolean restartHov=false, menuHov=false;
    private final int chapter;

    public GameOverScreen(int w, int h, int chapter, Runnable onRestart, Runnable onMenu) {
        this.W=w; this.H=h; this.chapter=chapter; this.onRestart=onRestart; this.onMenu=onMenu;
        setOpaque(true); setBackground(Color.BLACK);
        timer=new Timer(16,e->{frame++;repaint();});
        timer.start();
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){
                restartHov=restartR.contains(e.getPoint());
                menuHov=menuR.contains(e.getPoint());
                repaint();
            }
        });
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(restartR.contains(e.getPoint())){ timer.stop(); onRestart.run(); }
                else if(menuR.contains(e.getPoint())){ timer.stop(); onMenu.run(); }
            }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        // Dark red BG
        GradientPaint bg=new GradientPaint(0,0,new Color(25,3,3),W,H,new Color(8,0,0));
        g2.setPaint(bg); g2.fillRect(0,0,W,H);
        float p=(float)(0.4+0.4*Math.sin(frame*0.04));
        g2.setColor(new Color(180,0,0,(int)(30*p))); g2.fillOval(W/2-300,H/2-250,600,500);
        // GAME OVER
        g2.setFont(new Font("Serif",Font.BOLD,80));
        g2.setColor(new Color(220,30,30));
        String go="GAME OVER";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(go,(W-fm.stringWidth(go))/2,H/2-120);
        // Shadow flicker
        float flick=(float)(0.3+0.3*Math.sin(frame*0.15));
        g2.setColor(new Color(255,60,60,(int)(100*flick)));
        g2.drawString(go,(W-fm.stringWidth(go))/2+2,H/2-120+2);

        g2.setFont(new Font("Serif",Font.PLAIN,22));
        g2.setColor(new Color(200,160,160));
        String s="Kamu gugur di Chapter "+chapter+"...";
        fm=g2.getFontMetrics();
        g2.drawString(s,(W-fm.stringWidth(s))/2,H/2-40);

        g2.setFont(new Font("Serif",Font.ITALIC,17));
        g2.setColor(new Color(160,120,120));
        String s2="Dunia masih membutuhkanmu. Bangkit dan coba lagi!";
        fm=g2.getFontMetrics();
        g2.drawString(s2,(W-fm.stringWidth(s2))/2,H/2-10);

        drawBtn(g2,"↺  Mulai Ulang (Chapter 1)",W/2,H/2+80,restartR,restartHov,new Color(220,80,80));
        drawBtn(g2,"← Menu Utama",W/2,H/2+145,menuR,menuHov,new Color(100,130,200));
    }

    private void drawBtn(Graphics2D g2,String label,int cx,int cy,Rectangle bounds,boolean hov,Color accent){
        g2.setFont(new Font("Serif",Font.BOLD,22));
        FontMetrics fm=g2.getFontMetrics();
        int bw=fm.stringWidth(label)+60,bh=46,bx=cx-bw/2,by=cy-bh/2;
        bounds.setBounds(bx,by,bw,bh);
        if(hov){g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40));g2.fillRoundRect(bx,by,bw,bh,14,14);}
        g2.setColor(hov?accent:accent.darker());
        g2.setStroke(new BasicStroke(hov?2.5f:2f)); g2.drawRoundRect(bx,by,bw,bh,14,14); g2.setStroke(new BasicStroke(1));
        g2.setColor(hov?Color.WHITE:new Color(200,185,185));
        g2.drawString(label,bx+30,by+bh/2+8);
    }
    public void stopAnimation() { timer.stop(); }
}
