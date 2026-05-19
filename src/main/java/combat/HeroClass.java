package combat;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HeroClass.java
 * ============================================================
 * Data model untuk setiap Class karakter yang bisa dipilih.
 * Menyimpan: nama, stat dasar, warna tema, deskripsi, dan deck kartu.
 *
 * Untuk menambah class baru: cukup tambah satu entry di enum ClassType
 * dan implementasikan buildDeck() untuk class tersebut.
 * ============================================================
 */
public class HeroClass {

    // =========================================================
    // ENUM: Lima pilihan class karakter
    // =========================================================
    public enum ClassType {
        WARRIOR  ("Warrior",   "⚔",  100, 60,  8,  new Color(180, 80,  60)),
        TANKER   ("Tanker",    "🛡",  140, 40,  5,  new Color(100, 140, 200)),
        ARCHER   ("Archer",    "🏹",  85,  80,  9,  new Color(100, 180, 80)),
        MAGE     ("Mage",      "🔮",  70,  130, 7,  new Color(160, 80,  220)),
        ASSASSIN ("Assassin",  "🗡",  80,  70,  11, new Color(60,  60,  80));

        public final String displayName;
        public final String emoji;
        public final int baseHp;
        public final int baseMp;
        public final int baseSpeed;  // Dipakai untuk kartu cepat Assassin
        public final Color themeColor;

        ClassType(String displayName, String emoji, int baseHp, int baseMp,
                  int baseSpeed, Color themeColor) {
            this.displayName = displayName;
            this.emoji       = emoji;
            this.baseHp      = baseHp;
            this.baseMp      = baseMp;
            this.baseSpeed   = baseSpeed;
            this.themeColor  = themeColor;
        }
    }

    // =========================================================
    // INNER CLASS: Info lengkap karakter yang dipilih
    // =========================================================
    public static class CharacterProfile {
        public final ClassType classType;
        public final String name;
        public final int maxHp;
        public final int maxMp;
        public final List<Card> starterDeck;
        public final String lore;         // Deskripsi singkat karakter

        public CharacterProfile(ClassType classType) {
            this.classType   = classType;
            this.name        = classType.displayName;
            this.maxHp       = classType.baseHp;
            this.maxMp       = classType.baseMp;
            this.starterDeck = buildDeck(classType);
            this.lore        = buildLore(classType);
        }
    }

