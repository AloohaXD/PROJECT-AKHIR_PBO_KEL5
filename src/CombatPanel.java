import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * CombatPanel.java
 * ============================================================
 * Layar 3: Area Pertempuran
 *
 * TATA LETAK (dari atas ke bawah):
 *   ┌─────────────────────────────────────┐
 *   │  STATUS BAR  (HP/MP bars + nama)    │  ← NORTH
 *   ├──────────────┬──────────────────────┤
 *   │  SPRITE HERO │  SPRITE ENEMY        │  ← CENTER (split)
 *   │  (pixel art) │  (pixel art)         │
 *   ├──────────────┴──────────────────────┤
 *   │  BATTLE LOG  (text area)            │  ← CENTER bawah
 *   ├─────────────────────────────────────┤
 *   │  HAND KARTU  (4 tombol kartu)       │  ← SOUTH
 *   └─────────────────────────────────────┘
 * ============================================================
 */
public class CombatPanel extends JPanel {

    // ── Callback ──────────────────────────────────────────────
    private final Runnable onReturnToMenu;

    // ── Game Objects ─────────────────────────────────────────
    private Hero  hero;
    private Enemy enemy;

    // ── State ─────────────────────────────────────────────────
    private boolean playerTurn   = true;
    private boolean gameOver     = false;
    private boolean dialogShowing = false; // BUG FIX: guard agar dialog game over tidak muncul ganda
    private int     turnCount    = 0;

    // ── Warna Tema ────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(8,  8,  20);
    private static final Color BG_PANEL   = new Color(16, 14, 38);
    private static final Color GOLD       = new Color(255, 200, 50);
    private static final Color RED        = new Color(220, 60,  60);
    private static final Color GREEN      = new Color(60,  200, 100);
    private static final Color BLUE       = new Color(80,  150, 255);
    private static final Color PURPLE     = new Color(180, 80,  255);
    private static final Color TEXT_MAIN  = new Color(230, 225, 240);
    private static final Color TEXT_DIM   = new Color(140, 130, 160);
    private static final Color HP_COLOR   = new Color(60,  200, 80);
    private static final Color MP_COLOR   = new Color(60,  120, 255);
    private static final Color SHIELD_CLR = new Color(80,  160, 255);
    private static final Color ENEMY_HP   = new Color(220, 60,  60);

    // ── Komponen UI ───────────────────────────────────────────
    // Status atas
    private JLabel       heroNameLabel, heroHpLabel, heroMpLabel, heroShieldLabel;
    private JProgressBar heroHpBar, heroMpBar, heroShieldBar;
    private JLabel       enemyNameLabel, enemyHpLabel;
    private JProgressBar enemyHpBar;
    private JLabel       turnLabel;

    // Sprite panels
    private SpritePanel heroSpritePanel;
    private SpritePanel enemySpritePanel;

    // Battle log
    private JTextArea   battleLog;

    // Kartu
    private JPanel    handPanel;
    private JButton[] cardButtons;
    private JLabel    handLabel;
    private JLabel    manaWarning;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public CombatPanel(Runnable onReturnToMenu) {
        this.onReturnToMenu = onReturnToMenu;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildAllSections();
    }

    // =========================================================
    // INISIALISASI COMBAT — dipanggil dari GameFrame setelah pilih class
    // =========================================================
    public void startCombat(HeroClass.CharacterProfile profile) {
        this.hero     = new Hero(profile);
        this.enemy    = Enemy.createOrc(); // Bisa diganti sesuai level/pilihan
        this.gameOver      = false;
        this.playerTurn    = true;
        this.dialogShowing = false; // reset guard saat mulai combat baru
        this.turnCount     = 0;

        // Update sprite panels dengan class baru
        heroSpritePanel.setClassType(profile.classType);
        enemySpritePanel.setEnemyName(enemy.getName());

        // Bersihkan log
        battleLog.setText("");
        logMsg("═══════════════════════════════════", GOLD);
        logMsg("  " + hero.getName() + "  vs  " + enemy.getName(), TEXT_MAIN);
        logMsg("═══════════════════════════════════", GOLD);
        logMsg("Pertempuran dimulai! Pilih kartumu.", GREEN);

        updateAllUI();
    }

    // =========================================================
    // MEMBANGUN SEMUA SEKSI UI
    // =========================================================
    private void buildAllSections() {
        add(buildStatusBar(),   BorderLayout.NORTH);
        add(buildBattleArea(),  BorderLayout.CENTER);
        add(buildHandSection(), BorderLayout.SOUTH);
    }

