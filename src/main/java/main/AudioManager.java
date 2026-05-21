package main;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager — berdasarkan patch5 yang terbukti bekerja.
 * Menggunakan AudioSystem.getClip() langsung + fade thread sederhana.
 * Tambahan: BGM_BOSS, playBgmForCombat(), setBgmVolume(), setSfxVolume()
 */
public class AudioManager {

    private static final AudioManager INSTANCE = new AudioManager();
    public static AudioManager get() { return INSTANCE; }

    // ── BGM keys ────────────────────────────────────────────────────────────
    public static final String BGM_MAIN_MENU = "main_menu_bgm";
    public static final String BGM_EXPLORE   = "explore_bgm";
    public static final String BGM_COMBAT_1  = "combat_bgm1";
    public static final String BGM_COMBAT_2  = "combat_bgm2";
    public static final String BGM_BOSS      = "boss_fight";

    // ── SFX keys ────────────────────────────────────────────────────────────
    public static final String SFX_SWORD_SLASH   = "sword_slash";
    public static final String SFX_BLADE_STORM   = "blade_storm";
    public static final String SFX_WAR_CRY       = "war_cry";
    public static final String SFX_BATTLE_STANCE = "battle_stance";
    public static final String SFX_SHIELD_BASH   = "shield_bash";
    public static final String SFX_TRANSITION    = "transition_sfx";

    private static final String BGM_PATH = "/assets/audio/bgm/";
    private static final String SFX_PATH = "/assets/audio/sfx/";

    // ── BGM state ────────────────────────────────────────────────────────────
    private Clip   bgmClip    = null;
    private String currentBgm = null;
    private float  bgmVolume  = 0.75f;
    private float  sfxVolume  = 0.85f;

    // ── Fade thread ──────────────────────────────────────────────────────────
    private Thread  fadeThread  = null;
    private volatile boolean fadeRunning = false;

    // ── SFX cache ────────────────────────────────────────────────────────────
    private final Map<String, byte[]> sfxCache = new HashMap<>();

    private AudioManager() {
        preloadSfx();
    }

    // ── Preload SFX ─────────────────────────────────────────────────────────

    private void preloadSfx() {
        String[] keys = {
            SFX_SWORD_SLASH, SFX_BLADE_STORM, SFX_WAR_CRY,
            SFX_BATTLE_STANCE, SFX_SHIELD_BASH, SFX_TRANSITION
        };
        for (String key : keys) {
            try {
                InputStream is = getClass().getResourceAsStream(SFX_PATH + key + ".wav");
                if (is != null) {
                    sfxCache.put(key, is.readAllBytes());
                    is.close();
                } else {
                    System.err.println("[Audio] SFX tidak ditemukan: " + key);
                }
            } catch (Exception e) {
                System.err.println("[Audio] SFX gagal preload " + key + ": " + e.getMessage());
            }
        }
    }

    // ── BGM: Play ───────────────────────────────────────────────────────────

    /**
     * Putar BGM. Jika key sama sudah berjalan, skip.
     * Dipanggil langsung di thread apapun — aman dari EDT maupun game loop.
     */
    public synchronized void playBgm(String key, boolean loop) {
        if (key.equals(currentBgm) && bgmClip != null && bgmClip.isRunning()) return;
        stopBgmImmediate();
        playBgmInternal(key, loop);
    }

    /** Versi force — selalu restart meski BGM sama sedang berjalan. */
    public synchronized void playBgm(String key, boolean loop, boolean force) {
        if (!force && key.equals(currentBgm) && bgmClip != null && bgmClip.isRunning()) return;
        stopBgmImmediate();
        playBgmInternal(key, loop);
    }