    // =========================================================
    // DECK BUILDER — Setiap class punya kartu unik
    // =========================================================
    public static List<Card> buildDeck(ClassType type) {
        List<Card> deck = new ArrayList<>();
        switch (type) {

            // --- WARRIOR: Seimbang antara attack dan defense ---
            case WARRIOR:
                deck.add(Card.createAttack ("Sword Slash",    22, 0,  "Tebasan pedang standar."));
                deck.add(Card.createAttack ("Cleave",         30, 0,  "Tebasan lebar mengenai musuh."));
                deck.add(Card.createAttack ("Sword Slash",    22, 0,  "Tebasan pedang standar."));
                deck.add(Card.createDefense("Battle Stance",  18, 10, "Mengambil posisi bertahan."));
                deck.add(Card.createHeal   ("War Cry",        20, 15, "Teriakan memompa adrenalin, pulihkan HP."));
                deck.add(Card.createAttack ("Shield Bash",    15, 0,  "Pukul dengan tameng, stun ringan."));
                deck.add(Card.createUltimate("Bladestorm",    80, 40, "Berputar dan melukai semua sudut!"));
                break;

            // --- TANKER: Defense sangat tinggi, damage rendah ---
            case TANKER:
                deck.add(Card.createAttack ("Heavy Punch",   18, 0,  "Pukulan telak dengan kepalan besi."));
                deck.add(Card.createDefense("Iron Fortress", 30, 10, "Pertahanan penuh, shield besar."));
                deck.add(Card.createDefense("Iron Fortress", 30, 10, "Pertahanan penuh, shield besar."));
                deck.add(Card.createDefense("Taunt",         20, 15, "Memancing serangan musuh, naikkan shield."));
                deck.add(Card.createHeal   ("Endurance",     35, 20, "Ketahanan tubuh, pulihkan HP besar."));
                deck.add(Card.createAttack ("Ground Slam",   25, 0,  "Hantam tanah, getarkan musuh."));
                deck.add(Card.createUltimate("Unbreakable",  50, 35, "Jadi tak terkalahkan + pulihkan 30 HP!"));
                break;

            // --- ARCHER: Serangan cepat, combo kartu ---
            case ARCHER:
                deck.add(Card.createAttack ("Quick Shot",    18, 0,  "Tembakan cepat dari busur."));
                deck.add(Card.createAttack ("Quick Shot",    18, 0,  "Tembakan cepat dari busur."));
                deck.add(Card.createAttack ("Piercing Arrow",28, 0,  "Panah menembus armor."));
                deck.add(Card.createAttack ("Double Shot",   20, 10, "Dua panah sekaligus, damage x2."));
                deck.add(Card.createHeal   ("Camouflage",    15, 15, "Bersembunyi, pulihkan sedikit HP."));
                deck.add(Card.createDefense("Evasion",       12, 10, "Menghindar, naikkan sedikit shield."));
                deck.add(Card.createUltimate("Rain of Arrows",70, 35,"Hujan panah menghujam musuh!"));
                break;

            // --- MAGE: Damage sangat tinggi, MP intensif ---
            case MAGE:
                deck.add(Card.createAttack ("Magic Bolt",    25, 10, "Proyektil sihir dasar."));
                deck.add(Card.createAttack ("Fireball",      40, 20, "Bola api ledakan besar."));
                deck.add(Card.createAttack ("Ice Shard",     30, 15, "Paku es yang menusuk."));
                deck.add(Card.createAttack ("Magic Bolt",    25, 10, "Proyektil sihir dasar."));
                deck.add(Card.createHeal   ("Arcane Surge",  20, 5,  "Serap energi arcane, pulihkan HP."));
                deck.add(Card.createDefense("Mana Shield",   15, 15, "Konversi mana jadi perisai."));
                deck.add(Card.createUltimate("Meteor Strike",100,50, "Jatuhkan meteor dari langit!"));
                break;

            // --- ASSASSIN: Serangan sangat cepat, burst damage ---
            case ASSASSIN:
                deck.add(Card.createAttack ("Backstab",      35, 0,  "Tikam dari belakang, critical!"));
                deck.add(Card.createAttack ("Shadow Strike", 28, 10, "Serangan dari bayangan."));
                deck.add(Card.createAttack ("Backstab",      35, 0,  "Tikam dari belakang, critical!"));
                deck.add(Card.createDefense("Smoke Bomb",    10, 10, "Lempar bom asap, hindari serangan."));
                deck.add(Card.createHeal   ("Blood Drain",   15, 10, "Serap darah musuh, pulihkan HP."));
                deck.add(Card.createAttack ("Poison Blade",  20, 0,  "Racun merembes di bilah pisau."));
                deck.add(Card.createUltimate("Death Mark",   90, 40, "Tandai musuh untuk eksekusi instan!"));
                break;
        }
        return deck;
    }

    // =========================================================
    // LORE BUILDER — Deskripsi flavor text tiap class
    // =========================================================
    private static String buildLore(ClassType type) {
        switch (type) {
            case WARRIOR:  return "Pejuang sejati yang menguasai pedang dan tameng.\nSeimbang dalam serangan maupun pertahanan.";
            case TANKER:   return "Benteng berjalan yang tak bisa ditembus.\nHP besar, shield luar biasa, lambat namun tak tergoyahkan.";
            case ARCHER:   return "Penembak jitu dari kejauhan.\nSerangan cepat dan combo panah yang mematikan.";
            case MAGE:     return "Pengguna sihir paling kuat.\nDamage tertinggi, tapi HP rendah. Habiskan mana dengan bijak!";
            case ASSASSIN: return "Pembunuh bayangan yang bergerak di kegelapan.\nBurst damage ekstrem, sangat berbahaya dari jarak dekat.";
            default:       return "";
        }
    }

