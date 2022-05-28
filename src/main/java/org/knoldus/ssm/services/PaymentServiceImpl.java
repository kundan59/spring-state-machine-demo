package org.knoldus.ssm.services;

import lombok.RequiredArgsConstructor;
import org.knoldus.ssm.PaymentStateInterceptor;
import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.dto.Charge;
import org.knoldus.ssm.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateInterceptor paymentStateInterceptor;

    @Override
    public Payment newPayment(Payment payment) {

        LOGGER.info("Hotel check-in, saving payment with NEW State for id {}", payment.getId());
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> preAuth(Charge charge) {

        LOGGER.info("Hotel check-in, initiating pre-authorization for id {}", charge.getId());
        StateMachine<PaymentState, PaymentEvent> buildSm = build(charge.getId());
        Flux<StateMachineEventResult<PaymentState, PaymentEvent>> stateMachineEventResultFlux;

        if (charge.getAvailableAmount().compareTo(charge.getHoldAmount()) > 0) {

            LOGGER.info("Amount available for hotel services for id {}", charge.getId());
            stateMachineEventResultFlux = sendEvent(charge.getId(), buildSm, PaymentEvent.PRE_AUTHORIZE);
        } else {

            LOGGER.info("Sufficient amount not available for hotel services for id {}", charge.getId());
            stateMachineEventResultFlux = sendEvent(charge.getId(), buildSm, PaymentEvent.PRE_AUTHORIZE_DECLINE);
        }
        stateMachineEventResultFlux.subscribe();
        return buildSm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Charge charge) {

        LOGGER.info("Hotel check-out, initiating authorization for id {}", charge.getId());
        StateMachine<PaymentState, PaymentEvent> buildSm = build(charge.getId());
        Flux<StateMachineEventResult<PaymentState, PaymentEvent>> stateMachineEventResultFlux;

        if (PaymentState.PRE_AUTH.equals(buildSm.getState().getId()) && charge.isAuthorize()) {

            LOGGER.info("Pre-authorization done and authorization check " +
                    "met for id {}", charge.getId());
            stateMachineEventResultFlux = sendEvent(charge.getId(), buildSm, PaymentEvent.AUTHORIZE);
        } else {
            LOGGER.info("either Pre-authorization not done or authorization " +
                    "check failed for id {}", charge.getId());
            stateMachineEventResultFlux = sendEvent(charge.getId(), buildSm, PaymentEvent.AUTH_DECLINE);
        }
        stateMachineEventResultFlux.subscribe();
        return buildSm;
    }

    /**
     * Send event to state machine for a payment_id.
     *
     * @param paymentId    Payment Id of an event.
     * @param stateMachine State Machine.
     * @param paymentEvent Payment event.
     * @return Flux of state event result.
     */
    private Flux<StateMachineEventResult<PaymentState, PaymentEvent>> sendEvent(
            Long paymentId,
            StateMachine<PaymentState, PaymentEvent> stateMachine,
            PaymentEvent paymentEvent) {

        Message<PaymentEvent> msg = MessageBuilder.withPayload(paymentEvent)
                .setHeader("payment_id", paymentId)
                .build();

         return stateMachine.sendEvent(Mono.create(callback -> {
            try {
                callback.success(msg);
            } catch (Exception e) {
                callback.error(e);
            }
        }));

    }

    /**
     * Restore state machine for a payment id.
     *
     * @param paymentId payment id.
     * @return state machine for the payment id.
     */
    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        StateMachine<PaymentState, PaymentEvent> stateMachine
                = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        stateMachine.stopReactively().subscribe();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(smAccessor -> {
                        smAccessor.addStateMachineInterceptor(paymentStateInterceptor);
                        smAccessor.resetStateMachineReactively(
                                new DefaultStateMachineContext<>(
                                        payment.getPaymentState(),
                                        null, null, null)).subscribe();
                        });
        stateMachine.startReactively().subscribe();
        return stateMachine;
    }
}
