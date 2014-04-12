package com.unibet.worktest.bank;

/**
 * Business exception thrown if a referenced account does not exist.
 */
public class AccountNotFoundException extends BusinessException {
    private final String accountRef;

    public AccountNotFoundException(String accountRef) {
        super("No account found for reference '" + accountRef + "'");
        this.accountRef = accountRef;
    }

    public String getAccountRef() {
        return accountRef;
    }
}
