package org.knoldus.ssm.dto;

import lombok.*;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class Charge {

    private Long id;
    private BigDecimal holdAmount;
    private BigDecimal availableAmount;
    private String flow;
    private String date;
    private String hotel;
    private boolean authorize;
}
