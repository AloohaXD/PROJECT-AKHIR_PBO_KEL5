package entity;

/**
 * PlayerStats.java
 * ============================================================
 * Model data statistik pemain yang mendukung sistem leveling.
 *
 * Bertanggung jawab atas:
 *  - Penyimpanan semua stat (HP, Mana, Attack, EXP, Level)
 *  - Logika perhitungan EXP yang dibutuhkan per level
 *  - Kenaikan stat otomatis saat Level Up
 *
 * Digunakan oleh Player (eksplorasi) DAN Hero (combat) agar data
 * level/stat bisa dibagikan antar dua sistem tersebut.
 * ============================================================
 */
public class PlayerStats {

    // ─── Level & EXP ─────────────────────────────────────────
    private int level    = 1;
    private int currentExp = 0;
    private int expToNextLevel;

    // ─── Kapasitas Maksimum ───────────────────────────────────
    private int maxHp;
    private int maxMana;
    private int attack;

    // ─── HP & Mana Saat Ini ───────────────────────────────────
    private int currentHp;
    private int currentMana;

    // ─── Konstanta Pertumbuhan Stat Per Level ─────────────────
    // Setiap naik level, stat bertambah sebesar nilai ini
    private static final int HP_GROWTH     = 15;
    private static final int MANA_GROWTH   = 8;
    private static final int ATTACK_GROWTH = 3;

    // ─── Stat Dasar Level 1 ───────────────────────────────────
    private static final int BASE_HP     = 100;
    private static final int BASE_MANA   = 60;
    private static final int BASE_ATTACK = 12;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public PlayerStats() {
        // Inisialisasi stat dasar di level 1
        this.maxHp      = BASE_HP;
        this.maxMana    = BASE_MANA;
        this.attack     = BASE_ATTACK;
        this.currentHp  = maxHp;
        this.currentMana= maxMana;
        this.expToNextLevel = calculateExpRequired(1);
    }

    // =========================================================
    // SISTEM EXP & LEVEL UP
    // =========================================================

    /**
     * Menambahkan EXP ke pemain. Jika EXP cukup, otomatis naik level.
     * @param amount Jumlah EXP yang didapat
     * @return true jika terjadi Level Up, false jika tidak
     */
    public boolean addExp(int amount) {
        currentExp += amount;
        boolean didLevelUp = false;

        // Loop untuk menangani Level Up berantai (jika EXP sangat besar)
        while (currentExp >= expToNextLevel) {
            currentExp -= expToNextLevel;
            levelUp();
            didLevelUp = true;
        }
        return didLevelUp;
    }

    /**
     * Proses naik level:
     *  1. Naikkan level
     *  2. Tingkatkan semua stat maksimum
     *  3. Pulihkan HP dan Mana ke penuh (reward naik level)
     *  4. Hitung EXP yang dibutuhkan untuk level berikutnya
     */
    private void levelUp() {
        level++;

        // Tingkatkan kapasitas maksimum stat
        maxHp     += HP_GROWTH;
        maxMana   += MANA_GROWTH;
        attack    += ATTACK_GROWTH;

        // Pulihkan HP dan Mana ke maksimum saat naik level
        currentHp   = maxHp;
        currentMana = maxMana;

        // Hitung EXP untuk level berikutnya
        expToNextLevel = calculateExpRequired(level);
    }

    /**
     * Rumus EXP yang dibutuhkan untuk naik ke level berikutnya.
     * Menggunakan kurva kuadratik agar makin sulit seiring naik level.
     *
     * Formula: EXP = 50 * level^1.5
     * Contoh:  Level 1 → 50 EXP, Level 5 → ~559 EXP, Level 10 → ~1581 EXP
     *
     * @param currentLevel Level saat ini
     * @return EXP yang dibutuhkan untuk naik ke level berikutnya
     */
    public static int calculateExpRequired(int currentLevel) {
        return (int)(50 * Math.pow(currentLevel, 1.5));
    }

    // =========================================================
    // OPERASI BATTLE
    // =========================================================

    /** Kurangi HP. Nilai HP tidak akan kurang dari 0. */
    public void takeDamage(int amount) {
        currentHp = Math.max(0, currentHp - amount);
    }

    /** Pulihkan HP. Nilai HP tidak akan melebihi maxHp. */
    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    /** Kurangi Mana untuk casting skill. */
    public boolean useMana(int amount) {
        if (currentMana < amount) return false; // Mana tidak cukup
        currentMana = Math.max(0, currentMana - amount);
        return true;
    }

    /** Regen Mana otomatis (dipanggil tiap akhir giliran musuh). */
    public void regenMana(int amount) {
        currentMana = Math.min(maxMana, currentMana + amount);
    }

    /** Cek apakah pemain masih hidup. */
    public boolean isAlive() { return currentHp > 0; }

    // =========================================================
    // GETTERS (semua bersifat read-only dari luar)
    // =========================================================
    public int getLevel()          { return level; }
    public int getCurrentExp()     { return currentExp; }
    public int getExpToNextLevel() { return expToNextLevel; }
    public int getMaxHp()          { return maxHp; }
    public int getMaxMana()        { return maxMana; }
    public int getAttack()         { return attack; }
    public int getCurrentHp()      { return currentHp; }
    public int getCurrentMana()    { return currentMana; }

    /**
     * Persentase EXP untuk progress bar (0.0 - 1.0).
     */
    public float getExpPercent() {
        return expToNextLevel > 0 ? (float) currentExp / expToNextLevel : 0f;
    }

    /**
     * Persentase HP untuk progress bar (0.0 - 1.0).
     */
    public float getHpPercent() {
        return maxHp > 0 ? (float) currentHp / maxHp : 0f;
    }

    /**
     * Persentase Mana untuk progress bar (0.0 - 1.0).
     */
    public float getManaPercent() {
        return maxMana > 0 ? (float) currentMana / maxMana : 0f;
    }

    /**
     * Representasi teks stat untuk debug / log.
     */
    @Override
    public String toString() {
        return String.format("Lv.%d | HP:%d/%d | MP:%d/%d | ATK:%d | EXP:%d/%d",
            level, currentHp, maxHp, currentMana, maxMana, attack, currentExp, expToNextLevel);
    }
}