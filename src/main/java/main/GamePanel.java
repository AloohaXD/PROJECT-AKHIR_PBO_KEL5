package main;

import entity.*;
import tile.TileManager;
import ui.*;
import combat.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {

    public static final int TILE_SIZE    = 48;
    public static final int SCREEN_COLS  = 20;
    public static final int SCREEN_ROWS  = 15;
    public static final int SCREEN_WIDTH = TILE_SIZE * SCREEN_COLS;
    public static final int SCREEN_HEIGHT= TILE_SIZE * SCREEN_ROWS;
    public static final int WORLD_COLS   = 40;
    public static final int WORLD_ROWS   = 40;
    public static final int WORLD_WIDTH  = TILE_SIZE * WORLD_COLS;
    public static final int WORLD_HEIGHT = TILE_SIZE * WORLD_ROWS;
    private static final int TARGET_FPS  = 60;
    private static final long NS_PER_SEC = 1_000_000_000L;

    public enum GameState {
        MAIN_MENU, SETTINGS, HERO_SELECT, PROLOG, CHAPTER_INTRO,
        EXPLORATION, DIALOG, TRANSITION, COMBAT, WIN, GAME_OVER
    }
    public GameState gameState = GameState.MAIN_MENU;

    public final KeyHandler       keyHandler  = new KeyHandler();
    public final Camera           camera      = new Camera(this);
    public final TileManager      tileManager = new TileManager(this);
    public final CollisionChecker collision   = new CollisionChecker(this);

    public Player       player;
    public List<Entity> entities = new ArrayList<>();

    private HudRenderer    hudRenderer;
    private QuestTracker   questTracker;
    private ChapterManager chapterManager;

    // Overlay panels
    private MainMenuPanel     mainMenuOverlay;
    private SettingsPanel     settingsOverlay;
    private HeroSelectPanel   heroSelectOverlay;
    private PrologPanel       prologOverlay;
    private ChapterIntroPanel chapterIntroOverlay;
    private WinScreen         winOverlay;
    private GameOverScreen    gameOverOverlay;
    private CombatPanel       combatPanel;

    private final TransitionEffect transition = new TransitionEffect();
    private int     pendingEnemyIndex = -1;
    private boolean pendingIsBoss     = false;
    // BUG FIX: store the boss entity directly so retry uses same enemy
    private EnemyEntity pendingBossEntity = null;

    // Party profiles (scaled by player level)
    private HeroClass.CharacterProfile[] partyProfiles = null;

    private int currentFPS=0; private long fpsTimer=0; private int fpsCounter=0;
    private Thread gameThread;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        setFocusable(true);
        setLayout(null);
        chapterManager = new ChapterManager();
        questTracker   = new QuestTracker();
        // FIX: preload ALL images once at startup to prevent combat lag
        ImageCache.get().preloadAll();
        setupCombatPanel();
        showMainMenu();
    }

    // ── Setup ────────────────────────────────────────────────

    private void setupCombatPanel() {
        combatPanel = new CombatPanel(this::onCombatReturn);
        combatPanel.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        combatPanel.setVisible(false);
        add(combatPanel);
    }

    private void showMainMenu() {
        // FIX: force BGM restart on main menu
        AudioManager.get().stopBgmImmediate();
        AudioManager.get().playBgm(AudioManager.BGM_MAIN_MENU, true, true);
        mainMenuOverlay = new MainMenuPanel(SCREEN_WIDTH, SCREEN_HEIGHT,
            this::onStartGame, this::onOpenSettings, ()-> System.exit(0));
        mainMenuOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        add(mainMenuOverlay); setComponentZOrder(mainMenuOverlay,0);
        gameState = GameState.MAIN_MENU;
        repaint();
    }

    private void removeOverlay(JPanel p) {
        if (p != null) { try { remove(p); } catch(Exception ignored) {} }
    }

    private void onOpenSettings() {
        SwingUtilities.invokeLater(() -> {
            if(mainMenuOverlay!=null) mainMenuOverlay.setVisible(false);
            settingsOverlay = new SettingsPanel(SCREEN_WIDTH, SCREEN_HEIGHT, () -> {
                SwingUtilities.invokeLater(() -> {
                    if(settingsOverlay!=null){ settingsOverlay.stopAnimation(); removeOverlay(settingsOverlay); settingsOverlay=null; }
                    if(mainMenuOverlay!=null) mainMenuOverlay.setVisible(true);
                    gameState = GameState.MAIN_MENU; repaint();
                });
            });
            settingsOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
            add(settingsOverlay); setComponentZOrder(settingsOverlay,0);
            gameState = GameState.SETTINGS; repaint();
        });
    }

    private void onStartGame() {
        SwingUtilities.invokeLater(() -> {
            if(mainMenuOverlay!=null){ mainMenuOverlay.stopAnimation(); removeOverlay(mainMenuOverlay); mainMenuOverlay=null; }
            heroSelectOverlay = new HeroSelectPanel(SCREEN_WIDTH, SCREEN_HEIGHT, this::onHeroSelectDone);
            heroSelectOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
            add(heroSelectOverlay); setComponentZOrder(heroSelectOverlay,0);
            gameState = GameState.HERO_SELECT; repaint();
        });
    }

    private void onHeroSelectDone(HeroClass.ClassType[] party, HeroClass.ClassType leader) {
        SwingUtilities.invokeLater(() -> {
            if(heroSelectOverlay!=null){ heroSelectOverlay.stopAnimation(); removeOverlay(heroSelectOverlay); heroSelectOverlay=null; }
            // Build world with selected party
            chapterManager.reset();
            setupWorld(party, leader);
            chapterManager.startTimer();
            AudioManager.get().fadeOutBgm(800, null);
            // Show prolog first
            showProlog();
        });
    }

    private void showProlog() {
        gameState = GameState.PROLOG;
        prologOverlay = new PrologPanel(SCREEN_WIDTH, SCREEN_HEIGHT, () -> {
            SwingUtilities.invokeLater(() -> {
                if(prologOverlay!=null){ prologOverlay.stopAnimation(); removeOverlay(prologOverlay); prologOverlay=null; }
                AudioManager.get().playBgm(AudioManager.BGM_EXPLORE, true, true);
                showChapterIntro(1);
            });
        });
        prologOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        add(prologOverlay); setComponentZOrder(prologOverlay,0);
        repaint();
        SwingUtilities.invokeLater(() -> prologOverlay.requestFocusInWindow());
    }

    private void setupWorld(HeroClass.ClassType[] party, HeroClass.ClassType leader) {
        entities.clear();
        player = new Player(this, keyHandler);
        player.heroClass = leader;
        player.party     = party;
        player.getStats().setBaseStats(leader);
        player.reloadSprite(); // FIX: load correct hero sprite (not default Warrior)
        hudRenderer  = new HudRenderer(this);
        questTracker = new QuestTracker();
        questTracker.setupChapterQuests(1);
        player.setHudRenderer(hudRenderer);
        spawnNpc();
        spawnEnemiesForChapter(1);
    }

    private void setupWorldForChapter(int chapter) {
        entities.clear();
        player.worldX = TILE_SIZE*5; player.worldY = TILE_SIZE*5;
        player.restoreStats();
        player.reloadSprite();
        questTracker.setupChapterQuests(chapter);
        tileManager.loadMap(chapter);
        spawnNpc();
        spawnEnemiesForChapter(chapter);
        // Reconnect hud
        for(Entity e:entities) if(e instanceof EnemyEntity ee) ee.setHudRenderer(hudRenderer);
        pendingBossEntity = null; // BUG FIX: clear stored boss on new chapter
    }

    private void spawnNpc() {
        NPC guide = new NPC(this);
        guide.worldX=TILE_SIZE*8; guide.worldY=TILE_SIZE*7;
        int ch=chapterManager.getCurrentChapter();
        guide.dialogLines = new String[]{
            "Chapter "+ch+": "+chapterManager.getChapterTitle(),
            "Bunuh Kroco untuk dapat EXP, lalu kalahkan Demon Commander!",
            "Tekan [X] untuk menyerang musuh di dekatmu.",
            "Tekan [Q] untuk ganti quest yang tampil di HUD.",
            "Musuh sudah bisa diserang dari jarak 2 tile lebih jauh!"
        };
        entities.add(guide);
    }

    private void spawnEnemiesForChapter(int chapter) {
        Random rng = new Random(chapter * 12345L);
        int krocoCount = 3 + chapter;
        for(int i=0;i<krocoCount;i++){
            EnemyEntity e=new EnemyEntity(this,chapter,false,chapter);
            e.worldX=TILE_SIZE*(10+rng.nextInt(22));
            e.worldY=TILE_SIZE*(8+rng.nextInt(22));
            if(hudRenderer!=null) e.setHudRenderer(hudRenderer);
            entities.add(e);
        }
        // Boss
        EnemyEntity boss = new EnemyEntity(this, chapter==10?chapter*3:chapter*2+1, true, chapter);
        boss.worldX=TILE_SIZE*(20+rng.nextInt(8));
        boss.worldY=TILE_SIZE*(18+rng.nextInt(8));
        if(hudRenderer!=null) boss.setHudRenderer(hudRenderer);
        entities.add(boss);
        pendingBossEntity = boss; // BUG FIX: keep reference
    }

    private void showChapterIntro(int chapter) {
        gameState=GameState.CHAPTER_INTRO;
        String title=ChapterManager.CHAPTER_TITLES[chapter-1];
        boolean isFinal=(chapter==10);
        chapterIntroOverlay=new ChapterIntroPanel(SCREEN_WIDTH,SCREEN_HEIGHT,chapter,title,isFinal,()->{
            SwingUtilities.invokeLater(()->{
                if(chapterIntroOverlay!=null){removeOverlay(chapterIntroOverlay);chapterIntroOverlay=null;}
                gameState=GameState.EXPLORATION;
                requestFocusInWindow();
                repaint();
            });
        });
        chapterIntroOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        add(chapterIntroOverlay); setComponentZOrder(chapterIntroOverlay,0);
        repaint();
    }

    // ── Combat ───────────────────────────────────────────────

    /** BUG FIX: build scaled profiles from player level, not default stats */
    private void buildPartyProfiles() {
        if(player==null||player.party==null) return;
        partyProfiles = new HeroClass.CharacterProfile[player.party.length];
        for(int i=0;i<player.party.length;i++)
            partyProfiles[i]=player.buildScaledProfile(player.party[i]);
    }

    public void startCombatTransition(int enemyIndex) {
        if(gameState!=GameState.EXPLORATION) return;
        pendingEnemyIndex=enemyIndex;
        Entity e=entities.get(enemyIndex);
        pendingIsBoss=(e instanceof EnemyEntity ee&&ee.isBoss);
        gameState=GameState.TRANSITION;
        AudioManager.get().playSfx(AudioManager.SFX_TRANSITION);
        AudioManager.get().stopBgmImmediate();
        BufferedImage snap=new BufferedImage(SCREEN_WIDTH,SCREEN_HEIGHT,BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg=snap.createGraphics(); paintGame(sg); sg.dispose();
        transition.start(snap,this::enterCombat);
    }

    private void enterCombat() {
        gameState=GameState.COMBAT;
        int chapter=chapterManager.getCurrentChapter();
        combatPanel.currentChapter=chapter;
        // BUG FIX: BGM per combat type
        AudioManager.get().playBgmForCombat(pendingIsBoss, chapter==10&&pendingIsBoss);

        // BUG FIX: Build scaled hero profiles from current player level
        buildPartyProfiles();
        int leaderIdx=0;
        if(partyProfiles!=null&&player.heroClass!=null){
            for(int i=0;i<player.party.length;i++){
                if(player.party[i]==player.heroClass){leaderIdx=i;break;}
            }
        }

        // BUG FIX: always use same boss entity (pendingBossEntity), not a new one
        combat.Enemy foe;
        if(pendingIsBoss){
            if(chapter==10) foe=combat.Enemy.createDemonKing(player.party!=null?player.party.length:5);
            else            foe=combat.Enemy.createDemonCommander(chapter);
        } else {
            foe=combat.Enemy.createKroco(chapter);
        }

        final combat.Enemy finalFoe=foe;
        final HeroClass.CharacterProfile[] profiles=partyProfiles;
        final int ldrIdx=leaderIdx;
        SwingUtilities.invokeLater(()->{
            if(profiles!=null&&profiles.length>0)
                combatPanel.startCombat(profiles,ldrIdx,finalFoe);
            else
                combatPanel.startCombat(new HeroClass.CharacterProfile(player.heroClass),finalFoe);
            combatPanel.setVisible(true);
            combatPanel.requestFocusInWindow();
        });
    }

    private void onCombatReturn() {
        SwingUtilities.invokeLater(()->{
            combatPanel.setVisible(false);
            AudioManager.get().stopBgmImmediate();
            requestFocusInWindow();

            boolean enemyDefeated=false;
            if(pendingEnemyIndex>=0&&pendingEnemyIndex<entities.size()){
                Entity e=entities.get(pendingEnemyIndex);
                if(e instanceof EnemyEntity ee&&ee.isAlive){
                    ee.isAlive=false; enemyDefeated=true;
                    int exp=ee.getExpReward();
                    player.gainExp(exp);

                    if(ee.isBoss){
                        if(chapterManager.getCurrentChapter()==10){
                            chapterManager.stopTimer(); showWin(); return;
                        } else {
                            chapterManager.setCommanderDefeated();
                            int[] res=questTracker.updateProgress("kill_commander",1);
                            if(res[0]==1) player.gainExp(res[1]);
                            advanceChapter(); return;
                        }
                    } else {
                        chapterManager.addKrocoKill();
                        int[] res=questTracker.updateProgress("kill_kroco",1);
                        if(res[0]==1) player.gainExp(res[1]);
                    }
                }
            }

            // Check player dead
            if(player.getStats().isDead()){ showGameOver(); return; }

            pendingEnemyIndex=-1;
            BufferedImage black=new BufferedImage(SCREEN_WIDTH,SCREEN_HEIGHT,BufferedImage.TYPE_INT_ARGB);
            transition.start(black,null);
            gameState=GameState.EXPLORATION;
            AudioManager.get().playBgm(AudioManager.BGM_EXPLORE,true,true);
        });
    }

    private void advanceChapter(){
        int next=chapterManager.getCurrentChapter()+1;
        chapterManager.advanceChapter();
        setupWorldForChapter(next);
        AudioManager.get().fadeOutBgm(600,()->
            AudioManager.get().playBgm(AudioManager.BGM_EXPLORE,true,true));
        pendingEnemyIndex=-1;
        showChapterIntro(next);
    }

    private void showWin(){
        gameState=GameState.WIN;
        AudioManager.get().stopBgmImmediate();
        winOverlay=new WinScreen(SCREEN_WIDTH,SCREEN_HEIGHT,chapterManager.getElapsedString(),()->{
            SwingUtilities.invokeLater(()->{
                if(winOverlay!=null){winOverlay.stopAnimation();removeOverlay(winOverlay);winOverlay=null;}
                showMainMenu();
            });
        });
        winOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        add(winOverlay); setComponentZOrder(winOverlay,0); repaint();
    }

    private void showGameOver(){
        gameState=GameState.GAME_OVER;
        AudioManager.get().stopBgmImmediate();
        int chapter=chapterManager.getCurrentChapter();
        gameOverOverlay=new GameOverScreen(SCREEN_WIDTH,SCREEN_HEIGHT,chapter,
            ()->{// Restart from Ch.1
                SwingUtilities.invokeLater(()->{
                    if(gameOverOverlay!=null){gameOverOverlay.stopAnimation();removeOverlay(gameOverOverlay);gameOverOverlay=null;}
                    // Reset but keep party/hero selection
                    chapterManager.reset(); chapterManager.startTimer();
                    if(player!=null&&player.party!=null){
                        setupWorldForChapter(1);
                        AudioManager.get().playBgm(AudioManager.BGM_EXPLORE,true,true);
                        showChapterIntro(1);
                    } else { showMainMenu(); }
                });
            },
            ()->{// Back to menu
                SwingUtilities.invokeLater(()->{
                    if(gameOverOverlay!=null){gameOverOverlay.stopAnimation();removeOverlay(gameOverOverlay);gameOverOverlay=null;}
                    showMainMenu();
                });
            });
        gameOverOverlay.setBounds(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        add(gameOverOverlay); setComponentZOrder(gameOverOverlay,0); repaint();
    }

    // ── Game Loop ────────────────────────────────────────────

    public void startGameThread() {
        gameThread=new Thread(this,"GameLoop");
        gameThread.start();
    }

    @Override public void run() {
        final double nsPerFrame=(double)NS_PER_SEC/TARGET_FPS;
        double delta=0; long lastTime=System.nanoTime(); fpsTimer=lastTime;
        while(gameThread!=null){
            long now=System.nanoTime();
            delta+=(now-lastTime)/nsPerFrame; lastTime=now;
            if(delta>=1){ try{update();}catch(Exception e){e.printStackTrace();} delta--; }
            repaint();
            fpsCounter++;
            if(System.nanoTime()-fpsTimer>=NS_PER_SEC){currentFPS=fpsCounter;fpsCounter=0;fpsTimer=System.nanoTime();}
            long rem=(long)nsPerFrame-(System.nanoTime()-now);
            if(rem>0) try{Thread.sleep(rem/1_000_000,(int)(rem%1_000_000));}catch(InterruptedException ex){Thread.currentThread().interrupt();}
        }
    }

    private void update(){
        transition.update();
        if(gameState==GameState.EXPLORATION||gameState==GameState.DIALOG){
            if(player!=null) player.update();
            for(Entity e:new ArrayList<>(entities)) e.update();
            if(player!=null) camera.update(player);
            if(keyHandler.questJustPressed){questTracker.cycleActive();keyHandler.questJustPressed=false;}

            // BUG FIX: Dialog dismissal — ESC or Z closes dialog and returns to EXPLORATION
            if(gameState==GameState.DIALOG){
                if(keyHandler.escJustPressed){
                    gameState=GameState.EXPLORATION;
                    keyHandler.escJustPressed=false;
                    if(player!=null&&player.nearbyEntity instanceof NPC npc) npc.resetDialog();
                }
                if(keyHandler.interactJustPressed){
                    if(player!=null&&player.nearbyEntity instanceof NPC npc){
                        npc.advanceDialog();
                        // If cycled through all dialog lines, close dialog
                        if(npc.isDialogDone()) gameState=GameState.EXPLORATION;
                    }
                    keyHandler.interactJustPressed=false;
                }
            }
        }
    }

    // ── Paint ────────────────────────────────────────────────

    @Override protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
        if(gameState==GameState.MAIN_MENU||gameState==GameState.SETTINGS||
           gameState==GameState.HERO_SELECT||gameState==GameState.PROLOG||
           gameState==GameState.WIN||gameState==GameState.GAME_OVER||
           gameState==GameState.CHAPTER_INTRO){
            g2.setColor(Color.BLACK); g2.fillRect(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
            g2.dispose(); return;
        }
        if(gameState!=GameState.COMBAT){
            paintGame(g2);
            if(transition.isActive()) transition.draw(g2,SCREEN_WIDTH,SCREEN_HEIGHT);
        }
        g2.dispose();
    }

    private void paintGame(Graphics2D g2){
        if(player==null) return;
        g2.setColor(new Color(20,18,30)); g2.fillRect(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);
        tileManager.draw(g2);
        List<Entity> renderList=new ArrayList<>(entities);
        renderList.add(player);
        renderList.sort(Comparator.comparingInt(e->e.worldY+e.solidArea.y+e.solidArea.height));
        for(Entity e:renderList){
            if(e instanceof EnemyEntity ee&&!ee.isAlive) continue;
            e.draw(g2);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        drawHUD(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawHUD(Graphics2D g2){
        if(hudRenderer==null||player==null) return;
        hudRenderer.draw(g2,player);
        g2.setFont(new Font("Monospaced",Font.BOLD,11));
        g2.setColor(new Color(140,140,140,120));
        g2.drawString("FPS:"+currentFPS,10,SCREEN_HEIGHT-24);
        g2.setFont(new Font("Monospaced",Font.PLAIN,10));
        g2.setColor(new Color(120,115,140,120));
        g2.drawString("WASD:Gerak  X:Serang  Z:Bicara  Q:Quest  ESC:Tutup Dialog",10,SCREEN_HEIGHT-10);
        if(gameState==GameState.DIALOG) drawDialogBox(g2);
        if(player.nearbyEntity instanceof NPC&&gameState==GameState.EXPLORATION){
            String hint="[Z / Enter] Bicara";
            g2.setFont(new Font("Monospaced",Font.BOLD,14));
            FontMetrics fm=g2.getFontMetrics();
            int tw=fm.stringWidth(hint);
            g2.setColor(new Color(0,0,0,160));
            g2.fillRoundRect((SCREEN_WIDTH-tw)/2-10,SCREEN_HEIGHT-55,tw+20,28,8,8);
            g2.setColor(Color.WHITE);
            g2.drawString(hint,(SCREEN_WIDTH-tw)/2,SCREEN_HEIGHT-37);
        }
        // Attack range hint for nearby enemies
        if(player.nearbyEntity instanceof EnemyEntity ee&&ee.isAlive&&gameState==GameState.EXPLORATION){
            String hint="[X] Serang " + (ee.isBoss?"BOSS":"Kroco");
            g2.setFont(new Font("Monospaced",Font.BOLD,14));
            FontMetrics fm=g2.getFontMetrics();
            int tw=fm.stringWidth(hint);
            g2.setColor(new Color(0,0,0,160));
            g2.fillRoundRect((SCREEN_WIDTH-tw)/2-10,SCREEN_HEIGHT-55,tw+20,28,8,8);
            g2.setColor(new Color(255,150,80));
            g2.drawString(hint,(SCREEN_WIDTH-tw)/2,SCREEN_HEIGHT-37);
        }
    }

    private void drawDialogBox(Graphics2D g2){
        int bx=40,by=SCREEN_HEIGHT-180,bw=SCREEN_WIDTH-80,bh=160;
        g2.setColor(new Color(5,3,18,230)); g2.fillRoundRect(bx,by,bw,bh,12,12);
        g2.setColor(new Color(120,100,200)); g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(bx,by,bw,bh,12,12); g2.setStroke(new BasicStroke(1f));
        if(player.nearbyEntity instanceof NPC npc){
            g2.setFont(new Font("Monospaced",Font.PLAIN,17));
            g2.setColor(Color.WHITE);
            // Wrap long text
            String text=npc.getCurrentDialog();
            g2.drawString(text,bx+20,by+50);
            g2.setFont(new Font("Monospaced",Font.BOLD,11));
            g2.setColor(new Color(160,150,200));
            g2.drawString("[Z] Lanjut  |  [ESC] Tutup",bx+20,by+bh-18);
        }
    }

    // ── Accessors ────────────────────────────────────────────
    public QuestTracker   getQuestTracker()   { return questTracker; }
    public HudRenderer    getHudRenderer()    { return hudRenderer; }
    public ChapterManager getChapterManager() { return chapterManager; }
    public void           setGameState(GameState s){ this.gameState=s; }
}
