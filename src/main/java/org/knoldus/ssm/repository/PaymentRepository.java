package org.knoldus.ssm.repository;

import org.knoldus.ssm.domain.Payment;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PaymentRepository extends CassandraRepository<Payment, Long> {
}
