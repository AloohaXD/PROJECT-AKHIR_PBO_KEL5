package ui;

import java.util.ArrayList;
import java.util.List;

/**
 * QuestTracker.java
 * ============================================================
 * Sistem Quest Tracker sederhana untuk melacak misi pemain.
 *
 * Fitur:
 *  - Menyimpan daftar quest yang aktif
 *  - Melacak progress (misalnya: 0/3 Slime dibunuh)
 *  - Method untuk memperbarui progress dari game event
 *
 * Data quest dirender oleh HudRenderer di layar eksplorasi.
 * ============================================================
 */
public class QuestTracker {

    // ─── Inner Record: Satu Quest ─────────────────────────────
    /**
     * Record immutable untuk satu quest.
     * Menggunakan Java record (fitur modern Java 16+) agar ringkas.
     */
    public record Quest(
        String id,           // ID unik quest (untuk lookup)
        String title,        // Judul singkat
        String description,  // Deskripsi lengkap
        int    targetCount,  // Jumlah target yang harus diselesaikan
        String rewardText    // Teks reward (informasi saja)
    ) {}

    // ─── State Quest Aktif ────────────────────────────────────
    private final List<Quest>    questList    = new ArrayList<>();
    private final List<Integer>  progressList = new ArrayList<>(); // progress[i] untuk quest[i]
    private int activeQuestIndex = 0; // Quest mana yang sedang ditampilkan di HUD

    // =========================================================
    // INISIALISASI QUEST DEFAULT
    // =========================================================
    public QuestTracker() {
        // Tambahkan quest bawaan di awal game
        addQuest(new Quest(
            "kill_slime",
            "Pemburu Pertama",
            "Bunuh 3 Musuh di Peta",
            3,
            "+50 EXP, +1 Level percepatan"
        ));
        addQuest(new Quest(
            "explore_forest",
            "Penjelajah Muda",
            "Jelajahi seluruh peta (pergi ke pojok)",
            1,
            "+30 EXP"
        ));
        addQuest(new Quest(
            "reach_level3",
            "Tumbuh Lebih Kuat",
            "Capai Level 3",
            3,
            "+80 EXP, Unlock skill baru"
        ));
    }

    // =========================================================
    // OPERASI QUEST
    // =========================================================

    /** Menambahkan quest baru ke tracker. */
    public void addQuest(Quest quest) {
        questList.add(quest);
        progressList.add(0);
    }

    /**
     * Menambah progress pada quest tertentu berdasarkan ID.
     * Dipanggil saat event game terjadi (misalnya: musuh mati).
     *
     * @param questId  ID quest yang akan di-update
     * @param amount   Jumlah progress yang ditambahkan
     * @return true jika quest tersebut baru saja selesai
     */
    public boolean updateProgress(String questId, int amount) {
        for (int i = 0; i < questList.size(); i++) {
            if (questList.get(i).id().equals(questId)) {
                int oldProgress = progressList.get(i);
                int newProgress = Math.min(oldProgress + amount, questList.get(i).targetCount());
                progressList.set(i, newProgress);

                // Kembalikan true jika baru saja mencapai target
                return newProgress >= questList.get(i).targetCount()
                    && oldProgress < questList.get(i).targetCount();
            }
        }
        return false;
    }

    /** Mendapatkan progress saat ini untuk quest tertentu. */
    public int getProgress(String questId) {
        for (int i = 0; i < questList.size(); i++) {
            if (questList.get(i).id().equals(questId)) {
                return progressList.get(i);
            }
        }
        return 0;
    }

    /** Cek apakah quest sudah selesai. */
    public boolean isCompleted(String questId) {
        for (int i = 0; i < questList.size(); i++) {
            Quest q = questList.get(i);
            if (q.id().equals(questId)) {
                return progressList.get(i) >= q.targetCount();
            }
        }
        return false;
    }

    // ─── Navigasi Quest Aktif di HUD ─────────────────────────

    /** Quest yang sedang ditampilkan di HUD. */
    public Quest getActiveQuest() {
        if (questList.isEmpty()) return null;
        return questList.get(activeQuestIndex);
    }

    /** Progress dari quest yang sedang aktif di HUD. */
    public int getActiveProgress() {
        if (progressList.isEmpty()) return 0;
        return progressList.get(activeQuestIndex);
    }

    /** Pindah ke quest berikutnya di HUD (untuk navigasi dengan tombol). */
    public void nextQuest() {
        if (!questList.isEmpty()) {
            activeQuestIndex = (activeQuestIndex + 1) % questList.size();
        }
    }

    /** Total quest yang tersedia. */
    public int getQuestCount() { return questList.size(); }
    public int getActiveQuestIndex() { return activeQuestIndex; }
}