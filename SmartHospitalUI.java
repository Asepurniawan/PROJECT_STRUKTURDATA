import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class SmartHospitalUI extends JFrame {
    private final JLabel clockLabel = new JLabel();
    private final JLabel queueCountLabel = new JLabel();
    private final JLabel servedCountLabel = new JLabel();
    private final JTextField nameField = new JTextField();
    private final JTextField complaintField = new JTextField();
    private final JSpinner urgencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JTextArea queueArea = new JTextArea();
    private final JTextArea logArea = new JTextArea();
    private final JTextArea statsArea = new JTextArea();
    private final Timer clockTimer;

    public SmartHospitalUI() {
        super("Smart Hospital Queue System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 780));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0xF4F7FB));
        setLayout(new BorderLayout(16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        clockTimer = new Timer(1000, event -> updateClock());
        clockTimer.start();
        refreshView();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x16324F));
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SMART HOSPITAL QUEUE SYSTEM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        JLabel subtitle = new JLabel("Priority Queue untuk pasien darurat, FIFO untuk antrean normal");
        subtitle.setForeground(new Color(0xD8E6F5));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(subtitle);

        clockLabel.setForeground(new Color(0xFFE7A3));
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel left = buildInputPanel();
        JPanel center = buildDisplayPanel();

        main.add(left, BorderLayout.WEST);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(330, 1));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5DEE8)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel sectionTitle = new JLabel("Tambah Pasien");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(0x111827));
        panel.add(sectionTitle, gbc);

        gbc.gridy++;
        panel.add(label("Nama pasien"), gbc);
        gbc.gridy++;
        panel.add(nameField, gbc);

        gbc.gridy++;
        panel.add(label("Keluhan"), gbc);
        gbc.gridy++;
        complaintField.setPreferredSize(new Dimension(260, 34));
        panel.add(complaintField, gbc);

        gbc.gridy++;
        panel.add(label("Tingkat urgensi (1-10)"), gbc);
        gbc.gridy++;
        panel.add(urgencySpinner, gbc);

        gbc.gridy++;
        panel.add(Box.createVerticalStrut(10), gbc);

        JButton addButton = buildActionButton("Tambah Pasien", new Color(0x1F7A8C));
        addButton.addActionListener(event -> handleAddPatient());
        panel.add(addButton, gbc);

        gbc.gridy++;
        JButton callButton = buildActionButton("Panggil Pasien", new Color(0xC44536));
        callButton.addActionListener(event -> handleCallNext());
        panel.add(callButton, gbc);

        gbc.gridy++;
        JButton refreshButton = buildActionButton("Refresh Tampilan", new Color(0x3D5A80));
        refreshButton.addActionListener(event -> refreshView());
        panel.add(refreshButton, gbc);

        gbc.gridy++;
        JButton clearButton = buildActionButton("Bersihkan Input", new Color(0x7B8C94));
        clearButton.addActionListener(event -> clearInputs());
        panel.add(clearButton, gbc);

        gbc.gridy++;
        panel.add(Box.createVerticalStrut(12), gbc);

        queueCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        servedCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(queueCountLabel, gbc);
        gbc.gridy++;
        panel.add(servedCountLabel, gbc);

        return panel;
    }

    private JPanel buildDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 12));
        topRow.setOpaque(false);
        topRow.add(buildTextAreaCard("Visualisasi Antrian", queueArea, new Color(0xF6FBFF)));
        topRow.add(buildTextAreaCard("Statistik", statsArea, new Color(0xFFF8F1)));

        JPanel bottom = buildTextAreaCard("Log Proses Pasien", logArea, Color.WHITE);

        panel.add(topRow, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTextAreaCard(String titleText, JTextArea area, Color background) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5DEE8)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(0x111827));
        card.add(title, BorderLayout.NORTH);

        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setForeground(new Color(0x111827));
        area.setBackground(background);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xD5DEE8)));
        scrollPane.setPreferredSize(new Dimension(1, titleText.equals("Log Proses Pasien") ? 200 : 260));
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        footer.setOpaque(false);
        JLabel hint = new JLabel("Aturan prioritas: pasien darurat didahulukan, jika level sama maka FIFO.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(new Color(0x111827));
        footer.add(hint, BorderLayout.WEST);
        return footer;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(0x111827));
        return label;
    }

    private JButton buildActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.BLACK);
        button.setBackground(color);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(180, 38));
        return button;
    }

    private void handleAddPatient() {
        String name = nameField.getText().trim();
        String complaint = complaintField.getText().trim();
        int urgency = ((Number) urgencySpinner.getValue()).intValue();

        if (name.isEmpty() || complaint.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan keluhan harus diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = SmartHospital.addPatientRecord(name, complaint, urgency);
        SmartHospital.processLog.add(message);
        JOptionPane.showMessageDialog(this, message, "Pasien Ditambahkan", JOptionPane.INFORMATION_MESSAGE);
        clearInputs();
        refreshView();
    }

    private void handleCallNext() {
        String message = SmartHospital.callNextPatientRecord();
        if (message == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada pasien dalam antrian.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            SmartHospital.processLog.add(message);
            JOptionPane.showMessageDialog(this, message, "Pasien Dipanggil", JOptionPane.INFORMATION_MESSAGE);
        }
        refreshView();
    }

    private void clearInputs() {
        nameField.setText("");
        complaintField.setText("");
        urgencySpinner.setValue(1);
    }

    private void refreshView() {
        updateClock();
        queueArea.setText(SmartHospital.buildQueueVisualizationText());
        statsArea.setText(SmartHospital.buildStatisticsText());
        logArea.setText(SmartHospital.buildProcessLogText());
        queueArea.setCaretPosition(0);
        statsArea.setCaretPosition(0);
        logArea.setCaretPosition(0);
        queueCountLabel.setText("Total antrian aktif: " + (SmartHospital.emergencyQueue.size() + SmartHospital.normalQueue.size()));
        servedCountLabel.setText("Total pasien dipanggil: " + SmartHospital.servedPatients.size());
    }

    private void updateClock() {
        clockLabel.setText("Waktu real-time: " + LocalDateTime.now().format(SmartHospital.TIME_FORMAT));
    }
}
