package org.albertosegura.loteria.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class ElMundoPrizes {
    String estado;
    HashMap<String, Integer> premios;
}
