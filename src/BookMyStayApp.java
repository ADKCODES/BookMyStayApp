import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}

abstract class Room {
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

class RoomInventory {
    private Map<String, Integer> roomAvailability;

    public RoomInventory() {
        this.roomAvailability = new HashMap<String, Integer>();
        roomAvailability.put("Single", 5);
        roomAvailability.put("Double", 3);
        roomAvailability.put("Suite", 2);
    }

    public synchronized Map<String, Integer> getRoomAvailability() {
        return new HashMap<>(roomAvailability);
    }

    public synchronized void updateAvailability(String roomType, int count) throws BookingException {
        if (count < 0) {
            throw new BookingException("Inventory error: Cannot have negative rooms.");
        }
        roomAvailability.put(roomType, count);
    }

    public synchronized int getCount(String type) {
        return roomAvailability.getOrDefault(type, 0);
    }
}

class Reservation {
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

class BookingRequestQueue {
    private Queue<Reservation> requestQueue = new LinkedList<Reservation>();

    public synchronized void addRequest(Reservation reservation) {
        requestQueue.offer(reservation);
    }

    public synchronized Reservation getNextRequest() {
        return requestQueue.poll();
    }

    public synchronized boolean hasPendingRequests() {
        return !requestQueue.isEmpty();
    }
}

class BookingHistory {
    private List<Reservation> history = new ArrayList<Reservation>();
    public synchronized void recordBooking(Reservation reservation) { history.add(reservation); }
    public synchronized List<Reservation> getHistory() { return new ArrayList<>(history); }
}

class RoomAllocationService {
    private Map<String, Integer> typeCounter = new HashMap<String, Integer>();

    public synchronized String allocateRoom(Reservation reservation, RoomInventory inventory) throws BookingException {
        String type = reservation.getRoomType();
        int currentCount = inventory.getCount(type);

        if (currentCount <= 0) {
            throw new BookingException("No availability for " + type);
        }

        int nextId = typeCounter.getOrDefault(type, 0) + 1;
        typeCounter.put(type, nextId);
        String roomId = type + "-" + nextId;

        inventory.updateAvailability(type, currentCount - 1);
        reservation.setRoomId(roomId);
        return roomId;
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("BookMyStay App - Use Case 11: Multi-threaded Simulation\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService();
        BookingHistory history = new BookingHistory();

        queue.addRequest(new Reservation("Thread-User-1", "Single"));
        queue.addRequest(new Reservation("Thread-User-2", "Single"));
        queue.addRequest(new Reservation("Thread-User-3", "Single"));
        queue.addRequest(new Reservation("Thread-User-4", "Single"));
        queue.addRequest(new Reservation("Thread-User-5", "Single"));
        queue.addRequest(new Reservation("Thread-User-6", "Single")); // This should fail (only 5 rooms)

        Runnable processor = () -> {
            while (true) {
                Reservation request;
                synchronized (queue) {
                    if (!queue.hasPendingRequests()) break;
                    request = queue.getNextRequest();
                }

                try {
                    String id = allocationService.allocateRoom(request, inventory);
                    history.recordBooking(request);
                    System.out.println(Thread.currentThread().getName() + " SUCCESS: " + request.getGuestName() + " -> " + id);
                } catch (BookingException e) {
                    System.out.println(Thread.currentThread().getName() + " FAILED: " + e.getMessage());
                }
            }
        };

        Thread t1 = new Thread(processor, "Processor-1");
        Thread t2 = new Thread(processor, "Processor-2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nFinal Inventory: " + inventory.getRoomAvailability());
        System.out.println("Total Successful Bookings: " + history.getHistory().size());
    }
}