package entity;

import combat.HeroClass;

/**
 * PlayerStats — tracks level, exp, and scales stats based on HeroClass base + level bonuses.
 * BUG FIX: stats now carry to combat by computing scaled values per class.
 */
public class PlayerStats {
    private int level = 1, exp = 0, expToNext = 100;
    // Base stats from selected hero class (set on hero select)
    private int baseMaxHp = 250, baseMaxMp = 40, baseAtk = 25;
    // Scaled stats (updated on level up)
    private int maxHp, hp, maxMp, mp, atk, def = 5;

    public PlayerStats() { recalculate(); }

    public void setBaseStats(HeroClass.ClassType c) {
        baseMaxHp = c.baseHp; baseMaxMp = c.baseMp; baseAtk = c.baseAtk;
        level = 1; exp = 0; expToNext = 100;
        recalculate();
    }

    /** Recalculate all scaled stats from base + level bonuses */
    private void recalculate() {
        int lvl = level - 1;
        maxHp  = baseMaxHp + lvl * 20;
        maxMp  = baseMaxMp + lvl * 10;
        atk    = baseAtk   + lvl * 5;
        def    = 5         + lvl * 2;
        hp     = maxHp;
        mp     = maxMp;
    }

    public void gainExp(int amount) {
        exp += amount;
        while (exp >= expToNext) {
            exp -= expToNext;
            expToNext = (int)(expToNext * 1.35);
            level++;
            recalculate();  // full recalculate on level up
        }
    }

    public void restoreAll() { hp = maxHp; mp = maxMp; }

    // Getters
    public int getLevel()     { return level; }
    public int getExp()       { return exp; }
    public int getExpToNext() { return expToNext; }
    public int getHp()        { return hp; }
    public int getMaxHp()     { return maxHp; }
    public int getMp()        { return mp; }
    public int getMaxMp()     { return maxMp; }
    public int getBaseAtk()   { return atk; }
    public int getBaseDef()   { return def; }
    public int getBaseMaxHp() { return baseMaxHp; }
    public int getBaseMaxMp() { return baseMaxMp; }

    public void setHp(int v)  { hp = Math.max(0, Math.min(maxHp, v)); }
    public void setMp(int v)  { mp = Math.max(0, Math.min(maxMp, v)); }
    public boolean isDead()   { return hp <= 0; }

    @Override public String toString() {
        return "Lv" + level + " HP:" + hp + "/" + maxHp + " MP:" + mp + "/" + maxMp + " ATK:" + atk;
    }
}
