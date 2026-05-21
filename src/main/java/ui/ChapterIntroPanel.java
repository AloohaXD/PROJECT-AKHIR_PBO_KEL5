package ui;
import javax.swing.*;
import java.awt.*;

/** Fullscreen chapter intro animation — shows "Chapter N: Title" then fades out. */
public class ChapterIntroPanel extends JPanel {
    private final int W, H;
    private int frame=0;
    private final int TOTAL=180; // 3 sec @ 60fps
    private Timer timer;
    private final Runnable onDone;
    private final int chapter;
    private final String title;
    private final boolean isFinal;

    public ChapterIntroPanel(int w, int h, int chapter, String title, boolean isFinal, Runnable onDone) {
        this.W=w; this.H=h; this.chapter=chapter; this.title=title;
        this.isFinal=isFinal; this.onDone=onDone;
        setOpaque(true); setBackground(Color.BLACK);
        timer=new Timer(16,e->{ frame++; repaint(); if(frame>=TOTAL){timer.stop();onDone.run();} });
        timer.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        // Alpha envelope: fade in 40f, hold 100f, fade out 40f
        float alpha;
        if(frame<40)       alpha=frame/40f;
        else if(frame<140) alpha=1f;
        else               alpha=Math.max(0f,1f-(frame-140)/40f);
        g2.setColor(new Color(0,0,0)); g2.fillRect(0,0,W,H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));

        // BG effect
        if(isFinal) {
            GradientPaint gp=new GradientPaint(0,0,new Color(60,0,80),W,H,new Color(20,0,40));
            g2.setPaint(gp); g2.fillRect(0,0,W,H);
            // Ominous glow
            float pulse=(float)(0.5+0.5*Math.sin(frame*0.1));
            g2.setColor(new Color(180,0,80,(int)(60*pulse))); g2.fillOval(W/2-200,H/2-200,400,400);
        }

        // "Chapter N"
        g2.setFont(new Font("Serif",Font.PLAIN,24)); g2.setColor(new Color(180,160,220));
        String ch=isFinal?"~ Chapter Final ~":"~ Chapter "+chapter+" ~";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(ch,(W-fm.stringWidth(ch))/2,H/2-60);

        // Title
        g2.setFont(new Font("Serif",Font.BOLD,isFinal?56:44));
        Color tc=isFinal?new Color(255,80,80):new Color(255,200,60);
        g2.setColor(tc); fm=g2.getFontMetrics();
        g2.drawString(title,(W-fm.stringWidth(title))/2,H/2);

        // Sub
        g2.setFont(new Font("Serif",Font.ITALIC,18));
        g2.setColor(new Color(160,150,180));
        String sub=isFinal?"Saatnya menghadapi ancaman terbesar...":"Petualangan berlanjut...";
        fm=g2.getFontMetrics();
        g2.drawString(sub,(W-fm.stringWidth(sub))/2,H/2+50);

        g2.setComposite(AlphaComposite.SrcOver);
    }

    public void stopAnimation() { timer.stop(); }
}
