package org.knoldus.ssm.action;

import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * [[PreAuthToAuthAction]] - state machine will take Action when received event AUTHORIZE
 */
@Component
public class PreAuthToAuthAction implements Action <PaymentState, PaymentEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PreAuthToAuthAction.class);

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> stateContext) {

        LOGGER.info("calling authorization");

        Optional<Object> paymentId =
                Optional.ofNullable(stateContext.getMessageHeader("payment_id"));
        Flux<StateMachineEventResult<PaymentState, PaymentEvent>> stateMachineEventResultFlux;

        if (paymentId.isPresent()) {

            LOGGER.info("Approving authorization for id {}", paymentId.get());
            stateMachineEventResultFlux = stateContext.getStateMachine().sendEvent(getEvent(stateContext, PaymentEvent.AUTH_APPROVE));
            stateMachineEventResultFlux.subscribe();

        } else {

            LOGGER.info("Declining authorization for id");
            stateMachineEventResultFlux = stateContext.getStateMachine().sendEvent(getEvent(stateContext, PaymentEvent.AUTH_DECLINE));
            stateMachineEventResultFlux.subscribe();
        }
    }

    private Mono<Message<PaymentEvent>> getEvent(
            StateContext<PaymentState, PaymentEvent> stateContext,
            PaymentEvent paymentEvent) {

        Message<PaymentEvent> message = MessageBuilder.withPayload(paymentEvent)
                .setHeader("payment_id", stateContext.getMessageHeader("payment_id")).build();
        return Mono.create(callback -> {
            try {
                callback.success(message);
            } catch (Exception e) {
                callback.error(e);
            }
        });
    }
}
