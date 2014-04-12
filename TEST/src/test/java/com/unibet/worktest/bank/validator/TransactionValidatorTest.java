package com.unibet.worktest.bank.validator;

import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.TransferRequest;
import com.unibet.worktest.bank.UnbalancedLegsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Test for all transaction validations
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/application.xml" })
public class TransactionValidatorTest
{
	@Autowired
	private TransactionValidator validator;

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidRequestNullReference(){
		TransferRequest request=TransferRequest.builder()
			.reference(null).type("testing")
			.account("CASH_ACCOUNT_1").amount(toMoney("-5.00", "EUR"))
			.account("REVENUE_ACCOUNT_1").amount(toMoney("5.00", "EUR"))
			.account("CASH_ACCOUNT_2").amount(toMoney("-10.50", "SEK"))
			.account("REVENUE_ACCOUNT_2").amount(toMoney("10.50", "SEK"))
			.build();
		validator.validateRequest(request);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidRequestNullType(){
		TransferRequest request=TransferRequest.builder()
			.reference("T124").type(null)
			.account("CASH_ACCOUNT_1").amount(toMoney("-5.00", "EUR"))
			.account("REVENUE_ACCOUNT_1").amount(toMoney("5.00", "EUR"))
			.account("CASH_ACCOUNT_2").amount(toMoney("-10.50", "SEK"))
			.account("REVENUE_ACCOUNT_2").amount(toMoney("10.50", "SEK"))
			.build();
		validator.validateRequest(request);
	}
	@Test(expected = IllegalStateException.class)
	public void testInvalidRequestMinLegs(){
		TransferRequest.builder()
			.reference("T124").type("testing")
			.account("CASH_ACCOUNT_1").amount(toMoney("-5.00", "EUR"))
			.build();//thrown by builder
	}

	@Test(expected = UnbalancedLegsException.class)
	public void testUnbalancedLegs(){
		TransferRequest request=TransferRequest.builder()
			.reference("T124").type("testing")
			.account("CASH_ACCOUNT_1").amount(toMoney("-5.00", "EUR"))
			.account("REVENUE_ACCOUNT_1").amount(toMoney("15.00", "EUR"))
			.account("CASH_ACCOUNT_2").amount(toMoney("-10.50", "SEK"))
			.account("REVENUE_ACCOUNT_2").amount(toMoney("10.50", "SEK"))
			.build();
		validator.validateLegsAreBalanced(request);
	}

	@Test(expected = UnbalancedLegsException.class)
	public void testUnbalancedLegsDifferentCurrency(){
		TransferRequest request=TransferRequest.builder()
			.reference("T124").type("testing")
			.account("CASH_ACCOUNT_1").amount(toMoney("-5.00", "EUR"))
			.account("CASH_ACCOUNT_2").amount(toMoney("-10.50", "SEK"))
			.account("REVENUE_ACCOUNT_2").amount(toMoney("10.50", "SEK"))
			.build();
		validator.validateLegsAreBalanced(request);
	}


	private static Money toMoney(String amount, String currency) {
		return new Money(new BigDecimal(amount), Currency.getInstance(currency));
	}

}
