package main;

import javax.swing.*;
import java.awt.*;

/**
 * Main.java  (DIPERBARUI)
 * ============================================================
 * Entry point aplikasi game "The Last Chromatic Warrior".
 *
 * Alur startup:
 *   1. JFrame dibuat dengan judul game
 *   2. GamePanel ditambahkan (berisi semua subsistem)
 *   3. GamePanel menampilkan MainMenuPanel sebagai overlay awal
 *   4. Saat Start diklik → overlay hilang → eksplorasi dimulai
 *   5. Game loop (Thread) dijalankan setelah window visible
 * ============================================================
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Coba set Look and Feel sistem agar tampilan native
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Buat JFrame utama
            JFrame window = new JFrame("The Last Chromatic Warrior");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);

            // Set icon window (opsional — ganti path jika punya icon.png)
            // try {
            //     Image icon = ImageIO.read(Main.class.getResourceAsStream("/assets/icon.png"));
            //     window.setIconImage(icon);
            // } catch (Exception ignored) {}

            // Tambahkan GamePanel sebagai konten utama
            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);

            // Pack agar window menyesuaikan ukuran GamePanel
            window.pack();
            window.setLocationRelativeTo(null); // Tengah layar
            window.setVisible(true);

            // Fokus ke game panel agar input keyboard bekerja
            gamePanel.requestFocusInWindow();

            // Mulai game loop
            gamePanel.startGameThread();
        });
    }
}