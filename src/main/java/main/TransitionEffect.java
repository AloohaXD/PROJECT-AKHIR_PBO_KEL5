package main;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class TransitionEffect {
    public enum Phase { INACTIVE, DISTORT, HOLD, FADE_OUT, DONE }
    private Phase phase = Phase.INACTIVE;
    private int timer = 0;
    private static final int DISTORT_FRAMES=35, HOLD_FRAMES=15, FADE_OUT_FRAMES=25;
    private BufferedImage snapshot;
    private final Random rng = new Random();
    private float[] noiseX, noiseY;
    private static final int STRIPS=50;
    private Runnable onMidpoint;
    private float overlayAlpha=0f;

    public void start(BufferedImage snap, Runnable onMidpoint) {
        this.snapshot=snap; this.onMidpoint=onMidpoint;
        phase=Phase.DISTORT; timer=0; overlayAlpha=0f;
        noiseX=new float[STRIPS]; noiseY=new float[STRIPS];
        for(int i=0;i<STRIPS;i++){noiseX[i]=(rng.nextFloat()-0.5f)*20f;noiseY[i]=(rng.nextFloat()-0.5f)*4f;}
    }

    public void update() {
        if(phase==Phase.INACTIVE||phase==Phase.DONE) return;
        timer++;
        if(phase==Phase.DISTORT) {
            overlayAlpha=Math.min(1f,(float)timer/DISTORT_FRAMES);
            if(timer>=DISTORT_FRAMES){phase=Phase.HOLD;timer=0;}
        } else if(phase==Phase.HOLD) {
            if(timer==1&&onMidpoint!=null) onMidpoint.run();
            if(timer>=HOLD_FRAMES){phase=Phase.FADE_OUT;timer=0;}
        } else if(phase==Phase.FADE_OUT) {
            overlayAlpha=1f-(float)timer/FADE_OUT_FRAMES;
            if(timer>=FADE_OUT_FRAMES){phase=Phase.DONE;overlayAlpha=0f;}
        }
    }

    public void draw(Graphics2D g2, int w, int h) {
        if(phase==Phase.INACTIVE||phase==Phase.DONE) return;
        if(snapshot!=null&&phase==Phase.DISTORT&&timer<DISTORT_FRAMES) {
            float t=(float)timer/DISTORT_FRAMES;
            int sh=h/STRIPS;
            for(int i=0;i<STRIPS;i++) {
                int sy=i*sh, dy=(int)(i*sh+noiseY[i]*t*8);
                int ox=(int)(noiseX[i]*t);
                g2.drawImage(snapshot,ox,dy,ox+w,dy+sh,0,sy,w,sy+sh,null);
            }
        }
        g2.setColor(new Color(0,0,0,Math.min(255,(int)(overlayAlpha*255))));
        g2.fillRect(0,0,w,h);
    }

    public boolean isActive() { return phase!=Phase.INACTIVE&&phase!=Phase.DONE; }
}
