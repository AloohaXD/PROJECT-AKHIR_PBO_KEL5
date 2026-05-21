package main;
import java.awt.event.*;
public class KeyHandler extends KeyAdapter {
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean interactJustPressed; private boolean interactHeld;
    public boolean escJustPressed;      private boolean escHeld;
    public boolean attackJustPressed;   private boolean attackHeld;
    public boolean inventoryJustPressed;private boolean inventoryHeld;
    public boolean questJustPressed;    private boolean questHeld;

    @Override public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (c==KeyEvent.VK_W||c==KeyEvent.VK_UP)    upPressed=true;
        if (c==KeyEvent.VK_S||c==KeyEvent.VK_DOWN)  downPressed=true;
        if (c==KeyEvent.VK_A||c==KeyEvent.VK_LEFT)  leftPressed=true;
        if (c==KeyEvent.VK_D||c==KeyEvent.VK_RIGHT) rightPressed=true;
        if ((c==KeyEvent.VK_Z||c==KeyEvent.VK_ENTER)&&!interactHeld){ interactJustPressed=true; interactHeld=true; }
        if (c==KeyEvent.VK_ESCAPE&&!escHeld){ escJustPressed=true; escHeld=true; }
        if ((c==KeyEvent.VK_X||c==KeyEvent.VK_J)&&!attackHeld){ attackJustPressed=true; attackHeld=true; }
        if (c==KeyEvent.VK_I&&!inventoryHeld){ inventoryJustPressed=true; inventoryHeld=true; }
        if (c==KeyEvent.VK_Q&&!questHeld){ questJustPressed=true; questHeld=true; }
    }
    @Override public void keyReleased(KeyEvent e) {
        int c = e.getKeyCode();
        if (c==KeyEvent.VK_W||c==KeyEvent.VK_UP)    upPressed=false;
        if (c==KeyEvent.VK_S||c==KeyEvent.VK_DOWN)  downPressed=false;
        if (c==KeyEvent.VK_A||c==KeyEvent.VK_LEFT)  leftPressed=false;
        if (c==KeyEvent.VK_D||c==KeyEvent.VK_RIGHT) rightPressed=false;
        if (c==KeyEvent.VK_Z||c==KeyEvent.VK_ENTER) interactHeld=false;
        if (c==KeyEvent.VK_ESCAPE) escHeld=false;
        if (c==KeyEvent.VK_X||c==KeyEvent.VK_J) attackHeld=false;
        if (c==KeyEvent.VK_I) inventoryHeld=false;
        if (c==KeyEvent.VK_Q) questHeld=false;
    }
    public void clearOneShots() {
        interactJustPressed=false; escJustPressed=false;
        attackJustPressed=false; inventoryJustPressed=false; questJustPressed=false;
    }
}