    // =========================================================
    // PIXEL ART RENDERER — Menggambar sprite karakter dengan Graphics
    // =========================================================

    /**
     * Menggambar "pixel art" placeholder untuk Hero menggunakan Graphics2D.
     * CARA GANTI KE GAMBAR PNG ASLI:
     *   1. Hapus seluruh isi method ini.
     *   2. Tambahkan: Image img = ImageIO.read(new File("assets/" + type.name().toLowerCase() + ".png"));
     *   3. Lalu: g2d.drawImage(img, x, y, width, height, null);
     *
     * @param g2d    Graphics2D dari JPanel
     * @param type   Class karakter yang digambar
     * @param x      Koordinat X kiri atas
     * @param y      Koordinat Y kiri atas
     * @param width  Lebar area gambar
     * @param height Tinggi area gambar
     */
    public static void drawHeroSprite(Graphics2D g2d, ClassType type, int x, int y, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        Color c = type.themeColor;

        // Skala pixel berdasarkan area yang tersedia
        int px = width / 10; // Ukuran satu "pixel"
        int ox = x + (width  - px * 8) / 2; // Offset tengah X
        int oy = y + (height - px * 12) / 2; // Offset tengah Y

        // Warna variasi
        Color light   = c.brighter().brighter();
        Color dark    = c.darker().darker();
        Color skin    = new Color(240, 200, 160);
        Color skinDark= new Color(200, 160, 120);

        switch (type) {
            case WARRIOR:   drawWarriorPixel(g2d, ox, oy, px, c, light, dark, skin, skinDark); break;
            case TANKER:    drawTankerPixel (g2d, ox, oy, px, c, light, dark, skin, skinDark); break;
            case ARCHER:    drawArcherPixel (g2d, ox, oy, px, c, light, dark, skin, skinDark); break;
            case MAGE:      drawMagePixel   (g2d, ox, oy, px, c, light, dark, skin, skinDark); break;
            case ASSASSIN:  drawAssassinPixel(g2d,ox, oy, px, c, light, dark, skin, skinDark); break;
        }
    }

    /**
     * Menggambar sprite musuh (Orc/Dragon/Goblin).
     * Sama seperti drawHeroSprite — bisa diganti dengan PNG asli.
     */
    public static void drawEnemySprite(Graphics2D g2d, String enemyName, int x, int y, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int px = width / 10;
        int ox = x + (width  - px * 8) / 2;
        int oy = y + (height - px * 12) / 2;

        if (enemyName.contains("Dragon")) {
            drawDragonPixel(g2d, ox, oy, px);
        } else if (enemyName.contains("Orc")) {
            drawOrcPixel(g2d, ox, oy, px);
        } else {
            drawGoblinPixel(g2d, ox, oy, px);
        }
    }

    // ─── Private pixel-art drawing methods ────────────────────────────────────

    private static void fillPx(Graphics2D g, int ox, int oy, int px, int col, int row, Color c) {
        g.setColor(c);
        g.fillRect(ox + col * px, oy + row * px, px, px);
    }

