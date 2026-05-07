import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/**
 * Hero.java
 * ============================================================
 * Entitas pemain dengan sistem HP, Mana (MP), Shield, dan Deck.
 * Dibuat dari CharacterProfile setelah pemain memilih class.
 * ============================================================
 */
public class Hero {
 
    private final String name;
    private final HeroClass.ClassType classType;
 
    // --- Stat Utama ---
    private int hp;
    private final int maxHp;
    private int mp;
    private final int maxMp;
    private int defenseShield;
 
    // --- Deck System ---
    private List<Card> deck;
    private List<Card> hand;
    private List<Card> discard;
    private static final int HAND_SIZE = 4;
 
    // =========================================================
    // CONSTRUCTOR — Dibuat dari CharacterProfile
    // =========================================================
    public Hero(HeroClass.CharacterProfile profile) {
        this.name      = profile.name;
        this.classType = profile.classType;
        this.maxHp     = profile.maxHp;
        this.hp        = profile.maxHp;
        this.maxMp     = profile.maxMp;
        this.mp        = profile.maxMp;
        this.defenseShield = 0;
 
        this.deck    = new ArrayList<>(profile.starterDeck);
        this.hand    = new ArrayList<>();
        this.discard = new ArrayList<>();
 
        Collections.shuffle(this.deck);
        drawInitialHand();
    }
 
    // =========================================================
    // DECK OPERATIONS
    // =========================================================
    private void drawInitialHand() {
        for (int i = 0; i < HAND_SIZE; i++) drawOneCard();
    }
 
    private void drawOneCard() {
        if (deck.isEmpty()) {
            if (discard.isEmpty()) return;
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck);
        }
        if (!deck.isEmpty()) hand.add(deck.remove(0));
    }
 
    /** Mainkan kartu: keluarkan dari hand, masuk discard, tarik kartu baru */
    public void playCard(Card card) {
        hand.remove(card);
        discard.add(card);
        // Kurangi mana
        mp = Math.max(0, mp - card.getManaCost());
        drawOneCard();
    }
 
    /** Cek apakah kartu bisa dimainkan (mana cukup) */
    public boolean canPlayCard(Card card) {
        return mp >= card.getManaCost();
    }
 
    // =========================================================
    // COMBAT OPERATIONS
    // =========================================================
    /** Terima damage; shield menyerap terlebih dahulu */
    public int takeDamage(int damage) {
        int absorbed   = Math.min(defenseShield, damage);
        defenseShield  = Math.max(0, defenseShield - damage);
        int actualDmg  = Math.max(0, damage - absorbed);
        hp             = Math.max(0, hp - actualDmg);
        return actualDmg;
    }
 
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }
 
    public void addShield(int amount) {
        defenseShield += amount;
    }
 
    /** Pulihkan MP setiap akhir giliran musuh */
    public void regenMana(int amount) {
        mp = Math.min(maxMp, mp + amount);
    }
 
    // =========================================================
    // GETTERS
    // =========================================================
    public String                getName()         { return name; }
    public HeroClass.ClassType   getClassType()    { return classType; }
    public int                   getHp()           { return hp; }
    public int                   getMaxHp()        { return maxHp; }
    public int                   getMp()           { return mp; }
    public int                   getMaxMp()        { return maxMp; }
    public int                   getDefenseShield(){ return defenseShield; }
    public List<Card>            getHand()         { return new ArrayList<>(hand); }
    public boolean               isAlive()         { return hp > 0; }
 
    public String getStatusText() {
        return name + " | HP:" + hp + "/" + maxHp + " MP:" + mp + "/" + maxMp
               + (defenseShield > 0 ? " | Shield:" + defenseShield : "");
    }
}