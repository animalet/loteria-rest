package org.albertosegura.loteria.service.external;

import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;

import java.util.List;

public interface PrizeService {
    List<Prize> retrievePrizes(List<Participation> participationList);
}
