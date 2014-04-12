package com.unibet.worktest.bank;

/**
 * Business exception thrown if an account participating in a transaction
 * has insufficient funds.
 */
public class InsufficientFundsException extends BusinessException {
    private final String accountRef;

    public InsufficientFundsException(String accountRef) {
        super("Insufficient funds for '" + accountRef + "'");
        this.accountRef = accountRef;
    }

    public String getAccountRef() {
        return accountRef;
    }
}
