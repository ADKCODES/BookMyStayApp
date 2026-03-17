import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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

    public void updateAvailability(String roomType, int count) throws BookingException {
        if (count < 0) {
            throw new BookingException("Inventory error: Cannot have negative rooms for " + roomType);
        }
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
    public void recordBooking(Reservation reservation) { history.add(reservation); }
    public List<Reservation> getHistory() { return history; }
}

class BookingValidator {
    public void validateRequest(Reservation reservation, RoomInventory inventory) throws BookingException {
        if (reservation.getGuestName() == null || reservation.getGuestName().trim().isEmpty()) {
            throw new BookingException("Validation Failed: Guest name cannot be empty.");
        }
        if (!inventory.getRoomAvailability().containsKey(reservation.getRoomType())) {
            throw new BookingException("Validation Failed: Invalid room type '" + reservation.getRoomType() + "'.");
        }
        if (inventory.getRoomAvailability().get(reservation.getRoomType()) <= 0) {
            throw new BookingException("Availability Failed: No " + reservation.getRoomType() + " rooms left.");
        }
    }
}

class RoomAllocationService {
    private Set<String> allocatedRoomIds = new HashSet<String>();
    private Map<String, Set<String>> assignedRoomsByType = new HashMap<String, Set<String>>();

    public String allocateRoom(Reservation reservation, RoomInventory inventory) throws BookingException {
        String type = reservation.getRoomType();
        int currentCount = inventory.getRoomAvailability().get(type);

        String roomId = type + "-" + (assignedRoomsByType.getOrDefault(type, new HashSet<String>()).size() + 1);
        allocatedRoomIds.add(roomId);
        assignedRoomsByType.putIfAbsent(type, new HashSet<String>());
        assignedRoomsByType.get(type).add(roomId);

        inventory.updateAvailability(type, currentCount - 1);
        reservation.setRoomId(roomId);
        return roomId;
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("BookMyStay App - Use Case 9: Error Handling\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService();
        BookingHistory bookingHistory = new BookingHistory();
        BookingValidator validator = new BookingValidator();

        queue.addRequest(new Reservation("Abhi", "Single"));
        queue.addRequest(new Reservation("", "Double")); // Invalid Name
        queue.addRequest(new Reservation("Subha", "Penthouse")); // Invalid Type
        queue.addRequest(new Reservation("Vanmathi", "Suite"));

        while (queue.hasPendingRequests()) {
            Reservation request = queue.getNextRequest();
            try {
                validator.validateRequest(request, inventory);
                String roomId = allocationService.allocateRoom(request, inventory);
                bookingHistory.recordBooking(request);
                System.out.println("SUCCESS: " + request.getGuestName() + " assigned " + roomId);
            } catch (BookingException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        System.out.println("\nFinal Verified History Size: " + bookingHistory.getHistory().size());
    }
}