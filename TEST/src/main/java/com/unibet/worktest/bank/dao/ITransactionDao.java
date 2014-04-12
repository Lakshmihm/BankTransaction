package com.unibet.worktest.bank.dao;

import com.unibet.worktest.bank.Transaction;

import java.util.List;

/**
 * Interface for Transaction Data Access
 */
public interface ITransactionDao{

	/**
	 * returns the Transaction object associated  transactionRef
	 * @param transactionRef unique transaction reference
	 * @return Transaction
	 */
	public Transaction get(String transactionRef);

	/**
	 * Returns all the transactions for the account with reference accountRef
	 * @param accountRef account reference
	 * @return List<Transaction>
	 */
	public List<Transaction> findTransactionsByAccountRef(String accountRef);

	/**
	 * persists the transaction
	 * @param transaction Transaction
	 */
	public void save(Transaction transaction);

	/**
	 * For Testing purposes only
	 * Truncates the TRANSACTION related tables
	 */
	public void truncateTables();
}
