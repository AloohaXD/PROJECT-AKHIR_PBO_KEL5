# Game Java — Patch 3 (Combat Sprites + Effects)

## Cara Jalankan
Butuh JDK 17+. Jalankan dari folder ini:

```bash
# Maven (direkomendasikan jika ada pom.xml):
mvn compile exec:java -Dexec.mainClass=main.Main

# Atau manual:
find src/main/java -name "*.java" > sources.txt
mkdir -p target/classes
javac -d target/classes @sources.txt
java -cp target/classes main.Main
```

## Asset PNG (WAJIB)
Taruh file ini di folder `assets/` (sudah disertakan):
- `assets/warrior.png` — sprite hero Warrior
- `assets/orc.png`     — sprite musuh Orc

Jika file tidak ditemukan, otomatis fallback ke pixel art bawaan.

## Kontrol Eksplorasi
| Tombol | Aksi |
|--------|------|
| W/A/S/D | Gerak 360° |
| X / J  | Basic Attack — serang musuh terdekat |
| Z / Enter | Bicara dengan NPC |
| ESC    | Tutup dialog |

## Efek Visual Combat (Warrior)
| Kartu | Efek |
|-------|------|
| Sword Slash | Tiga garis tebasan diagonal kuning |
| Cleave | Garis horizontal oranye tebal + glow |
| Bladestorm | 8 pedang berjatuhan dari atas musuh |
| War Cry | Aura merah membesar + teks "WAR CRY!" |
| Shield Bash | Perisai retak berputar muncul di musuh |
| Battle Stance | Perisai membesar → lingkaran aura biru |
| Serangan Musuh | Tiga cakar merah menyambar dari kanan |
| Ultimate | Ledakan cahaya besar purple-kuning |

## Efek Shake Damage
Saat menerima damage → sprite bergetar (semakin keras untuk damage besar).
