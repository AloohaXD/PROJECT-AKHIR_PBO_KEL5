package main;
import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            javax.swing.JFrame w = new javax.swing.JFrame("The Last Chromatic Warrior");
            w.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            w.setResizable(false);
            GamePanel gp = new GamePanel();
            w.add(gp);
            w.pack();
            w.setLocationRelativeTo(null);
            w.setVisible(true);
            gp.requestFocusInWindow();
            gp.startGameThread();
        });
    }
}
