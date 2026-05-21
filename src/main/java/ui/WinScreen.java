package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WinScreen extends JPanel {
    private final int W, H;
    private final String elapsedTime;
    private int frame=0;
    private final Timer timer;
    private final Runnable onExit;
    private final Rectangle exitR=new Rectangle();
    private boolean exitHov=false;

    public WinScreen(int w, int h, String elapsedTime, Runnable onExit) {
        this.W=w; this.H=h; this.elapsedTime=elapsedTime; this.onExit=onExit;
        setOpaque(true); setBackground(Color.BLACK);
        timer=new Timer(16,e->{frame++;repaint();});
        timer.start();
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){exitHov=exitR.contains(e.getPoint());repaint();}
        });
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ if(exitR.contains(e.getPoint())){ timer.stop(); onExit.run(); } }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        // Gold burst BG
        GradientPaint bg=new GradientPaint(0,0,new Color(30,20,5),W,H,new Color(10,5,0));
        g2.setPaint(bg); g2.fillRect(0,0,W,H);
        float p=(float)(0.5+0.5*Math.sin(frame*0.05));
        g2.setColor(new Color(255,180,0,(int)(40*p))); g2.fillOval(W/2-300,H/2-300,600,600);
        g2.setColor(new Color(255,220,50,(int)(20*p))); g2.fillOval(W/2-200,H/2-200,400,400);
        // Stars
        for(int i=0;i<frame/3+20;i++){
            double a=i*2.4+frame*0.03; double r=50+i*8;
            int sx=(int)(W/2+Math.cos(a)*r), sy=(int)(H/2+Math.sin(a)*r);
            g2.setColor(new Color(255,220,50,Math.max(0,180-(int)(i*4))));
            g2.fillOval(sx-2,sy-2,4,4);
        }
        // VICTORY title
        g2.setFont(new Font("Serif",Font.BOLD,72));
        g2.setColor(new Color(255,200,50));
        String v="★ VICTORY! ★";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(v,(W-fm.stringWidth(v))/2,H/2-120);

        g2.setFont(new Font("Serif",Font.BOLD,28));
        g2.setColor(Color.WHITE);
        String s1="Demon King telah dikalahkan!";
        fm=g2.getFontMetrics();
        g2.drawString(s1,(W-fm.stringWidth(s1))/2,H/2-50);

        g2.setFont(new Font("Serif",Font.PLAIN,20));
        g2.setColor(new Color(200,190,220));
        String s2="Dunia kembali bersinar berkat para pahlawan Chromatik!";
        fm=g2.getFontMetrics();
        g2.drawString(s2,(W-fm.stringWidth(s2))/2,H/2-20);

        // Stopwatch
        g2.setFont(new Font("Monospaced",Font.BOLD,32));
        g2.setColor(new Color(255,220,80));
        String sw="⏱  Waktu Penyelesaian:  "+elapsedTime;
        fm=g2.getFontMetrics();
        int sw_x=(W-fm.stringWidth(sw))/2;
        g2.setColor(new Color(0,0,0,120)); g2.fillRoundRect(sw_x-16,H/2+20,fm.stringWidth(sw)+32,50,14,14);
        g2.setColor(new Color(255,220,80)); g2.drawString(sw,sw_x,H/2+52);

        g2.setFont(new Font("Serif",Font.ITALIC,16));
        g2.setColor(new Color(160,150,190));
        g2.drawString("Terima kasih telah bermain The Last Chromatic Warrior!",(W-350)/2,H/2+90);

        // Exit button
        drawBtn(g2,"← Kembali ke Menu Utama",W/2,H/2+150,exitR,exitHov,new Color(100,160,255));
    }

    private void drawBtn(Graphics2D g2,String label,int cx,int cy,Rectangle bounds,boolean hov,Color accent){
        g2.setFont(new Font("Serif",Font.BOLD,22));
        FontMetrics fm=g2.getFontMetrics();
        int bw=fm.stringWidth(label)+60,bh=46,bx=cx-bw/2,by=cy-bh/2;
        bounds.setBounds(bx,by,bw,bh);
        if(hov){g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40));g2.fillRoundRect(bx,by,bw,bh,14,14);}
        g2.setColor(hov?accent:accent.darker());
        g2.setStroke(new BasicStroke(hov?2.5f:2f)); g2.drawRoundRect(bx,by,bw,bh,14,14); g2.setStroke(new BasicStroke(1));
        g2.setColor(hov?Color.WHITE:new Color(200,195,215));
        g2.drawString(label,bx+30,by+bh/2+8);
    }
    public void stopAnimation() { timer.stop(); }
}
