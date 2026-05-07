import javax.swing.*;
import java.awt.*;

/**
 * GameFrame.java
 * ============================================================
 * Kelas utama (Entry Point) yang mengelola navigasi antar layar.
 * Menggunakan CardLayout untuk berpindah antara:
 *   - "MENU"      → MenuPanel      (Layar 1: Main Menu)
 *   - "SELECTION" → SelectionPanel (Layar 2: Character Selection)
 *   - "COMBAT"    → CombatPanel    (Layar 3: Area Pertempuran)
 *
 * ALUR NAVIGASI:
 *   MENU → [Klik Start] → SELECTION → [Pilih Class] → COMBAT
 *   COMBAT → [Menu Utama] → MENU
 *   COMBAT → [Main Lagi]  → COMBAT (reset dengan class sama)
 * ============================================================
 */
public class GameFrame extends JFrame {

    // ── Konstanta Nama Layar (untuk CardLayout) ───────────────
    private static final String SCREEN_MENU      = "MENU";
    private static final String SCREEN_SELECTION = "SELECTION";
    private static final String SCREEN_COMBAT    = "COMBAT";

    // ── Komponen Utama ────────────────────────────────────────
    private final CardLayout   cardLayout;
    private final JPanel       screenContainer;

    private final MenuPanel      menuPanel;
    private final SelectionPanel selectionPanel;
    private final CombatPanel    combatPanel;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public GameFrame() {
        // --- Konfigurasi JFrame ---
        setTitle("The Last Chromatic Warrior");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 650);
        setMinimumSize(new Dimension(720, 580));
        setLocationRelativeTo(null);
        setResizable(true);

        // --- CardLayout: satu container, banyak layar ---
        cardLayout       = new CardLayout();
        screenContainer  = new JPanel(cardLayout);
        screenContainer.setBackground(new Color(8, 8, 20));

        // --- Instansiasi semua panel dengan callback navigasi ---
        // PENTING: combatPanel harus dibuat LEBIH DULU sebelum selectionPanel,
        // karena lambda di selectionPanel mereferensikan combatPanel.

        // Layar 3: Combat (dibuat pertama agar bisa direferensikan di bawah)
        combatPanel = new CombatPanel(this::showMenu);

        // Layar 1: Menu → saat klik Start, pindah ke Selection
        menuPanel = new MenuPanel(this::showSelection);

        // Layar 2: Selection → saat pilih class, pindah ke Combat
        selectionPanel = new SelectionPanel(profile -> {
            combatPanel.startCombat(profile);
            showScreen(SCREEN_COMBAT);
        });

        // --- Daftarkan semua panel ke container ---
        screenContainer.add(menuPanel,      SCREEN_MENU);
        screenContainer.add(selectionPanel, SCREEN_SELECTION);
        screenContainer.add(combatPanel,    SCREEN_COMBAT);

        setContentPane(screenContainer);

        // --- Tampilkan layar pertama ---
        showMenu();
    }

    // =========================================================
    // NAVIGASI ANTAR LAYAR
    // =========================================================

    /** Tampilkan Main Menu */
    private void showMenu() {
        showScreen(SCREEN_MENU);
        setTitle("The Last Chromatic Warrior — Main Menu");
    }

    /** Tampilkan Character Selection */
    private void showSelection() {
        showScreen(SCREEN_SELECTION);
        setTitle("The Last Chromatic Warrior — Pilih Karaktermu");
    }

    /** Ganti layar aktif menggunakan CardLayout */
    private void showScreen(String screenName) {
        cardLayout.show(screenContainer, screenName);
    }

    // =========================================================
    // MAIN METHOD — Entry Point Aplikasi
    // =========================================================
    public static void main(String[] args) {
        // Coba set Look and Feel sistem agar lebih rapi di Windows/macOS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback ke default Swing L&F
        }

        // Semua operasi GUI harus dijalankan di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}