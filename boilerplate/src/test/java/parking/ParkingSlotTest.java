package parking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
public class ParkingSlotTest {
    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
    private final LocalDateTime end = start.plusHours(2);


    @Test
    @DisplayName("New slot initializes with default active state, empty bookings, and 0 balance")
    void newSlotInitializesProperly() {
        ParkingSlot slot = new ParkingSlot("S-101", ParkingSlotType.REGULAR);

        assertEquals("S-101", slot.getSlotId());
        assertEquals(ParkingSlotType.REGULAR, slot.getSlotType());
        assertTrue(slot.isActive());
        assertNotNull(slot.getWallet());
        assertNotNull(slot.getBookings());
        assertTrue(slot.getBookings().isEmpty());
        assertEquals(0.0, slot.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Activation and deactivation correctly update isActive flag")
    void slotCanBeDeactivatedAndActivated() {
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        slot.deactivate();
        assertFalse(slot.isActive());

        slot.activate();
        assertTrue(slot.isActive());
    }

    @Test
    @DisplayName("Motorcycle compatibility: COMPACT, REGULAR, LARGE allowed; others rejected")
    void motorcycleCompatibilityMatrix() {
        assertTrue(new ParkingSlot("C", ParkingSlotType.COMPACT).isCompatible(VehicleType.MOTORCYCLE, start, end));
        assertTrue(new ParkingSlot("R", ParkingSlotType.REGULAR).isCompatible(VehicleType.MOTORCYCLE, start, end));
        assertTrue(new ParkingSlot("L", ParkingSlotType.LARGE).isCompatible(VehicleType.MOTORCYCLE, start, end));
        assertFalse(new ParkingSlot("H", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.MOTORCYCLE, start, end));
    }

    @Test
    @DisplayName("Car compatibility: REGULAR and LARGE allowed; others rejected")
    void carCompatibilityMatrix() {
        assertFalse(new ParkingSlot("C", ParkingSlotType.COMPACT).isCompatible(VehicleType.CAR, start, end));
        assertTrue(new ParkingSlot("R", ParkingSlotType.REGULAR).isCompatible(VehicleType.CAR, start, end));
        assertTrue(new ParkingSlot("L", ParkingSlotType.LARGE).isCompatible(VehicleType.CAR, start, end));
        assertFalse(new ParkingSlot("H", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.CAR, start, end));
    }

    @Test
    @DisplayName("Bus compatibility: LARGE allowed; all others rejected")
    void busCompatibilityMatrix() {
        assertFalse(new ParkingSlot("C", ParkingSlotType.COMPACT).isCompatible(VehicleType.BUS, start, end));
        assertFalse(new ParkingSlot("R", ParkingSlotType.REGULAR).isCompatible(VehicleType.BUS, start, end));
        assertTrue(new ParkingSlot("L", ParkingSlotType.LARGE).isCompatible(VehicleType.BUS, start, end));
        assertFalse(new ParkingSlot("H", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.BUS, start, end));
    }

    @Test
    @DisplayName("Bicycle compatibility: COMPACT, REGULAR, LARGE, and HANDICAPPED allowed")
    void bicycleCompatibilityMatrix() {
        assertTrue(new ParkingSlot("C", ParkingSlotType.COMPACT).isCompatible(VehicleType.BICYCLE, start, end));
        assertTrue(new ParkingSlot("R", ParkingSlotType.REGULAR).isCompatible(VehicleType.BICYCLE, start, end));
        assertTrue(new ParkingSlot("L", ParkingSlotType.LARGE).isCompatible(VehicleType.BICYCLE, start, end));
        assertTrue(new ParkingSlot("H", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.BICYCLE, start, end));
    }

    @Test
    @DisplayName("Kills 'replaced boolean return with true': verifies compatible slots return false when already booked")
    void compatibleSlotReturnsFalseWhenUnavailable() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime end = start.plusHours(2);

        // Helper to create an active slot that already has an overlapping booking
        java.util.function.Function<ParkingSlotType, ParkingSlot> createBookedSlot = (type) -> {
            ParkingSlot slot = new ParkingSlot("S1", type);
            Vehicle placeholderVehicle = new Vehicle(99, VehicleType.CAR, 100.0);
            slot.getBookings().add(new Booking(99, placeholderVehicle, slot, start, end, 20.0));
            return slot;
        };

        // MOTORCYCLE on COMPACT, REGULAR, LARGE (kills line for MOTORCYCLE)
        assertFalse(createBookedSlot.apply(ParkingSlotType.COMPACT).isCompatible(VehicleType.MOTORCYCLE, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.REGULAR).isCompatible(VehicleType.MOTORCYCLE, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.LARGE).isCompatible(VehicleType.MOTORCYCLE, start, end));

        // CAR on REGULAR, LARGE (kills line for CAR)
        assertFalse(createBookedSlot.apply(ParkingSlotType.REGULAR).isCompatible(VehicleType.CAR, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.LARGE).isCompatible(VehicleType.CAR, start, end));

        // BUS on LARGE (kills line for BUS)
        assertFalse(createBookedSlot.apply(ParkingSlotType.LARGE).isCompatible(VehicleType.BUS, start, end));

        // BICYCLE on COMPACT, REGULAR, LARGE, HANDICAPPED (kills line for BICYCLE)
        assertFalse(createBookedSlot.apply(ParkingSlotType.COMPACT).isCompatible(VehicleType.BICYCLE, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.REGULAR).isCompatible(VehicleType.BICYCLE, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.LARGE).isCompatible(VehicleType.BICYCLE, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.BICYCLE, start, end));

        // MICROCAR on COMPACT, REGULAR (kills line for MICROCAR)
        assertFalse(createBookedSlot.apply(ParkingSlotType.COMPACT).isCompatible(VehicleType.MICROCAR, start, end));
        assertFalse(createBookedSlot.apply(ParkingSlotType.REGULAR).isCompatible(VehicleType.MICROCAR, start, end));
    }

    @Test
    @DisplayName("Microcar compatibility: COMPACT and REGULAR allowed; others rejected")
    void microcarCompatibilityMatrix() {
        assertTrue(new ParkingSlot("C", ParkingSlotType.COMPACT).isCompatible(VehicleType.MICROCAR, start, end));
        assertTrue(new ParkingSlot("R", ParkingSlotType.REGULAR).isCompatible(VehicleType.MICROCAR, start, end));
        assertFalse(new ParkingSlot("L", ParkingSlotType.LARGE).isCompatible(VehicleType.MICROCAR, start, end));
        assertFalse(new ParkingSlot("H", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.MICROCAR, start, end));
    }

    @Test
    @DisplayName("Truck compatibility: rejects all slot types in switch default branch")
    void truckCompatibilityMatrix() {
        assertFalse(new ParkingSlot("C", ParkingSlotType.COMPACT).isCompatible(VehicleType.TRUCK, start, end));
        assertFalse(new ParkingSlot("R", ParkingSlotType.REGULAR).isCompatible(VehicleType.TRUCK, start, end));
        assertFalse(new ParkingSlot("L", ParkingSlotType.LARGE).isCompatible(VehicleType.TRUCK, start, end));
        assertFalse(new ParkingSlot("H", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.TRUCK, start, end));
    }

    @Test
    @DisplayName("Inactive slot immediately returns false for compatibility check")
    void inactiveSlotIsNotCompatibleEvenIfTypeMatches() {
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        slot.deactivate();

        assertFalse(slot.isCompatible(VehicleType.CAR, start, end));
    }

    // ==========================================
    // Availability & Boundary Interval Tests
    // ==========================================

    @Test
    @DisplayName("Slot with no bookings is available")
    void emptySlotIsAvailable() {
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        assertTrue(slot.isAvailable(start, end));
    }

    @Test
    @DisplayName("Overlapping booking windows make slot unavailable (kills boundary mutants)")
    void overlappingBookingBlocksSlot() {
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        Booking booking = new Booking(1, vehicle, slot, start, end, 20.0);
        slot.getBookings().add(booking);

        // Subsumed completely inside existing booking
        assertFalse(slot.isAvailable(start.plusMinutes(15), end.minusMinutes(15)));

        // Overlapping start boundary (requested starts before and ends inside)
        assertFalse(slot.isAvailable(start.minusMinutes(30), start.plusMinutes(30)));

        // Overlapping end boundary (requested starts inside and ends after)
        assertFalse(slot.isAvailable(end.minusMinutes(30), end.plusMinutes(30)));

        // requested completely encloses existing
        assertFalse(slot.isAvailable(start.minusHours(1), end.plusHours(1)));
    }

    @Test
    @DisplayName("Consecutive bookings touching at exact boundaries are allowed")
    void adjacentBookingsDoNotOverlap() {
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        Booking booking = new Booking(1, vehicle, slot, start, end, 20.0);
        slot.getBookings().add(booking);

        // Requested window ends exactly when existing starts
        assertTrue(slot.isAvailable(start.minusHours(2), start));

        // Requested window starts exactly when existing ends
        assertTrue(slot.isAvailable(end, end.plusHours(2)));
    }

}
