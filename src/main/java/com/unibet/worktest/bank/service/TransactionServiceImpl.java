package com.unibet.worktest.bank.service;

import com.unibet.worktest.bank.*;
import com.unibet.worktest.bank.dao.IAccountDao;
import com.unibet.worktest.bank.dao.ITransactionDao;
import com.unibet.worktest.bank.validator.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Implementation class for TransactionService
 */
@Service(value="transactionService")
public class TransactionServiceImpl implements TransferService
{

	@Autowired
	private ITransactionDao transactionDao;

	@Autowired
	private  AccountService accountService;

	@Autowired
	private IAccountDao accountDao;

	@Autowired
	private TransactionValidator validator;

	/**
	 * Executes a balanced, multi-legged monetary transaction as a single unit of work.
	 *
	 * @param transferRequest a transfer request describing the transactions legs
	 * @throws IllegalArgumentException If the entries are less than two
	 * or other key properties are missing
	 * @throws com.unibet.worktest.bank.AccountNotFoundException if a specified account does not exist
	 * @throws com.unibet.worktest.bank.InsufficientFundsException if a participating account is overdrawn
	 * @throws com.unibet.worktest.bank.UnbalancedLegsException if the transaction legs are unbalanced
	 * @throws com.unibet.worktest.bank.InfrastructureException on non-recoverable infrastructure errors
	 */
	@Override
	@Transactional
	public void transferFunds(TransferRequest transferRequest)
	{
		validator.validateRequest(transferRequest);
		validator.validateLegsAreBalanced(transferRequest);

		for (TransactionLeg transactionLeg : transferRequest.getLegs())
		{
			String accountRef = transactionLeg.getAccountRef();
			Money accountBalance = accountService.getAccountBalance(transactionLeg.getAccountRef());

			BigDecimal amount = transactionLeg.getAmount().getAmount();
			BigDecimal currentAmount = accountBalance.getAmount();
			BigDecimal balance = currentAmount.add(amount);

			validator.validateBalance(balance,accountRef);//validate balance before updating account
			accountDao.update(accountRef, new Money(balance, transactionLeg.getAmount().getCurrency()));
		}
		//save transaction
		//TODO: there is no transaction Date in the builder and saving the transaction date with the current timestamp
		Transaction transaction = new Transaction(transferRequest.getTransactionRef(), transferRequest.getTransactionType(),new Date(),transferRequest.getLegs());
		transactionDao.save(transaction);

	}

	/**
	 * Finds all monetary transactions performed towards a given account.
	 *
	 * @param accountRef the client defined account reference to find transactions for
	 * @return list of transactions or an empty list if none is found
	 * @throws com.unibet.worktest.bank.AccountNotFoundException if the specified account does not exist
	 * @throws com.unibet.worktest.bank.InfrastructureException on non-recoverable infrastructure errors
	 */
	@Override
	public List<Transaction> findTransactionsByAccountRef(String accountRef)
	{

	  return transactionDao.findTransactionsByAccountRef(accountRef);

	}

	/**
	 * Get a given transaction by reference.
	 *
	 * @param transactionRef the transaction reference
	 * @return the transaction or null if it doesnt exist
	 * @throws com.unibet.worktest.bank.InfrastructureException on non-recoverable infrastructure errors
	 */
	@Override
	public Transaction getTransactionByRef(String transactionRef)
	{
	  return transactionDao.get(transactionRef);

	}
}
