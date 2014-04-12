package com.unibet.worktest.bank.service;

import com.unibet.worktest.bank.AccountNotFoundException;
import com.unibet.worktest.bank.AccountService;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.dao.IAccountDao;
import com.unibet.worktest.bank.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation class for Account Service
 * Created by Lakshmi on 06/04/14.
 */
@Service(value = "accountService")
public class AccountServiceImpl implements AccountService
{
	@Autowired
	private IAccountDao accountDao;

	/**
	 * Create a new account with an initial balance.
	 *
	 * @param accountRef a client defined account reference
	 * @param amount the initial account balance
	 * @throws com.unibet.worktest.bank.InfrastructureException on unrecoverable infrastructure errors
	 */
	@Override
	@Transactional
	public void createAccount(String accountRef, Money amount)
	{
		accountDao.create(accountRef, amount);
	}

	/**
	 * Get the current balance for a given account.
	 *
	 * @param accountRef the client defined account reference
	 * @return the account balance
	 * @throws com.unibet.worktest.bank.AccountNotFoundException if the referenced account does not exist
	 * @throws com.unibet.worktest.bank.InfrastructureException on unrecoverable infrastructure errors
	 */
	@Override
	@Transactional
	public Money getAccountBalance(String accountRef) throws AccountNotFoundException
	{
		Account account  = accountDao.get(accountRef);
		return account.getMoney();
	}
}
