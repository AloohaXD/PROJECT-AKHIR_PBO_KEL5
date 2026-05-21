package combat;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HeroClass — 5 Chromatic Warriors dengan stats dari Backstory.md
 * Health : Tanker 300, Knight 250, Archer 200, Mage 200, Assassin 150
 * Damage : Assassin > Mage > Archer > Knight > Tanker
 * MP     : Mage > Archer > Assassin > Tanker > Knight
 */
public class HeroClass {

    public enum ClassType {
        WARRIOR ("Knight",   "Kerajaan Merah",  250, 40,  25, "/assets/images/karakter/warrior.png",  new Color(200, 60,  60)),
        TANKER  ("Tanker",   "Kerajaan Biru",   300, 50,  15, "/assets/images/karakter/tanker.png",   new Color(60,  120, 220)),
        ARCHER  ("Archer",   "Kerajaan Hijau",  200, 90,  35, "/assets/images/karakter/archer.png",   new Color(60,  180, 80)),
        MAGE    ("Mage",     "Kerajaan Ungu",   200, 120, 40, "/assets/images/karakter/mage.png",     new Color(160, 60,  220)),
        ASSASSIN("Assassin", "Kerajaan Hitam",  150, 70,  50, "/assets/images/karakter/assassin.png", new Color(60,  55,  80));

        public final String displayName;
        public final String kingdom;
        public final int    baseHp;
        public final int    baseMp;
        public final int    baseAtk;
        public final String spritePath;
        public final Color  themeColor;

        ClassType(String n, String k, int hp, int mp, int atk, String sp, Color c) {
            displayName=n; kingdom=k; baseHp=hp; baseMp=mp; baseAtk=atk; spritePath=sp; themeColor=c;
        }
        // legacy compat
        public int baseSpeed  = 8;
        public String emoji   = "⚔";
    }

    public static class CharacterProfile {
        public final ClassType classType;
        public final String name;
        public int maxHp;   // mutable so level can scale
        public int maxMp;
        public int baseAtk;
        public final List<Card> starterDeck;
        public final String lore;

        /** Default profile (base stats) */
        public CharacterProfile(ClassType c) {
            this(c, c.baseHp, c.baseMp, c.baseAtk);
        }

        /** Scaled profile (player level applied) */
        public CharacterProfile(ClassType c, int scaledHp, int scaledMp, int scaledAtk) {
            classType  = c;
            name       = c.displayName;
            maxHp      = scaledHp;
            maxMp      = scaledMp;
            baseAtk    = scaledAtk;
            starterDeck= buildDeck(c, scaledAtk);
            lore       = buildLore(c);
        }
    }

    public static List<Card> buildDeck(ClassType type, int atk) {
        List<Card> d = new ArrayList<>();
        int a = atk; // scaled attack
        switch (type) {
            case WARRIOR -> {
                d.add(Card.createAttack ("Sword Slash",   a,     0,  "Tebasan pedang standar."));
                d.add(Card.createAttack ("Cleave",        a+10,  0,  "Tebasan lebar mengenai musuh."));
                d.add(Card.createAttack ("Sword Slash",   a,     0,  "Tebasan pedang standar."));
                d.add(Card.createDefense("Battle Stance", 20,    10, "Ambil posisi bertahan."));
                d.add(Card.createHeal   ("War Cry",       25,    15, "Pulihkan HP dengan semangat."));
                d.add(Card.createUltimate("Hero's Blade", a*2+20,30, "Tebasan terkuat knight!"));
            }
            case TANKER -> {
                d.add(Card.createAttack ("Shield Bash",   a+5,   0,  "Hantam dengan perisai."));
                d.add(Card.createDefense("Iron Wall",     35,    10, "Blok damage besar."));
                d.add(Card.createDefense("Iron Wall",     35,    10, "Blok damage besar."));
                d.add(Card.createHeal   ("Fortress",      40,    20, "Pulihkan HP dengan aura benteng."));
                d.add(Card.createAttack ("Ground Slam",   a+8,   0,  "Hantam tanah, pukul musuh."));
                d.add(Card.createUltimate("Titanium Body",a+15,  25, "Kurangi damage + balik serangan!"));
            }
            case ARCHER -> {
                d.add(Card.createAttack ("Quick Shot",    a,     0,  "Tembak cepat ke musuh."));
                d.add(Card.createAttack ("Volley",        a+12,  0,  "Seruang panah sekaligus."));
                d.add(Card.createAttack ("Poison Arrow",  a+8,   5,  "Panah beracun — damage extra."));
                d.add(Card.createDefense("Dodge",         18,    8,  "Hindari serangan."));
                d.add(Card.createHeal   ("Nature's Grace",20,    15, "Pulihkan HP dari alam."));
                d.add(Card.createUltimate("Rain of Arrows",a*2+15,25,"Hujan panah menghantam musuh!"));
            }
            case MAGE -> {
                d.add(Card.createAttack ("Fireball",      a+5,   10, "Lempar bola api."));
                d.add(Card.createAttack ("Ice Shard",     a+8,   12, "Lempar pecahan es."));
                d.add(Card.createAttack ("Thunder",       a+12,  15, "Petir menghantam musuh."));
                d.add(Card.createDefense("Mana Shield",   22,    8,  "Lindungi diri dengan mana."));
                d.add(Card.createHeal   ("Arcane Heal",   30,    20, "Pulihkan HP dengan sihir."));
                d.add(Card.createUltimate("Meteor Strike",a*2+30,35, "Meteor jatuh — damage masif!"));
            }
            case ASSASSIN -> {
                d.add(Card.createAttack ("Backstab",      a+15,  0,  "Tikam dari belakang."));
                d.add(Card.createAttack ("Dual Strike",   a+10,  0,  "Serang dua kali cepat."));
                d.add(Card.createAttack ("Poison Blade",  a+8,   5,  "Tikam dengan bilah beracun."));
                d.add(Card.createDefense("Shadow Step",   15,    5,  "Bergerak di bayangan."));
                d.add(Card.createHeal   ("Dark Elixir",   50,    15, "Elixir kegelapan untuk HP."));
                d.add(Card.createUltimate("Death Mark",   a*2+40,30, "Tandai kematian musuh!"));
            }
        }
        return d;
    }

    // Legacy compat (no scaling)
    public static List<Card> buildDeck(ClassType type) {
        return buildDeck(type, type.baseAtk);
    }

    public static String buildLore(ClassType c) {
        return switch(c) {
            case WARRIOR  -> "Knight dari Kerajaan Merah. Pertahanan tinggi, damage sedang. Pejuang sejati.";
            case TANKER   -> "Tanker dari Kerajaan Biru. Pertahanan tertinggi, damage rendah. Benteng hidup.";
            case ARCHER   -> "Archer dari Kerajaan Hijau. Damage tinggi dari jarak jauh. Mahir sihir.";
            case MAGE     -> "Mage dari Kerajaan Ungu. Damage sihir besar. Pertahanan rendah.";
            case ASSASSIN -> "Assassin dari Kerajaan Hitam. Damage tertinggi. Sangat lemah dalam bertahan.";
        };
    }
}
