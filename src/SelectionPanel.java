import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * SelectionPanel.java
 * ============================================================
 * Layar 2: Character Selection
 * Menampilkan 5 pilihan class dengan preview sprite pixel art,
 * statistik, dan lore. Klik kartu karakter → transisi ke Combat.
 * ============================================================
 */
public class SelectionPanel extends JPanel {

    // Callback: dipanggil saat pemain memilih class, passing CharacterProfile
    private final Consumer<HeroClass.CharacterProfile> onClassSelected;

    // Kartu yang sedang di-hover / dipilih
    private HeroClass.ClassType hoveredClass = null;

    // --- Konstanta Warna ---
    private static final Color BG        = new Color(10, 8, 25);
    private static final Color BG_PANEL  = new Color(20, 16, 45);
    private static final Color GOLD      = new Color(255, 200, 50);
    private static final Color TEXT_MAIN = new Color(230, 225, 240);
    private static final Color TEXT_DIM  = new Color(140, 130, 160);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public SelectionPanel(Consumer<HeroClass.CharacterProfile> onClassSelected) {
        this.onClassSelected = onClassSelected;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    // =========================================================
    // MEMBANGUN UI
    // =========================================================
    private void buildUI() {
        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel lblTitle = new JLabel("✦  PILIH PEJUANGMU  ✦", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 26));
        lblTitle.setForeground(GOLD);

        JLabel lblSub = new JLabel("Setiap class memiliki kekuatan dan deck kartu yang unik.", SwingConstants.CENTER);
        lblSub.setFont(new Font("Serif", Font.ITALIC, 13));
        lblSub.setForeground(TEXT_DIM);

        // Tombol Kembali
        JButton btnBack = new JButton("← Kembali");
        btnBack.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnBack.setForeground(TEXT_DIM);
        btnBack.setBackground(BG);
        btnBack.setBorder(BorderFactory.createLineBorder(new Color(80, 70, 100), 1));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(BG);
        backPanel.add(btnBack);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(BG);
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(lblSub);

        header.add(backPanel,   BorderLayout.WEST);
        header.add(titleBlock,  BorderLayout.CENTER);

        // --- Grid 5 kartu karakter ---
        JPanel cardGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        cardGrid.setBackground(BG);
        cardGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (HeroClass.ClassType ct : HeroClass.ClassType.values()) {
            cardGrid.add(buildCharCard(ct));
        }

        // --- Hint bawah ---
        JLabel hint = new JLabel("Klik karakter untuk memilih dan memulai pertarungan!", SwingConstants.CENTER);
        hint.setFont(new Font("Serif", Font.ITALIC, 13));
        hint.setForeground(TEXT_DIM);
        hint.setBorder(BorderFactory.createEmptyBorder(8, 0, 16, 0));

        add(header,   BorderLayout.NORTH);
        add(cardGrid, BorderLayout.CENTER);
        add(hint,     BorderLayout.SOUTH);
    }

    /**
     * Membangun satu kartu karakter interaktif.
     * Kartu ini adalah JPanel yang bisa diklik.
     */
    private JPanel buildCharCard(HeroClass.ClassType classType) {
        // Wrapper panel dengan custom paint untuk border & background
        JPanel card = new JPanel(new BorderLayout(0, 5)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCardBackground(g, classType, hoveredClass == classType);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        // --- Sprite Area (custom panel dengan pixel art) ---
        JPanel spritePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Gambar sprite pixel art karakter
                HeroClass.drawHeroSprite(g2d, classType, 0, 0, getWidth(), getHeight());
            }
        };
        spritePanel.setOpaque(false);
        spritePanel.setPreferredSize(new Dimension(0, 140));

        // --- Info Panel bawah ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Nama class
        JLabel lblName = new JLabel(classType.emoji + " " + classType.displayName, SwingConstants.CENTER);
        lblName.setFont(new Font("Serif", Font.BOLD, 15));
        lblName.setForeground(classType.themeColor.brighter());
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stat bar ringkas
        JPanel statsPanel = buildStatBars(classType);

