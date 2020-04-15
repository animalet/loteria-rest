package org.albertosegura.loteria.service.external;

import lombok.extern.slf4j.Slf4j;
import org.albertosegura.loteria.model.ElMundoPrizes;
import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
@Configurable
@Slf4j
public class ElMundoPrizeService implements PrizeService {
    private static final String CACHE_KEY = "elMundoResponseCacheKey";
    private Cache cache;

    @Autowired
    public ElMundoPrizeService(RestTemplate template, @Value("${elmundo.url}") String url, CacheManager cacheManager, @Value("${elmundo.cacheName}") String cacheName) {
        this.template = template;
        this.url = url;
        this.cache = cacheManager.getCache(cacheName);
    }

    private final RestTemplate template;
    private final String url;

    @Override
    public List<Prize> retrievePrizes(List<Participation> participationList) {
        requireNonNull(participationList);
        HashMap<String, Integer> elMundoPrizes = Optional.ofNullable(cache).map(cache -> cache.get(CACHE_KEY, this::callToElMundo)).orElseGet(() -> {
            Optional.ofNullable(cache).ifPresent(Cache::invalidate);
            return null;
        });
        return elMundoPrizes == null ? null : extractPrizes(participationList, elMundoPrizes);
    }

    private List<Prize> extractPrizes(List<Participation> participationList, Map<String, Integer> prizes) {
        return participationList.stream()
                .map(participation -> {
                    Integer prize = prizes.get(participation.getNumber());
                    return prize == null ? null : Prize.builder().amount(
                            new BigDecimal(prize)
                                    .divide(new BigDecimal(200), 2, RoundingMode.HALF_EVEN)
                                    .multiply(participation.getAmount()))
                            .participation(participation)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private HashMap<String, Integer> callToElMundo() {
        ResponseEntity<ElMundoPrizes> response = template.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        if (log.isInfoEnabled()) {
            log.info("Response from El Mundo: {}", response.getStatusCode());
        }
        return Optional.ofNullable(response.getBody()).map(ElMundoPrizes::getPremios).orElse(null);
    }
}
