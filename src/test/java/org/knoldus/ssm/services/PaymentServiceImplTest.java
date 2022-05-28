package org.knoldus.ssm.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.knoldus.ssm.domain.Payment;
import org.knoldus.ssm.domain.PaymentEvent;
import org.knoldus.ssm.domain.PaymentState;
import org.knoldus.ssm.dto.Charge;
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
    Charge charge;

    @Test
    @Transactional
    void testPreAuthHappyPath_new_to_pre_auth_state() {

        charge= Charge.builder().availableAmount(new BigDecimal("10000"))
                .holdAmount(new BigDecimal("700"))
                .date("2022-06-21")
                .flow("check-in")
                .id(1354L)
                .hotel("The Lalit, New Delhi").build();
        payment =
                Payment.builder().amount(charge.getAvailableAmount())
                        .id(charge.getId()).build();
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuth
                = paymentService.preAuth(charge);

        Payment preAthorizePayment = paymentRepository.findById(savedPayment.getId()).get();

        Assertions.assertEquals(PaymentState.PRE_AUTH, preAuth.getState().getId());
        Assertions.assertEquals(PaymentState.PRE_AUTH, preAthorizePayment.getPaymentState());

    }

    @Test
    @Transactional
    void testPreAuth_Non_HappyPath_new_to_pre_auth_error_state() {

        charge= Charge.builder().availableAmount(new BigDecimal("10000"))
                .holdAmount(new BigDecimal("1000000"))
                .date("2022-06-21")
                .flow("check-in")
                .id(1354L)
                .hotel("The Lalit, New Delhi").build();
        payment =
                Payment.builder().amount(charge.getAvailableAmount())
                        .id(charge.getId()).build();
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuth
                = paymentService.preAuth(charge);

        Payment preAthorizePayment = paymentRepository.findById(savedPayment.getId()).get();

        Assertions.assertEquals(PaymentState.PRE_AUTH_ERROR, preAuth.getState().getId());
        Assertions.assertEquals(PaymentState.PRE_AUTH_ERROR, preAthorizePayment.getPaymentState());
    }

    @Test
    @Transactional
    void testAuth_HappyPath_pre_auth_to_auth_state() {

        charge= Charge.builder().availableAmount(new BigDecimal("10000"))
                .holdAmount(new BigDecimal("1000"))
                .date("2022-06-21")
                .flow("check-in")
                .id(1354L)
                .hotel("The Lalit, New Delhi").build();
        payment =
                Payment.builder().amount(charge.getAvailableAmount())
                        .id(charge.getId()).build();
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuth
                = paymentService.preAuth(charge);
        charge= Charge.builder().availableAmount(new BigDecimal("10000"))
                .holdAmount(new BigDecimal("1000000"))
                .date("2022-06-21")
                .flow("check-out")
                .id(1354L)
                .authorize(true)
                .hotel("The Lalit, New Delhi").build();
        payment =
                Payment.builder().amount(charge.getAvailableAmount())
                        .id(charge.getId()).build();
        StateMachine<PaymentState, PaymentEvent> authorize = paymentService.authorizePayment(charge);
        Payment authorizePayment = paymentRepository.findById(savedPayment.getId()).get();

        Assertions.assertEquals(PaymentState.AUTH, authorize.getState().getId());
        Assertions.assertEquals(PaymentState.AUTH, authorizePayment.getPaymentState());
    }

    @Test
    @Transactional
    void testAuth_Non_HappyPath_pre_auth_to_auth_error_state() {

        charge= Charge.builder().availableAmount(new BigDecimal("10000"))
                .holdAmount(new BigDecimal("1000"))
                .date("2022-06-21")
                .flow("check-in")
                .id(1354L)
                .hotel("The Lalit, New Delhi").build();
        payment =
                Payment.builder().amount(charge.getAvailableAmount())
                        .id(charge.getId()).build();
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuth
                = paymentService.preAuth(charge);
        charge= Charge.builder().availableAmount(new BigDecimal("10000"))
                .holdAmount(new BigDecimal("1000000"))
                .date("2022-06-21")
                .flow("check-out")
                .id(1354L)
                .authorize(false)
                .hotel("The Lalit, New Delhi").build();
        payment =
                Payment.builder().amount(charge.getAvailableAmount())
                        .id(charge.getId()).build();
        StateMachine<PaymentState, PaymentEvent> authorize = paymentService.authorizePayment(charge);
        Payment authorizePayment = paymentRepository.findById(savedPayment.getId()).get();

        Assertions.assertEquals(PaymentState.AUTH_ERROR, authorize.getState().getId());
        Assertions.assertEquals(PaymentState.AUTH_ERROR, authorizePayment.getPaymentState());
    }
}