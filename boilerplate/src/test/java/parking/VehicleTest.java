package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class VehicleTest {
    @Test
    void constructor_With_Wallet_Stores_Vehicle_Details() {
        Wallet wallet = new Wallet(75.0);
        Vehicle vehicle = new Vehicle(7, VehicleType.CAR, wallet);

        assertEquals(7, vehicle.getVehicleId(), "Vehicle ID should match the supplied ID");
        assertEquals(VehicleType.CAR, vehicle.getVehicleType(), "VehicleType should match CAR");
        assertSame(wallet, vehicle.getWallet(), "getWallet() must return the exact Wallet instance passed");
        assertEquals(75.0, vehicle.getBalance(), 0.001, "getBalance() should reflect the wallet balance");
    }
    @Test
    void constructor_With_Double_Balance_Initializes_Wallet() {
        Vehicle vehicle = new Vehicle(8, VehicleType.BUS, 200.0);

        assertEquals(8, vehicle.getVehicleId());
        assertEquals(VehicleType.BUS, vehicle.getVehicleType());
        assertNotNull(vehicle.getWallet(), "Wallet should be initialized internally");
        assertEquals(200.0, vehicle.getBalance(), 0.001);
    }
    @Test
    void constructor_With_Zero_Balance_Initializes_Correctly() {
        Vehicle vehicle = new Vehicle(101, VehicleType.MOTORCYCLE, 0.0);

        assertEquals(101, vehicle.getVehicleId());
        assertEquals(VehicleType.MOTORCYCLE, vehicle.getVehicleType());
        assertEquals(0.0, vehicle.getBalance(), 0.001);
    }
    @Test
    void supports_All_Vehicle_Types() {
        for (VehicleType type : VehicleType.values()) {
            Vehicle vehicle = new Vehicle(1, type, 50.0);
            assertEquals(type, vehicle.getVehicleType());
        }
    }

    @Test
    void get_Balance_Reflects_Underlying_Wallet_Mutations() {
        Wallet wallet = new Wallet(50.0);
        Vehicle vehicle = new Vehicle(9, VehicleType.BICYCLE, wallet);
        wallet.addFunds(25.0);
        assertEquals(75.0, vehicle.getBalance(), 0.001);
        wallet.deductFunds(15.0);
        assertEquals(60.0, vehicle.getBalance(), 0.001);
    }

    @Test
    void to_String_Returns_Exact_Formatted_String() {
        Vehicle vehicle = new Vehicle(10, VehicleType.MOTORCYCLE, 30.0);
        String expected = "Vehicle{vehicleId=10, vehicleType=MOTORCYCLE, walletBalance=30.0}";
        assertEquals(expected, vehicle.toString(), "toString output must match the expected template exactly");
    }
    @Test
    void to_String_Reflects_Updated_Balance() {
        Vehicle vehicle = new Vehicle(42, VehicleType.TRUCK, 100.0);
        vehicle.getWallet().addFunds(50.0);
        String expected = "Vehicle{vehicleId=42, vehicleType=TRUCK, walletBalance=150.0}";
        assertEquals(expected, vehicle.toString());
    }

    @Test
    void constructor_Accepts_NullWallet_Throws_NpeOnGet_Balance() {
        Vehicle vehicle = new Vehicle(99, VehicleType.CAR, (Wallet) null);
        assertNull(vehicle.getWallet());
        assertThrows(NullPointerException.class, vehicle::getBalance);
    }
    @Test
    void allows_Negative_VehicleId() {
        Vehicle vehicle = new Vehicle(-5, VehicleType.MICROCAR, 50.0);
        assertEquals(-5, vehicle.getVehicleId());
    }
    @Test
    void allows_Negative_Initial_Balance() {
        Vehicle vehicle = new Vehicle(12, VehicleType.CAR, -50.0);
        assertEquals(-50.0, vehicle.getBalance(), 0.001);
    }


}
