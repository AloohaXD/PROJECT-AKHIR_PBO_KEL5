package main;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * AudioManager.java
 * ============================================================
 * Sistem audio terpusat untuk The Last Chromatic Warrior.
 *
 * Fitur:
 *  - BGM dengan loop, fade-out, dan pergantian track
 *  - SFX one-shot (sword slash, bladestorm, dll.)
 *  - Fade-out BGM saat transisi Main Menu → Eksplorasi
 *  - Combat BGM dipilih random antara bgm1 dan bgm2
 *  - Singleton pattern — akses via AudioManager.get()
 * ============================================================
 */
public class AudioManager {

    // ─── Singleton ────────────────────────────────────────────
    private static final AudioManager INSTANCE = new AudioManager();
    public static AudioManager get() { return INSTANCE; }

    // ─── Kunci BGM ────────────────────────────────────────────
    public static final String BGM_MAIN_MENU  = "main_menu_bgm";
    public static final String BGM_EXPLORE    = "explore_bgm";
    public static final String BGM_COMBAT_1   = "combat_bgm1";
    public static final String BGM_COMBAT_2   = "combat_bgm2";

    // ─── Kunci SFX ────────────────────────────────────────────
    public static final String SFX_SWORD_SLASH   = "sword_slash";
    public static final String SFX_BLADE_STORM   = "blade_storm";
    public static final String SFX_WAR_CRY       = "war_cry";
    public static final String SFX_BATTLE_STANCE = "battle_stance";
    public static final String SFX_SHIELD_BASH   = "shield_bash";
    public static final String SFX_TRANSITION    = "transition_sfx";

    // ─── Path resources ───────────────────────────────────────
    private static final String BGM_PATH = "/assets/audio/bgm/";
    private static final String SFX_PATH = "/assets/audio/sfx/";

    // ─── State BGM ────────────────────────────────────────────
    private Clip    bgmClip      = null;   // Clip BGM yang sedang bermain
    private String  currentBgm   = null;   // Nama BGM aktif
    private float   bgmVolume    = 0.75f;  // 0.0 – 1.0

    // ─── Fade Out Thread ──────────────────────────────────────
    private Thread  fadeThread   = null;
    private volatile boolean fadeRunning = false;

    // ─── Cache SFX (preloaded byte[]) ─────────────────────────
    // SFX di-cache sebagai byte array agar bisa diputar ulang cepat
    private final Map<String, byte[]> sfxCache = new HashMap<>();

    private final Random rng = new Random();

    // ─── Volume SFX ───────────────────────────────────────────
    private float sfxVolume = 0.85f;

    // =========================================================
    // CONSTRUCTOR (private — singleton)
    // =========================================================
    private AudioManager() {
        preloadSfx();
    }

    // =========================================================
    // PRELOAD SFX
    // =========================================================
    private void preloadSfx() {
        String[] sfxKeys = {
            SFX_SWORD_SLASH, SFX_BLADE_STORM, SFX_WAR_CRY,
            SFX_BATTLE_STANCE, SFX_SHIELD_BASH, SFX_TRANSITION
        };
        for (String key : sfxKeys) {
            try {
                InputStream is = getClass().getResourceAsStream(SFX_PATH + key + ".wav");
                if (is != null) {
                    byte[] data = is.readAllBytes();
                    sfxCache.put(key, data);
                    is.close();
                    System.out.println("[Audio] SFX preloaded: " + key);
                } else {
                    System.err.println("[Audio] SFX not found: " + key);
                }
            } catch (Exception e) {
                System.err.println("[Audio] Gagal preload SFX " + key + ": " + e.getMessage());
            }
        }
    }

    // =========================================================
    // BGM — Play
    // =========================================================

    /**
     * Putar BGM. Jika BGM yang sama sudah berjalan, tidak melakukan apa-apa.
     * @param key    Kunci BGM (gunakan konstanta BGM_*)
     * @param loop   true = loop terus (explore/combat), false = sekali putar
     */
    public void playBgm(String key, boolean loop) {
        // Jika BGM sama sudah berjalan, skip
        if (key.equals(currentBgm) && bgmClip != null && bgmClip.isRunning()) return;

        stopBgmImmediate();

        try {
            InputStream is = getClass().getResourceAsStream(BGM_PATH + key + ".wav");
            if (is == null) {
                System.err.println("[Audio] BGM tidak ditemukan: " + key);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                new BufferedInputStream(is));
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            setClipVolume(bgmClip, bgmVolume);

            if (loop) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                bgmClip.start();
            }
            currentBgm = key;
            System.out.println("[Audio] BGM play: " + key + (loop ? " (loop)" : ""));
        } catch (Exception e) {
            System.err.println("[Audio] Gagal play BGM " + key + ": " + e.getMessage());
        }
    }

    /**
     * Putar Combat BGM — dipilih random antara bgm1 dan bgm2.
     */
    public void playRandomCombatBgm() {
        String pick = rng.nextBoolean() ? BGM_COMBAT_1 : BGM_COMBAT_2;
        playBgm(pick, true);
    }

    // =========================================================
    // BGM — Stop
    // =========================================================

    /** Stop BGM langsung tanpa fade. */
    public void stopBgmImmediate() {
        cancelFade();
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
        currentBgm = null;
    }

    /**
     * Fade-out BGM dalam durasi tertentu, lalu jalankan callback saat selesai.
     * @param durationMs  Durasi fade dalam millisecond
     * @param onDone      Callback setelah fade selesai (boleh null)
     */
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
                int steps    = 40;
                int stepMs   = durationMs / steps;
                for (int i = 0; i <= steps && fadeRunning; i++) {
                    float vol = startVol * (1f - (float) i / steps);
                    if (clipToFade.isOpen()) setClipVolume(clipToFade, Math.max(0f, vol));
                    Thread.sleep(stepMs);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                fadeRunning = false;
                if (clipToFade.isOpen()) {
                    clipToFade.stop();
                    clipToFade.close();
                }
                if (bgmClip == clipToFade) {
                    bgmClip    = null;
                    currentBgm = null;
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

    // =========================================================
    // SFX — Play
    // =========================================================

    /**
     * Putar SFX sekali (one-shot). Non-blocking.
     * @param key Kunci SFX (gunakan konstanta SFX_*)
     */
    public void playSfx(String key) {
        byte[] data = sfxCache.get(key);
        if (data == null) {
            System.err.println("[Audio] SFX tidak ada di cache: " + key);
            return;
        }

        // Jalankan di thread terpisah agar tidak blocking game loop
        Thread t = new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(new java.io.ByteArrayInputStream(data)));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                setClipVolume(clip, sfxVolume);
                clip.start();
                // Tutup clip otomatis setelah selesai
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.err.println("[Audio] Gagal play SFX " + key + ": " + e.getMessage());
            }
        }, "SFX-" + key);
        t.setDaemon(true);
        t.start();
    }

    // =========================================================
    // VOLUME UTILITY
    // =========================================================

    /** Set volume clip via FloatControl MASTER_GAIN (dB). */
    private void setClipVolume(Clip clip, float volume) {
        if (!clip.isOpen()) return;
        try {
            FloatControl gainControl =
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (volume <= 0f) ? gainControl.getMinimum()
                                      : (float)(20.0 * Math.log10(volume));
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            // Kontrol tidak tersedia — abaikan
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    /** Hentikan semua audio (panggil saat game ditutup). */
    public void shutdown() {
        cancelFade();
        stopBgmImmediate();
    }
}
