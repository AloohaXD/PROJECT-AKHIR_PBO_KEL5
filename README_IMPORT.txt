=== CARA IMPORT KE NETBEANS IDE 29 ===

1. Extract ZIP ini → akan muncul folder "TLCW"
2. Buka NetBeans IDE 29
3. File → Open Project (Ctrl+Shift+O)
4. Arahkan ke folder "TLCW" (yang ada pom.xml-nya)
5. Klik "Open Project"
6. Tunggu NetBeans scan project (beberapa detik)
7. Klik kanan project → Run (F6)

=== STRUKTUR PROJECT ===
TLCW/
├── pom.xml                          ← Maven config (buka ini sbg project)
├── src/
│   └── main/
│       ├── java/                    ← SEMUA FILE JAVA ADA DI SINI
│       │   ├── main/                (Main.java, GamePanel.java, dll)
│       │   ├── entity/              (Player.java, EnemyEntity.java, dll)
│       │   ├── combat/              (CombatPanel.java, Hero.java, dll)
│       │   ├── tile/                (TileManager.java)
│       │   └── ui/                  (MainMenuPanel.java, dll)
│       └── resources/
│           └── assets/              ← Gambar & Audio
└── nbproject/project.xml            ← NetBeans project descriptor

=== CATATAN ===
- File Java ADA, tapi di dalam src/main/java/ (struktur Maven standar)
- NetBeans akan otomatis menampilkan semua package di Project panel
- Pastikan JDK 21 sudah terinstall dan dikonfigurasi di NetBeans
