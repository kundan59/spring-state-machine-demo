package org.knoldus.ssm.listener;

import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class StateListener
        extends StateMachineListenerAdapter<PaymentState, PaymentEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(StateListener.class);

    @Override
    public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
      LOGGER.info("state changed from {} to {}", from, to);
    }
}
