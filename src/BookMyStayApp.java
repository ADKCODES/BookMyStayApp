import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

abstract class Room {
    protected int numberOfBeds;
    protected int squareFeet;
    protected double pricePerNight;

    public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
        this.numberOfBeds = numberOfBeds;
        this.squareFeet = squareFeet;
        this.pricePerNight = pricePerNight;
    }

    public void displayRoomDetails() {
        System.out.println("Beds: " + numberOfBeds);
        System.out.println("Size: " + squareFeet + " sqft");
        System.out.println("Price per night: " + pricePerNight);
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

    public Map<String, Integer> getRoomAvailability() {
        return roomAvailability;
    }

    public void updateAvailability(String roomType, int count) {
        roomAvailability.put(roomType, count);
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
    public void addRequest(Reservation reservation) { requestQueue.offer(reservation); }
    public Reservation getNextRequest() { return requestQueue.poll(); }
    public boolean hasPendingRequests() { return !requestQueue.isEmpty(); }
}

class BookingHistory {
    private List<Reservation> history = new ArrayList<Reservation>();

    public void recordBooking(Reservation reservation) {
        history.add(reservation);
    }

    public List<Reservation> getHistory() {
        return history;
    }
}

class BookingReportService {
    public void generateSummaryReport(BookingHistory bookingHistory) {
        List<Reservation> history = bookingHistory.getHistory();
        System.out.println("--- Final Booking Report ---");
        System.out.println("Total Bookings Processed: " + history.size());
        for (Reservation res : history) {
            System.out.println("Guest: " + res.getGuestName() + " | Room: " + res.getRoomId());
        }
        System.out.println("----------------------------");
    }
}

class RoomAllocationService {
    private Set<String> allocatedRoomIds = new HashSet<String>();
    private Map<String, Set<String>> assignedRoomsByType = new HashMap<String, Set<String>>();

    public String allocateRoom(Reservation reservation, RoomInventory inventory) {
        String type = reservation.getRoomType();
        int currentCount = inventory.getRoomAvailability().getOrDefault(type, 0);

        if (currentCount > 0) {
            String roomId = generateRoomId(type);
            allocatedRoomIds.add(roomId);
            assignedRoomsByType.putIfAbsent(type, new HashSet<String>());
            assignedRoomsByType.get(type).add(roomId);
            inventory.updateAvailability(type, currentCount - 1);
            reservation.setRoomId(roomId);
            return roomId;
        }
        return null;
    }

    private String generateRoomId(String roomType) {
        int nextId = assignedRoomsByType.getOrDefault(roomType, new HashSet<String>()).size() + 1;
        return roomType + "-" + nextId;
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Welcome to the Hotel Booking Management System");
        System.out.println("System initialized successfully.");
        System.out.println("----------------------------------------------");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService();
        BookingHistory bookingHistory = new BookingHistory();
        BookingReportService reportService = new BookingReportService();

        queue.addRequest(new Reservation("Abhi", "Single"));
        queue.addRequest(new Reservation("Subha", "Double"));
        queue.addRequest(new Reservation("Vanmathi", "Suite"));

        while (queue.hasPendingRequests()) {
            Reservation request = queue.getNextRequest();
            String roomId = allocationService.allocateRoom(request, inventory);

            if (roomId != null) {
                bookingHistory.recordBooking(request);
                System.out.println("Allocation Success: " + request.getGuestName() + " assigned " + roomId);
            }
        }

        System.out.println();
        reportService.generateSummaryReport(bookingHistory);
    }
}