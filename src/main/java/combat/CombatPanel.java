package combat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatPanel extends JPanel {

    private final Runnable onReturnToMenu;
    private Hero  hero;
    private Enemy enemy;
    private boolean playerTurn    = true;
    private boolean gameOver      = false;
    private boolean dialogShowing = false;
    private int     turnCount     = 0;

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

    private JLabel       heroNameLabel, heroHpLabel, heroMpLabel, heroShieldLabel;
    private JProgressBar heroHpBar, heroMpBar, heroShieldBar;
    private JLabel       enemyNameLabel, enemyHpLabel;
    private JProgressBar enemyHpBar;
    private JLabel       turnLabel;
    private SpritePanel  heroSpritePanel;
    private SpritePanel  enemySpritePanel;
    private JTextArea    battleLog;
    private JPanel       handPanel;
    private JButton[]    cardButtons;
    private JLabel       handLabel;
    private JLabel       manaWarning;

    final List<CardEffect> activeEffects = new ArrayList<>();
    private Timer effectTimer;
    int heroShakeFrames  = 0;
    int enemyShakeFrames = 0;
    private final Random rng = new Random();

    public CombatPanel(Runnable onReturnToMenu) {
        this.onReturnToMenu = onReturnToMenu;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildAllSections();
        startEffectLoop();
    }

    private void startEffectLoop() {
        effectTimer = new Timer(16, e -> {
            activeEffects.removeIf(ef -> { ef.tick(); return ef.isDone(); });
            if (heroShakeFrames  > 0) heroShakeFrames--;
            if (enemyShakeFrames > 0) enemyShakeFrames--;
            if (heroSpritePanel  != null) heroSpritePanel.repaint();
            if (enemySpritePanel != null) enemySpritePanel.repaint();
        });
        effectTimer.start();
    }

    public void startCombat(HeroClass.CharacterProfile profile) {
        this.hero          = new Hero(profile);
        this.enemy         = Enemy.createOrc();
        this.gameOver      = false;
        this.playerTurn    = true;
        this.dialogShowing = false;
        this.turnCount     = 0;
        activeEffects.clear();
        heroShakeFrames = enemyShakeFrames = 0;
        heroSpritePanel.setClassType(profile.classType);
        enemySpritePanel.setEnemyName(enemy.getName());
        battleLog.setText("");
        logMsg("══════════════════════════════════", GOLD);
        logMsg("  " + hero.getName() + "  vs  " + enemy.getName(), TEXT_MAIN);
        logMsg("══════════════════════════════════", GOLD);
        logMsg("Pertempuran dimulai! Pilih kartumu.", GREEN);
        updateAllUI();
    }

    private void buildAllSections() {
        add(buildStatusBar(),   BorderLayout.NORTH);
        add(buildBattleArea(),  BorderLayout.CENTER);
        add(buildHandSection(), BorderLayout.SOUTH);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(new LineBorder(new Color(60,50,100),1), new EmptyBorder(8,12,8,12)));

        JPanel heroStatus = new JPanel();
        heroStatus.setLayout(new BoxLayout(heroStatus, BoxLayout.Y_AXIS));
        heroStatus.setBackground(BG_PANEL);
        heroStatus.setPreferredSize(new Dimension(240, 90));
        heroNameLabel   = makeLabel("Hero",        new Font("Serif",      Font.BOLD,  14), GOLD);
        heroHpLabel     = makeLabel("HP: 100/100", new Font("Monospaced", Font.PLAIN, 11), GREEN);
        heroMpLabel     = makeLabel("MP: 60/60",   new Font("Monospaced", Font.PLAIN, 11), BLUE);
        heroShieldLabel = makeLabel("Shield: 0",   new Font("Monospaced", Font.PLAIN, 11), SHIELD_CLR);
        heroHpBar    = makeBar(HP_COLOR);
        heroMpBar    = makeBar(MP_COLOR);
        heroShieldBar= makeBar(SHIELD_CLR);
        heroStatus.add(heroNameLabel); heroStatus.add(Box.createVerticalStrut(2));
        heroStatus.add(heroHpLabel);    heroStatus.add(heroHpBar);
        heroStatus.add(heroMpLabel);    heroStatus.add(heroMpBar);
        heroStatus.add(heroShieldLabel);heroStatus.add(heroShieldBar);

        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setBackground(BG_PANEL);
        turnLabel = new JLabel("TURN 0", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 16));
        turnLabel.setForeground(GOLD);
        JButton btnFlee = new JButton("Lari");
        btnFlee.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnFlee.setForeground(TEXT_DIM); btnFlee.setBackground(BG_PANEL);
        btnFlee.setBorder(BorderFactory.createLineBorder(new Color(80,70,100),1));
        btnFlee.setFocusPainted(false);
        btnFlee.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFlee.addActionListener(e -> confirmFlee());
        midPanel.add(turnLabel, BorderLayout.CENTER);
        midPanel.add(btnFlee,   BorderLayout.SOUTH);

        JPanel enemyStatus = new JPanel();
        enemyStatus.setLayout(new BoxLayout(enemyStatus, BoxLayout.Y_AXIS));
        enemyStatus.setBackground(BG_PANEL);
        enemyStatus.setPreferredSize(new Dimension(240, 90));
        enemyNameLabel = makeLabel("Enemy",       new Font("Serif",      Font.BOLD,  14), RED);
        enemyNameLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        enemyHpLabel   = makeLabel("HP: 120/120", new Font("Monospaced", Font.PLAIN, 11), ENEMY_HP);
        enemyHpLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        enemyHpBar = makeBar(ENEMY_HP);
        enemyStatus.add(enemyNameLabel); enemyStatus.add(Box.createVerticalStrut(2));
        enemyStatus.add(enemyHpLabel); enemyStatus.add(enemyHpBar);

        bar.add(heroStatus,  BorderLayout.WEST);
        bar.add(midPanel,    BorderLayout.CENTER);
        bar.add(enemyStatus, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBattleArea() {
        JPanel area = new JPanel(new BorderLayout(6, 6));
        area.setBackground(BG_DARK);
        JPanel spriteRow = new JPanel(new GridLayout(1, 2, 10, 0));
        spriteRow.setBackground(BG_DARK);
        spriteRow.setPreferredSize(new Dimension(0, 220));
        heroSpritePanel  = new SpritePanel(true,  null, null);
        enemySpritePanel = new SpritePanel(false, null, null);
        spriteRow.add(heroSpritePanel);
        spriteRow.add(enemySpritePanel);
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(5,5,15));
        battleLog.setForeground(TEXT_MAIN);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        battleLog.setLineWrap(true); battleLog.setWrapStyleWord(true);
        battleLog.setBorder(new EmptyBorder(6,10,6,10));
        JScrollPane scroll = new JScrollPane(battleLog);
        scroll.setBorder(new LineBorder(new Color(40,35,70),1));
        scroll.getViewport().setBackground(new Color(5,5,15));
        area.add(spriteRow, BorderLayout.NORTH);
        area.add(scroll,    BorderLayout.CENTER);
        return area;
    }

    private JPanel buildHandSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0,4));
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(new EmptyBorder(4,0,0,0));
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(BG_DARK);
        handLabel = new JLabel("Kartu di Tangan  —  Klik untuk memainkan", SwingConstants.CENTER);
        handLabel.setFont(new Font("Serif", Font.BOLD, 13));
        handLabel.setForeground(GOLD);
        manaWarning = new JLabel("", SwingConstants.RIGHT);
        manaWarning.setFont(new Font("SansSerif", Font.ITALIC, 11));
        manaWarning.setForeground(RED);
        manaWarning.setBorder(new EmptyBorder(0,0,0,10));
        headerRow.add(handLabel, BorderLayout.CENTER);
        headerRow.add(manaWarning, BorderLayout.EAST);
        handPanel = new JPanel(new GridLayout(1,4,8,0));
        handPanel.setBackground(BG_DARK);
        handPanel.setPreferredSize(new Dimension(0,120));
        cardButtons = new JButton[4];
        for (int i=0; i<4; i++) { cardButtons[i]=createCardSlotButton(); handPanel.add(cardButtons[i]); }
        wrapper.add(headerRow, BorderLayout.NORTH);
        wrapper.add(handPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton createCardSlotButton() {
        JButton btn = new JButton();
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(new Color(22,18,50));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(60,55,100),1), new EmptyBorder(6,4,6,4)));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(new Color(45,38,88)); }
            public void mouseExited (MouseEvent e) { btn.setBackground(new Color(22,18,50)); }
        });
        return btn;
    }

    private void updateAllUI() {
        if (hero==null||enemy==null) return;
        updateStatusBars(); updateCardButtons(); updateTurnLabel();
        heroSpritePanel.repaint(); enemySpritePanel.repaint();
    }

    private void updateStatusBars() {
        int hpPct = pct(hero.getHp(), hero.getMaxHp());
        heroHpBar.setValue(hpPct); heroHpLabel.setText("HP: "+hero.getHp()+"/"+hero.getMaxHp());
        heroHpBar.setForeground(hpPct<25?RED:HP_COLOR);
        int mpPct = pct(hero.getMp(), hero.getMaxMp());
        heroMpBar.setValue(mpPct); heroMpLabel.setText("MP: "+hero.getMp()+"/"+hero.getMaxMp());
        boolean hasShield = hero.getDefenseShield()>0;
        heroShieldBar.setValue(hasShield?Math.min(100,hero.getDefenseShield()*3):0);
        heroShieldLabel.setText("Shield: "+hero.getDefenseShield());
        heroShieldBar.setVisible(hasShield); heroShieldLabel.setVisible(hasShield);
        heroNameLabel.setText("  "+hero.getName()+"  ["+hero.getClassType().displayName+"]");
        int eHpPct = pct(enemy.getHp(), enemy.getMaxHp());
        enemyHpBar.setValue(eHpPct); enemyHpLabel.setText("HP: "+enemy.getHp()+"/"+enemy.getMaxHp());
        enemyNameLabel.setText("  "+enemy.getName());
    }

    private void updateCardButtons() {
        List<Card> hand = hero.getHand();
        manaWarning.setText("");
        for (int i=0; i<cardButtons.length; i++) {
            JButton btn = cardButtons[i];
            for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
            if (i < hand.size()) {
                Card card = hand.get(i);
                boolean canPlay = hero.canPlayCard(card)&&playerTurn&&!gameOver;
                btn.setText(card.getButtonLabel()); btn.setToolTipText(card.getTooltipText());
                btn.setEnabled(canPlay);
                Color bc = getCardColor(card);
                if (card.getType()==Card.CardType.ULTIMATE&&!hero.canPlayCard(card)) {
                    bc=new Color(100,80,80);
                    manaWarning.setText("  Mana kurang untuk Ultimate!");
                }
                Color finalBc=bc;
                btn.setBorder(new CompoundBorder(new LineBorder(finalBc,canPlay?2:1),new EmptyBorder(6,4,6,4)));
                final Card cc=card; btn.addActionListener(e->onCardPlayed(cc));
            } else {
                btn.setText("<html><center>-<br>Kosong</center></html>"); btn.setEnabled(false);
                btn.setBorder(new CompoundBorder(new LineBorder(new Color(40,35,70),1),new EmptyBorder(6,4,6,4)));
            }
        }
    }

    private void updateTurnLabel() {
        if (gameOver) { turnLabel.setText("SELESAI"); turnLabel.setForeground(TEXT_DIM); }
        else { turnLabel.setText("TURN "+turnCount+(playerTurn?"  ":"  ")); turnLabel.setForeground(playerTurn?GOLD:RED); }
    }

    private Color getCardColor(Card card) {
        switch(card.getType()) {
            case ATTACK: return RED; case ULTIMATE: return PURPLE;
            default: return card.getSkillEffect()==Card.SkillEffect.HEAL?GREEN:BLUE;
        }
    }

    private void onCardPlayed(Card card) {
        if (!playerTurn||gameOver) return;
        if (!hero.canPlayCard(card)) { logMsg("  Mana tidak cukup! Butuh "+card.getManaCost()+" MP.", RED); return; }
        playerTurn=false; setHandEnabled(false); turnCount++;
        logMsg("", TEXT_MAIN);
        logMsg("-- GILIRAN HERO (Turn "+turnCount+") --", GOLD);
        // Spawn efek visual dulu
        spawnCardEffect(card);
        Timer t = new Timer(400, e -> {
            executeCard(card); hero.playCard(card); updateAllUI();
            if (!enemy.isAlive()) { endGame(true); return; }
            Timer et = new Timer(1000, ev -> doEnemyTurn()); et.setRepeats(false); et.start();
        });
        t.setRepeats(false); t.start();
    }

    private void executeCard(Card card) {
        logMsg("  " + hero.getName() + " memainkan [" + card.getName() + "]"
               + (card.getManaCost()>0?" (-"+card.getManaCost()+" MP)":""), GOLD);
        switch (card.getType()) {
            case ATTACK -> {
                enemy.takeDamage(card.getValue()); enemyShakeFrames=20;
                logMsg("    Menyerang "+enemy.getName()+": -"+card.getValue()+" HP", RED);
                logMsg("    "+enemy.getName()+" sisa HP: "+enemy.getHp(), TEXT_DIM);
            }
            case SKILL -> {
                if (card.getSkillEffect()==Card.SkillEffect.HEAL) {
                    int before=hero.getHp(); hero.heal(card.getValue());
                    logMsg("    Memulihkan "+(hero.getHp()-before)+" HP!", GREEN);
                } else { hero.addShield(card.getValue()); logMsg("    Shield +"+card.getValue()+" poin!", BLUE); }
                logMsg("    "+hero.getStatusText(), TEXT_DIM);
            }
            case ULTIMATE -> {
                enemy.takeDamage(card.getValue()); enemyShakeFrames=30;
                logMsg("    ULTIMATE! Damage "+card.getValue()+" menghancurkan "+enemy.getName()+"!", PURPLE);
                logMsg("    "+enemy.getName()+" sisa HP: "+enemy.getHp(), TEXT_DIM);
            }
        }
    }

    private void doEnemyTurn() {
        logMsg("", TEXT_MAIN); logMsg("-- GILIRAN MUSUH --", RED);
        Enemy.AttackResult atk = enemy.attack();
        logMsg("  "+atk.message, TEXT_MAIN);
        activeEffects.add(new CardEffect("ENEMY_ATTACK", false, heroSpritePanel));
        heroShakeFrames=20;
        int shieldBefore=hero.getDefenseShield(); int actual=hero.takeDamage(atk.damage);
        if (shieldBefore>0) logMsg("    Shield menyerap! Damage aktual: "+actual, BLUE);
        logMsg("    "+hero.getStatusText(), TEXT_DIM);
        int manaRegen=8+(hero.getClassType()==HeroClass.ClassType.MAGE?5:0);
        hero.regenMana(manaRegen); if (manaRegen>0) logMsg("    Mana regen +"+manaRegen, MP_COLOR);
        updateAllUI();
        if (!hero.isAlive()) { endGame(false); return; }
        playerTurn=true; setHandEnabled(true); updateAllUI();
        logMsg("", TEXT_MAIN); logMsg("Giliranmu! Pilih kartu.", GREEN);
    }

    private void spawnCardEffect(Card card) {
        boolean onEnemy = card.getType()==Card.CardType.ATTACK||card.getType()==Card.CardType.ULTIMATE;
        SpritePanel target = onEnemy ? enemySpritePanel : heroSpritePanel;
        activeEffects.add(new CardEffect(card.getName(), onEnemy, target));

        // ─── SFX berdasarkan nama kartu ───────────────────────
        String n = card.getName().toLowerCase();
        if      (n.contains("bladestorm") || n.contains("blade storm"))
            main.AudioManager.get().playSfx(main.AudioManager.SFX_BLADE_STORM);
        else if (n.contains("sword slash") || n.contains("cleave"))
            main.AudioManager.get().playSfx(main.AudioManager.SFX_SWORD_SLASH);
        else if (n.contains("war cry"))
            main.AudioManager.get().playSfx(main.AudioManager.SFX_WAR_CRY);
        else if (n.contains("shield bash"))
            main.AudioManager.get().playSfx(main.AudioManager.SFX_SHIELD_BASH);
        else if (n.contains("battle stance"))
            main.AudioManager.get().playSfx(main.AudioManager.SFX_BATTLE_STANCE);
        else if (n.contains("slash") || n.contains("strike") || n.contains("attack"))
            main.AudioManager.get().playSfx(main.AudioManager.SFX_SWORD_SLASH);
    }

    private void endGame(boolean heroWon) {
        gameOver=true; setHandEnabled(false); updateAllUI();
        logMsg("", TEXT_MAIN);
        if (heroWon) { logMsg("KEMENANGAN!", GOLD); logMsg(hero.getName()+" mengalahkan "+enemy.getName()+"!", GOLD); }
        else { logMsg("KEKALAHAN", RED); logMsg(hero.getName()+" telah gugur...", RED); }
        Timer t = new Timer(600, e -> showEndDialog(heroWon)); t.setRepeats(false); t.start();
    }

    private void showEndDialog(boolean heroWon) {
        if (dialogShowing) return; dialogShowing=true;
        if (heroWon) {
            String msg="KEMENANGAN!\n"+hero.getName()+" mengalahkan "+enemy.getName()+"!\nHP tersisa: "+hero.getHp()+"/"+hero.getMaxHp()+"\n\nKembali ke eksplorasi...";
            JOptionPane.showMessageDialog(this,msg,"KEMENANGAN!",JOptionPane.INFORMATION_MESSAGE);
            dialogShowing=false; onReturnToMenu.run();
        } else {
            String msg="GAME OVER\n"+hero.getName()+" telah gugur...\nHP Musuh tersisa: "+enemy.getHp()+"/"+enemy.getMaxHp();
            int c=JOptionPane.showOptionDialog(this,msg,"GAME OVER",JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,null,new String[]{"Coba Lagi","Keluar"},"Coba Lagi");
            if (c==0) { dialogShowing=false; startCombat(new HeroClass.CharacterProfile(hero.getClassType())); }
            else { dialogShowing=false; System.exit(0); }
        }
    }

    private void confirmFlee() {
        int c=JOptionPane.showConfirmDialog(this,"Yakin ingin mundur?","Mundur",JOptionPane.YES_NO_OPTION);
        if (c==JOptionPane.YES_OPTION) onReturnToMenu.run();
    }

    private void logMsg(String msg,Color color) {
        battleLog.append(msg+"\n");
        SwingUtilities.invokeLater(()->battleLog.setCaretPosition(battleLog.getDocument().getLength()));
    }
    private void setHandEnabled(boolean e) { for (JButton b:cardButtons) b.setEnabled(e); }
    private static int pct(int v,int m) { return m>0?(int)((double)v/m*100):0; }
    private static JLabel makeLabel(String t,Font f,Color c) { JLabel l=new JLabel(t); l.setFont(f); l.setForeground(c); return l; }
    private static JProgressBar makeBar(Color c) {
        JProgressBar b=new JProgressBar(0,100); b.setValue(100); b.setForeground(c);
        b.setBackground(new Color(30,25,50)); b.setBorderPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,12)); return b;
    }

    // =========================================================
    // SpritePanel — PNG + shake + efek overlay
    // =========================================================
    class SpritePanel extends JPanel {
        final boolean isHero;
        HeroClass.ClassType classType;
        String enemyName;
        private BufferedImage cachedImg;
        private String        cachedKey;
        private final Random  localRng = new Random();

        SpritePanel(boolean isHero, HeroClass.ClassType classType, String enemyName) {
            this.isHero=isHero; this.classType=classType; this.enemyName=enemyName;
            setBackground(new Color(12,10,30));
            setBorder(new LineBorder(new Color(40,35,70),1));
        }
        void setClassType(HeroClass.ClassType ct) { classType=ct; cachedKey=null; repaint(); }
        void setEnemyName(String n)               { enemyName=n;  cachedKey=null; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);
            int W=getWidth(), H=getHeight();

            // background gradient
            GradientPaint bg = isHero
                ? new GradientPaint(0,H,new Color(10,20,10),0,0,new Color(12,10,30))
                : new GradientPaint(0,H,new Color(25,8,8),  0,0,new Color(12,10,30));
            g2.setPaint(bg); g2.fillRect(0,0,W,H);

            // shake
            int sf = isHero ? heroShakeFrames : enemyShakeFrames;
            int sx=0, sy=0;
            if (sf>0) { sx=(int)((localRng.nextFloat()-0.5f)*10*(sf/20f)); sy=(int)((localRng.nextFloat()-0.5f)*6*(sf/20f)); }
            g2.translate(sx, sy);

            // platform
            Color plat = isHero ? new Color(20,40,20,180) : new Color(40,15,15,180);
            g2.setColor(plat); g2.fillOval(W/2-55, H-22, 110, 18);

            // sprite
            if (isHero && classType!=null) drawHero(g2,W,H);
            else if (!isHero && enemyName!=null) drawEnemy(g2,W,H);

            // shield aura
            if (isHero && hero!=null && hero.getDefenseShield()>0) drawShieldAura(g2,W,H);

            g2.translate(-sx,-sy);

            // overlay efek kartu
            for (CardEffect ef : activeEffects)
                if (ef.targetPanel==this) ef.draw(g2,W,H);

            g2.dispose();
        }

        private void drawHero(Graphics2D g2, int W, int H) {
            String key = (classType!=null?classType.name().toLowerCase():"hero");
            // Cari PNG: assets/warrior.png (untuk WARRIOR), atau assets/<class>.png
            String pngFile = "assets/" + key + ".png";
            // Jika WARRIOR, fallback ke assets/warrior.png
            if (classType==HeroClass.ClassType.WARRIOR) pngFile="assets/warrior.png";
            BufferedImage img = loadImg(key, pngFile);
            if (img==null) img = loadImg(key+"_b", key+".png"); // coba root
            if (img!=null) drawFit(g2,img,W,H);
            else HeroClass.drawHeroSprite(g2,classType,0,0,W,H-20);
            // label
            if (classType!=null) {
                g2.setFont(new Font("Serif",Font.BOLD,11));
                g2.setColor(classType.themeColor);
                FontMetrics fm=g2.getFontMetrics();
                String lbl=classType.emoji+" "+classType.displayName;
                g2.drawString(lbl,(W-fm.stringWidth(lbl))/2,H-5);
            }
        }

        private void drawEnemy(Graphics2D g2, int W, int H) {
            String key = enemyName!=null?enemyName.toLowerCase().replace(" ","_"):"orc";
            String pngFile = "assets/"+key+".png";
            if (key.contains("orc")) pngFile="assets/orc.png";
            BufferedImage img = loadImg(key, pngFile);
            if (img==null) img = loadImg(key+"_b", key+".png");
            if (img!=null) drawFit(g2,img,W,H);
            else HeroClass.drawEnemySprite(g2,enemyName,0,0,W,H-20);
            // label
            g2.setFont(new Font("Serif",Font.BOLD,11));
            g2.setColor(new Color(220,80,80));
            FontMetrics fm=g2.getFontMetrics();
            String lbl=enemyName!=null?enemyName:"";
            g2.drawString(lbl,(W-fm.stringWidth(lbl))/2,H-5);
        }

        private void drawFit(Graphics2D g2, BufferedImage img, int W, int H) {
            int dh=H-28;
            double sc=Math.min((double)W/img.getWidth(),(double)dh/img.getHeight());
            int dw=(int)(img.getWidth()*sc), dH2=(int)(img.getHeight()*sc);
            int dx=(W-dw)/2, dy=dh-dH2;
            g2.drawImage(img,dx,dy,dw,dH2,null);
        }

        private BufferedImage loadImg(String key, String path) {
            if (key.equals(cachedKey)) return cachedImg;
            try {
                File f=new File(path);
                if (f.exists()) { cachedImg=ImageIO.read(f); cachedKey=key; return cachedImg; }
                var s=getClass().getResourceAsStream("/"+path);
                if (s==null) s=getClass().getResourceAsStream(path);
                if (s!=null) { cachedImg=ImageIO.read(s); cachedKey=key; return cachedImg; }
            } catch (IOException ignored) {}
            cachedKey=key+"_miss"; cachedImg=null; return null;
        }

        private void drawShieldAura(Graphics2D g2, int W, int H) {
            int cx=W/2, cy=(H-28)/2+10, r=Math.min(W,H-28)/2-8;
            float p=(float)(0.3+0.15*Math.sin(System.currentTimeMillis()*0.004));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,p));
            g2.setColor(new Color(80,160,255));
            g2.setStroke(new BasicStroke(3f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,p*0.35f));
            g2.fillOval(cx-r,cy-r,r*2,r*2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // =========================================================
    // CardEffect — animasi per nama kartu
    // =========================================================
    static class CardEffect {
        final String      name;
        final boolean     onEnemy;
        final SpritePanel targetPanel;
        int   frame    = 0;
        int   maxFrame = 40;
        private final Random rng = new Random();
        private final float[] swordX, swordDelay;
        private final boolean[] swordFromLeft; // true = dari kiri atas ke kanan bawah, false = sebaliknya

        CardEffect(String name, boolean onEnemy, SpritePanel targetPanel) {
            this.name=name; this.onEnemy=onEnemy; this.targetPanel=targetPanel;
            String n=name.toLowerCase();
            if (n.contains("bladestorm")||n.contains("blade storm")) {
                maxFrame=65;
                swordX=new float[8]; swordDelay=new float[8]; swordFromLeft=new boolean[8];
                Random initRng=new Random();
                for (int i=0;i<8;i++) {
                    swordFromLeft[i]=initRng.nextBoolean();
                    // Titik awal X: dari kiri atas mulai di sisi kiri, dari kanan atas mulai di sisi kanan
                    swordX[i]=swordFromLeft[i] ? (0.02f+initRng.nextFloat()*0.25f) : (0.73f+initRng.nextFloat()*0.25f);
                    swordDelay[i]=i*4f + initRng.nextFloat()*3f;
                }
            } else if (n.contains("war cry"))     { maxFrame=50; swordX=null; swordDelay=null; swordFromLeft=null; }
            else if (n.contains("battle stance")) { maxFrame=60; swordX=null; swordDelay=null; swordFromLeft=null; }
            else { swordX=null; swordDelay=null; swordFromLeft=null; }
        }

        void tick() { frame++; }
        boolean isDone() { return frame>=maxFrame; }

        void draw(Graphics2D g, int W, int H) {
            String n=name.toLowerCase();
            if      (n.contains("sword slash"))                          drawSwordSlash(g,W,H);
            else if (n.contains("cleave"))                               drawCleave(g,W,H);
            else if (n.contains("bladestorm")||n.contains("blade storm"))drawBladestorm(g,W,H);
            else if (n.contains("war cry"))                              drawWarCry(g,W,H);
            else if (n.contains("shield bash"))                          drawShieldBash(g,W,H);
            else if (n.contains("battle stance"))                        drawBattleStance(g,W,H);
            else if (n.equals("enemy_attack"))                           drawEnemyAttack(g,W,H);
            else if (n.contains("ultimate")||n.contains("death")||n.contains("meteor")||n.contains("unbreakable")) drawUltimate(g,W,H);
            else                                                         drawGeneric(g,W,H);
        }

        // Sword Slash — tebasan diagonal kiri-atas ke kanan-bawah
        private void drawSwordSlash(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float a=t<0.3f?t/0.3f:(1f-t)/0.7f; a=Math.min(1f,a);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
            Stroke old=g.getStroke();
            int cx=W/2, cy=H/2-10;
            for (int i=-1;i<=1;i++) {
                float off=i*14;
                g.setColor(i==0?new Color(255,255,200):new Color(255,200,100,180));
                g.setStroke(new BasicStroke(i==0?5f:2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g.drawLine((int)(cx-65+off),cy-55,(int)(cx+65+off),cy+55);
            }
            g.setColor(new Color(255,255,150,(int)(90*a)));
            g.fillOval(cx-40,cy-40,80,80);
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Cleave — garis horizontal lebar dengan glow
        private void drawCleave(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float a=t<0.3f?t/0.3f:(1f-t)/0.7f; a=Math.min(1f,a);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
            int cy=H/2; Stroke old=g.getStroke();
            g.setColor(new Color(255,120,50));
            g.setStroke(new BasicStroke(9f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.drawLine(8,cy,W-8,cy);
            g.setColor(new Color(255,200,100,160));
            g.setStroke(new BasicStroke(3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.drawLine(8,cy-16,W-8,cy-16); g.drawLine(8,cy+16,W-8,cy+16);
            g.setColor(new Color(255,150,50,(int)(60*a)));
            g.fillRect(8,cy-22,W-16,44);
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Bladestorm — pedang muncul dari kiri atas atau kanan atas secara acak, melesat diagonal
        private void drawBladestorm(Graphics2D g, int W, int H) {
            Stroke old=g.getStroke();
            for (int i=0;i<8;i++) {
                float lf=frame-swordDelay[i]; if (lf<0) continue;
                float prog=Math.min(1f,lf/35f);

                boolean fromLeft=swordFromLeft[i];
                // Diagonal: dari kiri atas ke kanan bawah, atau kanan atas ke kiri bawah
                float startX=swordX[i]*W;
                float endX  =fromLeft ? (startX + W*0.55f) : (startX - W*0.55f);
                float sx    =startX + (endX-startX)*prog;
                float sy    =(float)(-60 + (H+60)*prog);

                // Sudut rotasi pedang sesuai arah diagonal (~45 derajat)
                double angle=fromLeft ? Math.toRadians(135) : Math.toRadians(45);

                float a=prog<0.75f?1f:(1f-prog)/0.25f;
                a=Math.max(0f,a);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));

                // Ekor cahaya trailing (jejak gerakan)
                float trailLen=60f;
                float tx=sx - (float)Math.cos(angle-Math.PI/2)*trailLen;
                float ty=sy - (float)Math.sin(angle-Math.PI/2)*trailLen;
                g.setColor(new Color(160,190,255,(int)(50*a)));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int)tx,(int)ty,(int)sx,(int)sy);

                // Gambar pedang
                AffineTransform at=g.getTransform();
                g.translate(sx,sy);
                g.rotate(angle);
                // Mata pedang (blade)
                g.setColor(new Color(210,230,255)); g.fillRoundRect(-4,-28,8,44,3,3);
                // Kilau tepi blade
                g.setColor(new Color(255,255,255,(int)(180*a))); g.fillRect(-2,-28,2,44);
                // Crossguard (gagang palang)
                g.setColor(new Color(255,220,80)); g.fillRect(-10,14,20,6);
                // Grip
                g.setColor(new Color(140,100,60)); g.fillRoundRect(-3,20,6,18,2,2);
                // Pommel
                g.setColor(new Color(200,170,90)); g.fillOval(-5,36,10,8);

                g.setTransform(at);

                // Kilat impact kecil di posisi pedang
                if (prog>0.85f) {
                    float spark=a;
                    g.setColor(new Color(255,240,150,(int)(120*spark)));
                    g.setStroke(new BasicStroke(1.5f));
                    int ex=(int)sx, ey=(int)sy;
                    g.drawLine(ex-8,ey-8,ex+8,ey+8);
                    g.drawLine(ex+8,ey-8,ex-8,ey+8);
                    g.drawLine(ex,ey-10,ex,ey+10);
                }
            }
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // War Cry — aura merah menyala membesar
        private void drawWarCry(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float r=t*Math.min(W,H)*0.6f;
            float a=t<0.5f?t*2f:(1f-t)*2f; a=Math.max(0f,Math.min(1f,a));
            int cx=W/2, cy=H/2;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a*0.55f));
            RadialGradientPaint rg=new RadialGradientPaint(cx,cy,Math.max(1f,r),
                new float[]{0f,0.7f,1f},
                new Color[]{new Color(255,80,0,200),new Color(255,40,0,100),new Color(255,0,0,0)});
            g.setPaint(rg); g.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
            Stroke old=g.getStroke();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
            g.setColor(new Color(255,140,40)); g.setStroke(new BasicStroke(3f));
            g.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
            if (frame<30) {
                g.setFont(new Font("Serif",Font.BOLD,22));
                g.setColor(new Color(255,200,50,(int)(255*a)));
                FontMetrics fm=g.getFontMetrics(); String txt="WAR CRY!";
                g.drawString(txt,(W-fm.stringWidth(txt))/2,H/3);
            }
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Shield Bash — perisai retak muncul di target
        private void drawShieldBash(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float a=t<0.2f?t/0.2f:(1f-t)/0.8f; a=Math.max(0f,Math.min(1f,a));
            float sc=Math.min(1f,frame/20f);
            int sw=(int)(85*sc), sh=(int)(95*sc), cx=W/2, cy=H/2;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
            AffineTransform at=g.getTransform();
            g.translate(cx,cy); g.rotate(Math.toRadians(frame*1.5));
            g.translate(-sw/2,-sh/2);
            int[] px={sw/2,sw,sw,sw/2,0,0};
            int[] py={0,sh/4,sh*3/4,sh,sh*3/4,sh/4};
            g.setColor(new Color(100,140,210,210)); g.fillPolygon(px,py,6);
            g.setColor(new Color(180,210,255));
            Stroke old=g.getStroke(); g.setStroke(new BasicStroke(3f)); g.drawPolygon(px,py,6);
            if (frame>18) {
                g.setColor(new Color(255,100,50,200)); g.setStroke(new BasicStroke(2.5f));
                g.drawLine(sw/2,sh/4,sw/2+16,sh/2); g.drawLine(sw/2+16,sh/2,sw/2-12,sh*3/4);
                g.drawLine(sw/2+16,sh/2,sw/2+32,sh*3/5);
            }
            g.setStroke(old); g.setTransform(at);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Battle Stance — perisai membesar lalu lingkaran aura
        private void drawBattleStance(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame; int cx=W/2, cy=H/2;
            if (frame<35) {
                float grow=Math.min(1f,frame/20f);
                float a=frame<30?1f:(35f-frame)/5f; a=Math.max(0f,a);
                int sw=(int)(65*grow), sh=(int)(75*grow);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
                AffineTransform at=g.getTransform();
                g.translate(cx-sw/2,cy-sh/2);
                int[] px={sw/2,sw,sw,sw/2,0,0}; int[] py={0,sh/4,sh*3/4,sh,sh*3/4,sh/4};
                g.setColor(new Color(80,160,255,210)); g.fillPolygon(px,py,6);
                g.setColor(new Color(150,210,255));
                Stroke old=g.getStroke(); g.setStroke(new BasicStroke(3f)); g.drawPolygon(px,py,6);
                g.setFont(new Font("SansSerif",Font.BOLD,(int)(20*grow)));
                g.setColor(Color.WHITE);
                FontMetrics fm=g.getFontMetrics(); String s="[STANCE]";
                g.drawString(s,(sw-fm.stringWidth(s))/2,sh/2+fm.getAscent()/2);
                g.setStroke(old); g.setTransform(at);
            } else {
                float lT=(frame-35f)/(maxFrame-35f);
                float r=lT*Math.min(W,H)*0.55f; float a=1f-lT;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
                g.setColor(new Color(80,160,255,110));
                g.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
                Stroke old=g.getStroke(); g.setColor(new Color(150,220,255));
                g.setStroke(new BasicStroke(4f));
                g.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
                g.setStroke(old);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Enemy Attack — cakar menyambar dari kanan
        private void drawEnemyAttack(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float a=t<0.3f?t/0.3f:(1f-t)/0.7f; a=Math.min(1f,a);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
            int cy=H/2; Stroke old=g.getStroke();
            for (int i=-1;i<=1;i++) {
                int oy=i*20;
                g.setColor(i==0?new Color(255,80,80):new Color(210,55,55,180));
                g.setStroke(new BasicStroke(i==0?6f:3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g.drawLine(W-15,cy+oy,15,cy+oy+i*12);
            }
            g.setColor(new Color(255,100,100,(int)(90*a)));
            g.fillOval(8,cy-35,70,70);
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Ultimate — ledakan besar
        private void drawUltimate(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float a=t<0.2f?t/0.2f:(1f-t)/0.8f; a=Math.min(1f,a);
            int cx=W/2, cy=H/2;
            float r1=t*W*0.85f, r2=t*W*0.55f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a*0.7f));
            RadialGradientPaint rg=new RadialGradientPaint(cx,cy,Math.max(1f,r1),
                new float[]{0f,0.5f,1f},
                new Color[]{new Color(255,255,100,230),new Color(255,100,255,130),new Color(0,0,0,0)});
            g.setPaint(rg); g.fillOval((int)(cx-r1),(int)(cy-r1),(int)(r1*2),(int)(r1*2));
            Stroke old=g.getStroke();
            g.setColor(new Color(255,200,50,(int)(255*a))); g.setStroke(new BasicStroke(5f));
            g.drawOval((int)(cx-r2),(int)(cy-r2),(int)(r2*2),(int)(r2*2));
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }

        // Generic slash fallback
        private void drawGeneric(Graphics2D g, int W, int H) {
            float t=(float)frame/maxFrame;
            float a=t<0.3f?t/0.3f:(1f-t)/0.7f; a=Math.min(1f,a);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
            Stroke old=g.getStroke();
            g.setColor(new Color(255,200,100)); g.setStroke(new BasicStroke(4.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.drawLine(W/2-55,H/2-45,W/2+55,H/2+45);
            g.setStroke(old); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
        }
    }
}
