package com.unibet.worktest.bank.dao;

import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.Transaction;
import com.unibet.worktest.bank.TransactionLeg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for TransactionDao
 * Created by Lakshmi on 07/04/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/application.xml" })
public class TestTransactionDao
{

	@Autowired
	ITransactionDao transactionDao;

	@Autowired
	IAccountDao accountDao;



	@Before
	public void setUp(){
	   accountDao.truncateTables();
       transactionDao.truncateTables();
	}


    @Test
	public void testSave(){


		accountDao.create("DEBIT",new Money(new BigDecimal("50.00"),Currency.getInstance("GBP")));
		accountDao.create("CREDIT",new Money(new BigDecimal("0"),Currency.getInstance("GBP")));

		List<TransactionLeg> t1Legs = new ArrayList<>();
		Money credit=new Money(new BigDecimal("50.00"), Currency.getInstance("GBP"));
		Money debit=new Money(new BigDecimal("-50.00"), Currency.getInstance("GBP"));
		t1Legs.add(new TransactionLeg("CREDIT", credit));
		t1Legs.add(new TransactionLeg("DEBIT", debit));

		transactionDao.save(new Transaction("T1", "TEST", new Date(), t1Legs));

		Transaction transaction=transactionDao.get("T1");
		assertEquals("T1",transaction.getTransactionRef());
		assertEquals("TEST",transaction.getTransactionType());
		assertEquals(t1Legs.size(),transaction.getLegs().size());

		for(int i=0;i<t1Legs.size();i++){
			assertEquals(t1Legs.get(i).getAccountRef(),transaction.getLegs().get(i).getAccountRef());
			assertEquals(t1Legs.get(i).getAmount(),transaction.getLegs().get(i).getAmount());
		}

	}
	@Test
	public void testFindTransactionsByAccountRef(){
		accountDao.create("DEBIT",new Money(new BigDecimal("50"),Currency.getInstance("GBP")));
		accountDao.create("CREDIT",new Money(new BigDecimal("0"),Currency.getInstance("GBP")));

		List<TransactionLeg> t1Legs = new ArrayList<>();
		Money credit=new Money(new BigDecimal("50"), Currency.getInstance("GBP"));
		Money debit=new Money(new BigDecimal("-50"), Currency.getInstance("GBP"));
		t1Legs.add(new TransactionLeg("CREDIT", credit));
		t1Legs.add(new TransactionLeg("DEBIT", debit));

		transactionDao.save(new Transaction("T1", "TEST", new Date(), t1Legs));

		List<TransactionLeg> t2Legs = new ArrayList<>();
		Money credit2=new Money(new BigDecimal("50"), Currency.getInstance("GBP"));
		Money debit2=new Money(new BigDecimal("-50"), Currency.getInstance("GBP"));
		t2Legs.add(new TransactionLeg("CREDIT", credit2));
		t2Legs.add(new TransactionLeg("DEBIT", debit2));
		transactionDao.save(new Transaction("T2", "TEST2", new Date(), t2Legs));

		//get by account ref
		List<Transaction> transactionList=transactionDao.findTransactionsByAccountRef("CREDIT");

		//verify
		assertEquals(2,transactionList.size());
		assertEquals("T1",transactionList.get(0).getTransactionRef());
		assertEquals("T2",transactionList.get(1).getTransactionRef());

	}
}
