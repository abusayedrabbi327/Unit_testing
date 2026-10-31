# STQA Assignment: Unit & Mutation Testing Report (Wallet Module)

---

## A) Test Case List

| Test ID | Class.Method under Test | Why This Test? (Rule / Edge Case) | Verdict | Comments / Observations |
| :--- | :--- | :--- | :--- | :--- |
| **P1** | `Wallet.Wallet()` | Default constructor rule: A new wallet must initialize with a zero balance. | **PASS** | `balance` is correctly set to `0.0`. |
| **P2** | `Wallet.Wallet(double)` | Parameterized constructor rule: A wallet can start with a valid initial positive balance. | **PASS** | `balance` correctly reflects the given amount (`100.0`). |
| **P3** | `Wallet.addFunds(double)` | Happy path: Adding a positive amount must increment the existing balance. | **PASS** | Adding `50.0` to `100.0` results in `150.0`. |
| **P4** | `Wallet.addFunds(double)` | Boundary condition: Depositing zero (`0.0`) must be rejected as non-positive. | **PASS** | Successfully throws `InvalidAmountException`. |
| **P5** | `Wallet.addFunds(double)` | Equivalence partition: Depositing a negative amount must be rejected. | **PASS** | Successfully throws `InvalidAmountException`. |
| **P6** | `Wallet.deductFunds(double)` | Happy path: Deducting a valid amount must decrease the wallet balance. | **PASS** | Deducting `30.0` from `100.0` results in `70.0`. |
| **P7** | `Wallet.deductFunds(double)` | Equivalence partition: Deducting a negative amount must be rejected. | **PASS** | Successfully throws `InvalidAmountException`. |
| **P8** | `Wallet.deductFunds(double)` | Boundary condition: Deducting zero (`0.0`) must be rejected. | **PASS** | Successfully throws `InvalidAmountException`. |
| **P9** | `Wallet.deductFunds(double)` | Boundary/Rule: Deducting more than available balance must be prohibited. | **PASS** | Successfully throws `InsufficientFundsException`. |
| **P10** | `Wallet.deductFunds(double)` | Boundary condition: Deducting the exact available balance (`balance == amount`) is allowed. | **PASS** | Balance reduces cleanly to `0.0`. |
| **P11** | `Wallet.transferFunds(Wallet, double)` | Happy path: Transferring money between two valid distinct wallets. | **PASS** | Source is deducted and destination is credited cleanly. |
| **P12** | `Wallet.transferFunds(Wallet, double)` | Rule: Transferring an amount greater than the available balance must fail. | **PASS** | Successfully throws `InsufficientFundsException`. |
| **P13** | `Wallet.transferFunds(Wallet, double)` | Boundary condition: Transferring an amount of zero (`0.0`) must fail. | **PASS** | Successfully throws `InvalidAmountException`. |
| **P14** | `Wallet.transferFunds(Wallet, double)` | Edge case: Transferring zero when source wallet has a negative balance. | **PASS** | Condition `amount > 0` correctly triggers `InvalidAmountException`. |
| **P15** | `Wallet.transferFunds(Wallet, double)` | Boundary condition: Transferring 100% of available balance (`amount == balance`). | **PASS** | Source becomes `0.0`, recipient receives exact amount. |
| **D1** | `Wallet.Wallet(double)` | Invariant check: Initial balance must not be negative (`balance < 0.0`). | **FAIL** | **Defect:** Allows instantiation of negative-debt wallets (`-100.0`). |
| **D2** | `Wallet.addFunds(double)` | Boundary/Robustness: Adding `Double.POSITIVE_INFINITY` must be rejected. | **FAIL** | **Defect:** `Double.POSITIVE_INFINITY > 0` is true; balance becomes `Infinity`. |
| **D3** | `Wallet.transferFunds(Wallet, double)` | Robustness/Contract: Transferring to a `null` recipient must be cleanly rejected. | **FAIL** | **Defect:** Throws unhandled `NullPointerException` instead of `IllegalArgumentException`. |
| **D4** | `Wallet.transferFunds(Wallet, double)` | Atomicity/Integrity: If transfer fails, sender funds must not be deducted. | **FAIL** | **Defect:** Deducts money from sender before checking recipient; money is permanently lost. |
| **D5** | `Wallet.transferFunds(Wallet, double)` | Domain rule: Transferring funds to the same wallet (`toWallet == this`) must fail. | **FAIL** | **Defect:** Self-transfer succeeds silently without restriction. |
| **D6** | `Wallet.transferFunds(Wallet, double)` | Validation precedence: Recipient nullity must be validated before checking balance. | **FAIL** | **Defect:** Throws `InsufficientFundsException` instead of `IllegalArgumentException`. |

