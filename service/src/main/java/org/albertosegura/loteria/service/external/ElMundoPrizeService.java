package org.albertosegura.loteria.service.external;

import lombok.extern.slf4j.Slf4j;
import org.albertosegura.loteria.model.ElMundoPrizes;
import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
@Configurable
@Slf4j
public class ElMundoPrizeService implements PrizeService {
    private static final String CACHE_KEY = "elMundoResponseCacheKey";
    private Cache cache;

    @Autowired
    public ElMundoPrizeService(RestTemplate template,
                               @Value("${elmundo.url}") String url,
                               @Value("#{cacheManager.getCache('${elmundo.cacheName}')}") Cache cache) {
        this.template = template;
        this.url = url;
        this.cache = cache;
    }

    private final RestTemplate template;
    private final String url;

    @Override
    public List<Prize> retrievePrizes(List<Participation> participationList) {
        requireNonNull(participationList);
        return Optional.ofNullable(cache)
                .map(cache -> cache.get(CACHE_KEY, this::callToElMundo))
                .map(prizeMap -> extractPrizes(participationList, prizeMap))
                .orElse(null);
    }

    private List<Prize> extractPrizes(List<Participation> participationList, Map<String, Integer> prizes) {
        return participationList.stream()
                .map(participation -> Optional.ofNullable(prizes.get(participation.getNumber()))
                        .map(prize -> Prize.builder().amount(
                                new BigDecimal(prize)
                                        .divide(new BigDecimal(200), 2, RoundingMode.HALF_EVEN)
                                        .multiply(participation.getAmount()))
                                .participation(participation)
                                .build())
                        .orElse(null))
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

    @Scheduled(fixedDelayString = "${elmundo.ttlCache}")
    void evictCache() {
        cache.evict(CACHE_KEY);
        log.info("Cache evicted.");
    }
}
