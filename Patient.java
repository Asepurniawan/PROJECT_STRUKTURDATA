import java.time.LocalDateTime;

public class Patient {
    private final String name;
    private final String complaint;
    private final int urgency;
    private final LocalDateTime arrivalTime;
    private final long sequence;
    private LocalDateTime servedTime;

    public Patient(String name, String complaint, int urgency, LocalDateTime arrivalTime, long sequence) {
        this.name = name;
        this.complaint = complaint;
        this.urgency = urgency;
        this.arrivalTime = arrivalTime;
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public String getComplaint() {
        return complaint;
    }

    public int getUrgency() {
        return urgency;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public long getSequence() {
        return sequence;
    }

    public boolean isEmergency() {
        return urgency >= 7;
    }

    public LocalDateTime getServedTime() {
        return servedTime;
    }

    public void setServedTime(LocalDateTime servedTime) {
        this.servedTime = servedTime;
    }
}
