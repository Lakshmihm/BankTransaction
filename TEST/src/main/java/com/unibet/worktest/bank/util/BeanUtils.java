package com.unibet.worktest.bank.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Lakshmi on 06/04/14.
 * Utility class to getForUpdate autowired beans from Application context
 */
public class BeanUtils
{
	private static final ApplicationContext APPLICATION_CONTEXT = new ClassPathXmlApplicationContext("application.xml");

	public static <T> T getBean(java.lang.Class<T> aClass){
		return APPLICATION_CONTEXT.getBean(aClass);
	}
}