    private void playBgmInternal(String key, boolean loop) {
        try {
            InputStream is = getClass().getResourceAsStream(BGM_PATH + key + ".wav");
            if (is == null) {
                System.err.println("[Audio] BGM tidak ditemukan: " + key);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            bgmClip = AudioSystem.getClip();   // ← kunci: getClip() bukan getLine()
            bgmClip.open(ais);
            setClipVolume(bgmClip, bgmVolume);
            if (loop) bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            else      bgmClip.start();
            currentBgm = key;
            System.out.println("[Audio] BGM: " + key + (loop ? " (loop)" : ""));
        } catch (Exception e) {
            System.err.println("[Audio] BGM gagal '" + key + "': " + e.getMessage());
        }
    }

    /** BGM untuk combat: kroco=bgm1, boss=boss_fight, DemonKing=bgm2 */
    public void playBgmForCombat(boolean isBoss, boolean isDemonKing) {
        if (isDemonKing) playBgm(BGM_COMBAT_2, true, true);
        else if (isBoss) playBgm(BGM_BOSS,     true, true);
        else             playBgm(BGM_COMBAT_1,  true, true);
    }

    // ── BGM: Stop ───────────────────────────────────────────────────────────

    public synchronized void stopBgmImmediate() {
        cancelFade();
        if (bgmClip != null) {
            try {
                if (bgmClip.isRunning()) bgmClip.stop();
                bgmClip.close();
            } catch (Exception ignored) {}
            bgmClip = null;
        }
        currentBgm = null;
    }

    /** Fade-out BGM lalu jalankan callback. */
    public void fadeOutBgm(int durationMs, Runnable onDone) {
        if (bgmClip == null || !bgmClip.isRunning()) {
            stopBgmImmediate();
            if (onDone != null) onDone.run();
            return;
        }
        cancelFade();
        fadeRunning = true;
        final Clip clipToFade = bgmClip;
        final float startVol  = bgmVolume;

        fadeThread = new Thread(() -> {
            try {
                int steps  = 40;
                int stepMs = Math.max(1, durationMs / steps);
                for (int i = 0; i <= steps && fadeRunning; i++) {
                    float vol = startVol * (1f - (float)i / steps);
                    if (clipToFade.isOpen()) setClipVolume(clipToFade, Math.max(0f, vol));
                    Thread.sleep(stepMs);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                fadeRunning = false;
                try {
                    if (clipToFade.isOpen()) { clipToFade.stop(); clipToFade.close(); }
                } catch (Exception ignored) {}
                synchronized (AudioManager.this) {
                    if (bgmClip == clipToFade) { bgmClip = null; currentBgm = null; }
                }
                if (onDone != null) onDone.run();
            }
        }, "AudioFadeThread");
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    private void cancelFade() {
        fadeRunning = false;
        if (fadeThread != null && fadeThread.isAlive()) {
            fadeThread.interrupt();
            fadeThread = null;
        }
    }

    // ── SFX: Play ───────────────────────────────────────────────────────────

    public void playSfx(String key) {
        byte[] data = sfxCache.get(key);
        if (data == null) return;
        Thread t = new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(new ByteArrayInputStream(data)));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                setClipVolume(clip, sfxVolume);
                clip.start();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Exception ignored) {}
        }, "SFX-" + key);
        t.setDaemon(true);
        t.start();
    }

    // ── Volume ───────────────────────────────────────────────────────────────

    /** Volume pakai rumus log10 — sama dengan AudioManager asli patch5 */
    private void setClipVolume(Clip clip, float volume) {
        if (!clip.isOpen()) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (volume <= 0f)
                ? gain.getMinimum()
                : (float)(20.0 * Math.log10(volume));
            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
            gain.setValue(dB);
        } catch (Exception ignored) {}
    }

    public synchronized void setBgmVolume(float v) {
        bgmVolume = Math.max(0f, Math.min(1f, v));
        if (bgmClip != null && bgmClip.isOpen()) setClipVolume(bgmClip, bgmVolume);
    }
    public void  setSfxVolume(float v) { sfxVolume = Math.max(0f, Math.min(1f, v)); }
    public float getBgmVolume()        { return bgmVolume; }
    public float getSfxVolume()        { return sfxVolume; }

    public void shutdown() { cancelFade(); stopBgmImmediate(); }
}