        // Lore (teks singkat)
        JTextArea loreArea = new JTextArea(new HeroClass.CharacterProfile(classType).lore);
        loreArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
        loreArea.setForeground(TEXT_DIM);
        loreArea.setOpaque(false);
        loreArea.setEditable(false);
        loreArea.setLineWrap(true);
        loreArea.setWrapStyleWord(true);
        loreArea.setFocusable(false);

        // Tombol pilih
        JButton btnSelect = new JButton("PILIH " + classType.displayName.toUpperCase());
        btnSelect.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnSelect.setForeground(classType.themeColor.brighter());
        btnSelect.setBackground(new Color(0, 0, 0, 180));
        btnSelect.setBorder(BorderFactory.createLineBorder(classType.themeColor, 1));
        btnSelect.setFocusPainted(false);
        btnSelect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(statsPanel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(loreArea);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(btnSelect);

        card.add(spritePanel, BorderLayout.CENTER);
        card.add(infoPanel,   BorderLayout.SOUTH);

        // --- Event Listeners ---
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hoveredClass = classType;
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                hoveredClass = null;
                card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                selectClass(classType);
            }
        };
        card.addMouseListener(hoverListener);
        spritePanel.addMouseListener(hoverListener);

        btnSelect.addActionListener(e -> selectClass(classType));

        return card;
    }

    /** Membangun mini stat bar untuk HP/MP/Speed */
    private JPanel buildStatBars(HeroClass.ClassType ct) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(buildOneStat("HP",    ct.baseHp,    140, new Color(80, 200, 100)));
        p.add(Box.createVerticalStrut(2));
        p.add(buildOneStat("MP",    ct.baseMp,    130, new Color(80, 150, 255)));
        p.add(Box.createVerticalStrut(2));
        p.add(buildOneStat("SPD",   ct.baseSpeed * 10, 110, new Color(255, 180, 50)));

        return p;
    }

    private JPanel buildOneStat(String label, int value, int max, Color color) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 9));
        lbl.setForeground(TEXT_DIM);
        lbl.setPreferredSize(new Dimension(30, 12));

        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(value);
        bar.setForeground(color);
        bar.setBackground(new Color(30, 25, 50));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 8));

        row.add(lbl, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    /** Dipanggil saat karakter dipilih — buat profile dan panggil callback */
    private void selectClass(HeroClass.ClassType classType) {
        HeroClass.CharacterProfile profile = new HeroClass.CharacterProfile(classType);
        onClassSelected.accept(profile);
    }

    // =========================================================
    // CUSTOM PAINTING — Background kartu karakter
    // =========================================================
    private void drawCardBackground(Graphics g, HeroClass.ClassType ct, boolean hovered) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();  // Ini width dari card panel, bukan SelectionPanel
        // Gunakan parent width dari komponen yang memanggil
        // Karena dipanggil dari JPanel anonymous, kita tidak bisa ambil width langsung
        // Ambil dari g clip bounds
        Rectangle clip = g2d.getClipBounds();
        int W = clip != null ? clip.width  : 130;
        int H = clip != null ? clip.height : 400;

        Color base  = ct.themeColor;
        Color alpha = new Color(base.getRed(), base.getGreen(), base.getBlue(), hovered ? 40 : 20);
        Color border= new Color(base.getRed(), base.getGreen(), base.getBlue(), hovered ? 200 : 80);

        // Background
        g2d.setColor(new Color(15, 12, 35, 220));
        g2d.fillRoundRect(0, 0, W, H, 12, 12);

        // Overlay warna class
        g2d.setColor(alpha);
        g2d.fillRoundRect(0, 0, W, H, 12, 12);

        // Border
        g2d.setColor(border);
        g2d.setStroke(new BasicStroke(hovered ? 2f : 1f));
        g2d.drawRoundRect(1, 1, W - 2, H - 2, 12, 12);

        // Glow efek atas saat hover
        if (hovered) {
            GradientPaint glow = new GradientPaint(
                0, 0, new Color(base.getRed(), base.getGreen(), base.getBlue(), 60),
                0, H / 3, new Color(0, 0, 0, 0));
            g2d.setPaint(glow);
            g2d.fillRoundRect(0, 0, W, H, 12, 12);
        }

        g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        // Background gradient
        GradientPaint bg = new GradientPaint(0, 0, new Color(10, 8, 25),
                0, getHeight(), new Color(20, 10, 40));
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}