package org.knoldus.ssm.domain;

public enum PaymentEvent {

    PRE_AUTHORIZE,
    PRE_AUTHORIZE_DECLINE,
    PRE_AUTHORIZE_APPROVE,
    AUTHORIZE,
    AUTH_APPROVE,
    AUTH_DECLINE
}
