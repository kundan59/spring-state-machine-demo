package org.knoldus.ssm.services;

import lombok.RequiredArgsConstructor;
import org.knoldus.ssm.PaymentStateInterceptor;
import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.repository.PaymentRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateInterceptor paymentStateInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> buildSm = build(paymentId);
        sendEvent(paymentId, buildSm, PaymentEvent.PRE_AUTHORIZE);
        return buildSm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> buildSm = build(paymentId);
        sendEvent(paymentId, buildSm, PaymentEvent.AUTHORIZE);
        return buildSm;
    }

    private void sendEvent(
            Long paymentId,
            StateMachine<PaymentState, PaymentEvent> sm,
            PaymentEvent paymentEvent) {

        Message<PaymentEvent> msg = MessageBuilder.withPayload(paymentEvent)
                .setHeader("payment_id", paymentId)
                .build();

        sm.sendEvent(msg);
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {

        Payment payment = paymentRepository.getOne(paymentId);
        StateMachine<PaymentState, PaymentEvent> stateMachine
                = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(smAccessor -> {
                        smAccessor.addStateMachineInterceptor(paymentStateInterceptor);
                        smAccessor.resetStateMachine(
                                new DefaultStateMachineContext<>(
                                        payment.getPaymentState(), null, null, null));
                        });
        stateMachine.start();
        return stateMachine;
    }
}
