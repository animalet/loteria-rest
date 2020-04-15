package org.albertosegura.loteria.service.external;

import org.albertosegura.loteria.model.ElMundoPrizes;
import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ElMundoPrizeServiceTest {
    private static final String PRIZE_NUMBER = "123";
    private static final String NON_PRIZE_NUMBER = "456";
    private static final String EL_MUNDO_RESPONSE_CACHE_KEY = "elMundoResponseCacheKey";
    private static final String URL = "mockUrl";
    @Mock
    private ElMundoPrizeService service;
    @Mock
    private RestTemplate template;
    @Mock
    private Participation participationWithPrize;
    @Mock
    private Participation participationWithoutPrize;
    @Spy
    private HashMap<String, Integer> elMundoPrizesMap = new HashMap<>(Map.ofEntries(
            entry(PRIZE_NUMBER, 5000)
    ));
    @Mock
    private Cache cache;
    @Mock
    private ResponseEntity<ElMundoPrizes> elMundoPrizesResponseEntity;
    @Mock
    private ElMundoPrizes elMundoPrizes;

    @BeforeEach
    void setUp() {
        service = new ElMundoPrizeService(template, URL, cache);
    }

    @Test
    void retrievePrizesWithCacheHit() {
        when(participationWithoutPrize.getNumber()).thenReturn(NON_PRIZE_NUMBER);
        when(participationWithPrize.getNumber()).thenReturn(PRIZE_NUMBER);
        when(participationWithPrize.getAmount()).thenReturn(BigDecimal.ONE);
        when(cache.get(eq(EL_MUNDO_RESPONSE_CACHE_KEY), Mockito.<Callable<Map<String, Integer>>>any())).thenReturn(elMundoPrizesMap);
        List<Prize> prizes = service.retrievePrizes(Arrays.asList(participationWithoutPrize, participationWithPrize));
        assertNotNull(prizes);
        assertEquals(prizes.size(), 1);
        assertEquals(prizes.get(0).getParticipation(), participationWithPrize);

        verify(participationWithPrize).getNumber();
        verify(participationWithPrize).getAmount();
        verify(participationWithoutPrize).getNumber();
        verify(elMundoPrizesMap).get(PRIZE_NUMBER);
        verify(elMundoPrizesMap).get(NON_PRIZE_NUMBER);
        verifyNoMoreInteractions(template, participationWithoutPrize, participationWithPrize, cache, elMundoPrizesMap);
    }

    @Test
    void retrievePrizesWithoutCacheHit() {
        when(participationWithoutPrize.getNumber()).thenReturn(NON_PRIZE_NUMBER);
        when(participationWithPrize.getNumber()).thenReturn(PRIZE_NUMBER);
        when(participationWithPrize.getAmount()).thenReturn(BigDecimal.ONE);
        when(cache.get(eq(EL_MUNDO_RESPONSE_CACHE_KEY), Mockito.<Callable<Map<String, Integer>>>any())).thenAnswer(invocation -> {
            Callable<Map<String, Integer>> argument = invocation.getArgument(1);
            return argument.call();
        });
        when(elMundoPrizesResponseEntity.getBody()).thenReturn(elMundoPrizes);
        when(elMundoPrizes.getPremios()).thenReturn(elMundoPrizesMap);
        when(elMundoPrizesResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(template.exchange(eq(URL), eq(HttpMethod.GET), eq(null), eq(new ParameterizedTypeReference<ElMundoPrizes>() {
        }))).thenReturn(elMundoPrizesResponseEntity);
        List<Prize> prizes = service.retrievePrizes(Arrays.asList(participationWithoutPrize, participationWithPrize));
        assertNotNull(prizes);
        assertEquals(prizes.size(), 1);
        assertEquals(prizes.get(0).getParticipation(), participationWithPrize);

        verify(participationWithPrize).getNumber();
        verify(participationWithPrize).getAmount();
        verify(participationWithoutPrize).getNumber();
        verify(elMundoPrizesMap).get(PRIZE_NUMBER);
        verify(elMundoPrizesMap).get(NON_PRIZE_NUMBER);
        verify(template).exchange(eq(URL), eq(HttpMethod.GET), eq(null), eq(new ParameterizedTypeReference<ElMundoPrizes>() {
        }));
        verifyNoMoreInteractions(template, participationWithoutPrize, participationWithPrize, cache, elMundoPrizesMap);
    }

    @Test
    void retrievePrizesWithNullResponse() {
        when(cache.get(eq(EL_MUNDO_RESPONSE_CACHE_KEY), Mockito.<Callable<Map<String, Integer>>>any())).thenReturn(null);
        assertNull(service.retrievePrizes(Arrays.asList(participationWithoutPrize, participationWithPrize)));
        verifyNoMoreInteractions(template, participationWithoutPrize, participationWithPrize, cache, elMundoPrizesMap);
    }

    @Test
    void evictCache() {
        service.evictCache();
        verify(cache).evict(EL_MUNDO_RESPONSE_CACHE_KEY);
        verifyNoMoreInteractions(cache);
    }
}