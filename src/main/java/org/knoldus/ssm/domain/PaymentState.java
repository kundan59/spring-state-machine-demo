package org.knoldus.ssm.domain;

/**
 * States of a state machine.
 */
public enum PaymentState {
    NEW,
    PRE_AUTH,
    PRE_AUTH_ERROR,
    AUTH,
    AUTH_ERROR
}
