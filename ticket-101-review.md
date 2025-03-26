# TICKET-101 Review:
This review covers the backend part of TICKET-101. For frontend review check the frontend repository.

##  What Was Done Well

### 1. **Clean Architecture**
- Good structure and separation: controller, service, config, exceptions.
- Decision logic lives inside a service, as expected.

### 2. **Custom Exception Handling**
- Uses clear and domain-specific exceptions (`InvalidLoanAmountException`, etc.) to communicate issues.

### 3. **Segment Resolution Logic**
- Personal code segmentation is well implemented using the last 4 digits of the ID, mapping to different credit modifiers.
- Debt case is correctly handled as `creditModifier = 0`.

### 4. **Use of Constants**
- All thresholds (min/max loan amount and period) are stored in `DecisionEngineConstants`, making the code maintainable.

---

##  Suggestions for Improvement

### 1. [SRP] Split the DecisionEngine Responsibilities
- The `DecisionEngine` class does too much (validation, scoring, segmentation, and decision logic).
- Recommended to split responsibilities into dedicated services like `CreditScoreCalculator` and `CreditModifierResolver` to follow SRP and make testing easier.

### 2. [DIP] Inject EstonianPersonalCodeValidator
- `EstonianPersonalCodeValidator` is instantiated directly, which makes testing and mocking harder. Should be injected as a dependency.

### 3. [OCP] Extract Segment Thresholds to Constants

- The segmentation logic uses magic numbers (`2500`, `5000`, `7500`) directly in the code.
- These values should be moved to `DecisionEngineConstants` as named constants for better readability and maintainability.(for example: `public static final int SEGMENT_1_LOWER_LIMIT = 2500;`)

### 4. Thread-Safety Issue in DecisionEngineController
- The controller injects a single shared instance of DecisionResponse via constructor.
- Since Spring beans are singleton by default, this object is reused across all HTTP requests, which may cause race conditions or data leaks between users.

### 5. Custom Exceptions Are Overengineered
- Custom exceptions like InvalidLoanAmountException currently extend Throwable and override message/cause manually — they should instead extend Exception or RuntimeException and use built-in constructors.

---

## Most Important Shortcomings (Fixed)

### 1.DecisionEngineConstants wrong MAXIMUM_LOAN_PERIOD
-MAXIMUM_LOAN_PERIOD = 60 changed to MAXIMUM_LOAN_PERIOD = 48. The assignment says that the Maximum loan period can be 48 months.

### 2.void testNoValidLoanFound() wrong loanPeriod
-loanPeriod changed to 48 from 60. The test was written for 60 months, but our maximum loan period is 48 months.

###  **Missing Credit Score Calculation**

The assignment clearly defined a core rule for loan approval:

> A loan should only be approved if the credit score is ≥ 0.1.  
> Otherwise, the engine should try to find a valid loan amount with a longer loan period (up to 48 months).  
> If no valid combination is found, a `NoValidLoanException` must be thrown.

### Original Implementation

- The original implementation calculated the approved loan amount using:

  approvedAmount = creditModifier * loanPeriod

- It returned this amount without checking the required credit score formula:
  credit score = ((creditModifier / loanAmount) * loanPeriod) / 10

- This meant **invalid loans could be approved**, breaking assignment constraints.

---

####  Fix Implemented

The logic was rewritten to correctly follow the assignment’s business rules:

1. Loop from the requested loan period up to the max (48 months).

2. For each period:
- Calculate the potential loan using:  
  `potentialLoan = creditModifier * period`
- Clamp to the maximum allowed amount.
- Skip values below the minimum allowed amount.
- Calculate the credit score: `(creditModifier / loanAmount) * loanPeriod / 10`
- If score ≥ 0.1, return the approved loan.

3. If no valid period with a high enough score, throw `NoValidLoanException`.
