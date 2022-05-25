package org.knoldus.ssm.listener;

import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class StateListener
        extends StateMachineListenerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
        System.out.println("state changed from " + from+ " to " + to);    }
}