---

## B) Defects List

| Defect ID | Class.Method | Description of the Defect |
| :--- | :--- | :--- |
| **D1** | `Wallet.Wallet(double)` | **Negative initial balance allowed:** The parameterized constructor accepts negative balances (e.g. `-100.0`) without validation, allowing wallets to start with debt. |
| **D2** | `Wallet.addFunds(double)` | **Unbounded infinite deposit:** `Double.POSITIVE_INFINITY > 0` evaluates to `true`, corrupting the balance into `Infinity` and breaking subsequent arithmetic. |
| **D3** | `Wallet.transferFunds(Wallet, double)` | **Null recipient throws NPE:** Calling `transferFunds` with `toWallet == null` throws an unhandled runtime `NullPointerException` at line 42 instead of a clean business exception. |
| **D4** | `Wallet.transferFunds(Wallet, double)` | **Atomicity violation / Fund destruction:** `this.deductFunds(amount)` runs before accessing `toWallet`. When `toWallet` is null, money is deducted from sender and permanently destroyed without rollback. |
| **D5** | `Wallet.transferFunds(Wallet, double)` | **Self-transfer allowed:** Does not check `if (toWallet == this)`. Transferring money to the exact same account is an invalid financial operation. |
| **D6** | `Wallet.transferFunds(Wallet, double)` | **Validation precedence defect:** The method checks `balance >= amount` before validating `toWallet != null`. For amounts exceeding balance with null recipient, it incorrectly throws `InsufficientFundsException`. |

---

## C) Mutant Analysis (Wallet Module)

* **Overall Mutation Score (from PIT Report):**
  * **Line Coverage:** 100% (24/24 lines covered)
  * **Mutation Coverage:** 100% (15/15 mutants killed)
  * **Test Strength:** 100% (15/15)

* **Mutant Killed by Tests:**
  * **Mutant:** `Line 29: Replaced double subtraction with addition -> KILLED`
  * **Killed by:** `parking.WalletTest.deduct_Funds_Decreases_Balance()`
  * **Behavior Explanation:** PIT mutated `balance -= amount` to `balance += amount` inside `deductFunds()`. The unit test initializes a wallet with `100.0`, deducts `30.0`, and asserts that the resulting balance is strictly `70.0`. Under the mutated code, the balance became `130.0`, triggering an `AssertionFailedError` and successfully killing the mutant.

* **Surviving / Equivalent Mutant Analysis:**
  * **Surviving Mutants in Report:** `0` (All 15 generated mutants were killed by the test suite).
  * **Equivalent / Hard-to-Kill Mutant Analysis:** In `transferFunds()`, lines 39–40 duplicate the checks already performed inside `deductFunds()` (`amount > 0` and `balance >= amount`). If a mutation engine removes the inner condition `balance >= amount` in `transferFunds()` while leaving `this.deductFunds(amount)` intact, the behavior observable to callers remains identical because `deductFunds()` enforces the exact same check. This represents an **equivalent mutant** that survives unless individual bytecode instructions are specifically isolated.

---

## D) Individual Contribution

* **Module Ownership:** As part of a 4-member team, I took exclusive ownership of testing the `Wallet` module (`Wallet.java` and `WalletTest.java`).
* **Test Case Design & Execution:** Thoroughly reviewed the assignment specifications and `Wallet.java` implementation to design comprehensive positive and boundary test cases, successfully identifying 6 distinct defects (including unhandled exceptions, validation ordering issues, and atomicity violations).
* **Mutation Testing & Team Collaboration:** Designed mutation-killing test cases achieving an initial ~90% mutation kill rate independently, then collaborated with team members on the final edge cases to achieve a complete 100% mutation coverage score (15/15 mutants killed) on the `Wallet` module.
