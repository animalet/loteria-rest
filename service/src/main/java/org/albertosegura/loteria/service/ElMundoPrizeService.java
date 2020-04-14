package org.albertosegura.loteria.service;

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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
@Configurable
@Slf4j
public class ElMundoPrizeService implements PrizeService {
    private static final String CACHE_KEY = "elMundoResponseCacheKey";

    @Autowired
    public ElMundoPrizeService(RestTemplate template, @Value("${elmundo.url}") String url, CacheManager cacheManager, @Value("${elmundo.cacheName}") String cacheName) {
        this.template = template;
        this.url = url;
        this.cacheManager = cacheManager;
        this.cacheName = cacheName;
    }

    private final RestTemplate template;
    private final String url;
    private final CacheManager cacheManager;
    private final String cacheName;

    @Override
    public List<Prize> retrievePrizes(List<Participation> participationList) {
        requireNonNull(participationList);
        Optional<HashMap<String, Integer>> elMundoPrizes = getElMundoPrizes();
        if (elMundoPrizes.isEmpty()) {
            Optional.ofNullable(cacheManager.getCache(cacheName)).ifPresent(Cache::invalidate);
            return null;
        } else {
            return elMundoPrizes.map(prizes -> extractPrizes(participationList, prizes)).orElse(null);
        }
    }


    private Optional<HashMap<String, Integer>> getElMundoPrizes() {
        return Optional.ofNullable(cacheManager.getCache(cacheName)).map(cache -> cache.get(CACHE_KEY, this::getHashMapFromURL));
    }

    private HashMap<String, Integer> getHashMapFromURL() {
        ResponseEntity<ElMundoPrizes> response = template.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
        if (log.isInfoEnabled()) {
            log.info("Response from El Mundo: {}", response.getStatusCode());
        }
        return Optional.ofNullable(response.getBody()).map(ElMundoPrizes::getPremios).orElse(null);
    }

    private List<Prize> extractPrizes(List<Participation> participationList, HashMap<String, Integer> prizes) {
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
}
