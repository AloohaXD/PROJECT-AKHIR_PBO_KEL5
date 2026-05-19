package main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * KeyHandler.java  (DIPERBARUI)
 * ============================================================
 * Perubahan dari versi sebelumnya:
 *
 *  1. Tambah tombol [I] → inventoryJustPressed (buka inventory)
 *  2. Tambah tombol [Q] → questJustPressed (ganti quest di HUD)
 *
 * Semua flag "JustPressed" direset di akhir setiap frame oleh clearOneShots().
 * ============================================================
 */
public class KeyHandler extends KeyAdapter {

    // ─── Movement ─────────────────────────────────────────────
    public boolean upPressed;
    public boolean downPressed;
    public boolean leftPressed;
    public boolean rightPressed;

    // ─── Interaksi ────────────────────────────────────────────
    public boolean interactJustPressed;
    private boolean interactHeld;

    public boolean escJustPressed;
    private boolean escHeld;

    // ─── Combat ───────────────────────────────────────────────
    /** Basic attack — X atau J. Aktif satu frame saja. */
    public boolean attackJustPressed;
    private boolean attackHeld;

    // ─── Inventory ────────────────────────────────────────────
    /** Buka inventory — tombol I. Aktif satu frame saja. */
    public boolean inventoryJustPressed;
    private boolean inventoryHeld;

    // ─── Quest Tracker ────────────────────────────────────────
    /** Ganti quest yang ditampilkan di HUD — tombol Q. Aktif satu frame saja. */
    public boolean questJustPressed;
    private boolean questHeld;

    // =========================================================
    // KEY PRESSED
    // =========================================================
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Movement (WASD + Arrow Keys)
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    upPressed    = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed  = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  leftPressed  = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;

        // Interaksi — Z atau ENTER
        if ((code == KeyEvent.VK_Z || code == KeyEvent.VK_ENTER) && !interactHeld) {
            interactJustPressed = true;
            interactHeld        = true;
        }

        // Escape
        if (code == KeyEvent.VK_ESCAPE && !escHeld) {
            escJustPressed = true;
            escHeld        = true;
        }

        // Basic Attack — X atau J
        if ((code == KeyEvent.VK_X || code == KeyEvent.VK_J) && !attackHeld) {
            attackJustPressed = true;
            attackHeld        = true;
        }

        // Inventory — I
        if (code == KeyEvent.VK_I && !inventoryHeld) {
            inventoryJustPressed = true;
            inventoryHeld        = true;
        }

        // Quest Tracker — Q
        if (code == KeyEvent.VK_Q && !questHeld) {
            questJustPressed = true;
            questHeld        = true;
        }
    }

    // =========================================================
    // KEY RELEASED
    // =========================================================
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    upPressed    = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed  = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  leftPressed  = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;

        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_ENTER) interactHeld    = false;
        if (code == KeyEvent.VK_ESCAPE)                          escHeld         = false;
        if (code == KeyEvent.VK_X || code == KeyEvent.VK_J)     attackHeld      = false;
        if (code == KeyEvent.VK_I)                               inventoryHeld   = false;
        if (code == KeyEvent.VK_Q)                               questHeld       = false;
    }

    // =========================================================
    // RESET FLAG ONE-SHOT
    // =========================================================
    /**
     * Dipanggil di akhir setiap game-loop tick.
     * Menghapus semua flag "JustPressed" agar hanya aktif satu frame.
     */
    public void clearOneShots() {
        interactJustPressed  = false;
        escJustPressed       = false;
        attackJustPressed    = false;
        inventoryJustPressed = false;
        questJustPressed     = false;
    }
}