package com.unibet.worktest.bank;

/**
 * Business exception thrown if a monetary transaction request legs are unbalanced, e.g.
 * the sum of all leg amounts does not equal zero (double-entry principle).
 */
public class UnbalancedLegsException extends BusinessException {
    public UnbalancedLegsException(String message) {
        super(message);
    }
}
