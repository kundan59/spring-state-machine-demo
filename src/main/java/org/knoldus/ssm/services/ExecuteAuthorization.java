package org.knoldus.ssm.services;

import lombok.RequiredArgsConstructor;
import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.dto.Charge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ExecuteAuthorization {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExecuteAuthorization.class);

    private final PaymentService paymentService;

    public void execute(Charge charge)  {

        switch (charge.getFlow()) {

            case "check-in":
            {
                Payment payment = Payment.builder().id(charge.getId())
                        .amount(charge.getHoldAmount()).build();
                paymentService.newPayment(payment);
                StateMachine<PaymentState, PaymentEvent> preAuth
                        = paymentService.preAuth(charge);
                if (PaymentState.PRE_AUTH.equals(preAuth.getState().getId())) {

                    LOGGER.info("Pre authorization done, Enjoy the services");
                } else {

                    LOGGER.info("Pre authorization failed, provide another card");
                }
                break;

            }

            case "check-out":
            {

                StateMachine<PaymentState, PaymentEvent> auth = paymentService.authorizePayment(charge);
                if (PaymentState.AUTH.equals(auth.getState().getId())) {

                    LOGGER.info("Authorization done, captured amount {} " +
                            "from you card, visit again", charge.getHoldAmount());
                } else {

                    LOGGER.info("Authorization failed, provide another card");
                }
                break;
            }

            default: LOGGER.info("Wrong service flow");
        }
    }

}
