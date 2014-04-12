package com.unibet.worktest.bank.dao.impl;

import com.unibet.worktest.bank.InfrastructureException;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.Transaction;
import com.unibet.worktest.bank.TransactionLeg;
import com.unibet.worktest.bank.dao.ITransactionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

/**
 * Implementation class for link{ITransactionDao}
 * Uses Spring NamedParameterJdbcTemplate for Persistence.
 * The unrecoverable DataAccessException thrown by Spring is wrapped into InfrastructureException
 */

@SuppressWarnings("unchecked") @Repository(value = "transactionDao")
public class TransactionDao implements ITransactionDao
{
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	// COLUMN NAMES
	private static final String TRANS_REF = "TRANS_REF";

	private static final String TRANS_TYPE = "TRANS_TYPE";

	private static final String TRANS_DATE = "TRANS_DATE";

	private static final String LEG_ACCOUNT_REF = "ACCOUNT_REF";

	private static final String LEG_AMOUNT = "AMOUNT";

	private static final String LEG_CURRENCY = "CURRENCY";

	private static final String LEG_TRANS_REF = "TRANS_REF";

	private static final String INSERT_TRANSACTION = "INSERT INTO TRANSACTION (TRANS_REF, TRANS_TYPE, TRANS_DATE) " +
		"VALUES (:TRANS_REF, :TRANS_TYPE, :TRANS_DATE)";

	private static final String INERT_TRANSACTION_LEG = "INSERT INTO TRANSACTION_LEG(TRANS_REF,ACCOUNT_REF,AMOUNT,CURRENCY)" +
		" VALUES (:TRANS_REF,:ACCOUNT_REF,:AMOUNT,:CURRENCY)";

	private static final String SELECT_TRANSACTION_LEGS = "SELECT ACCOUNT_REF, AMOUNT, CURRENCY FROM TRANSACTION_LEG WHERE TRANS_REF = :TRANS_REF";

	private static final  String SELECT_TRANSACTIONS="SELECT TRANS_REF, TRANS_TYPE, TRANS_DATE FROM TRANSACTION WHERE TRANS_REF IN (:TRANS_REF)";

	private static final String SELECT_LEGS_BY_ACCOUNT_REF = "SELECT TRANS_REF FROM TRANSACTION_LEG WHERE ACCOUNT_REF = :ACCOUNT_REF";
	@Autowired
	public void setDataSource(DataSource dataSource)
	{

		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

	}

	/**
	 * persists the transaction details into TRANSACTION and TRANSACTION_LEG tables
	 * @param transaction Transaction
	 */
	@Override
	public void save(Transaction transaction)
	{
		try
		{
			jdbcTemplate.update(INSERT_TRANSACTION, getTransactionParameterMap(transaction));

			//INSERT TO TRANSACTION_LEG
			Map<String, Object>[] maps = new HashMap[transaction.getLegs().size()];
			int i = 0;
			for (TransactionLeg leg : transaction.getLegs())
			{
				Map<String, Object> map = new HashMap<>();
				map.put(LEG_TRANS_REF, transaction.getTransactionRef());
				map.put(LEG_ACCOUNT_REF, leg.getAccountRef());
				map.put(LEG_AMOUNT, leg.getAmount().getAmount());
				map.put(LEG_CURRENCY, leg.getAmount().getCurrency().getCurrencyCode());
				maps[i++] = map;
			}
			// Batch Update
			jdbcTemplate.batchUpdate(INERT_TRANSACTION_LEG, maps);
		}catch (DataAccessException e){
			throw new InfrastructureException("couldn't save transaction",e);
		}
	}

	private MapSqlParameterSource getTransactionParameterMap(Transaction transaction)
	{
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		namedParameters.addValue(TRANS_REF, transaction.getTransactionRef());
		namedParameters.addValue(TRANS_TYPE, transaction.getTransactionType());
		namedParameters.addValue(TRANS_DATE, new Timestamp(transaction.getTransactionDate().getTime()), Types.TIMESTAMP);
		return namedParameters;
	}

	/**
	 *
	 * @param transactionRef unique transaction reference
	 * @return the transaction associated with that reference
	 */
	@Override
	public Transaction get(String transactionRef)
	{
		try
		{
			List<Transaction> transactions = get(Collections.singletonList(transactionRef));
			if (transactions.size() > 0) return transactions.get(0);
		}catch (Exception e){
			throw new InfrastructureException("couldn't get transaction"+transactionRef,e);
		}
		return null;
	}

	private List<Transaction> get(List<String> transactionRefs){
		Map namedParameters = Collections.singletonMap(TRANS_REF, transactionRefs);
		return jdbcTemplate.query(SELECT_TRANSACTIONS,namedParameters,new TransactionMapper());
	}

	/**
	 *
	 * @param accountRef account reference
	 * @return all transaction associated with accountRef
	 */
	@Override
	public List<Transaction> findTransactionsByAccountRef(String accountRef)
	{
		List<Transaction> transactions=new ArrayList<>();
		try
		{
			Map<String, String> map = Collections.singletonMap(LEG_ACCOUNT_REF, accountRef);
			List<String> transRefs = jdbcTemplate.queryForList(SELECT_LEGS_BY_ACCOUNT_REF,map, String.class);
			for (String ref : transRefs)
			{
				transactions.add(get(ref));
			}
		}catch(DataAccessException e){
             throw new InfrastructureException("couldn't find transactions for"+accountRef,e);
		}
		return transactions;
	}


	/**
	 * Row Mapper class for Transaction
	 */
	private  class TransactionMapper implements RowMapper<Transaction> {

		@Override
		public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			String transactionRef = rs.getString(TRANS_REF);
			String transactionType = rs.getString(TRANS_TYPE);
			Date transactionDate = new Date(rs.getTimestamp(TRANS_DATE).getTime());
			List<TransactionLeg> legs = getTransactionLegs(transactionRef);
			return new Transaction(transactionRef, transactionType, transactionDate, legs);
		}
	}

	/**
	 * Row Mapper class for TransactionLeg Mapper
	 */
	private  class TransactionLegMapper implements RowMapper<TransactionLeg>
	{

		@Override
		public TransactionLeg mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			String accountRef = rs.getString(LEG_ACCOUNT_REF);
			BigDecimal amount = rs.getBigDecimal(LEG_AMOUNT);
			Currency currency = Currency.getInstance(rs.getString(LEG_CURRENCY));
			Money money = new Money(amount, currency);
			return new TransactionLeg(accountRef, money);
		}
	}
	private List<TransactionLeg> getTransactionLegs(String transactionRef) {
		Map<String,String> map=new HashMap<>();
		map.put(LEG_TRANS_REF,transactionRef);
		return jdbcTemplate.query(SELECT_TRANSACTION_LEGS,map,new TransactionLegMapper());
	}

	@Override
	/**
	 * truncates transaction related tables
	 * drops foreign key constraints, truncates table and adds the constraint back
	 * To be used for testing purposes only
	 */
	public void truncateTables()
	{
		jdbcTemplate.getJdbcOperations().execute("alter table TRANSACTION_LEG drop foreign key FK_TRANS_REF");
		jdbcTemplate.getJdbcOperations().execute("TRUNCATE TRANSACTION_LEG");
		jdbcTemplate.getJdbcOperations().execute("TRUNCATE TRANSACTION");
		jdbcTemplate.getJdbcOperations().execute(
			"alter table TRANSACTION_LEG  add constraint FK_TRANS_REF foreign key (TRANS_REF) references TRANSACTION (TRANS_REF)");
	}
}
