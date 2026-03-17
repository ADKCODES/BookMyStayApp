import java.io.*;
import java.util.*;

class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}

abstract class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;

    public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
    }
}

class SingleRoom extends Room {
    public SingleRoom() { super(1, 250, 1500.0); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super(2, 400, 2500.0); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super(3, 750, 5000.0); }
}

class RoomInventory implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Integer> roomAvailability;

    public RoomInventory() {
        this.roomAvailability = new HashMap<>();
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }

    public synchronized Map<String, Integer> getRoomAvailability() {
        return new HashMap<>(roomAvailability);
    }

    public synchronized void updateAvailability(String roomType, int count) throws BookingException {
        if (count < 0) throw new BookingException("Negative inventory error.");
        roomAvailability.put(roomType, count);
    }

    public synchronized int getCount(String type) {
        return roomAvailability.getOrDefault(type, 0);
    }
}

class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String guestName;
    private String roomType;
    private String roomId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}

class BookingHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Reservation> history = new ArrayList<>();
    public synchronized void recordBooking(Reservation res) { history.add(res); }
    public synchronized List<Reservation> getHistory() { return new ArrayList<>(history); }
}

class PersistenceService {
    private static final String FILE_NAME = "hotel_data.ser";

    public static void saveState(RoomInventory inventory, BookingHistory history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(inventory);
            oos.writeObject(history);
            System.out.println("SYSTEM: State persisted successfully.");
        } catch (IOException e) {
            System.err.println("PERSISTENCE ERROR: Save failed - " + e.getMessage());
        }
    }

    public static Object[] loadState() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            RoomInventory inventory = (RoomInventory) ois.readObject();
            BookingHistory history = (BookingHistory) ois.readObject();
            System.out.println("SYSTEM: State recovered from file.");
            return new Object[]{inventory, history};
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("PERSISTENCE ERROR: Recovery failed - " + e.getMessage());
            return null;
        }
    }
}

class RoomAllocationService {
    private Map<String, Integer> typeCounter = new HashMap<>();

    public synchronized String allocateRoom(Reservation res, RoomInventory inventory) throws BookingException {
        String type = res.getRoomType();
        int currentCount = inventory.getCount(type);
        if (currentCount <= 0) throw new BookingException("No availability for " + type);

        int nextId = typeCounter.getOrDefault(type, 0) + 1;
        typeCounter.put(type, nextId);
        String roomId = type + "-" + nextId;

        inventory.updateAvailability(type, currentCount - 1);
        res.setRoomId(roomId);
        return roomId;
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        RoomInventory inventory;
        BookingHistory history;

        Object[] recoveredState = PersistenceService.loadState();
        if (recoveredState != null) {
            inventory = (RoomInventory) recoveredState[0];
            history = (BookingHistory) recoveredState[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
            System.out.println("SYSTEM: Initialized new state.");
        }

        RoomAllocationService allocationService = new RoomAllocationService();
        Reservation newRes = new Reservation("Guest-" + System.currentTimeMillis() % 1000, "Single");

        try {
            String id = allocationService.allocateRoom(newRes, inventory);
            history.recordBooking(newRes);
            System.out.println("ALLOCATED: " + newRes.getGuestName() + " -> " + id);
        } catch (BookingException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        System.out.println("Current History Size: " + history.getHistory().size());
        PersistenceService.saveState(inventory, history);
    }
}