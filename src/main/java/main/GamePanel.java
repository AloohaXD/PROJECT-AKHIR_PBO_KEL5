package main;

import entity.Entity;
import entity.EnemyEntity;
import entity.NPC;
import entity.Player;
import tile.TileManager;
import ui.HudRenderer;
import ui.QuestTracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GamePanel.java  (DIPERBARUI)
 * ============================================================
 * Perubahan dari versi sebelumnya:
 *
 *  1. Enum GameState ditambah: MAIN_MENU, EXPLORATION, DIALOG,
 *     PAUSED, TRANSITION, COMBAT
 *
 *  2. State MAIN_MENU → Tampilkan ui.MainMenuPanel di atas game
 *     Saat Start diklik, hilangkan menu dan mulai eksplorasi
 *
 *  3. Integrasi HudRenderer → HUD lengkap (HP, Mana, EXP, Quest, Inventory)
 *
 *  4. Integrasi QuestTracker → daftar quest aktif untuk HUD
 *
 *  5. Reward EXP setelah combat → onCombatReturn() memberikan EXP ke player
 *     berdasarkan level musuh yang dikalahkan
 *
 *  6. Mouse Listener untuk tombol Inventory di HUD
 *
 *  7. Musuh dibuat dengan level bervariasi
 * ============================================================
 */
public class GamePanel extends JPanel implements Runnable {

    // ─── Konstanta Layar & Dunia ──────────────────────────────
    public static final int TILE_SIZE     = 48;
    public static final int SCREEN_COLS   = 20;
    public static final int SCREEN_ROWS   = 15;
    public static final int SCREEN_WIDTH  = TILE_SIZE * SCREEN_COLS;  // 960
    public static final int SCREEN_HEIGHT = TILE_SIZE * SCREEN_ROWS;  // 720

    public static final int WORLD_COLS   = 40;
    public static final int WORLD_ROWS   = 40;
    public static final int WORLD_WIDTH  = TILE_SIZE * WORLD_COLS;
    public static final int WORLD_HEIGHT = TILE_SIZE * WORLD_ROWS;

    private static final int  TARGET_FPS  = 60;
    private static final long NS_PER_SEC  = 1_000_000_000L;

    // ─── Subsistem Inti ───────────────────────────────────────
    public final KeyHandler       keyHandler  = new KeyHandler();
    public final Camera           camera      = new Camera(this);
    public final TileManager      tileManager = new TileManager(this);
    public final CollisionChecker collision   = new CollisionChecker(this);

    // ─── Entitas ──────────────────────────────────────────────
    public Player       player;
    public List<Entity> entities = new ArrayList<>();

    // ─── UI Subsistem (BARU) ──────────────────────────────────
    private HudRenderer    hudRenderer;
    private QuestTracker   questTracker;

    // ─── Game State ───────────────────────────────────────────
    public enum GameState {
        MAIN_MENU,    // Layar awal sebelum game dimulai
        EXPLORATION,  // Mode jelajah peta
        DIALOG,       // Sedang dialog dengan NPC
        PAUSED,       // Game di-pause
        TRANSITION,   // Efek transisi masuk/keluar combat
        COMBAT        // Mode pertarungan kartu
    }
    public GameState gameState = GameState.MAIN_MENU; // Mulai dari MAIN_MENU

    // ─── Transisi ─────────────────────────────────────────────
    private final TransitionEffect transition    = new TransitionEffect();
    private int                     pendingEnemyIndex = -1;

    // ─── Combat Integration ───────────────────────────────────
    private combat.CombatPanel combatPanel;

    // ─── Main Menu Panel (BARU) ───────────────────────────────
    // Panel overlay untuk main menu — di-add ke JLayeredPane
    private ui.MainMenuPanel mainMenuOverlay;

    // ─── FPS Debug ────────────────────────────────────────────
    private int  currentFPS = 0;
    private long fpsTimer   = 0;
    private int  fpsCounter = 0;

    private Thread gameThread;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        setFocusable(true);
        setLayout(null); // Absolute layout untuk overlay

