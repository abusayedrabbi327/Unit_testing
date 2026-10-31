# Wallet Module: Comprehensive Interview & Viva Defense Guide

> **Target Audience:** STQA Viva / Lab Defense / Technical Interview  
> **Module Under Test:** `parking.Wallet` & `parking.WalletTest`  
> **Source Files:** [`Wallet.java`](file:///d:/UIU/14%20trimester/STQA/Assignmenet%20Unit/boilerplate/src/main/java/parking/Wallet.java) | [`WalletTest.java`](file:///d:/UIU/14%20trimester/STQA/Assignmenet%20Unit/boilerplate/src/test/java/parking/WalletTest.java) | [`WALLET_REPORT FINAL.md`](file:///d:/UIU/14%20trimester/STQA/Assignmenet%20Unit/boilerplate/src/WALLET_REPORT%20FINAL.md)

---

## 1. Executive Summary for Interviewers

* **Project Scope:** Part of a 4-member team, I took exclusive responsibility for the **`Wallet`** component.
* **Testing Methodology:** Applied **Equivalence Partitioning (EP)**, **Boundary Value Analysis (BVA)**, **State Invariant Verification**, and **Fault Injection/Defect Probing**.
* **Test Suite Metrics:**
  * **15 Passing Tests (P1–P15):** Validate functional correctness, business rules, and boundary limits.
  * **6 Defect Tests (D1–D6):** Expose critical design flaws, security risks, atomicity violations, and input validation oversights.
* **Mutation Testing (PITest):**
  * **Line Coverage:** 100% (24/24 lines)
  * **Mutation Coverage:** 100% (15/15 mutants killed)

---

## 2. In-Depth Explanation of All Passing Test Cases (P1 – P15)

### Constructor & Initialization Tests

#### P1: `default_Wallet_Starts_At_Zero_Balance()`
* **Concept:** Default State Invariant Verification.
* **What it does:** Instantiates `new Wallet()` and verifies `wallet.getBalance() == 0.0`.
* **Interview Explanation:** *"This test ensures the object starts in a clean, predictable state without garbage values or uninitialized fields. A freshly created user wallet must always start with zero balance."*

#### P2: `wallet_Can_Start_With_Initial_Balance()`
* **Concept:** Parameterized State Initialization (Happy Path).
* **What it does:** Instantiates `new Wallet(100.0)` and verifies `wallet.getBalance() == 100.0`.
* **Interview Explanation:** *"Verifies that valid positive seed capital is correctly assigned to the internal `balance` field without truncation or mutation."*

---

### Deposit Operations (`addFunds`)

#### P3: `add_Funds_Increases_Balance()`
* **Concept:** State Transition / State Mutation (Happy Path).
* **What it does:** Starts at `100.0`, deposits `50.0`, asserts new balance is `150.0`.
* **Interview Explanation:** *"Ensures the mathematical addition operation (`balance += amount`) correctly mutates object state for standard valid positive inputs."*

#### P4: `add_Fund_Rejects_Zero()`
* **Concept:** Boundary Value Analysis (BVA - On the boundary: `amount = 0.0`).
* **What it does:** Calls `wallet.addFunds(0.0)` and asserts that `InvalidAmountException` is thrown.
* **Interview Explanation:** *"Zero is the critical boundary between negative and positive numbers. Business rules dictate that a deposit must strictly be positive (`amount > 0`). Adding zero performs no meaningful transaction and should be rejected."*

#### P5: `add_Fund_Reject_Negative_Amount()`
* **Concept:** Equivalence Partitioning (Invalid Negative Partition: `amount < 0`).
* **What it does:** Calls `wallet.addFunds(-10.0)` and asserts `InvalidAmountException` is thrown.
* **Interview Explanation:** *"Prevents negative deposits which would act as unauthorized deductions. It verifies input sanitization logic."*

---

### Withdrawal Operations (`deductFunds`)

#### P6: `deduct_Funds_Decreases_Balance()`
* **Concept:** State Transition / Subtraction (Happy Path).
* **What it does:** From `100.0`, deducts `30.0`, asserts balance decreases to `70.0`.
* **Interview Explanation:** *"Validates that valid withdrawals decrement the internal balance accurately."*

#### P7: `deduct_Funds_Rejects_Negative_Amount()`
* **Concept:** Equivalence Partitioning (Invalid Negative Input).
* **What it does:** Calls `wallet.deductFunds(-100.0)` and asserts `InvalidAmountException`.
* **Interview Explanation:** *"Deducting a negative number mathematically evaluates to addition (`x - (-y) = x + y`). Without this guard, an attacker could artificially increase their balance."*

#### P8: `deduct_Funds_Rejects_Zero_Amount()`
* **Concept:** Boundary Value Analysis (BVA: `amount = 0.0`).
* **What it does:** Calls `wallet.deductFunds(0.0)` and asserts `InvalidAmountException`.
* **Interview Explanation:** *"Withdrawals must be strictly positive (`amount > 0`). Withdrawing zero is an invalid transaction request."*

#### P9: `deduct_Funds_Rejects_Insufficient_Balance()`
* **Concept:** Business Rule Invariant (`balance >= amount`).
* **What it does:** Wallet has `2000.0`, attempts to deduct `2100.0`, asserts `InsufficientFundsException`.
* **Interview Explanation:** *"Protects the core financial invariant: a wallet cannot spend money it does not possess. Overdraft is prohibited."*

#### P10: `deduct_Funds_Allows_Exact_Available_Balance()`
* **Concept:** Boundary Value Analysis (Upper Limit Boundary: `amount == balance`).
* **What it does:** Wallet has `100.0`, deducts `100.0`, asserts balance reaches `0.0`.
* **Interview Explanation:** *"Tests the exact boundary condition where `balance == amount`. This verifies the condition is `>=` rather than `>`."*

---

### Fund Transfer Operations (`transferFunds`)

#### P11: `transfer_Money_Between_Wallets()`
* **Concept:** Multi-Object State Transition (Happy Path).
* **What it does:** Transfers `40.0` from Source (`100.0`) to Destination (`10.0`). Asserts Source becomes `60.0` and Destination becomes `50.0`.
* **Interview Explanation:** *"Verifies bilateral state consistency: the sender is debited and the recipient is credited by the exact same amount."*

#### P12: `transfer_Funds_more_than_amount()`
* **Concept:** Insufficient Balance Guard on Transfers.
* **What it does:** Wallet has `100.0`, attempts to transfer `200.0` to another wallet. Asserts `InsufficientFundsException`.
* **Interview Explanation:** *"Ensures fund transfers respect the same solvency checks as direct withdrawals; you cannot transfer funds you do not have."*

#### P13: `transfer_Funds_Rejects_Zero_amount()`
* **Concept:** Boundary Value Analysis on Transfers (`amount = 0.0`).
* **What it does:** Source tries to transfer `0.0`. Asserts `InvalidAmountException`.
* **Interview Explanation:** *"Zero-value transfers generate redundant system load and must be blocked at the boundary."*

#### P14: `transfer_Funds_Rejects_Zero_On_Negative_Balance_Wallet()`
* **Concept:** Edge Case / Defect-Exposing Boundary Probe.
* **What it does:** Created a wallet with `-100.0`, tried transferring `0.0`. Asserts `InvalidAmountException`.
* **Interview Explanation:** *"Verifies that even if a wallet somehow holds an abnormal/negative balance, the primary input check (`amount > 0`) still halts execution before balance checks."*

#### P15: `transfer_Funds_Allows_Exact_balance_Transfer()`
* **Concept:** Boundary Condition on Transfers (`amount == balance`).
* **What it does:** Source has `100.0`, transfers all `100.0`. Asserts Source balance is `0.0` and recipient balance increases by `100.0`.
* **Interview Explanation:** *"Confirms that transferring 100% of available funds cleanly empties the account without triggering an incorrect insufficient funds error."*

---

## 3. In-Depth Explanation of All Discovered Defects (D1 – D6)

| Defect ID | Target Method | Core Defect | Real-World Impact | How to Fix |
| :---: | :--- | :--- | :--- | :--- |
| **D1** | `Wallet(double balance)` | No validation on initial balance parameter. | Allows negative-debt wallets (`-100.0`) at creation. | `if (balance < 0) throw new InvalidAmountException();` |
| **D2** | `addFunds(double amount)` | `Double.POSITIVE_INFINITY > 0` evaluates to `true`. | Balance becomes `Infinity`, corrupting all future math. | Check `Double.isInfinite(amount) \|\| Double.isNaN(amount)`. |
| **D3** | `transferFunds(Wallet, double)` | No null check on `toWallet`. | Throws raw `NullPointerException` instead of `IllegalArgumentException`. | `if (toWallet == null) throw new IllegalArgumentException();` |
| **D4** | `transferFunds(Wallet, double)` | Sender is deducted **before** accessing `toWallet`. | **Money loss:** If recipient is null, sender loses money permanently. | Validate recipient before calling `this.deductFunds(amount)`. |
| **D5** | `transferFunds(Wallet, double)` | Self-transfer (`toWallet == this`) is allowed. | Redundant cycles; violates banking domain invariants. | `if (toWallet == this) throw new IllegalArgumentException();` |
| **D6** | `transferFunds(Wallet, double)` | Checks `balance >= amount` before validating `toWallet != null`. | Throws `InsufficientFundsException` for invalid recipient requests. | Validate input parameters before evaluating internal state. |

### Scripted Interview Answers for Defects:

* **Question on D4 (Critical): "What is the most severe bug you found in `Wallet.java`?"**
  * **Answer:** *"The most severe bug is in `transferFunds`. At line 41, the method calls `this.deductFunds(amount)` first, and only at line 42 does it invoke `toWallet.addFunds(amount)`. If `toWallet` is `null`, line 42 throws a `NullPointerException`. Because there is no try-catch or rollback, the money already deducted from the sender is permanently destroyed. This directly violates the ACID atomicity principle in financial transactions."*

* **Question on D1: "Why is a negative balance in constructor a bug if `addFunds` and `deductFunds` prevent negatives?"**
  * **Answer:** *"It violates class invariant integrity. A class should never allow itself to be constructed into an invalid state. Permitting `new Wallet(-500.0)` allows invalid objects to circulate in the system, bypassing operational guards."*

* **Question on D2: "Why does `Double.POSITIVE_INFINITY` pass through `amount > 0`?"**
  * **Answer:** *"In IEEE 754 floating-point specifications, `Double.POSITIVE_INFINITY` is considered greater than zero. Therefore, standard `amount > 0` checks return `true`. To defend against this, input sanitization must explicitly check `!Double.isInfinite(amount) && !Double.isNaN(amount)`."*

---

## 4. Mutation Testing (PITest) Defense & Viva Mastery

### Key Concepts to Know:
1. **What is Mutation Testing?**
   * A fault-based testing technique where the tool introduces small syntactic changes (mutations) into bytecode to see if your tests detect (kill) them.
2. **What is a Killed Mutant?**
   * A mutant that causes at least one test case to fail or throw an unexpected exception.
3. **What is a Surviving Mutant?**
   * A mutant where all tests still pass, revealing a gap in test suite assertions or test coverage.
4. **What is an Equivalent Mutant?**
   * A mutant that syntactically differs from the original code but produces semantically identical behavior for all possible inputs, making it impossible to kill.

---

### The Big Interview Trap:
> **Question:** *"Your PIT report shows 100% mutation coverage (15/15 killed). Does that mean `Wallet.java` has zero bugs?"*

* **Model Answer:**
  > *"No, absolutely not. Mutation testing measures the strength of our test suite against **existing code perturbations**. It tests for errors of commission, not **errors of omission**.
  > PIT only mutates the bytecode that was actually written (e.g., changing `-` to `+`, or changing `>=` to `>`). It cannot invent missing requirements—such as a missing null-check on `toWallet` or missing validation for negative constructor balances. If code doesn't exist, PIT cannot mutate it. That is why we achieved 100% mutation score while still discovering 6 real-world architectural and functional defects."*

---

### Your Report's Specific Killed Mutant:
* **Mutator:** Math Mutator (`Replaced double subtraction with addition`).
* **Location:** [Wallet.java: Line 29](file:///d:/UIU/14%20trimester/STQA/Assignmenet%20Unit/boilerplate/src/main/java/parking/Wallet.java#L29) (`balance -= amount;`).
* **Killed by:** `deduct_Funds_Decreases_Balance()`
* **Explanation:** *"PIT mutated `balance -= amount` into `balance += amount`. Our test initialized a wallet with `100.0`, deducted `30.0`, and strictly asserted `assertEquals(70.0, wallet.getBalance())`. Under the mutated code, the balance became `130.0`, causing the assertion to fail immediately and killing the mutant."*

---

### Your Report's Equivalent Mutant Analysis:
* **Location:** [Wallet.java: Lines 39–40](file:///d:/UIU/14%20trimester/STQA/Assignmenet%20Unit/boilerplate/src/main/java/parking/Wallet.java#L39-L40) inside `transferFunds()`.
* **Explanation:**
  ```java
  public void transferFunds(Wallet toWallet, double amount) {
      if(amount > 0) {
          if(balance >= amount) {      // <-- Redundant check
              this.deductFunds(amount); // <-- deductFunds ALREADY checks balance >= amount!
  ```
  *"If a mutation operator changes or eliminates the condition `balance >= amount` at line 40, the program behavior from the perspective of any caller remains 100% identical because `this.deductFunds(amount)` at line 41 immediately re-evaluates `balance >= amount` and throws `InsufficientFundsException`. This redundant code creates an equivalent mutant that is impossible to isolate at the public API level."*

---

## 5. Quick Reference Card for Exam Day

```text
+------------------------------------------------------------------------------------+
|                               WALLET MODULE CHEAT SHEET                            |
+------------------------------------------------------------------------------------+
| Module:           parking.Wallet                                                   |
| Test File:        parking.WalletTest                                               |
| Total Tests:      21 (15 Passing P1-P15, 6 Defect D1-D6)                           |
| Mutation Score:   100% (15 of 15 mutants killed)                                   |
| Line Coverage:    100% (24 of 24 lines covered)                                    |
+------------------------------------------------------------------------------------+
| 6 DEFECTS FOUND:                                                                   |
|   D1: Wallet(-100.0) allowed (Missing constructor validation)                      |
|   D2: addFunds(Double.POSITIVE_INFINITY) allowed (No isInfinite check)             |
|   D3: transferFunds(null, 50.0) throws NPE instead of IllegalArgumentException     |
|   D4: Atomicity violation: sender money deducted before NPE on null recipient      |
|   D5: transferFunds(self, 50.0) allowed (Self-transfer not guarded)                |
|   D6: Validation order: Checks balance before validating recipient nullity         |
+------------------------------------------------------------------------------------+
```