    // WARRIOR — Plate armor merah, pedang
    private static void drawWarriorPixel(Graphics2D g, int ox, int oy, int px,
                                          Color c, Color light, Color dark, Color skin, Color skinDark) {
        // Helm
        fillPx(g,ox,oy,px,2,0,dark);   fillPx(g,ox,oy,px,3,0,c);
        fillPx(g,ox,oy,px,4,0,c);      fillPx(g,ox,oy,px,5,0,dark);
        fillPx(g,ox,oy,px,2,1,c);      fillPx(g,ox,oy,px,3,1,light);
        fillPx(g,ox,oy,px,4,1,light);  fillPx(g,ox,oy,px,5,1,c);
        // Wajah
        fillPx(g,ox,oy,px,3,2,skin);   fillPx(g,ox,oy,px,4,2,skin);
        fillPx(g,ox,oy,px,3,3,skinDark);fillPx(g,ox,oy,px,4,3,skin);
        // Badan (armor)
        for(int r=4;r<=7;r++) for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,r,c);
        fillPx(g,ox,oy,px,3,4,light);  fillPx(g,ox,oy,px,4,4,light);
        fillPx(g,ox,oy,px,3,5,dark);   fillPx(g,ox,oy,px,4,5,dark);
        // Kaki
        fillPx(g,ox,oy,px,2,8,dark);   fillPx(g,ox,oy,px,3,8,dark);
        fillPx(g,ox,oy,px,4,8,dark);   fillPx(g,ox,oy,px,5,8,dark);
        fillPx(g,ox,oy,px,2,9,c);      fillPx(g,ox,oy,px,3,9,c);
        fillPx(g,ox,oy,px,4,9,c);      fillPx(g,ox,oy,px,5,9,c);
        // Pedang (kanan)
        fillPx(g,ox,oy,px,6,3,new Color(200,200,220));
        fillPx(g,ox,oy,px,6,4,new Color(200,200,220));
        fillPx(g,ox,oy,px,6,5,new Color(200,200,220));
        fillPx(g,ox,oy,px,6,6,new Color(160,120,60));
        // Tameng (kiri)
        fillPx(g,ox,oy,px,1,4,new Color(180,160,60));
        fillPx(g,ox,oy,px,1,5,new Color(180,160,60));
        fillPx(g,ox,oy,px,1,6,new Color(180,160,60));
    }

    // TANKER — Armor biru besar, tameng raksasa
    private static void drawTankerPixel(Graphics2D g, int ox, int oy, int px,
                                         Color c, Color light, Color dark, Color skin, Color skinDark) {
        Color shield = new Color(180,160,60);
        // Helm besar
        for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,0,dark);
        for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,1,c);
        fillPx(g,ox,oy,px,2,1,light); fillPx(g,ox,oy,px,5,1,light);
        // Wajah kecil
        fillPx(g,ox,oy,px,3,2,skin);  fillPx(g,ox,oy,px,4,2,skin);
        // Badan lebar (armor tebal)
        for(int r=3;r<=8;r++) for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,r,c);
        fillPx(g,ox,oy,px,3,4,light); fillPx(g,ox,oy,px,4,4,light);
        fillPx(g,ox,oy,px,2,5,dark);  fillPx(g,ox,oy,px,5,5,dark);
        // Kaki
        for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,9,dark);
        // Tameng besar (kiri)
        for(int r=2;r<=8;r++) fillPx(g,ox,oy,px,0,r,shield);
        fillPx(g,ox,oy,px,0,4,new Color(200,180,80));
        fillPx(g,ox,oy,px,0,5,new Color(200,180,80));
    }

    // ARCHER — Pakaian hijau ramping, busur
    private static void drawArcherPixel(Graphics2D g, int ox, int oy, int px,
                                         Color c, Color light, Color dark, Color skin, Color skinDark) {
        Color bow = new Color(139, 90, 43);
        Color arrow = new Color(200,200,180);
        // Topi
        fillPx(g,ox,oy,px,3,0,dark);  fillPx(g,ox,oy,px,4,0,dark);
        fillPx(g,ox,oy,px,2,1,c);     fillPx(g,ox,oy,px,3,1,c);
        fillPx(g,ox,oy,px,4,1,c);     fillPx(g,ox,oy,px,5,1,c);
        // Wajah
        fillPx(g,ox,oy,px,3,2,skin);  fillPx(g,ox,oy,px,4,2,skin);
        fillPx(g,ox,oy,px,3,3,skinDark);
        // Badan ramping
        for(int r=4;r<=7;r++) for(int col=3;col<=4;col++) fillPx(g,ox,oy,px,col,r,c);
        fillPx(g,ox,oy,px,2,4,c); fillPx(g,ox,oy,px,5,4,c);
        fillPx(g,ox,oy,px,2,5,c); fillPx(g,ox,oy,px,5,5,c);
        fillPx(g,ox,oy,px,3,4,light); fillPx(g,ox,oy,px,4,5,light);
        // Kaki
        fillPx(g,ox,oy,px,3,8,dark);  fillPx(g,ox,oy,px,4,8,dark);
        fillPx(g,ox,oy,px,3,9,c);     fillPx(g,ox,oy,px,4,9,c);
        // Busur (kiri)
        fillPx(g,ox,oy,px,1,2,bow); fillPx(g,ox,oy,px,1,3,bow);
        fillPx(g,ox,oy,px,1,4,bow); fillPx(g,ox,oy,px,1,5,bow);
        fillPx(g,ox,oy,px,1,6,bow);
        // Anak panah
        fillPx(g,ox,oy,px,2,4,arrow); fillPx(g,ox,oy,px,3,4,arrow);
        fillPx(g,ox,oy,px,4,4,arrow); fillPx(g,ox,oy,px,5,4,arrow);
        fillPx(g,ox,oy,px,6,4,new Color(200,50,50)); // Ujung panah
    }

    // MAGE — Jubah ungu, tongkat sihir
    private static void drawMagePixel(Graphics2D g, int ox, int oy, int px,
                                       Color c, Color light, Color dark, Color skin, Color skinDark) {
        Color staff = new Color(120, 80, 40);
        Color orb   = new Color(150, 80, 255);
        Color orbGlow = new Color(200,150,255);
        // Topi penyihir
        fillPx(g,ox,oy,px,3,0,dark);
        fillPx(g,ox,oy,px,3,1,c);    fillPx(g,ox,oy,px,4,1,c);
        fillPx(g,ox,oy,px,2,2,dark); fillPx(g,ox,oy,px,3,2,dark);
        fillPx(g,ox,oy,px,4,2,dark); fillPx(g,ox,oy,px,5,2,dark);
        // Wajah
        fillPx(g,ox,oy,px,3,3,skin); fillPx(g,ox,oy,px,4,3,skin);
        fillPx(g,ox,oy,px,4,4,skinDark);
        // Jubah
        fillPx(g,ox,oy,px,3,4,c);   fillPx(g,ox,oy,px,4,4,c);
        fillPx(g,ox,oy,px,2,5,c);   fillPx(g,ox,oy,px,3,5,light);
        fillPx(g,ox,oy,px,4,5,c);   fillPx(g,ox,oy,px,5,5,c);
        for(int col=2;col<=5;col++) { fillPx(g,ox,oy,px,col,6,c); fillPx(g,ox,oy,px,col,7,c); }
        fillPx(g,ox,oy,px,1,6,c); fillPx(g,ox,oy,px,6,6,c);
        fillPx(g,ox,oy,px,1,7,dark); fillPx(g,ox,oy,px,6,7,dark);
        // Kaki
        fillPx(g,ox,oy,px,3,8,dark); fillPx(g,ox,oy,px,4,8,dark);
        // Tongkat (kanan)
        fillPx(g,ox,oy,px,6,2,orbGlow); fillPx(g,ox,oy,px,7,2,orbGlow);
        fillPx(g,ox,oy,px,6,3,orb);
        for(int r=4;r<=8;r++) fillPx(g,ox,oy,px,6,r,staff);
    }

    // ASSASSIN — Hitam, ramping, dagger
    private static void drawAssassinPixel(Graphics2D g, int ox, int oy, int px,
                                           Color c, Color light, Color dark, Color skin, Color skinDark) {
        Color blade = new Color(180,180,200);
        // Tudung hitam
        fillPx(g,ox,oy,px,3,0,dark); fillPx(g,ox,oy,px,4,0,dark);
        fillPx(g,ox,oy,px,2,1,dark); fillPx(g,ox,oy,px,3,1,c);
        fillPx(g,ox,oy,px,4,1,c);    fillPx(g,ox,oy,px,5,1,dark);
        // Wajah tersembunyi
        fillPx(g,ox,oy,px,3,2,skin); fillPx(g,ox,oy,px,4,2,dark);
        fillPx(g,ox,oy,px,3,3,dark);
        // Badan ramping gelap
        for(int r=4;r<=7;r++) for(int col=3;col<=4;col++) fillPx(g,ox,oy,px,col,r,c);
        fillPx(g,ox,oy,px,2,4,c);    fillPx(g,ox,oy,px,5,4,c);
        fillPx(g,ox,oy,px,2,5,dark); fillPx(g,ox,oy,px,5,5,dark);
        fillPx(g,ox,oy,px,3,5,light);
        // Kaki
        fillPx(g,ox,oy,px,3,8,dark); fillPx(g,ox,oy,px,4,8,c);
        fillPx(g,ox,oy,px,3,9,c);    fillPx(g,ox,oy,px,4,9,dark);
        // Dagger kanan
        fillPx(g,ox,oy,px,6,3,blade); fillPx(g,ox,oy,px,6,4,blade);
        fillPx(g,ox,oy,px,6,5,new Color(120,80,40));
        // Dagger kiri
        fillPx(g,ox,oy,px,1,4,blade); fillPx(g,ox,oy,px,1,5,blade);
        fillPx(g,ox,oy,px,1,6,new Color(120,80,40));
    }

    // GOBLIN — Hijau kecil
    private static void drawGoblinPixel(Graphics2D g, int ox, int oy, int px) {
        Color body   = new Color(80, 160, 80);
        Color dark   = new Color(40, 100, 40);
        Color eyes   = new Color(255, 60, 60);
        Color weapon = new Color(160, 120, 60);
        // Kepala
        fillPx(g,ox,oy,px,3,1,body);  fillPx(g,ox,oy,px,4,1,body);
        fillPx(g,ox,oy,px,2,2,body);  fillPx(g,ox,oy,px,3,2,body);
        fillPx(g,ox,oy,px,4,2,body);  fillPx(g,ox,oy,px,5,2,body);
        fillPx(g,ox,oy,px,3,3,eyes);  fillPx(g,ox,oy,px,5,3,eyes);
        fillPx(g,ox,oy,px,4,3,body);
        fillPx(g,ox,oy,px,3,4,dark);  fillPx(g,ox,oy,px,4,4,dark); // Mulut
        // Telinga
        fillPx(g,ox,oy,px,1,2,body);  fillPx(g,ox,oy,px,6,2,body);
        // Badan
        for(int r=5;r<=7;r++) for(int col=3;col<=4;col++) fillPx(g,ox,oy,px,col,r,body);
        fillPx(g,ox,oy,px,2,5,body); fillPx(g,ox,oy,px,5,5,body);
        // Kaki
        fillPx(g,ox,oy,px,2,8,dark); fillPx(g,ox,oy,px,5,8,dark);
        fillPx(g,ox,oy,px,2,9,body); fillPx(g,ox,oy,px,5,9,body);
        // Senjata
        fillPx(g,ox,oy,px,6,4,weapon); fillPx(g,ox,oy,px,6,5,weapon); fillPx(g,ox,oy,px,6,6,weapon);
    }

    // ORC — Abu-abu besar
    private static void drawOrcPixel(Graphics2D g, int ox, int oy, int px) {
        Color body   = new Color(120, 140, 100);
        Color dark   = new Color(70, 90, 60);
        Color eyes   = new Color(255, 80, 0);
        Color armor  = new Color(80, 80, 100);
        Color axe    = new Color(160, 160, 180);
        // Kepala besar
        for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,0,dark);
        for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,1,body);
        fillPx(g,ox,oy,px,2,2,eyes); fillPx(g,ox,oy,px,5,2,eyes);
        for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,2,body);
        fillPx(g,ox,oy,px,2,2,eyes); fillPx(g,ox,oy,px,5,2,eyes);
        fillPx(g,ox,oy,px,3,3,dark); fillPx(g,ox,oy,px,4,3,dark); // Tusk
        fillPx(g,ox,oy,px,2,3,body); fillPx(g,ox,oy,px,5,3,body);
        // Badan
        for(int r=4;r<=7;r++) for(int col=1;col<=6;col++) fillPx(g,ox,oy,px,col,r,armor);
        fillPx(g,ox,oy,px,3,5,body); fillPx(g,ox,oy,px,4,5,body);
        // Kaki
        for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,8,dark);
        for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,9,armor);
        // Kapak besar
        fillPx(g,ox,oy,px,7,0,axe); fillPx(g,ox,oy,px,7,1,axe);
        fillPx(g,ox,oy,px,7,2,axe); fillPx(g,ox,oy,px,7,3,axe);
        fillPx(g,ox,oy,px,7,4,new Color(120,80,40));
        fillPx(g,ox,oy,px,7,5,new Color(120,80,40));
    }

    // DRAGON — Merah besar
    private static void drawDragonPixel(Graphics2D g, int ox, int oy, int px) {
        Color body  = new Color(180, 40, 40);
        Color dark  = new Color(100, 20, 20);
        Color fire  = new Color(255, 140, 0);
        Color wing  = new Color(140, 20, 20);
        Color eyes  = new Color(255, 240, 0);
        // Sayap kiri
        fillPx(g,ox,oy,px,0,1,wing); fillPx(g,ox,oy,px,0,2,wing); fillPx(g,ox,oy,px,0,3,wing);
        fillPx(g,ox,oy,px,1,2,wing);
        // Sayap kanan
        fillPx(g,ox,oy,px,7,1,wing); fillPx(g,ox,oy,px,7,2,wing); fillPx(g,ox,oy,px,7,3,wing);
        fillPx(g,ox,oy,px,6,2,wing);
        // Kepala
        fillPx(g,ox,oy,px,2,0,dark); fillPx(g,ox,oy,px,5,0,dark);
        for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,1,body);
        fillPx(g,ox,oy,px,2,2,eyes); fillPx(g,ox,oy,px,5,2,eyes);
        for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,2,body);
        fillPx(g,ox,oy,px,2,2,eyes); fillPx(g,ox,oy,px,5,2,eyes);
        // Moncong + api
        for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,3,dark);
        fillPx(g,ox,oy,px,2,4,fire); fillPx(g,ox,oy,px,3,4,fire);
        fillPx(g,ox,oy,px,4,4,fire); fillPx(g,ox,oy,px,5,4,fire);
        fillPx(g,ox,oy,px,1,5,fire); fillPx(g,ox,oy,px,6,5,fire);
        // Badan
        for(int r=5;r<=8;r++) for(int col=2;col<=5;col++) fillPx(g,ox,oy,px,col,r,body);
        fillPx(g,ox,oy,px,3,6,dark); fillPx(g,ox,oy,px,4,7,dark);
        // Ekor
        fillPx(g,ox,oy,px,5,9,body); fillPx(g,ox,oy,px,6,9,dark); fillPx(g,ox,oy,px,7,9,body);
        // Kaki
        fillPx(g,ox,oy,px,2,9,dark); fillPx(g,ox,oy,px,3,9,dark);
        fillPx(g,ox,oy,px,4,9,dark); fillPx(g,ox,oy,px,5,9,dark);
    }
}