        // Inisialisasi semua subsistem
        setupQuestTracker();
        setupCombatPanel();
        setupWorld();
        setupHud();
        setupMainMenu();
    }

    // =========================================================
    // SETUP SUBSISTEM
    // =========================================================

    /** Inisialisasi Quest Tracker dengan quest-quest default. */
    private void setupQuestTracker() {
        questTracker = new QuestTracker();
    }

    /** Buat CombatPanel sebagai overlay tersembunyi. */
    private void setupCombatPanel() {
        combatPanel = new combat.CombatPanel(this::onCombatReturn);
        combatPanel.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        combatPanel.setVisible(false);
        add(combatPanel);
    }

    /** Bangun dunia: player, NPC, musuh. */
    private void setupWorld() {
        player = new Player(this, keyHandler);

        // NPC biasa
        NPC npc1 = new NPC(this);
        npc1.worldX     = TILE_SIZE * 10;
        npc1.worldY     = TILE_SIZE * 8;
        npc1.dialogLines = new String[]{
            "Selamat datang, pejuang muda!",
            "Level-mu akan naik dengan mengalahkan musuh.",
            "Gunakan [X] untuk menyerang dan [I] untuk inventory.",
            "Tekan [Q] untuk melihat quest berikutnya."
        };
        entities.add(npc1);

        // Musuh dengan level bervariasi
        addEnemy(TILE_SIZE * 15, TILE_SIZE * 10, 1); // Level 1
        addEnemy(TILE_SIZE * 22, TILE_SIZE * 18, 2); // Level 2
        addEnemy(TILE_SIZE * 8,  TILE_SIZE * 20, 1); // Level 1
        addEnemy(TILE_SIZE * 30, TILE_SIZE * 12, 3); // Level 3 (boss area)
        addEnemy(TILE_SIZE * 18, TILE_SIZE * 28, 2); // Level 2
    }

    /**
     * Helper: membuat EnemyEntity dengan level tertentu dan set hudRenderer.
     */
    private void addEnemy(int worldX, int worldY, int level) {
        EnemyEntity enemy = new EnemyEntity(this, level);
        enemy.worldX = worldX;
        enemy.worldY = worldY;
        entities.add(enemy);
    }

    /**
     * Inisialisasi HUD Renderer dan sambungkan ke player.
     * Harus dipanggil SETELAH setupWorld() karena butuh referensi player.
     */
    private void setupHud() {
        hudRenderer = new HudRenderer(this);

        // Sambungkan HudRenderer ke player agar bisa:
        //  - Gambar label level di bawah sprite
        //  - Tampilkan notifikasi level up
        player.setHudRenderer(hudRenderer);

        // Sambungkan HudRenderer ke semua musuh
        for (Entity e : entities) {
            if (e instanceof EnemyEntity enemy) {
                enemy.setHudRenderer(hudRenderer);
            }
        }

        // Mouse listener untuk tombol Inventory di HUD
        setupInventoryMouseListener();
    }

    /**
     * Buat dan tampilkan Main Menu overlay.
     * Menu dibuat sebagai JPanel yang di-add ke game panel ini.
     */
    private void setupMainMenu() {
        mainMenuOverlay = new ui.MainMenuPanel(
            SCREEN_WIDTH,
            SCREEN_HEIGHT,
            this::onStartGame  // Callback saat tombol Start diklik
        );
        mainMenuOverlay.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        mainMenuOverlay.setVisible(true);
        add(mainMenuOverlay); // Overlay di atas game
    }

    /**
     * Mouse listener untuk mendeteksi klik dan hover tombol Inventory di HUD.
     */
    private void setupInventoryMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameState == GameState.EXPLORATION
                    && hudRenderer.inventoryClickArea.contains(e.getPoint())) {
                    // Tombol Inventory di HUD diklik
                    System.out.println("[INFO] Inventory dibuka via klik mouse.");
                    // TODO: Ganti dengan: gameState = GameState.INVENTORY;
                    JOptionPane.showMessageDialog(GamePanel.this,
                        "Inventory dibuka!\n(Fitur lengkap akan ditambahkan di update berikutnya)",
                        "Inventory", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (hudRenderer != null) {
                    hudRenderer.setInventoryHovered(
                        hudRenderer.inventoryClickArea.contains(e.getPoint())
                    );
                }
            }
        });
    }

    // =========================================================
    // CALLBACK: Tombol Start di Main Menu diklik
    // =========================================================
    /**
     * Dipanggil saat pemain mengklik Start di Main Menu.
     * Menyembunyikan overlay menu dan memulai mode eksplorasi.
     */
    private void onStartGame() {
        SwingUtilities.invokeLater(() -> {
            // Sembunyikan dan hapus main menu overlay
            if (mainMenuOverlay != null) {
                mainMenuOverlay.stopAnimation();
                mainMenuOverlay.setVisible(false);
                remove(mainMenuOverlay);
                mainMenuOverlay = null;
            }

            // Mulai mode eksplorasi
            gameState = GameState.EXPLORATION;

            // ─── Audio: Fade out Main Menu BGM, lalu putar Explore BGM ──
            AudioManager.get().fadeOutBgm(1500, () -> {
                AudioManager.get().playBgm(AudioManager.BGM_EXPLORE, true);
            });

            // Kembalikan fokus ke game panel agar input keyboard bekerja
            requestFocusInWindow();
        });
    }

    // =========================================================
    // COMBAT LIFECYCLE
    // =========================================================

    /**
     * Dipanggil oleh Player saat basic attack mengenai EnemyEntity.
     * Hanya bisa dipanggil dari state EXPLORATION.
     */
    public void startCombatTransition(int enemyIndex) {
        if (gameState != GameState.EXPLORATION) return;

        pendingEnemyIndex = enemyIndex;
        gameState = GameState.TRANSITION;

        // ─── Audio: SFX transisi + stop Explore BGM ───────────
        AudioManager.get().playSfx(AudioManager.SFX_TRANSITION);
        AudioManager.get().stopBgmImmediate();

        // Snapshot layar untuk efek distorsi
        BufferedImage snap = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = snap.createGraphics();
        printAll(sg);
        sg.dispose();

        transition.start(snap, this::enterCombat);
    }

    /** Masuk ke mode combat (dipanggil saat layar hitam di tengah transisi). */
    private void enterCombat() {
        gameState = GameState.COMBAT;

        // ─── Audio: Combat BGM random ─────────────────────────
        AudioManager.get().playRandomCombatBgm();

        combatPanel.startCombat(new combat.HeroClass.CharacterProfile(
            combat.HeroClass.ClassType.WARRIOR // Default — bisa diganti ke class pilihan player
        ));

        SwingUtilities.invokeLater(() -> {
            combatPanel.setVisible(true);
            combatPanel.requestFocusInWindow();
        });
    }

    /**
     * Dipanggil oleh CombatPanel saat combat selesai (menang/kalah).
     * Memberikan EXP reward ke player dan update quest.
     */
    private void onCombatReturn() {
        SwingUtilities.invokeLater(() -> {
            combatPanel.setVisible(false);
            requestFocusInWindow();

            // ─── Audio: Stop combat BGM, kembali ke Explore BGM ──
            AudioManager.get().stopBgmImmediate();
            AudioManager.get().playBgm(AudioManager.BGM_EXPLORE, true);

            // Proses musuh yang dikalahkan
            if (pendingEnemyIndex >= 0 && pendingEnemyIndex < entities.size()) {
                Entity e = entities.get(pendingEnemyIndex);
                if (e instanceof EnemyEntity ee && ee.isAlive) {
                    ee.isAlive = false;

                    // ─── Berikan EXP ke Player ─────────────────────────────
                    int expGained = ee.getExpReward();
                    player.gainExp(expGained); // gainExp akan trigger level up jika cukup

                    System.out.printf("[INFO] Musuh Lv.%d dikalahkan! +%d EXP → %s%n",
                        ee.getEnemyLevel(), expGained, player.getStats());

                    // ─── Update Quest "Kill" ───────────────────────────────
                    boolean questCompleted = questTracker.updateProgress("kill_slime", 1);
                    if (questCompleted) {
                        System.out.println("[INFO] Quest 'Pemburu Pertama' selesai!");
                        // Berikan bonus EXP quest
                        player.gainExp(50);
                    }

                    // ─── Update Quest Level ───────────────────────────────
                    int currentLevel = player.getStats().getLevel();
                    // Sinkronkan progress quest level dengan level saat ini
                    questTracker.updateProgress("reach_level3",
                        Math.min(currentLevel, 3) - questTracker.getProgress("reach_level3"));
                }
            }

            pendingEnemyIndex = -1;

            // Transisi keluar (fade dari hitam ke eksplorasi)
            BufferedImage black = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            transition.start(black, null);

            gameState = GameState.EXPLORATION;
        });
    }

    // =========================================================
    // GETTER untuk subsistem (dipakai oleh Player, HudRenderer, dll.)
    // =========================================================
    public QuestTracker getQuestTracker() { return questTracker; }
    public HudRenderer  getHudRenderer()  { return hudRenderer; }

    // =========================================================
    // GAME LOOP
    // =========================================================

    public void startGameThread() {
        gameThread = new Thread(this, "GameLoopThread");
        gameThread.start();
    }

    @Override
    public void run() {
        final double nsPerFrame = (double) NS_PER_SEC / TARGET_FPS;
        double delta   = 0;
        long lastTime  = System.nanoTime();
        fpsTimer       = System.nanoTime();

        while (gameThread != null) {
            long now  = System.nanoTime();
            delta    += (now - lastTime) / nsPerFrame;
            lastTime  = now;

            if (delta >= 1) {
                update();
                delta--;
            }
            repaint();

            fpsCounter++;
            if (System.nanoTime() - fpsTimer >= NS_PER_SEC) {
                currentFPS = fpsCounter;
                fpsCounter = 0;
                fpsTimer   = System.nanoTime();
            }
            sleepRemainder(lastTime, nsPerFrame);
        }
    }

    private void sleepRemainder(long frameStart, double nsPerFrame) {
        long remaining = (long) nsPerFrame - (System.nanoTime() - frameStart);
        if (remaining > 0) {
            try {
                Thread.sleep(remaining / 1_000_000, (int)(remaining % 1_000_000));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // =========================================================
    // UPDATE (logika game per frame)
    // =========================================================
    public void update() {
        // Selalu update transisi efek
        transition.update();

        // Jika di main menu, tidak ada update game
        if (gameState == GameState.MAIN_MENU) return;

        if (gameState == GameState.EXPLORATION || gameState == GameState.DIALOG) {
            player.update();
            for (Entity e : entities) e.update();
            camera.update(player);
        }
        // TRANSITION & COMBAT: game pause, hanya transisi berjalan
    }

    // =========================================================
    // RENDER
    // =========================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Rendering hints untuk performa
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Main Menu dirender sebagai JPanel overlay — tidak perlu paint di sini
        if (gameState == GameState.MAIN_MENU) return;

        if (gameState != GameState.COMBAT) {
            // ─── Gambar Dunia Eksplorasi ─────────────────────────
            tileManager.draw(g2);

            // Urutkan entity berdasarkan Y (depth sort)
            List<Entity> renderList = new ArrayList<>(entities);
            renderList.add(player);
            renderList.sort(Comparator.comparingInt(e ->
                (e.worldY + e.solidArea.y + e.solidArea.height)));

            for (Entity e : renderList) {
                if (e instanceof EnemyEntity ee && !ee.isAlive) continue;
                e.draw(g2);
            }

            // ─── Gambar HUD di atas segalanya ────────────────────
            drawHUD(g2);

            // ─── Efek transisi DI ATAS HUD ───────────────────────
            if (transition.isActive()) {
                transition.draw(g2, SCREEN_WIDTH, SCREEN_HEIGHT);
            }
        }
        // Jika COMBAT: combatPanel (child component) menggambar sendiri

        g2.dispose();
    }

    // =========================================================
    // HUD
    // =========================================================
    private void drawHUD(Graphics2D g2) {
        // Aktifkan antialiasing untuk HUD agar teks dan bar halus
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ─── HUD Utama (profil, quest, inventory) ─────────────
        hudRenderer.draw(g2, player);

        // ─── FPS Counter (debug) ──────────────────────────────
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.setColor(new Color(200, 200, 200, 140));
        g2.drawString("FPS: " + currentFPS, 10, SCREEN_HEIGHT - 24);

        // ─── Hint Kontrol di bawah layar ─────────────────────
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(180, 170, 200, 140));
        g2.drawString("WASD:Gerak  X:Serang  Z:Bicara  I:Inventory  Q:GantiQuest",
            10, SCREEN_HEIGHT - 10);

        // ─── Hint interaksi NPC ──────────────────────────────
        if (player.nearbyEntity instanceof NPC) {
            drawInteractionHint(g2, "[Z] / [ENTER] — Bicara");
        }

        // ─── Dialog Box ───────────────────────────────────────
        if (gameState == GameState.DIALOG) drawDialogBox(g2);

        // Kembalikan rendering hint ke default game
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /** Gambar hint interaksi di tengah bawah layar. */
    private void drawInteractionHint(Graphics2D g2, String hint) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(hint);
        int x = (SCREEN_WIDTH - textW) / 2;
        int y = SCREEN_HEIGHT - 46;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(x - 10, y - fm.getAscent() - 2, textW + 20, fm.getHeight() + 6, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(hint, x, y);
    }

    private void drawDialogBox(Graphics2D g2) {
        int boxX = 40, boxY = SCREEN_HEIGHT - 180, boxW = SCREEN_WIDTH - 80, boxH = 160;

        g2.setColor(new Color(5, 3, 18, 220));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        g2.setColor(new Color(120, 100, 200));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        if (player.nearbyEntity instanceof NPC npc) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g2.setColor(Color.WHITE);
            g2.drawString(npc.getCurrentDialog(), boxX + 20, boxY + 50);

            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.setColor(new Color(160, 150, 200));
            g2.drawString("[Z] Lanjut  |  [ESC] Tutup", boxX + 20, boxY + boxH - 18);
        }
    }
}