package com.orderco.legacy.service;

/**
 * Signals a violation of an order business rule (e.g. an invalid status
 * transition). Modeled as a checked exception the way an EJB session bean's
 * business methods would declare application exceptions.
 */
public class BusinessRuleException extends Exception {

    public BusinessRuleException(String message) {
        super(message);
    }
}
