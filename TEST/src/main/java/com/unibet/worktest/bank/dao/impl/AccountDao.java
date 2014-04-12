package com.unibet.worktest.bank.dao.impl;

import com.unibet.worktest.bank.AccountNotFoundException;
import com.unibet.worktest.bank.InfrastructureException;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.dao.IAccountDao;
import com.unibet.worktest.bank.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Implementation class for link{IAccountDao}
 * Uses Spring NamedParameterJdbcTemplate for Persistence.
 * The unrecoverable DataAccessException thrown by Spring is wrapped into InfrastructureException
 */
@SuppressWarnings("unchecked") @Repository(value = "accountDao")
public class AccountDao implements IAccountDao
{
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	//COLUMNS
	private static final String ACCOUNT_REF="ACCOUNT_REF";

	private static final String AMOUNT="AMOUNT";

	private static final String CURRENCY="CURRENCY";


	//SQL QUERIES
	private static final String UPDATE_QUERY="UPDATE ACCOUNT SET AMOUNT = :AMOUNT  WHERE ACCOUNT_REF = :ACCOUNT_REF";

	private static final String INSERT_QUERY="INSERT INTO ACCOUNT (ACCOUNT_REF, AMOUNT, CURRENCY) VALUES (:ACCOUNT_REF, :AMOUNT, :CURRENCY)";

	private static final String SELECT_QUERY="SELECT ACCOUNT_REF,AMOUNT,CURRENCY FROM ACCOUNT WHERE ACCOUNT_REF=:ACCOUNT_REF for update";

	@Autowired
	public void setDataSource(DataSource dataSource){
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	/**
	 * updates account balance
	 * @param accountRef unique account reference
	 * @param money amount and currency
	 */
	@Override
	public void update(String accountRef, Money money)
	{
		try{
			jdbcTemplate.update(UPDATE_QUERY, getMap(accountRef, money));
		}catch(DataAccessException e){
			throw new InfrastructureException("could not update account",e);
		}
	}

	/**
	 * FOR TESTING ONLY
	 */
	@Override
	public void truncateTables()
	{
		try
		{
			jdbcTemplate.getJdbcOperations().execute("TRUNCATE ACCOUNT");
		}catch (DataAccessException e){
			throw new InfrastructureException("couldn't truncate table",e);
		}
	}

	/**
	 * creates a new account
	 * @param accountRef unique account reference
	 * @param amount currency and initial balance
	 */
	@Override
	public void create(String accountRef, Money amount)
	{
        try
		{
			jdbcTemplate.update(INSERT_QUERY, getMap(accountRef, amount));
		}catch(DataAccessException e){
			throw new InfrastructureException("couldn't create account",e);
		}
	}

	private Map getMap(String accountRef, Money amount)
	{
		Map<String,Object> namedParameters = new HashMap();
		namedParameters.put(ACCOUNT_REF,accountRef);
		namedParameters.put(AMOUNT, amount.getAmount());
		namedParameters.put(CURRENCY,amount.getCurrency().getCurrencyCode());
		return namedParameters;
	}

	/**
	 *
	 * @param accountRef unique account reference
	 * @return the account associated with accountRef
	 */
	@Override
	public Account get(String accountRef){

		try
		{
			SqlParameterSource namedParameters = new MapSqlParameterSource(ACCOUNT_REF, accountRef);

			List<Account> accountList = jdbcTemplate.query(SELECT_QUERY,
				namedParameters, new RowMapper()
				{
					public Object mapRow(ResultSet resultSet, int rowNum)
						throws SQLException
					{
						return new Account(resultSet.getString(ACCOUNT_REF),
							new Money(resultSet.getBigDecimal(AMOUNT), Currency.getInstance(resultSet.getString(CURRENCY))));
					}
				}
			);
			if (accountList.size()!=1) throw new AccountNotFoundException(accountRef);
			return accountList.get(0);
		}catch(DataAccessException e){
			throw new InfrastructureException("couldn't get account"+accountRef,e);
		}
	}


}
