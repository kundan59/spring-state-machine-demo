package org.knoldus.ssm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("payment")
@Data
public class Payment {

    @PrimaryKey
    @CassandraType(type = CassandraType.Name.BIGINT)
    private Long id;

    @CassandraType(type = CassandraType.Name.TEXT)
    private PaymentState paymentState;

    @CassandraType(type = CassandraType.Name.DECIMAL)
    private BigDecimal amount;
}
