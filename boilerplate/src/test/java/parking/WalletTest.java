package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WalletTest {
    @Test
    void default_Wallet_Starts_At_Zero_Balance() {
        Wallet wallet = new Wallet();
        assertEquals(0.0, wallet.getBalance(), 0.001);
    }

    @Test
    void wallet_Can_Start_With_Initial_Balance() {
        Wallet wallet = new Wallet(100.00);
        assertEquals(100.0, wallet.getBalance(), 0.001);
    }

    @Test
    void add_Funds_Increases_Balance() {
        Wallet wallet = new Wallet(100.00);
        wallet.addFunds(50.0);
        assertEquals(150.0, wallet.getBalance(), 0.001);

    }

    @Test
    void add_Fund_Rejects_Zero() {
        Wallet wallet = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> wallet.addFunds(0.0));
    }

    @Test
    void add_Fund_Reject_Negative_Amount() {
        Wallet wallet = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> wallet.addFunds(-10.0));
    }

    @Test
    void deduct_Funds_Decreases_Balance() {
        Wallet wallet = new Wallet(100.0);
        wallet.deductFunds(30.0);
        assertEquals(70.0, wallet.getBalance(), 0.001);
    }

    @Test
    void deduct_Funds_Rejects_Negative_Amount() {
        Wallet wallet = new Wallet(1000.0);
        assertThrows(InvalidAmountException.class, () -> wallet.deductFunds(-100.0));
    }

    @Test
    void deduct_Funds_Rejects_Zero_Amount() {
        Wallet wallet = new Wallet(1000.0);
        assertThrows(InvalidAmountException.class, () -> wallet.deductFunds(0.0));
    }

    @Test
    void deduct_Funds_Rejects_Insufficient_Balance() {
        Wallet wallet = new Wallet(2000.0);
        assertThrows(InsufficientFundsException.class, () -> wallet.deductFunds(2100.0));
    }

    @Test
    void deduct_Funds_Allows_Exact_Available_Balance() {
        Wallet wallet = new Wallet(100.0);
        wallet.deductFunds(100.0);
        assertEquals(0.0, wallet.getBalance(), 0.01);
    }

    @Test
    void transfer_Money_Between_Wallets() {
        Wallet source = new Wallet(100.0);
        Wallet des = new Wallet(10.0);
        source.transferFunds(des, 40.0);
        assertEquals(60.0, source.getBalance(), 0.001);
        assertEquals(50.0, des.getBalance(), 0.001);
    }

    @Test
    void transfer_Funds_more_than_amount() {
        Wallet wallet = new Wallet(100.0);
        Wallet des = new Wallet(10.0);
        assertThrows(InsufficientFundsException.class, () -> wallet.transferFunds(des, 200.0));
    }

    @Test
    void transfer_Funds_Rejects_Zero_amount() {
        Wallet source = new Wallet(100.0);
        Wallet des = new Wallet(10.0);
        assertThrows(InvalidAmountException.class, () -> source.transferFunds(des, 0.0));

    }

    @Test
    void transfer_Funds_Rejects_Zero_On_Negative_Balance_Wallet() {
        Wallet wallet = new Wallet(-100.0);
        Wallet des = new Wallet(10.0);
        assertThrows(InvalidAmountException.class, () -> wallet.transferFunds(des, 0.0));
    }

    @Test
    void transfer_Funds_Allows_Exact_balance_Transfer() {
        Wallet wallet = new Wallet(100.0);
        Wallet des = new Wallet(10.0);
        wallet.transferFunds(des, 100.0);
        assertEquals(0.0, wallet.getBalance(), 0.001);
        assertEquals(110.0, des.getBalance(), 0.001);
    }

//    // Negative initial balance
//    @Test
//    void rejects_negative_init_balance() {
//        assertThrows(InvalidAmountException.class, () -> new Wallet(-100.0));
//    }
//
//    // Infinity amount should say error
//    @Test
//    void add_Funds_infinite_amount() {
//        Wallet wallet = new Wallet(100.0);
//        assertThrows(InvalidAmountException.class, () -> wallet.addFunds(Double.POSITIVE_INFINITY));
//    }
//    @Test
//    void in_transfer_Null_Rejects() {
//        Wallet wallet = new Wallet(10.0);
//        assertThrows(IllegalArgumentException.class, () -> wallet.transferFunds(null, 50.0));
//    }
//    @Test
//    void transferFunds_ToNullWallet_DoesNotDeductMoneyFromSender() {
//        Wallet sender = new Wallet(100.0);
//        try {
//            sender.transferFunds(null, 40.0);
//        } catch (Exception ignored) {
//
//        }
//        assertEquals(100.0, sender.getBalance(), 0.001);
//    }
//    @Test
//    void transferFunds_ToSelf_ShouldBeRejected() {
//        Wallet wallet = new Wallet(100.0);
//        assertThrows(IllegalArgumentException.class, () -> wallet.transferFunds(wallet, 50.0));
//    }
//    @Test
//    void transferFunds_WithNullTargetAndInsufficientFunds_RejectsNullFirst() {
//        Wallet wallet = new Wallet(10.0);
//
//        assertThrows(IllegalArgumentException.class, () -> wallet.transferFunds(null, 100.0));
//    }

}