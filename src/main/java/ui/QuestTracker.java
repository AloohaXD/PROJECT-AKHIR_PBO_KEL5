package ui;
import java.util.*;

public class QuestTracker {
    public record Quest(String id, String title, String description, int targetCount, String rewardText, int rewardExp) {}
    private final List<Quest>    questList    = new ArrayList<>();
    private final List<Integer>  progressList = new ArrayList<>();
    private int activeQuestIndex = 0;

    // Notification state
    private boolean notifActive = false;
    private int     notifTimer  = 0;
    private String  notifText   = "";
    private static final int NOTIF_DUR = 180; // 3 sec

    public QuestTracker() { setupChapterQuests(1); }

    public void setupChapterQuests(int chapter) {
        questList.clear(); progressList.clear(); activeQuestIndex = 0;
        int krocoTarget = 2 + chapter;
        questList.add(new Quest("kill_kroco",
            "Basmi Kroco Ch." + chapter,
            "Bunuh " + krocoTarget + " musuh di Chapter " + chapter,
            krocoTarget,
            "+" + (krocoTarget * 30) + " EXP",
            krocoTarget * 30));
        questList.add(new Quest("kill_commander",
            "Kalahkan Panglima",
            "Kalahkan Demon Commander Chapter " + chapter,
            1,
            "+" + (chapter * 80 + 50) + " EXP – Lanjut Ch." + (chapter < 10 ? chapter+1 : 10),
            chapter * 80 + 50));
        progressList.add(0);
        progressList.add(0);
    }

    /** Returns (completed=true, rewardExp) if quest just completed */
    public int[] updateProgress(String id, int delta) {
        for (int i = 0; i < questList.size(); i++) {
            if (!questList.get(i).id().equals(id)) continue;
            int cur = progressList.get(i);
            int target = questList.get(i).targetCount();
            if (cur >= target) return new int[]{0, 0}; // already done
            int newProg = Math.min(cur + delta, target);
            progressList.set(i, newProg);
            if (newProg >= target) {
                int exp = questList.get(i).rewardExp();
                showNotif("Quest Selesai: " + questList.get(i).title() + "  +" + exp + " EXP!");
                return new int[]{1, exp};
            }
            return new int[]{0, 0};
        }
        return new int[]{0, 0};
    }

    public void showNotif(String text) {
        notifText  = text;
        notifTimer = NOTIF_DUR;
        notifActive= true;
    }

    public void updateNotif() {
        if (notifActive) { notifTimer--; if (notifTimer <= 0) notifActive = false; }
    }

    public boolean isNotifActive() { return notifActive; }
    public String  getNotifText()  { return notifText; }
    public float   getNotifAlpha() {
        if (!notifActive) return 0f;
        if (notifTimer > NOTIF_DUR - 30) return (NOTIF_DUR - notifTimer) / 30f;
        if (notifTimer < 30)             return notifTimer / 30f;
        return 1f;
    }

    public int getProgress(String id) {
        for (int i = 0; i < questList.size(); i++) if (questList.get(i).id().equals(id)) return progressList.get(i);
        return 0;
    }
    public boolean isCompleted(String id) {
        for (int i = 0; i < questList.size(); i++)
            if (questList.get(i).id().equals(id)) return progressList.get(i) >= questList.get(i).targetCount();
        return false;
    }

    public Quest   getActiveQuest()    { return questList.isEmpty() ? null : questList.get(activeQuestIndex); }
    public int     getActiveProgress() { return questList.isEmpty() ? 0 : progressList.get(activeQuestIndex); }
    public void    cycleActive()       { if (!questList.isEmpty()) activeQuestIndex = (activeQuestIndex+1) % questList.size(); }
    public List<Quest>   getQuests()   { return questList; }
    public List<Integer> getProgress() { return progressList; }
}
