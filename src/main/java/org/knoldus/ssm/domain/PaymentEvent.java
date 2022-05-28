package org.knoldus.ssm.domain;

/**
 * events can transition over state machine.
 */
public enum PaymentEvent {

    PRE_AUTHORIZE,
    PRE_AUTHORIZE_DECLINE,
    PRE_AUTHORIZE_APPROVE,
    AUTHORIZE,
    AUTH_APPROVE,
    AUTH_DECLINE
}
