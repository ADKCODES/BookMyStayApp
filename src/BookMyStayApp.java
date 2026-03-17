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
            throw new BookingException("Inventory error: Cannot have negative rooms.");
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
    public void removeBooking(Reservation reservation) { history.remove(reservation); }
    public List<Reservation> getHistory() { return history; }
}

class BookingValidator {
    public void validateRequest(Reservation reservation, RoomInventory inventory) throws BookingException {
        if (reservation.getGuestName() == null || reservation.getGuestName().trim().isEmpty()) {
            throw new BookingException("Guest name cannot be empty.");
        }
        if (!inventory.getRoomAvailability().containsKey(reservation.getRoomType())) {
            throw new BookingException("Invalid room type.");
        }
    }
}

class BookingCancellationService {
    private Stack<String> releasedRoomIds = new Stack<String>();

    public void cancelBooking(Reservation reservation, RoomInventory inventory, BookingHistory history) throws BookingException {
        if (reservation.getRoomId() == null) {
            throw new BookingException("Cannot cancel a reservation without a room ID.");
        }

        String type = reservation.getRoomType();
        int currentCount = inventory.getRoomAvailability().get(type);

        releasedRoomIds.push(reservation.getRoomId());
        inventory.updateAvailability(type, currentCount + 1);
        history.removeBooking(reservation);

        System.out.println("CANCELLATION: Released " + reservation.getRoomId() + " for " + reservation.getGuestName());
    }
}

class RoomAllocationService {
    private Map<String, Integer> typeCounter = new HashMap<String, Integer>();

    public String allocateRoom(Reservation reservation, RoomInventory inventory) throws BookingException {
        String type = reservation.getRoomType();
        int currentCount = inventory.getRoomAvailability().get(type);

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
        System.out.println("BookMyStay App - Use Case 10: Cancellation\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService();
        BookingHistory history = new BookingHistory();
        BookingValidator validator = new BookingValidator();
        BookingCancellationService cancellationService = new BookingCancellationService();

        Reservation res1 = new Reservation("Abhi", "Single");
        queue.addRequest(res1);

        try {
            while (queue.hasPendingRequests()) {
                Reservation req = queue.getNextRequest();
                validator.validateRequest(req, inventory);
                String id = allocationService.allocateRoom(req, inventory);
                history.recordBooking(req);
                System.out.println("ALLOCATED: " + req.getGuestName() + " -> " + id);
            }

            System.out.println("Availability before cancellation: " + inventory.getRoomAvailability());

            cancellationService.cancelBooking(res1, inventory, history);

            System.out.println("Availability after cancellation: " + inventory.getRoomAvailability());
            System.out.println("History count: " + history.getHistory().size());

        } catch (BookingException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}