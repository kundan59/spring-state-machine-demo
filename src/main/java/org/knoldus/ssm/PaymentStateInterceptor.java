package org.knoldus.ssm;

import lombok.RequiredArgsConstructor;
import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.repository.PaymentRepository;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persist Payment before state change in state machine.
 */
@RequiredArgsConstructor
@Component
public class PaymentStateInterceptor
        extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state,
                               Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine,
                               StateMachine<PaymentState, PaymentEvent> rootStateMachine) {

                Optional.ofNullable(message).flatMap(
                        msg -> Optional.ofNullable((Long) msg.getHeaders()
                                .getOrDefault("payment_id", -1L)))
                        .ifPresent(paymentId -> {
                    Payment payment = paymentRepository.findById(paymentId).orElseThrow();
                    payment.setPaymentState(state.getId());
                    paymentRepository.save(payment);
                });
    }
}