    // ── STATUS BAR (atas) ─────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new LineBorder(new Color(60, 50, 100), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        // === Panel Hero (kiri) ===
        JPanel heroStatus = new JPanel();
        heroStatus.setLayout(new BoxLayout(heroStatus, BoxLayout.Y_AXIS));
        heroStatus.setBackground(BG_PANEL);
        heroStatus.setPreferredSize(new Dimension(240, 90));

        heroNameLabel  = makeLabel("Hero", new Font("Serif", Font.BOLD, 14), GOLD);
        heroHpLabel    = makeLabel("HP: 100/100", new Font("Monospaced", Font.PLAIN, 11), GREEN);
        heroMpLabel    = makeLabel("MP: 60/60",   new Font("Monospaced", Font.PLAIN, 11), BLUE);
        heroShieldLabel= makeLabel("Shield: 0",   new Font("Monospaced", Font.PLAIN, 11), SHIELD_CLR);

        heroHpBar    = makeBar(HP_COLOR);
        heroMpBar    = makeBar(MP_COLOR);
        heroShieldBar= makeBar(SHIELD_CLR);

        heroStatus.add(heroNameLabel);
        heroStatus.add(Box.createVerticalStrut(2));
        heroStatus.add(heroHpLabel);    heroStatus.add(heroHpBar);
        heroStatus.add(heroMpLabel);    heroStatus.add(heroMpBar);
        heroStatus.add(heroShieldLabel);heroStatus.add(heroShieldBar);

        // === Label Turn (tengah) ===
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setBackground(BG_PANEL);

