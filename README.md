# spring-state-machine-demo

This template demonstrating the pre-authorization payment mechanism using Spring state machine.
Spring boot application consuming events from kafka, state machine changing the behavior w.r.t the event and persisting state to the cassandra db.

#### States of a state machine -

NEW - Initial State of Payment.

PRE_AUTH - Payment Pre-authorized.

PRE_AUTH_ERROR - Payment Pre-authorization declined.

AUTH - Payment authorization after pre-authorization done.

AUTH_ERROR - Payment authorization failed

#### Events can flow through different states of state machine-

PRE_AUTHORIZE - Call processing for the payment pre-authorization.

PRE_AUTHORIZE_DECLINE - Call processing for declining the payment pre-authorization.

PRE_AUTHORIZE_APPROVE - Call processing for Approving the payment pre-authorization.

AUTHORIZE - Call processing for the payment authorization.

AUTH_APPROVE - Call processing for approving the payment authorization.

AUTH_DECLINE - Call processing for declining the payment authorization.

#### State machine -

![](/home/knoldus/Downloads/payment.drawio.png)


### How to Run- 

#### Start kafka and Cassandra
```
docker-compose up -d
```

#### Start the spring-boot application -

Run the main class from intellij
  or
```
mvn spring-boot:run
```

#### send event to Kafka 

hotel check-in event(new payment and pre-authorization)
```
kafkacat -P -b localhost:9092 -t payment-authorize  src/test/resources/check-in.json
```
hotel check-out event(authorization and amount capture hold at check-in time)
```
kafkacat -P -b localhost:9092 -t payment-authorize  src/test/resources/check-out.json
```

### Results-
#### On hotel check-in event -

Check logs Pre authorization will be done if criteria met i.e, available amount is grater then hold amount.
 - state will change from NEW to PRE_auth, if pre-auth approved.
 - state will change from NEW to PRE_auth_ERROR, if pre-auth declined.

 ##### check the state persist in cassandra -
   ```
   docker exec -it cassandra-server cqlsh
   ```
   Run Query on interactive CQL shell
   ```
   select * from payment_state.payment;
   ```
    id  | amount | paymentstate
    -----+--------+--------------
    123 |    365 |     PRE_AUTH

#### On hotel check-out event -

Check logs authorization will be done if criteria met i.e, pre-authorization already done for that id as well authorization true in event.
- state will change from PRE_auth to AUTH, if auth approved.
- state will change from PRE_auth_ERROR to AUTH_ERROR, if auth declined.

##### check the state persist in cassandra -
   ```
   docker exec -it cassandra-server cqlsh
   ```
Run Query on interactive CQL shell
   ```
   select * from payment_state.payment;
   ```
    id  | amount | paymentstate
    -----+--------+--------------
    123 |    365 |   AUTH


