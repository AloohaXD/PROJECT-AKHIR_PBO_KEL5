package ui;

import main.ImageCache;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * MainMenuPanel — uses preloaded images.
 * FIX: background now stretches to fill full screen (no crop).
 * FIX: uses ImageCache instead of loading per-frame.
 */
public class MainMenuPanel extends JPanel {
    private final Runnable onStart, onSettings, onExit;
    private final int W, H;
    private int animFrame = 0;
    private final Timer timer;
    private final Rectangle startR   = new Rectangle();
    private final Rectangle settingR = new Rectangle();
    private final Rectangle exitR    = new Rectangle();
    private boolean startHov=false, settingHov=false, exitHov=false;

    public MainMenuPanel(int w, int h, Runnable onStart, Runnable onSettings, Runnable onExit) {
        this.W=w; this.H=h; this.onStart=onStart; this.onSettings=onSettings; this.onExit=onExit;
        setOpaque(true); setBackground(Color.BLACK);
        timer = new Timer(16, e -> { animFrame++; repaint(); });
        timer.start();
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                startHov   = startR.contains(e.getPoint());
                settingHov = settingR.contains(e.getPoint());
                exitHov    = exitR.contains(e.getPoint());
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if      (startR.contains(e.getPoint()))   { timer.stop(); onStart.run(); }
                else if (settingR.contains(e.getPoint())) onSettings.run();
                else if (exitR.contains(e.getPoint()))    System.exit(0);
            }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);

        // FIX: background stretched to fill entire screen (no crop, no letterbox)
        BufferedImage bg = ImageCache.get().get("/assets/images/main_menu/background.png");
        if (bg != null) {
            g2.drawImage(bg, 0, 0, W, H, null);   // stretch to fill completely
        } else {
            GradientPaint gp = new GradientPaint(0,0,new Color(5,3,18),0,H,new Color(12,6,30));
            g2.setPaint(gp); g2.fillRect(0,0,W,H);
        }

        // Dark overlay for readability
        g2.setColor(new Color(0,0,0,90));
        g2.fillRect(0,0,W,H);

        // Title (top 240px)
        drawTitle(g2);

        // Buttons — centred vertically in remaining space
        int btnW=260, btnH=70, gap=14;
        int totalH = 3*btnH + 2*gap;
        int btnX = W/2-btnW/2;
        int startY = H/2-totalH/2 + 30;

        drawImageButton(g2, ImageCache.get().get("/assets/images/main_menu/btn_start.png"),
            btnX, startY,            btnW, btnH, startR,   startHov);
        drawImageButton(g2, ImageCache.get().get("/assets/images/main_menu/btn_setting.png"),
            btnX, startY+btnH+gap,   btnW, btnH, settingR, settingHov);
        drawImageButton(g2, ImageCache.get().get("/assets/images/main_menu/btn_exit.png"),
            btnX, startY+2*(btnH+gap),btnW, btnH, exitR,   exitHov);

        // Credit
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(200, 190, 220, 180));
        g2.drawString("The Last Chromatic Warrior v3.1 — Tugas Akhir PBO", 12, H-10);
    }

    private void drawImageButton(Graphics2D g2, BufferedImage img,
                                  int x, int y, int w, int h,
                                  Rectangle bounds, boolean hov) {
        bounds.setBounds(x, y, w, h);
        if (hov) {
            g2.setColor(new Color(255,255,200,40));
            g2.fillRoundRect(x-4, y-4, w+8, h+8, 14,14);
            g2.setColor(new Color(255,220,80,180));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(x-4, y-4, w+8, h+8, 14,14);
            g2.setStroke(new BasicStroke(1f));
        }
        if (img != null) {
            float alpha = hov ? 1.0f : 0.90f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            // FIX: draw button image stretched to fit button area
            g2.drawImage(img, x, y, w, h, null);
            g2.setComposite(AlphaComposite.SrcOver);
        } else {
            g2.setColor(hov ? new Color(255,220,80,50) : new Color(0,0,0,60));
            g2.fillRoundRect(x,y,w,h,12,12);
            g2.setColor(hov ? new Color(255,220,80) : new Color(200,180,220));
            g2.setStroke(new BasicStroke(2f)); g2.drawRoundRect(x,y,w,h,12,12); g2.setStroke(new BasicStroke(1f));
        }
    }

    private void drawTitle(Graphics2D g2) {
        String t1="The Last Chromatic", t2="Warrior";
        int ty = H/2 - 180;
        // Dark panel behind title
        g2.setColor(new Color(0,0,0,130));
        g2.fillRoundRect(W/2-310, ty-50, 620, 130, 20,20);

        g2.setFont(new Font("Serif",Font.BOLD,50));
        FontMetrics fm = g2.getFontMetrics();
        // Shadow
        g2.setColor(new Color(0,0,0,160));
        g2.drawString(t1,(W-fm.stringWidth(t1))/2+2,ty+2);
        // Chromatic
        int[][] offs={{-2,0},{2,0},{0,-1}};
        Color[] chroma={new Color(1f,0.1f,0.1f,0.4f),new Color(0.1f,0.6f,1f,0.4f),new Color(0.1f,1f,0.3f,0.3f)};
        for(int k=0;k<3;k++){g2.setColor(chroma[k]);g2.drawString(t1,(W-fm.stringWidth(t1))/2+offs[k][0],ty+offs[k][1]);}
        // Wave
        char[] cs=t1.toCharArray(); int cx=(W-fm.stringWidth(t1))/2;
        for(int i=0;i<cs.length;i++){
            int wy=(int)(Math.sin(animFrame*0.05+i*0.35)*4);
            g2.setColor(Color.WHITE); g2.drawString(String.valueOf(cs[i]),cx,ty+wy); cx+=fm.charWidth(cs[i]);
        }
        // "Warrior"
        g2.setFont(new Font("Serif",Font.BOLD,64));
        fm=g2.getFontMetrics();
        Color wc=new Color((float)(0.7+0.3*Math.sin(animFrame*0.03)),0.85f,1f);
        g2.setColor(new Color(0,0,0,140)); g2.drawString(t2,(W-fm.stringWidth(t2))/2+2,ty+66);
        g2.setColor(wc); g2.drawString(t2,(W-fm.stringWidth(t2))/2,ty+64);
    }

    public void stopAnimation() { timer.stop(); }
}
