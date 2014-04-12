package com.unibet.worktest.bank;

/**
 * Defines the business logic for managing monetary accounts.
 */
public interface AccountService {
    /**
     * Create a new account with an initial balance.
     *
     * @param accountRef a client definied account reference
     * @param amount the initial account balance
     * @throws InfrastructureException on unrecoverable infrastructure errors
     */
    void createAccount(String accountRef, Money amount);

    /**
     * Get the current balance for a given account.
     *
     * @param accountRef the client definied account reference
     * @return the account balance
     * @throws AccountNotFoundException if the referenced account does not exist
     * @throws InfrastructureException on unrecoverable infrastructure errors
     */
    Money getAccountBalance(String accountRef) throws AccountNotFoundException;
}
