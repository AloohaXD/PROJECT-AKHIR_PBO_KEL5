package combat;
/**
 * Card.java
 * ============================================================
 * Model data satu kartu. Diperbarui dari versi sebelumnya:
 *   - Menambah field manaCost (biaya mana untuk memainkan kartu)
 *   - Ultimate card memerlukan cukup mana untuk dimainkan
 * ============================================================
 */
public class Card {

    public enum CardType { ATTACK, SKILL, ULTIMATE }
    public enum SkillEffect { NONE, HEAL, DEFENSE }

    private final String name;
    private final CardType type;
    private final SkillEffect skillEffect;
    private final int value;       // Damage / heal / defense amount
    private final int manaCost;    // Biaya mana (0 = gratis)
    private final String description;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public Card(String name, CardType type, SkillEffect skillEffect,
                int value, int manaCost, String description) {
        this.name        = name;
        this.type        = type;
        this.skillEffect = skillEffect;
        this.value       = value;
        this.manaCost    = manaCost;
        this.description = description;
    }

    // =========================================================
    // FACTORY METHODS
    // =========================================================
    public static Card createAttack(String name, int damage, int mana, String desc) {
        return new Card(name, CardType.ATTACK, SkillEffect.NONE, damage, mana, desc);
    }
    public static Card createHeal(String name, int amount, int mana, String desc) {
        return new Card(name, CardType.SKILL, SkillEffect.HEAL, amount, mana, desc);
    }
    public static Card createDefense(String name, int amount, int mana, String desc) {
        return new Card(name, CardType.SKILL, SkillEffect.DEFENSE, amount, mana, desc);
    }
    public static Card createUltimate(String name, int damage, int mana, String desc) {
        return new Card(name, CardType.ULTIMATE, SkillEffect.NONE, damage, mana, desc);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================
    public String getButtonLabel() {
        // BUG FIX: Semua teks pakai warna eksplisit agar terlihat di background gelap
        String typeColor = getTypeHexColor();
        String icon      = getTypeIcon();
        String manaStr   = manaCost > 0
            ? "<br><font color='#88BBFF'>MP: " + manaCost + "</font>"
            : "";
        return "<html><center>"
             + "<font color='" + typeColor + "'><b>" + icon + " " + name + "</b></font>"
             + "<br><font color='#FFD700'>Efek: " + value + "</font>"
             + manaStr
             + "</center></html>";
    }

    /** Warna hex berdasarkan tipe kartu — dipakai di label HTML */
    private String getTypeHexColor() {
        switch (type) {
            case ATTACK:   return "#FF6666"; // merah terang
            case ULTIMATE: return "#CC66FF"; // ungu terang
            default:
                return skillEffect == SkillEffect.HEAL ? "#55EE88" : "#66AAFF"; // hijau / biru
        }
    }

    public String getTooltipText() {
        return "<html><b>" + name + "</b> [" + type + "]<br>"
             + description + "<br>"
             + "Efek: " + value
             + (manaCost > 0 ? " | Mana: " + manaCost : "") + "</html>";
    }

    private String getTypeIcon() {
        switch (type) {
            case ATTACK:   return "⚔";
            case ULTIMATE: return "✨";
            default:
                return skillEffect == SkillEffect.HEAL ? "💚" : "🛡";
        }
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public String      getName()       { return name; }
    public CardType    getType()       { return type; }
    public SkillEffect getSkillEffect(){ return skillEffect; }
    public int         getValue()      { return value; }
    public int         getManaCost()   { return manaCost; }
    public String      getDescription(){ return description; }

    @Override public String toString() {
        return name + "(" + type + ", val=" + value + ", mp=" + manaCost + ")";
    }
}