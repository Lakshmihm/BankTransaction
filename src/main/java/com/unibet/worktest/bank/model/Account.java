package com.unibet.worktest.bank.model;

import com.unibet.worktest.bank.Money;

/**
 * Model POJO class for Account
 */
public class Account
{

	private String accountRef;
	private Money money;

	public Account(final String accountRef, final Money money)
	{
		this.accountRef = accountRef;
		this.money = money;
	}

	public String getAccountRef(){
		return accountRef;
	}

	public Money getMoney(){
		return money;
	}

	public void setMoney(Money money){
		this.money=money;
	}
}
