package org.albertosegura.loteria.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Prize {
    BigDecimal amount;
    Participation participation;
}
