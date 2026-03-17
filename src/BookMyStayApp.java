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

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

class BookingRequestQueue {
    private Queue<Reservation> requestQueue = new LinkedList<Reservation>();
    public void addRequest(Reservation reservation) { requestQueue.offer(reservation); }
    public Reservation getNextRequest() { return requestQueue.poll(); }
    public boolean hasPendingRequests() { return !requestQueue.isEmpty(); }
}

class AddOnService {
    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
}

class AddOnServiceManager {
    private Map<String, List<AddOnService>> reservationAddOns = new HashMap<String, List<AddOnService>>();

    public void addService(String roomId, AddOnService service) {
        reservationAddOns.putIfAbsent(roomId, new ArrayList<AddOnService>());
        reservationAddOns.get(roomId).add(service);
    }

    public double calculateTotalAddOnCost(String roomId) {
        double total = 0;
        if (reservationAddOns.containsKey(roomId)) {
            for (AddOnService service : reservationAddOns.get(roomId)) {
                total += service.getCost();
            }
        }
        return total;
    }

    public void displayServices(String roomId) {
        if (reservationAddOns.containsKey(roomId)) {
            for (AddOnService service : reservationAddOns.get(roomId)) {
                System.out.println("- " + service.getServiceName() + ": " + service.getCost());
            }
        }
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
        System.out.println("BookMyStay App - Use Case 7\n");

        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        RoomAllocationService allocationService = new RoomAllocationService();
        AddOnServiceManager addOnManager = new AddOnServiceManager();

        Reservation res1 = new Reservation("Abhi", "Single");
        queue.addRequest(res1);

        while (queue.hasPendingRequests()) {
            Reservation request = queue.getNextRequest();
            String roomId = allocationService.allocateRoom(request, inventory);

            if (roomId != null) {
                System.out.println("Confirmed: " + request.getGuestName() + " -> " + roomId);

                addOnManager.addService(roomId, new AddOnService("Breakfast", 250.0));
                addOnManager.addService(roomId, new AddOnService("WiFi", 100.0));

                System.out.println("Add-on Services for " + roomId + ":");
                addOnManager.displayServices(roomId);
                System.out.println("Total Add-on Cost: " + addOnManager.calculateTotalAddOnCost(roomId) + "\n");
            }
        }
    }
}