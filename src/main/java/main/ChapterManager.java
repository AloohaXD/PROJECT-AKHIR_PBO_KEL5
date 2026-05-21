package main;

public class ChapterManager {
    public static final int TOTAL_CHAPTERS = 10;
    private int currentChapter   = 1;
    private int krocoKilled      = 0;
    private boolean commanderDefeated = false;
    private long startTimeMs     = 0;
    private long endTimeMs       = 0;
    private boolean timerRunning = false;

    public static final String[] CHAPTER_TITLES = {
        "Fajar Kegelapan",    // 1
        "Hutan Terkutuk",     // 2
        "Gua Para Iblis",     // 3
        "Benteng Runtuh",     // 4
        "Rawa Hitam",         // 5
        "Puncak Neraka",      // 6
        "Menara Iblis",       // 7
        "Jurang Keabadian",   // 8
        "Istana Kegelapan",   // 9
        "Tahta Iblis Raja"    // 10
    };

    // BUG FIX: correct boss names per chapter matching assets
    public static final String[] COMMANDER_NAMES = {
        "The Great Goblin",    // ch1 — boss_ch1.png
        "Big Orc",             // ch2 — boss_ch2.png
        "V Undead",            // ch3 — boss_ch3.png
        "Bloodly Vampire",     // ch4 — boss_ch4.png
        "Chimera Hell",        // ch5 — boss_ch5.png
        "Minotaur King",       // ch6 — boss_ch6.png
        "Wither Skeleton",     // ch7 — boss_ch7.png
        "The Swamper Lizard",  // ch8 — boss_ch8.png
        "The Big Ogre",        // ch9 — boss_ch9.png
        "DEMON KING"           // ch10 — boss_ch10.png (Demon Lord)
    };

    public static final String[] KROCO_NAMES = {
        "Goblin",       // ch1
        "Orc",          // ch2
        "Undead",       // ch3
        "Vampire",      // ch4
        "Chimera",      // ch5
        "Minotaur",     // ch6
        "Skeleton",     // ch7
        "Lizardman",    // ch8
        "Ogre",         // ch9
        "Iblis Kroco"   // ch10
    };

    public void startTimer()  { startTimeMs = System.currentTimeMillis(); timerRunning = true; }
    public void stopTimer()   { endTimeMs   = System.currentTimeMillis(); timerRunning = false; }

    public long getElapsedMs() {
        if (timerRunning) return System.currentTimeMillis() - startTimeMs;
        return Math.max(0, endTimeMs - startTimeMs);
    }

    public String getElapsedString() {
        long ms   = getElapsedMs();
        long sec  = (ms / 1000) % 60;
        long min  = (ms / 60000) % 60;
        long hour = ms / 3600000;
        if (hour > 0) return String.format("%02d:%02d:%02d", hour, min, sec);
        return String.format("%02d:%02d", min, sec);
    }

    public int  getCurrentChapter()      { return currentChapter; }
    public String getChapterTitle()      { return CHAPTER_TITLES[currentChapter-1]; }
    public String getCommanderName()     { return COMMANDER_NAMES[currentChapter-1]; }
    public String getKrocoName()         { return KROCO_NAMES[Math.min(currentChapter-1, KROCO_NAMES.length-1)]; }
    public boolean isFinalChapter()      { return currentChapter == TOTAL_CHAPTERS; }
    public int  getKrocoKilled()         { return krocoKilled; }
    public void addKrocoKill()           { krocoKilled++; }
    public boolean isCommanderDefeated() { return commanderDefeated; }
    public void setCommanderDefeated()   { commanderDefeated = true; }

    public boolean advanceChapter() {
        if (currentChapter >= TOTAL_CHAPTERS) return false;
        currentChapter++;
        krocoKilled       = 0;
        commanderDefeated = false;
        return true;
    }

    public void reset() {
        currentChapter    = 1;
        krocoKilled       = 0;
        commanderDefeated = false;
        timerRunning      = false;
        startTimeMs       = 0;
        endTimeMs         = 0;
    }
}
