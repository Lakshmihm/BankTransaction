package com.unibet.worktest.bank;

import com.unibet.worktest.bank.dao.IAccountDao;
import com.unibet.worktest.bank.dao.ITransactionDao;
import com.unibet.worktest.bank.util.BeanUtils;

/**
 * Implementation class for BeanFactory
 * Uses link{BeanUtils} to get Spring beans from application context
 * Created by Lakshmi on 06/04/14.
 */
public class BankFactoryImpl implements BankFactory
{
	/**
	 * @return an instance of the AccountService providing account management
	 */
	@Override
	public AccountService getAccountService()
	{
		return BeanUtils.getBean(AccountService.class);
	}

	/**
	 * @return an instance of the TransferService providing account transfers
	 */
	@Override public TransferService getTransferService()
	{
		return BeanUtils.getBean(TransferService.class);
	}

	/**
	 * Support method for setting up the initial state in a persistent store. Targeted
	 * for testing only.
	 */
	@Override
	public void setupInitialData()
	{
		IAccountDao accountDao=BeanUtils.getBean(IAccountDao.class);
		accountDao.truncateTables();
		ITransactionDao transactionDao=BeanUtils.getBean(ITransactionDao.class);
		transactionDao.truncateTables();

	}
}
