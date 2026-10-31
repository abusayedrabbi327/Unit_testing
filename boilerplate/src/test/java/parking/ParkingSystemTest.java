package parking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class ParkingSystemTest {

    private ParkingSystem system;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        system = ParkingSystem.getInstance();
        system.resetForTesting();
        start = LocalDateTime.of(2026, 1, 1, 10, 0);
        end = start.plusHours(2); // 2 hours
    }

    @Test
    @DisplayName("getInstance returns singleton instance")
    void getInstanceReturnsSingleton() {
        assertSame(system, ParkingSystem.getInstance());
    }

    @Test
    @DisplayName("resetForTesting restores initial state, rates, and resets collections")
    void resetForTestingRestoresDefaults() {
        system.addVehicle(new Vehicle(1, VehicleType.CAR, 100.0));
        system.addParkingSlot(new ParkingSlot("R1", ParkingSlotType.REGULAR));
        system.setPARKING_RATE_PER_HOUR(50.0);
        system.getSYSTEM_WALLET().addFunds(100.0);

        system.resetForTesting();

        assertTrue(system.getVehicles().isEmpty());
        assertTrue(system.getParkingSlots().isEmpty());
        assertTrue(system.getBookings().isEmpty());
        assertEquals(10.0, system.getPARKING_RATE_PER_HOUR(), 0.001);
        assertEquals(0.0, system.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Collection setters and getters function as expected")
    void collectionSettersAndGetters() {
        List<Vehicle> vehicles = new ArrayList<>(List.of(new Vehicle(1, VehicleType.CAR, 50.0)));
        List<ParkingSlot> slots = new ArrayList<>(List.of(new ParkingSlot("R1", ParkingSlotType.REGULAR)));
        List<Booking> bookings = new ArrayList<>();
        Wallet wallet = new Wallet(200.0);

        system.setVehicles(vehicles);
        system.setParkingSlots(slots);
        system.setBookings(bookings);
        system.setSYSTEM_WALLET(wallet);

        assertSame(vehicles, system.getVehicles());
        assertSame(slots, system.getParkingSlots());
        assertSame(bookings, system.getBookings());
        assertSame(wallet, system.getSYSTEM_WALLET());
        assertEquals(200.0, system.getBalance(), 0.001);
    }

    @Test
    @DisplayName("getAvailableParkingSlots filters out incompatible, unavailable, or inactive slots")
    void getAvailableParkingSlotsFiltersCorrectly() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot regular = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        ParkingSlot compact = new ParkingSlot("C1", ParkingSlotType.COMPACT);
        ParkingSlot inactiveRegular = new ParkingSlot("R2", ParkingSlotType.REGULAR);
        inactiveRegular.deactivate();

        system.addParkingSlot(regular);
        system.addParkingSlot(compact);
        system.addParkingSlot(inactiveRegular);

        List<ParkingSlot> available = system.getAvailableParkingSlots(car, start, end);

        assertEquals(1, available.size());
        assertTrue(available.contains(regular));
        assertFalse(available.contains(compact));
        assertFalse(available.contains(inactiveRegular));
    }

    @Test
    @DisplayName("book transfers 100% funds from vehicle to system escrow and registers booking")
    void bookCreatesActiveBookingAndChargesVehicle() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        system.addVehicle(vehicle);
        system.addParkingSlot(slot);

        // 2 hours * base rate (10.0) * CAR (1.0) * REGULAR (1.0) = 20.0
        Booking booking = system.book(vehicle, slot, start, end);

        assertNotNull(booking);
        assertEquals(1, booking.getBookingId());
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
        assertEquals(20.0, booking.getAmount(), 0.001);
        assertEquals(80.0, vehicle.getBalance(), 0.001);
        assertEquals(20.0, system.getBalance(), 0.001);
        assertTrue(system.getBookings().contains(booking));
        assertTrue(slot.getBookings().contains(booking));
    }

    @Test
    @DisplayName("completeBooking transfers 80% to slot wallet and leaves 20% in system wallet")
    void completeBookingTransfersEightyPercentToSlot() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking booking = system.book(vehicle, slot, start, end); // amount = 20.0

        system.completeBooking(booking);

        assertEquals(BookingStatus.COMPLETED, booking.getBookingStatus());
        // 80% of 20 = 16.0 to slot wallet
        assertEquals(16.0, slot.getBalance(), 0.001);
        // 20% retained = 4.0 in system wallet
        assertEquals(4.0, system.getBalance(), 0.001);
    }

    @Test
    @DisplayName("cancelBooking refunds 90% to vehicle and leaves 10% in system wallet")
    void cancelBookingRefundsNinetyPercentToVehicle() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking booking = system.book(vehicle, slot, start, end); // amount = 20.0

        system.cancelBooking(booking);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        // 90% of 20 = 18.0 refunded; balance 80 + 18 = 98.0
        assertEquals(98.0, vehicle.getBalance(), 0.001);
        // 10% retained = 2.0 in system wallet
        assertEquals(2.0, system.getBalance(), 0.001);
    }

    // ==========================================
    // Multipliers & Math Mutation Killers
    // ==========================================

    @Test
    @DisplayName("Verifies rates for all vehicle types (kills math & switch mutations)")
    void verifiesAllVehicleTypeRates() {
        ParkingSlot regularSlot = new ParkingSlot("R", ParkingSlotType.REGULAR);
        ParkingSlot largeSlot = new ParkingSlot("L", ParkingSlotType.LARGE);

        // BICYCLE (0.2) on REGULAR (1.0) -> 2h * 10 * 0.2 * 1.0 = 4.0
        assertEquals(4.0, system.book(new Vehicle(1, VehicleType.BICYCLE, 50.0), regularSlot, start, end).getAmount(), 0.001);

        // MOTORCYCLE (0.5) on REGULAR (1.0) -> 2h * 10 * 0.5 * 1.0 = 10.0
        assertEquals(10.0, system.book(new Vehicle(2, VehicleType.MOTORCYCLE, 50.0), regularSlot, start.plusDays(1), end.plusDays(1)).getAmount(), 0.001);

        // MICROCAR (1.5) on REGULAR (1.0) -> 2h * 10 * 1.5 * 1.0 = 30.0
        assertEquals(30.0, system.book(new Vehicle(3, VehicleType.MICROCAR, 50.0), regularSlot, start.plusDays(2), end.plusDays(2)).getAmount(), 0.001);

        // BUS (2.0) on LARGE (1.5) -> 2h * 10 * 2.0 * 1.5 = 60.0
        assertEquals(60.0, system.book(new Vehicle(4, VehicleType.BUS, 100.0), largeSlot, start, end).getAmount(), 0.001);
    }

    @Test
    @DisplayName("Verifies slot type multipliers for COMPACT and HANDICAPPED (kills multiplier mutants)")
    void verifiesSlotMultipliers() {
        // COMPACT multiplier (0.8) with MOTORCYCLE (0.5) -> 2h * 10 * 0.5 * 0.8 = 8.0
        ParkingSlot compactSlot = new ParkingSlot("C1", ParkingSlotType.COMPACT);
        Booking compactBooking = system.book(new Vehicle(10, VehicleType.MOTORCYCLE, 50.0), compactSlot, start, end);
        assertEquals(8.0, compactBooking.getAmount(), 0.001);

        // HANDICAPPED multiplier (1.2) with BICYCLE (0.2) -> 2h * 10 * 0.2 * 1.2 = 4.8
        ParkingSlot handicappedSlot = new ParkingSlot("H1", ParkingSlotType.HANDICAPPED);
        Booking handiBooking = system.book(new Vehicle(11, VehicleType.BICYCLE, 50.0), handicappedSlot, start, end);
        assertEquals(4.8, handiBooking.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Custom hourly rate is used properly in price calculation")
    void customHourlyRateIsApplied() {
        system.setPARKING_RATE_PER_HOUR(25.0);
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 200.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        // 2 hours * 25.0 * 1.0 * 1.0 = 50.0
        Booking booking = system.book(vehicle, slot, start, end);
        assertEquals(50.0, booking.getAmount(), 0.001);
    }

    // ==========================================
    // Exception & Validation Handling
    // ==========================================

    @Test
    @DisplayName("Throws IllegalBookingTimeException when end time is before start time")
    void rejectsEndTimeBeforeStartTime() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        assertThrows(IllegalBookingTimeException.class,
                () -> system.book(vehicle, slot, start, start.minusHours(1)));
    }

    @Test
    @DisplayName("Throws IllegalBookingTimeException when end time equals start time")
    void rejectsEqualStartAndEndTime() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        assertThrows(IllegalBookingTimeException.class,
                () -> system.book(vehicle, slot, start, start));
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when slot is incompatible or unavailable")
    void rejectsIncompatibleOrUnavailableSlot() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot compactSlot = new ParkingSlot("C1", ParkingSlotType.COMPACT);

        // Incompatible slot type
        assertThrows(IllegalArgumentException.class,
                () -> system.book(car, compactSlot, start, end));

        // Overlapping slot reservation
        ParkingSlot regularSlot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        system.book(car, regularSlot, start, end);

        Vehicle secondCar = new Vehicle(2, VehicleType.CAR, 100.0);
        assertThrows(IllegalArgumentException.class,
                () -> system.book(secondCar, regularSlot, start.plusMinutes(30), end.plusMinutes(30)));
    }

    @Test
    @DisplayName("Throws InsufficientFundsException when vehicle wallet cannot cover parking fee")
    void rejectsInsufficientFunds() {
        Vehicle poorVehicle = new Vehicle(1, VehicleType.CAR, 5.0); // Cost is 20.0
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        assertThrows(InsufficientFundsException.class,
                () -> system.book(poorVehicle, slot, start, end));
    }
    @Test
    void truckRateReturnsThreeViaReflection() throws Exception {
        Method method = ParkingSystem.class.getDeclaredMethod("getVehicleTypeRate", VehicleType.class);
        method.setAccessible(true);

        double rate = (double) method.invoke(system, VehicleType.TRUCK);

        // Kills 'replaced double return with 0.0d'
        assertEquals(3.0, rate, 0.001);
    }

    // ==========================================
    // Defect Probes (Passing assertions documenting real behavior)
    // ==========================================

    @Test
    @DisplayName("Documents defect: Duration.toHours() truncates partial hours instead of prorating or rounding up")
    void observesPartialHourTruncationDefect() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);

        // 90 minutes is 1.5 hours. Expected fee: 15.0; Actual fee due to toHours() truncation: 10.0
        Booking booking = system.book(vehicle, slot, start, start.plusMinutes(90));
        assertEquals(10.0, booking.getAmount(), 0.001);
    }
}

