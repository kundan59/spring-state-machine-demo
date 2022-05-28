package org.knoldus.ssm.services;

import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.dto.Charge;
import org.springframework.statemachine.StateMachine;

/**
 *  [[PaymentService]] is service class to execute new payment,
 *  do pre-authorization and authorization transactions
 */
public interface PaymentService {

    /**
     * Execute new payment. set State to NEW and persist payment to database
     *
     * @param payment contains id, state and amount to persist for a check-in event
     * @return [[Payment]]
     */
    Payment newPayment(final Payment payment);

    /**
     * Execute pre Authorization. check available amount in credit card is
     * grater than amount to be capture. Change state from NEW to PRE_AUTH
     * if check is true. otherwise, Change state to PRE_AUTH_DECLINE
     *
     * @param charge [[Charge]]
     * @return StateMachine<PaymentState, PaymentEvent>
     */
    StateMachine<PaymentState, PaymentEvent> preAuth(final Charge charge);

    /**
     * Execute Authorization. check pre-authorization done as well authorization is true.
     * If check is true, Change the state from PRE_AUTH to AUTH. otherwise,
     * Change state to AUTH_DECLINE
     *
     * @param charge [[Charge]]
     * @return StateMachine<PaymentState, PaymentEvent>
     */
    StateMachine<PaymentState, PaymentEvent> authorizePayment(final Charge charge);

}
