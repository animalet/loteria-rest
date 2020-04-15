package org.albertosegura.loteria.model;

import lombok.*;

import java.util.HashMap;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ElMundoPrizes {
    String estado;
    HashMap<String, Integer> premios;
}
