package com.unibet.worktest.bank.service;

import com.unibet.worktest.bank.AccountService;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.TransferRequest;
import com.unibet.worktest.bank.TransferService;
import com.unibet.worktest.bank.dao.IAccountDao;
import com.unibet.worktest.bank.dao.ITransactionDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Lakshmi on 05/04/14.
 * Tests application robustness and transaction handling
 * when transactions are created by multiple threads
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/application.xml" })

public class MultiThreadedTransactionTest
{
	@Autowired
	private TransferService transferService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private IAccountDao accountDao;

	@Autowired
	private ITransactionDao transactionDao;

	private static int THREAD_COUNT = 20;

	@Before
	public void setUp()
	{
		accountDao.truncateTables();
		transactionDao.truncateTables();
	}

	@Test
	public void testUpdateBalance() throws Exception
	{
		accountService.createAccount("debit", new Money(new BigDecimal(100), Currency.getInstance("GBP")));
		accountService.createAccount("credit", new Money(new BigDecimal(20), Currency.getInstance("GBP")));

		Assert.assertEquals("The balance is 1000", 100, accountService.getAccountBalance("debit").getAmount().intValue());
		Assert.assertEquals("The balance is 200", 20, accountService.getAccountBalance("credit").getAmount().intValue());
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		List<Future<Void>> futures = new ArrayList<Future<Void>>();
		for (int x = 0; x < THREAD_COUNT; x++)
		{
			final int finalX = x;
			Callable<Void> callable = new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					transferService.transferFunds(TransferRequest.builder()
						.reference("T" + finalX).type("testing")
						.account("debit").amount(new Money(new BigDecimal(-5), Currency.getInstance("GBP")))
						.account("credit").amount(new Money(new BigDecimal(5), Currency.getInstance("GBP")))
						.build());

					return null;
				}
			};
			Future<Void> submit = executorService.submit(callable);
			futures.add(submit);
		}

		List<Exception> exceptions = new ArrayList<Exception>();
		for (Future<Void> future : futures)
		{
			try
			{
				future.get();
			} catch (Exception e)
			{
				exceptions.add(e);
				e.printStackTrace(System.err);
			}
		}

		executorService.shutdown();
		Money credit = accountService.getAccountBalance("credit");
		Money debit=accountService.getAccountBalance("debit");
		Assert.assertEquals("no Exceptions", 0, exceptions.size());
		Assert.assertEquals("Credit Account Balance", 120, credit.getAmount().intValue());
		Assert.assertEquals("Debit Account Balance", 0, debit.getAmount().intValue());

	}
}
