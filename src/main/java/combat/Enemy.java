package combat;
import java.util.Random;

/**
 * Enemy.java — Entitas musuh, sama seperti versi sebelumnya
 * tapi dengan lebih banyak preset dan pola serangan.
 */
public class Enemy {

    private final String name;
    private int hp;
    private final int maxHp;
    private final int baseAttack;
    private final Random rng = new Random();
    private int turn = 0;

    public Enemy(String name, int maxHp, int baseAttack) {
        this.name       = name;
        this.maxHp      = maxHp;
        this.hp         = maxHp;
        this.baseAttack = baseAttack;
    }

    // =========================================================
    // PRESET ENEMIES
    // =========================================================
    public static Enemy createGoblin() { return new Enemy("Goblin Penjelajah", 75,  12); }
    public static Enemy createOrc()    { return new Enemy("Orc Barbar",        120, 18); }
    public static Enemy createDragon() { return new Enemy("Naga Kuno",         180, 26); }

    /** Create Kroco (regular enemy) for a given chapter. */
    public static Enemy createKroco(int chapter) {
        int hp  = 230 + chapter * 20;
        int atk = 8  + chapter * 4;
        String[] names = {"Goblin","Orc","Undead","Vampire Kroco","Chimera Kroco",
                          "Minotaur Kroco","Skeleton","Lizardman","Ogre Kroco","Iblis Kroco"};
        String name = names[Math.min(chapter-1, names.length-1)] + " Lv." + chapter;
        return new Enemy(name, hp, atk);
    }

    /** Create Demon Commander — uses proper boss name from ChapterManager */
    public static Enemy createDemonCommander(int chapter) {
        int hp  = 400 + chapter * 40;
        int atk = 12 + chapter * 6;
        int idx = Math.max(0, Math.min(chapter-1, main.ChapterManager.COMMANDER_NAMES.length-1));
        String name = main.ChapterManager.COMMANDER_NAMES[idx];
        return new Enemy(name, hp, atk);
    }

    /** Create Demon King (Chapter 10 final boss). */
    public static Enemy createDemonKing(int partySize) {
        int hp  = 40000 + partySize * 50;
        int atk = 55;
        return new Enemy("DEMON KING - Iblis Raja Sejati", hp, atk);
    }

    // =========================================================
    // COMBAT
    // =========================================================
    public AttackResult attack() {
        turn++;
        int roll = rng.nextInt(100);
        if (turn % 3 == 0) {
            int dmg = (int)(baseAttack * 1.9);
            return new AttackResult(dmg, "⚡ " + name + " melancarkan SERANGAN KERAS: " + dmg + " damage!");
        } else if (roll < 20) {
            int dmg = Math.max(1, (int)(baseAttack * 0.55));
            return new AttackResult(dmg, name + " menyerang lemah: " + dmg + " damage.");
        } else {
            int variance = rng.nextInt(7) - 3;
            int dmg = Math.max(1, baseAttack + variance);
            return new AttackResult(dmg, name + " menyerang: " + dmg + " damage.");
        }
    }

    public void takeDamage(int dmg) { hp = Math.max(0, hp - dmg); }

    // =========================================================
    // GETTERS
    // =========================================================
    public String  getName()  { return name; }
    public int     getHp()    { return hp; }
    public int     getMaxHp() { return maxHp; }
    public boolean isAlive()  { return hp > 0; }

    public static class AttackResult {
        public final int    damage;
        public final String message;
        public AttackResult(int damage, String message) {
            this.damage  = damage;
            this.message = message;
        }
    }
}