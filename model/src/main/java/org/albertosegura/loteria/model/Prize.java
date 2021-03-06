package org.albertosegura.loteria.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Prize {
    BigDecimal amount;
    Participation participation;
}