        turnLabel = new JLabel("TURN 0", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 16));
        turnLabel.setForeground(GOLD);

        JButton btnFlee = new JButton("Lari");
        btnFlee.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnFlee.setForeground(TEXT_DIM);
        btnFlee.setBackground(BG_PANEL);
        btnFlee.setBorder(BorderFactory.createLineBorder(new Color(80,70,100),1));
        btnFlee.setFocusPainted(false);
        btnFlee.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFlee.addActionListener(e -> confirmFlee());

        midPanel.add(turnLabel, BorderLayout.CENTER);
        midPanel.add(btnFlee,   BorderLayout.SOUTH);

        // === Panel Enemy (kanan) ===
        JPanel enemyStatus = new JPanel();
        enemyStatus.setLayout(new BoxLayout(enemyStatus, BoxLayout.Y_AXIS));
        enemyStatus.setBackground(BG_PANEL);
        enemyStatus.setPreferredSize(new Dimension(240, 90));

        enemyNameLabel = makeLabel("Enemy", new Font("Serif", Font.BOLD, 14), RED);
        enemyNameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        enemyHpLabel   = makeLabel("HP: 120/120", new Font("Monospaced", Font.PLAIN, 11), ENEMY_HP);
        enemyHpLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        enemyHpBar = makeBar(ENEMY_HP);

        enemyStatus.add(enemyNameLabel);
        enemyStatus.add(Box.createVerticalStrut(2));
        enemyStatus.add(enemyHpLabel);
        enemyStatus.add(enemyHpBar);

        bar.add(heroStatus,  BorderLayout.WEST);
        bar.add(midPanel,    BorderLayout.CENTER);
        bar.add(enemyStatus, BorderLayout.EAST);

        return bar;
    }

    // ── BATTLE AREA (tengah: sprite + log) ───────────────────
    private JPanel buildBattleArea() {
        JPanel area = new JPanel(new BorderLayout(6, 6));
        area.setBackground(BG_DARK);

        // --- Sprite Row ---
        JPanel spriteRow = new JPanel(new GridLayout(1, 2, 10, 0));
        spriteRow.setBackground(BG_DARK);
        spriteRow.setPreferredSize(new Dimension(0, 200));

        // Placeholder panels — akan diperbarui saat startCombat()
        heroSpritePanel  = new SpritePanel(true,  null, null);
        enemySpritePanel = new SpritePanel(false, null, null);

        spriteRow.add(heroSpritePanel);
        spriteRow.add(enemySpritePanel);

        // --- Battle Log ---
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(5, 5, 15));
        battleLog.setForeground(TEXT_MAIN);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scroll = new JScrollPane(battleLog);
        scroll.setBorder(new LineBorder(new Color(40, 35, 70), 1));
        scroll.setBackground(new Color(5, 5, 15));
        scroll.getViewport().setBackground(new Color(5, 5, 15));

        area.add(spriteRow, BorderLayout.NORTH);
        area.add(scroll,    BorderLayout.CENTER);

        return area;
    }

    // ── HAND / KARTU (bawah) ─────────────────────────────────
    private JPanel buildHandSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Header kartu
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(BG_DARK);

        handLabel = new JLabel("🃏  Kartu di Tangan  —  Klik untuk memainkan", SwingConstants.CENTER);
        handLabel.setFont(new Font("Serif", Font.BOLD, 13));
        handLabel.setForeground(GOLD);

        manaWarning = new JLabel("", SwingConstants.RIGHT);
        manaWarning.setFont(new Font("SansSerif", Font.ITALIC, 11));
        manaWarning.setForeground(RED);
        manaWarning.setBorder(new EmptyBorder(0, 0, 0, 10));

        headerRow.add(handLabel,   BorderLayout.CENTER);
        headerRow.add(manaWarning, BorderLayout.EAST);

        // Grid 4 tombol kartu
        handPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        handPanel.setBackground(BG_DARK);
        handPanel.setPreferredSize(new Dimension(0, 120));

        cardButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            cardButtons[i] = createCardSlotButton();
            handPanel.add(cardButtons[i]);
        }

        wrapper.add(headerRow, BorderLayout.NORTH);
        wrapper.add(handPanel, BorderLayout.CENTER);
        return wrapper;
    }

    /** Membuat satu slot tombol kartu kosong */
    private JButton createCardSlotButton() {
        JButton btn = new JButton();
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(new Color(22, 18, 50));
        // BUG FIX: Jangan set foreground di sini — biarkan warna HTML dari getButtonLabel() bekerja.
        // Jika foreground di-set, Swing bisa override warna <font> tag di HTML.
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(new Color(60, 55, 100), 1),
            new EmptyBorder(6, 4, 6, 4)
        ));
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(45, 38, 88));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(22, 18, 50));
            }
        });
        return btn;
    }

    // =========================================================
    // UPDATE UI — Render semua komponen dari state game
    // =========================================================
    private void updateAllUI() {
        if (hero == null || enemy == null) return;
        updateStatusBars();
        updateCardButtons();
        updateTurnLabel();
        heroSpritePanel.repaint();
        enemySpritePanel.repaint();
    }

    private void updateStatusBars() {
        // Hero HP
        int hpPct = pct(hero.getHp(), hero.getMaxHp());
        heroHpBar.setValue(hpPct);
        heroHpLabel.setText("HP: " + hero.getHp() + "/" + hero.getMaxHp());
        heroHpBar.setForeground(hpPct < 25 ? RED : HP_COLOR);

        // Hero MP
        int mpPct = pct(hero.getMp(), hero.getMaxMp());
        heroMpBar.setValue(mpPct);
        heroMpLabel.setText("MP: " + hero.getMp() + "/" + hero.getMaxMp());

        // Hero Shield
        boolean hasShield = hero.getDefenseShield() > 0;
        heroShieldBar.setValue(hasShield ? Math.min(100, hero.getDefenseShield() * 3) : 0);
        heroShieldLabel.setText("Shield: " + hero.getDefenseShield());
        heroShieldBar.setVisible(hasShield);
        heroShieldLabel.setVisible(hasShield);

        // Hero Name
        heroNameLabel.setText("⚔ " + hero.getName() + "  [" + hero.getClassType().displayName + "]");

        // Enemy HP
        int eHpPct = pct(enemy.getHp(), enemy.getMaxHp());
        enemyHpBar.setValue(eHpPct);
        enemyHpLabel.setText("HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
        enemyNameLabel.setText("👹 " + enemy.getName());
    }

    private void updateCardButtons() {
        List<Card> hand = hero.getHand();
        manaWarning.setText("");

        for (int i = 0; i < cardButtons.length; i++) {
            JButton btn = cardButtons[i];
            // Hapus listener lama
            for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);

            if (i < hand.size()) {
                Card card = hand.get(i);
                boolean canPlay = hero.canPlayCard(card) && playerTurn && !gameOver;

                btn.setText(card.getButtonLabel());
                btn.setToolTipText(card.getTooltipText());
                btn.setEnabled(canPlay);

                // Warna border berdasarkan tipe
                Color borderColor = getCardColor(card);
                // Ultimate: tampilkan disabled jika mana kurang
                if (card.getType() == Card.CardType.ULTIMATE && !hero.canPlayCard(card)) {
                    borderColor = new Color(100, 80, 80);
                    btn.setToolTipText("<html><b>" + card.getName() + "</b><br><font color='red'>Mana tidak cukup! Butuh "
                                       + card.getManaCost() + " MP</font></html>");
                    manaWarning.setText("⚠ Mana kurang untuk Ultimate!");
                }

                Color finalBorder = borderColor;
                btn.setBorder(new CompoundBorder(
                    new LineBorder(finalBorder, canPlay ? 2 : 1),
                    new EmptyBorder(6, 4, 6, 4)
                ));

                final Card chosenCard = card;
                btn.addActionListener(e -> onCardPlayed(chosenCard));

            } else {
                btn.setText("<html><center>─<br>Kosong</center></html>");
                btn.setEnabled(false);
                btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(40, 35, 70), 1),
                    new EmptyBorder(6, 4, 6, 4)
                ));
            }
        }
    }

    private void updateTurnLabel() {
        if (gameOver) {
            turnLabel.setText("SELESAI");
            turnLabel.setForeground(TEXT_DIM);
        } else {
            turnLabel.setText("TURN " + turnCount + (playerTurn ? "  ⚔" : "  👹"));
            turnLabel.setForeground(playerTurn ? GOLD : RED);
        }
    }

    private Color getCardColor(Card card) {
        switch (card.getType()) {
            case ATTACK:   return RED;
            case ULTIMATE: return PURPLE;
            default:
                return card.getSkillEffect() == Card.SkillEffect.HEAL ? GREEN : BLUE;
        }
    }

    // =========================================================
    // GAME LOGIC — Alur Giliran
    // =========================================================

    /** Dipanggil saat pemain klik kartu */
    private void onCardPlayed(Card card) {
        if (!playerTurn || gameOver) return;

        // Cek mana
        if (!hero.canPlayCard(card)) {
            logMsg("⚠ Mana tidak cukup! Butuh " + card.getManaCost() + " MP.", RED);
            return;
        }

        playerTurn = false;
        setHandEnabled(false);
        turnCount++;

        // Log pemisah
        logMsg("", TEXT_MAIN);
        logMsg("── GILIRAN HERO (Turn " + turnCount + ") ──", GOLD);

        // Eksekusi efek kartu
        executeCard(card);
        hero.playCard(card);  // Keluarkan dari hand, kurangi mana, tarik kartu baru
        updateAllUI();

        // Cek musuh mati
        if (!enemy.isAlive()) { endGame(true); return; }

        // Jeda lalu giliran musuh
        // BUG FIX: setRepeats(false) agar timer hanya jalan SEKALI, bukan terus-menerus
        Timer enemyTimer = new Timer(1000, e -> doEnemyTurn());
        enemyTimer.setRepeats(false);
        enemyTimer.start();
    }

    /** Eksekusi efek kartu berdasarkan tipe */
    private void executeCard(Card card) {
        logMsg("▶ " + hero.getName() + " memainkan [" + card.getName() + "]"
               + (card.getManaCost() > 0 ? " (-" + card.getManaCost() + " MP)" : ""), GOLD);

        switch (card.getType()) {
            case ATTACK:
                enemy.takeDamage(card.getValue());
                logMsg("  ⚔ Menyerang " + enemy.getName() + ": -" + card.getValue() + " HP", RED);
                logMsg("    " + enemy.getName() + " sisa HP: " + enemy.getHp(), TEXT_DIM);
                break;

            case SKILL:
                if (card.getSkillEffect() == Card.SkillEffect.HEAL) {
                    int before = hero.getHp();
                    hero.heal(card.getValue());
                    int healed = hero.getHp() - before;
                    logMsg("  💚 Memulihkan " + healed + " HP!", GREEN);
                } else { // DEFENSE
                    hero.addShield(card.getValue());
                    logMsg("  🛡 Shield +" + card.getValue() + " poin!", BLUE);
                }
                logMsg("    " + hero.getStatusText(), TEXT_DIM);
                break;

            case ULTIMATE:
                enemy.takeDamage(card.getValue());
                logMsg("  ✨ ULTIMATE! Damage " + card.getValue() + " menghancurkan " + enemy.getName() + "!", PURPLE);
                logMsg("    " + enemy.getName() + " sisa HP: " + enemy.getHp(), TEXT_DIM);
                break;
        }
    }

    /** Giliran musuh menyerang */
    private void doEnemyTurn() {
        logMsg("", TEXT_MAIN);
        logMsg("── GILIRAN MUSUH ──", RED);

        Enemy.AttackResult atk = enemy.attack();
        logMsg("  " + atk.message, TEXT_MAIN);

        int shieldBefore = hero.getDefenseShield();
        int actual = hero.takeDamage(atk.damage);

        if (shieldBefore > 0) {
            logMsg("  🛡 Shield menyerap! Damage aktual: " + actual, BLUE);
        }
        logMsg("    " + hero.getStatusText(), TEXT_DIM);

        // Regen sedikit mana setiap giliran
        int manaRegen = 8 + (hero.getClassType() == HeroClass.ClassType.MAGE ? 5 : 0);
        hero.regenMana(manaRegen);
        if (manaRegen > 0) logMsg("  ✦ Mana regen +" + manaRegen, MP_COLOR);

        updateAllUI();

        if (!hero.isAlive()) { endGame(false); return; }

        // Kembali ke giliran player
        playerTurn = true;
        setHandEnabled(true);
        updateAllUI();
        logMsg("", TEXT_MAIN);
        logMsg("Giliranmu! Pilih kartu.", GREEN);
    }

    /** Mengakhiri permainan */
    private void endGame(boolean heroWon) {
        gameOver = true;
        setHandEnabled(false);
        updateAllUI();

        logMsg("", TEXT_MAIN);
        if (heroWon) {
            logMsg("★★★  KEMENANGAN!  ★★★", GOLD);
            logMsg(hero.getName() + " mengalahkan " + enemy.getName() + "!", GOLD);
        } else {
            logMsg("✗✗✗  KEKALAHAN  ✗✗✗", RED);
            logMsg(hero.getName() + " telah gugur dalam pertempuran...", RED);
        }

        // BUG FIX: setRepeats(false) agar dialog hanya muncul SEKALI
        Timer endTimer = new Timer(600, e -> showEndDialog(heroWon));
        endTimer.setRepeats(false);
        endTimer.start();
    }

    private void showEndDialog(boolean heroWon) {
        // BUG FIX: Jika dialog sudah terbuka (dari timer yang terlanjur jalan), abaikan
        if (dialogShowing) return;
        dialogShowing = true;

        String title = heroWon ? "KEMENANGAN! ★" : "KEKALAHAN ✗";
        String msg   = heroWon
            ? hero.getName() + " menang!\nHP tersisa: " + hero.getHp()
            : hero.getName() + " gugur...\nHP Musuh tersisa: " + enemy.getHp();

        int choice = JOptionPane.showOptionDialog(
            this, msg, title,
            JOptionPane.YES_NO_OPTION,
            heroWon ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE,
            null, new String[]{"Main Lagi", "Menu Utama"}, "Main Lagi"
        );

        if (choice == 0) {
            dialogShowing = false; // reset sebelum restart
            startCombat(new HeroClass.CharacterProfile(hero.getClassType()));
        } else {
            dialogShowing = false; // reset sebelum kembali ke menu
            onReturnToMenu.run();
        }
    }

    private void confirmFlee() {
        int c = JOptionPane.showConfirmDialog(this,
            "Yakin ingin mundur ke Menu Utama?", "Mundur", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) onReturnToMenu.run();
    }

    // =========================================================
    // UTILITY
    // =========================================================
    private void logMsg(String msg, Color color) {
        // JTextArea tidak mendukung warna per-baris
        // Untuk upgrade ke warna penuh: ganti JTextArea → JTextPane + StyledDocument
        battleLog.append(msg + "\n");
        SwingUtilities.invokeLater(() ->
            battleLog.setCaretPosition(battleLog.getDocument().getLength()));
    }

    private void setHandEnabled(boolean enabled) {
        for (JButton btn : cardButtons) btn.setEnabled(enabled);
    }

    private static int pct(int val, int max) {
        return max > 0 ? (int)((double)val / max * 100) : 0;
    }

    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    private static JProgressBar makeBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(100);
        bar.setForeground(color);
        bar.setBackground(new Color(30, 25, 50));
        bar.setBorderPainted(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        return bar;
    }

    // =========================================================
    // INNER CLASS: SpritePanel — Panel khusus untuk pixel art sprite
    // =========================================================
    /**
     * Panel yang menggambar sprite karakter menggunakan Graphics2D.
     *
     * CARA MENGGANTI KE GAMBAR PNG ASLI:
     *   Di method paintComponent, ganti pemanggilan drawHeroSprite/drawEnemySprite
     *   dengan kode berikut:
     *
     *   try {
     *       Image img = loadHeroImage(classType); // Lihat method loadHeroImage di bawah
     *       g2d.drawImage(img, 20, 20, getWidth()-40, getHeight()-40, null);
     *   } catch (Exception e) {
     *       // Fallback ke pixel art jika gambar tidak ditemukan
     *       HeroClass.drawHeroSprite(g2d, classType, 0, 0, getWidth(), getHeight());
     *   }
     */
    private class SpritePanel extends JPanel {
        private final boolean isHero;
        private HeroClass.ClassType classType;
        private String enemyName;

        SpritePanel(boolean isHero, HeroClass.ClassType classType, String enemyName) {
            this.isHero    = isHero;
            this.classType = classType;
            this.enemyName = enemyName;
            setBackground(new Color(12, 10, 30));
            setBorder(new LineBorder(new Color(40, 35, 70), 1));
        }

        void setClassType(HeroClass.ClassType ct)  { this.classType = ct; repaint(); }
        void setEnemyName(String name)              { this.enemyName = name; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            int W = getWidth();
            int H = getHeight();

            // Background panel
            GradientPaint bgGrad = isHero
                ? new GradientPaint(0, H, new Color(10, 20, 10), 0, 0, new Color(12, 10, 30))
                : new GradientPaint(0, H, new Color(25, 8, 8),   0, 0, new Color(12, 10, 30));
            g2d.setPaint(bgGrad);
            g2d.fillRect(0, 0, W, H);

            // "Platform" tempat karakter berdiri
            Color platformColor = isHero ? new Color(20, 40, 20, 180) : new Color(40, 15, 15, 180);
            g2d.setColor(platformColor);
            g2d.fillOval(W/2 - 50, H - 25, 100, 20);

            // Gambar sprite
            if (isHero && classType != null) {
                HeroClass.drawHeroSprite(g2d, classType, 0, 0, W, H - 20);
                // Label class di bawah sprite
                g2d.setFont(new Font("Serif", Font.BOLD, 11));
                g2d.setColor(classType.themeColor);
                FontMetrics fm = g2d.getFontMetrics();
                String label = classType.emoji + " " + classType.displayName;
                g2d.drawString(label, (W - fm.stringWidth(label)) / 2, H - 5);

            } else if (!isHero && enemyName != null) {
                HeroClass.drawEnemySprite(g2d, enemyName, 0, 0, W, H - 20);
                // Label musuh di bawah
                g2d.setFont(new Font("Serif", Font.BOLD, 11));
                g2d.setColor(new Color(220, 80, 80));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(enemyName, (W - fm.stringWidth(enemyName)) / 2, H - 5);
            }

            g2d.dispose();
        }
    }

    /**
     * loadHeroImage() — STUB METHOD untuk mengganti pixel art dengan file PNG.
     *
     * CARA PAKAI:
     *   1. Buat folder "assets" di direktori project NetBeans-mu.
     *   2. Simpan file PNG dengan nama: warrior.png, tanker.png, archer.png, mage.png, assassin.png
     *   3. Uncomment kode di bawah dan hapus return null.
     *
     * @param classType Class karakter yang ingin dimuat gambarnya
     * @return Image dari file PNG, atau null jika file tidak ada
     */
    @SuppressWarnings("unused")
    private java.awt.Image loadHeroImage(HeroClass.ClassType classType) {
        // ── UNCOMMENT UNTUK MENGGUNAKAN FILE PNG ASLI ──────────
        // try {
        //     String filename = "assets/" + classType.name().toLowerCase() + ".png";
        //     return javax.imageio.ImageIO.read(new java.io.File(filename));
        // } catch (java.io.IOException e) {
        //     System.err.println("Gambar tidak ditemukan: " + e.getMessage());
        //     return null;
        // }
        return null; // Saat ini menggunakan pixel art
    }

    /**
     * loadEnemyImage() — STUB METHOD untuk mengganti pixel art musuh dengan PNG.
     * Sama dengan loadHeroImage, simpan file misalnya "assets/orc.png".
     */
    @SuppressWarnings("unused")
    private java.awt.Image loadEnemyImage(String enemyName) {
        // try {
        //     String filename = "assets/" + enemyName.toLowerCase().replace(" ", "_") + ".png";
        //     return javax.imageio.ImageIO.read(new java.io.File(filename));
        // } catch (java.io.IOException e) {
        //     return null;
        // }
        return null;
    }
}