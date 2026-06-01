import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class SmartHospital {

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final Scanner scanner = new Scanner(System.in);

    public static final Queue<Patient> normalQueue = new LinkedList<>();
    public static final PriorityQueue<Patient> emergencyQueue = new PriorityQueue<>(
            Comparator.comparingInt(Patient::getUrgency).reversed()
                    .thenComparingLong(Patient::getSequence)
    );
    public static final List<String> processLog = new ArrayList<>();
    public static final List<Patient> servedPatients = new ArrayList<>();

    private static long sequenceCounter = 0;

    public static void main(String[] args) {
        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            runMenu();
            return;
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new SmartHospitalUI().setVisible(true);
        });
    }

    private static void runMenu() {
        while (true) {
            printHeader();
            printMenu();
            int choice = readInt("Pilih menu: ");

            switch (choice) {
                case 1 -> addPatientFlow();
                case 2 -> callNextPatient();
                case 3 -> showQueues();
                case 4 -> showStatistics();
                case 5 -> showProcessLog();
                case 6 -> {
                    System.out.println("Program selesai.");
                    return;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }

            pause();
        }
    }

    private static void printHeader() {
        System.out.println("\n====================================================");
        System.out.println(" SMART HOSPITAL QUEUE SYSTEM");
        System.out.println(" Waktu sistem: " + LocalDateTime.now().format(TIME_FORMAT));
        System.out.println("====================================================");
    }

    private static void printMenu() {
        System.out.println("1. Tambah pasien");
        System.out.println("2. Panggil pasien berikutnya");
        System.out.println("3. Lihat visualisasi antrian");
        System.out.println("4. Lihat statistik");
        System.out.println("5. Lihat log proses pasien");
        System.out.println("6. Keluar");
        System.out.println();
    }

    private static void addPatientFlow() {
        System.out.println("\n=== Tambah Pasien ===");
        String name = readNonEmptyString("Nama pasien: ");
        String complaint = readNonEmptyString("Keluhan: ");
        int urgency = readIntInRange("Tingkat urgensi (1-10): ", 1, 10);

        Patient patient = new Patient(
                name,
                complaint,
                urgency,
                LocalDateTime.now(),
                sequenceCounter++
        );

        if (patient.isEmergency()) {
            emergencyQueue.offer(patient);
        } else {
            normalQueue.offer(patient);
        }

        String category = patient.isEmergency() ? "DARURAT" : "NORMAL";
        String message = String.format("[%s] Pasien ditambahkan: %s | %s | urgensi=%d | kategori=%s | estimasi tunggu=%s",
                LocalDateTime.now().format(TIME_FORMAT),
                patient.getName(),
                patient.getComplaint(),
                patient.getUrgency(),
                category,
                formatDuration(calculateEstimatedWait(patient))
        );
        processLog.add(message);
        System.out.println(message);
    }

    public static void callNextPatient() {
        System.out.println("\n=== Panggil Pasien ===");

        String message = callNextPatientRecord();
        if (message == null) {
            System.out.println("Tidak ada pasien dalam antrian.");
            return;
        }
        processLog.add(message);
        System.out.println(message);
    }

    public static String addPatientRecord(String name, String complaint, int urgency) {
        Patient patient = new Patient(
                name,
                complaint,
                urgency,
                LocalDateTime.now(),
                sequenceCounter++
        );

        if (patient.isEmergency()) {
            emergencyQueue.offer(patient);
        } else {
            normalQueue.offer(patient);
        }

        String category = patient.isEmergency() ? "DARURAT" : "NORMAL";
        return String.format("[%s] Pasien ditambahkan: %s | %s | urgensi=%d | kategori=%s | estimasi tunggu=%s",
                LocalDateTime.now().format(TIME_FORMAT),
                patient.getName(),
                patient.getComplaint(),
                patient.getUrgency(),
                category,
                formatDuration(calculateEstimatedWait(patient))
        );
    }

    public static String callNextPatientRecord() {
        Patient next = pollNextPatient();
        if (next == null) {
            return null;
        }

        Duration actualWait = Duration.between(next.getArrivalTime(), LocalDateTime.now());
        next.setServedTime(LocalDateTime.now());
        servedPatients.add(next);

        String category = next.isEmergency() ? "DARURAT" : "NORMAL";
        return String.format("[%s] Pasien dipanggil: %s | %s | urgensi=%d | kategori=%s | waktu tunggu aktual=%s",
                LocalDateTime.now().format(TIME_FORMAT),
                next.getName(),
                next.getComplaint(),
                next.getUrgency(),
                category,
                formatDuration(actualWait)
        );
    }

    private static Patient pollNextPatient() {
        if (!emergencyQueue.isEmpty()) {
            return emergencyQueue.poll();
        }
        return normalQueue.poll();
    }

    public static void showQueues() {
        System.out.println("\n=== Visualisasi Antrian ===");
        List<Patient> emergencySnapshot = getEmergencySnapshot();
        List<Patient> normalSnapshot = new ArrayList<>(normalQueue);

        System.out.println("[PRIORITY QUEUE - PASIEN DARURAT]");
        if (emergencySnapshot.isEmpty()) {
            System.out.println("(kosong)");
        } else {
            for (int i = 0; i < emergencySnapshot.size(); i++) {
                Patient patient = emergencySnapshot.get(i);
                System.out.println(renderQueueItem(i + 1, patient));
            }
        }

        System.out.println();
        System.out.println("[QUEUE NORMAL - PASIEN NORMAL]");
        if (normalSnapshot.isEmpty()) {
            System.out.println("(kosong)");
        } else {
            for (int i = 0; i < normalSnapshot.size(); i++) {
                Patient patient = normalSnapshot.get(i);
                System.out.println(renderQueueItem(i + 1, patient));
            }
        }

        System.out.println();
        System.out.println("[URUTAN PANGGIL SAAT INI]");
        List<Patient> servingOrder = getServingOrderSnapshot();
        if (servingOrder.isEmpty()) {
            System.out.println("(kosong)");
        } else {
            for (int i = 0; i < servingOrder.size(); i++) {
                Patient patient = servingOrder.get(i);
                System.out.println((i + 1) + ". " + patient.getName() + " [urgensi " + patient.getUrgency() + "] -> estimasi tunggu "
                        + formatDuration(calculateEstimatedWait(patient)));
            }
        }
    }

    public static void showStatistics() {
        System.out.println("\n=== Statistik ===");
        if (servedPatients.isEmpty()) {
            System.out.println("Belum ada pasien yang dipanggil.");
            return;
        }

        long totalSeconds = 0;
        Patient longestWaitPatient = servedPatients.get(0);
        Duration longestWait = Duration.between(longestWaitPatient.getArrivalTime(), longestWaitPatient.getServedTime());

        for (Patient patient : servedPatients) {
            Duration wait = Duration.between(patient.getArrivalTime(), patient.getServedTime());
            totalSeconds += wait.getSeconds();
            if (wait.compareTo(longestWait) > 0) {
                longestWait = wait;
                longestWaitPatient = patient;
            }
        }

        Duration averageWait = Duration.ofSeconds(totalSeconds / servedPatients.size());
        System.out.println("Jumlah pasien dipanggil : " + servedPatients.size());
        System.out.println("Rata-rata waktu tunggu  : " + formatDuration(averageWait));
        System.out.println("Pasien terlama menunggu : " + longestWaitPatient.getName() + " (" + formatDuration(longestWait) + ")");
        System.out.println("Sisa antrian darurat    : " + emergencyQueue.size());
        System.out.println("Sisa antrian normal     : " + normalQueue.size());
    }

    public static void showProcessLog() {
        System.out.println("\n=== Log Proses Pasien ===");
        if (processLog.isEmpty()) {
            System.out.println("Belum ada aktivitas.");
            return;
        }

        for (int i = 0; i < processLog.size(); i++) {
            System.out.println((i + 1) + ". " + processLog.get(i));
        }
    }

    private static String renderQueueItem(int position, Patient patient) {
        return position + ". " + patient.getName()
                + " | keluhan: " + patient.getComplaint()
                + " | urgensi: " + patient.getUrgency()
                + " | kategori: " + (patient.isEmergency() ? "DARURAT" : "NORMAL")
                + " | estimasi tunggu: " + formatDuration(calculateEstimatedWait(patient));
    }

    public static String buildQueueVisualizationText() {
        StringBuilder builder = new StringBuilder();
        builder.append("[PRIORITY QUEUE - PASIEN DARURAT]\n");
        List<Patient> emergencySnapshot = getEmergencySnapshot();
        if (emergencySnapshot.isEmpty()) {
            builder.append("(kosong)\n");
        } else {
            for (int i = 0; i < emergencySnapshot.size(); i++) {
                builder.append(renderQueueItem(i + 1, emergencySnapshot.get(i))).append('\n');
            }
        }

        builder.append("\n[QUEUE NORMAL - PASIEN NORMAL]\n");
        List<Patient> normalSnapshot = new ArrayList<>(normalQueue);
        if (normalSnapshot.isEmpty()) {
            builder.append("(kosong)\n");
        } else {
            for (int i = 0; i < normalSnapshot.size(); i++) {
                builder.append(renderQueueItem(i + 1, normalSnapshot.get(i))).append('\n');
            }
        }

        builder.append("\n[URUTAN PANGGIL SAAT INI]\n");
        List<Patient> servingOrder = getServingOrderSnapshot();
        if (servingOrder.isEmpty()) {
            builder.append("(kosong)\n");
        } else {
            for (int i = 0; i < servingOrder.size(); i++) {
                Patient patient = servingOrder.get(i);
                builder.append(i + 1)
                        .append(". ")
                        .append(patient.getName())
                        .append(" [urgensi ")
                        .append(patient.getUrgency())
                        .append("] -> estimasi tunggu ")
                        .append(formatDuration(calculateEstimatedWait(patient)))
                        .append('\n');
            }
        }

        return builder.toString();
    }

    public static String buildStatisticsText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Jumlah pasien dipanggil : ").append(servedPatients.size()).append('\n');
        builder.append("Sisa antrian darurat    : ").append(emergencyQueue.size()).append('\n');
        builder.append("Sisa antrian normal     : ").append(normalQueue.size()).append('\n');

        if (servedPatients.isEmpty()) {
            builder.append("Rata-rata waktu tunggu  : -\n");
            builder.append("Pasien terlama menunggu : -\n");
            return builder.toString();
        }

        long totalSeconds = 0;
        Patient longestWaitPatient = servedPatients.get(0);
        Duration longestWait = Duration.between(longestWaitPatient.getArrivalTime(), longestWaitPatient.getServedTime());

        for (Patient patient : servedPatients) {
            Duration wait = Duration.between(patient.getArrivalTime(), patient.getServedTime());
            totalSeconds += wait.getSeconds();
            if (wait.compareTo(longestWait) > 0) {
                longestWait = wait;
                longestWaitPatient = patient;
            }
        }

        Duration averageWait = Duration.ofSeconds(totalSeconds / servedPatients.size());
        builder.append("Rata-rata waktu tunggu  : ").append(formatDuration(averageWait)).append('\n');
        builder.append("Pasien terlama menunggu : ").append(longestWaitPatient.getName())
                .append(" (")
                .append(formatDuration(longestWait))
                .append(")\n");
        return builder.toString();
    }

    public static String buildProcessLogText() {
        if (processLog.isEmpty()) {
            return "Belum ada aktivitas.";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < processLog.size(); i++) {
            builder.append(i + 1).append(". ").append(processLog.get(i)).append('\n');
        }
        return builder.toString();
    }

    private static List<Patient> getEmergencySnapshot() {
        List<Patient> snapshot = new ArrayList<>(emergencyQueue);
        snapshot.sort(Comparator.comparingInt(Patient::getUrgency).reversed()
                .thenComparingLong(Patient::getSequence));
        return snapshot;
    }

    private static List<Patient> getServingOrderSnapshot() {
        List<Patient> snapshot = new ArrayList<>();
        snapshot.addAll(getEmergencySnapshot());
        snapshot.addAll(normalQueue);
        return snapshot;
    }

    private static Duration calculateEstimatedWait(Patient target) {
        long secondsAhead = 0;

        for (Patient patient : getEmergencySnapshot()) {
            if (isAheadOf(patient, target)) {
                secondsAhead += serviceDurationSeconds(patient.getUrgency());
            }
        }

        if (!target.isEmergency()) {
            for (Patient patient : normalQueue) {
                if (isAheadOf(patient, target)) {
                    secondsAhead += serviceDurationSeconds(patient.getUrgency());
                }
            }
        } else {
            for (Patient patient : normalQueue) {
                secondsAhead += serviceDurationSeconds(patient.getUrgency());
            }
        }

        return Duration.ofSeconds(secondsAhead);
    }

    private static boolean isAheadOf(Patient candidate, Patient target) {
        if (candidate == target) {
            return false;
        }
        if (candidate.isEmergency() && !target.isEmergency()) {
            return true;
        }
        if (candidate.isEmergency() && target.isEmergency()) {
            if (candidate.getUrgency() > target.getUrgency()) {
                return true;
            }
            if (candidate.getUrgency() == target.getUrgency()) {
                return candidate.getSequence() < target.getSequence();
            }
            return false;
        }
        if (!candidate.isEmergency() && !target.isEmergency()) {
            return candidate.getSequence() < target.getSequence();
        }
        return false;
    }

    private static long serviceDurationSeconds(int urgency) {
        if (urgency >= 9) {
            return 240;
        }
        if (urgency >= 7) {
            return 360;
        }
        if (urgency >= 5) {
            return 480;
        }
        if (urgency >= 3) {
            return 600;
        }
        return 720;
    }

    public static String formatDuration(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Masukkan angka yang valid.");
            }
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Nilai harus berada di antara " + min + " sampai " + max + ".");
        }
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input tidak boleh kosong.");
        }
    }

    private static void pause() {
        System.out.println();
        System.out.print("Tekan Enter untuk lanjut...");
        scanner.nextLine();
    }
}
