package parking;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
public class BookingTest {
    private final LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 10, 0);
    private final LocalDateTime endTime = startTime.plusHours(2);
    private Booking createStandardBooking() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        return new Booking(101, vehicle, slot, startTime, endTime, 25.50);
    }

    @Test
    void constructor_Initializes_AllFields_And_Sets_Active_Status() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        LocalDateTime start = LocalDateTime.of(2026, 5, 10, 8, 30);
        LocalDateTime end = start.plusHours(3);
        double amount = 45.0;
        Booking booking = new Booking(12, vehicle, slot, start, end, amount);
        assertEquals(12, booking.getBookingId());
        assertSame(vehicle, booking.getVehicle());
        assertSame(slot, booking.getParkingSlot());
        assertEquals(start, booking.getStartTime());
        assertEquals(end, booking.getEndTime());
        assertEquals(amount, booking.getAmount(), 0.001);
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
    }

    @Test
    void constructor_Accepts_ZeroId_And_ZeroAmount() {
        Vehicle vehicle = new Vehicle(2, VehicleType.MOTORCYCLE, 50.0);
        ParkingSlot slot = new ParkingSlot("C1", ParkingSlotType.COMPACT);
        Booking booking = new Booking(0, vehicle, slot, startTime, endTime, 0.0);
        assertEquals(0, booking.getBookingId());
        assertEquals(0.0, booking.getAmount(), 0.001);
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
    }

    @Test
    void complete_Booking_Transitions_Status_To_Completed() {
        Booking booking = createStandardBooking();
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
        booking.completeBooking();
        assertEquals(BookingStatus.COMPLETED, booking.getBookingStatus());
    }

    @Test
    void cancel_Booking_Transitions_Status_To_Cancelled() {
        Booking booking = createStandardBooking();
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
        booking.cancelBooking();
        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
    }

    @Test
    void toString_Returns_Exact_Formatted_String() {
        Vehicle vehicle = new Vehicle(1, VehicleType.CAR, 100.0);
        ParkingSlot slot = new ParkingSlot("R1", ParkingSlotType.REGULAR);
        Booking booking = new Booking(101, vehicle, slot, startTime, endTime, 25.5);

        String expectedInitial = "Booking{" +
                "bookingId=101" +
                ", vehicle=" + vehicle.toString() +
                ", parkingSlot=" + slot.toString() +
                ", startTime=" + startTime.toString() +
                ", endTime=" + endTime.toString() +
                ", amount=25.5" +
                ", bookingStatus=ACTIVE}";

        assertEquals(expectedInitial, booking.toString());
        booking.completeBooking();
        assertTrue(booking.toString().contains("bookingStatus=COMPLETED"));
        booking.cancelBooking();
        assertTrue(booking.toString().contains("bookingStatus=CANCELLED"));
    }

    @Test
    void observesBookingAcceptsNullReferencesWithoutValidation() {
        Booking booking = new Booking(-1, null, null, null, null, -50.0);
        assertEquals(-1, booking.getBookingId());
        assertNull(booking.getVehicle());
        assertNull(booking.getParkingSlot());
        assertNull(booking.getStartTime());
        assertNull(booking.getEndTime());
        assertEquals(-50.0, booking.getAmount(), 0.001);
    }

    @Test
    void observesDirectTransitionFromCompletedToCancelledAllowed() {
        Booking booking = createStandardBooking();
        booking.completeBooking();
        assertEquals(BookingStatus.COMPLETED, booking.getBookingStatus());
        booking.cancelBooking();
        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
    }


}
