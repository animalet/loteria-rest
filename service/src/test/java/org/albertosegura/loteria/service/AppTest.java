package org.albertosegura.loteria.service;

import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;
import org.albertosegura.loteria.service.external.PrizeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppTest {
    @Mock
    private PrizeService prizeService;
    private App app;
    @Mock
    private List<Participation> participationList;
    @Mock
    private List<Prize> prizeList;

    @BeforeEach
    void setUp() {
        app = new App(prizeService);
    }

    @Test
    void calculatePrizes() {
        when(prizeService.retrievePrizes(participationList)).thenReturn(prizeList);
        List<Prize> prizes = app.calculatePrizes(participationList);
        assertSame(prizeList, prizes);
        verifyNoMoreInteractions(prizes);
    }
}