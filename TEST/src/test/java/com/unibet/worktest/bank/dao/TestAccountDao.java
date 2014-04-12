package com.unibet.worktest.bank.dao;

import com.unibet.worktest.bank.AccountNotFoundException;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.model.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Currency;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * tests functionality of AccountDao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/application.xml" })
public class TestAccountDao
{

	@Autowired
	private IAccountDao accountDao;

	private static final String TEST_ACCOUNT="TEST123";
	@Before
	public void setUp(){
		//truncate
         accountDao.truncateTables();
	}


	@Test
	public void testCreate(){
		Money money=new Money(new BigDecimal("1000.23"), Currency.getInstance("GBP"));
		accountDao.create(TEST_ACCOUNT, money);
        Account account=accountDao.get(TEST_ACCOUNT);
		assertNotNull(account);
		assertEquals(money,account.getMoney());
		assertEquals(TEST_ACCOUNT,account.getAccountRef());

	}

	@Test(expected = AccountNotFoundException.class)
	public void testAccountNotFound(){
		accountDao.get(TEST_ACCOUNT);
	}

	@Test
	public void testUpdate(){
		accountDao.create(TEST_ACCOUNT, new Money(new BigDecimal("1000.45"), Currency.getInstance("GBP")));

		//Update
		Money money=new Money(new BigDecimal("50.23"),Currency.getInstance("GBP"));
		accountDao.update(TEST_ACCOUNT,money);

		Account account1=accountDao.get(TEST_ACCOUNT);
		assertEquals(money,account1.getMoney());
		assertEquals(TEST_ACCOUNT,account1.getAccountRef());
	}

}
