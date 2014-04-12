package com.unibet.worktest.bank.dao;

import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.model.Account;

/**
 * Interface for Account Data Access
 */
public interface IAccountDao
{
	/**
	 * creates an account with the specified accountRef and amount
	 * @param accountRef unique account reference
	 * @param amount currency and initial balance
	 */
	public void create(String accountRef, Money amount);

	/**
	 * This method returns the account with specified account reference
	 * Since the account table is updated by transactions, each new transaction should
	 * see the latest account balance and reads should not be optimised
	 * It should hold a pessimistic lock of the row for further update
	 * @param accountRef unique account reference
	 *
	 * @return Account
	 */
	public Account get(String accountRef);

	/**
	 * Updates the account with accountRef with the specified amount
	 * @param accountRef unique account reference
	 * @param money amount and currency
	 */
	public void update(String accountRef, Money money);

	/**
	 * To be used only for Testing purposes
	 * truncates account table
	 */
	public void truncateTables();

}
