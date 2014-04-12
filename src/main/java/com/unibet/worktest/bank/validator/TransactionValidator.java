package com.unibet.worktest.bank.validator;

import com.unibet.worktest.bank.InsufficientFundsException;
import com.unibet.worktest.bank.TransactionLeg;
import com.unibet.worktest.bank.TransferRequest;
import com.unibet.worktest.bank.UnbalancedLegsException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to validate Transactions
 */
public class TransactionValidator
{
	/**
	 * throws UnbalancedLegsException if the legs are not balanced
	 * @param transferRequest TransferRequest
	 */
	public void validateLegsAreBalanced(TransferRequest transferRequest)
	{

		List<TransactionLeg> legs=transferRequest.getLegs();
		Map<Currency,BigDecimal> currencyMap=getTotalByCurrency(legs);
		for(BigDecimal total:currencyMap.values()){
			if(total.compareTo(BigDecimal.ZERO)!=0){
				throw new UnbalancedLegsException("amount should be 0 but was"+total);
			}
		}

	}

	private  Map<Currency, BigDecimal> getTotalByCurrency(List<TransactionLeg> legs){
		 Map<Currency, BigDecimal> currencyMap= new HashMap<Currency,BigDecimal>();
			for (TransactionLeg leg : legs) {
				BigDecimal amount=currencyMap.get(leg.getAmount().getCurrency());
				if(amount==null)
				{
					amount=BigDecimal.ZERO;
				}
				currencyMap.put(leg.getAmount().getCurrency(), amount.add(leg.getAmount().getAmount()));
			}
		return currencyMap;
	}
	/**
	 * @throws  java.lang.IllegalArgumentException if any of the required
	 * parameters are missing
	 * @param transferRequest TransferRequest
	 */
	public void validateRequest(TransferRequest transferRequest)
	{
		if(StringUtils.isEmpty(transferRequest.getTransactionRef()))
			throw new IllegalArgumentException("Transaction reference required");
		if(StringUtils.isEmpty(transferRequest.getTransactionType()))
			throw new IllegalArgumentException("Transaction type required");
		if(transferRequest.getLegs().isEmpty())
			throw new IllegalArgumentException("No Transaction Legs defined");
		if(transferRequest.getLegs().size()<2)
			throw new IllegalArgumentException("Transaction leg entries should be atleast two");
	}

	/**
	 * @throws  com.unibet.worktest.bank.InsufficientFundsException if balance is 0
	 * @param balance balance
	 * @param accountRef account reference
	 */
	public void validateBalance(BigDecimal balance,String accountRef){
		if (balance.compareTo(BigDecimal.ZERO) < 0)
			throw new InsufficientFundsException("Not enough funds in account" + accountRef);

	}
}
