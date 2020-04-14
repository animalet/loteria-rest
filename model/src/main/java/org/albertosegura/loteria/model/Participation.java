package org.albertosegura.loteria.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Participation {
    private String number;
    private BigDecimal amount;
}
