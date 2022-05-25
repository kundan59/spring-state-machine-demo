package org.knoldus.ssm.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;
    
    @BeforeEach
    void setUp() {
         payment =
                Payment.builder().amount(new BigDecimal("12.67")).build();
    }

    @Test
    @Transactional
    void testPreAuth() {

        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> paymentStatePaymentEventStateMachine
                = paymentService.preAuth(savedPayment.getId());


        Payment preAthorizePayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println(paymentStatePaymentEventStateMachine.getState());
        System.out.println(preAthorizePayment);
    }

    @Test
    @Transactional
    @RepeatedTest(10)
    void testAuthorization() {

        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuth
                = paymentService.preAuth(savedPayment.getId());

        if(PaymentState.PRE_AUTH.equals(preAuth.getState().getId())) {
            System.out.println("pre authorization done for payment");
            StateMachine<PaymentState, PaymentEvent> authorize
                    = paymentService.authorizePayment(savedPayment.getId());

            System.out.println(authorize.getState().getId());

        } else {
            System.out.println("pre-auth failed");
        }


    }
}