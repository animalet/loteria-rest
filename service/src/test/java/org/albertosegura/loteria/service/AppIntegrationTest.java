package org.albertosegura.loteria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;
import org.albertosegura.loteria.service.external.PrizeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@Slf4j
class AppIntegrationTest {
    public static final String PRIZE_NUMBER = "123";
    public static final BigDecimal BIG_PRIZE = new BigDecimal("100000");
    private MockMvc mvc;

    @MockBean
    private PrizeService prizeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new App(prizeService)).build();
    }

    @Test
    void calculatePrizesResponse() throws Exception {
        Participation winner = Participation.builder().amount(BigDecimal.TEN).number(PRIZE_NUMBER).build();
        List<Prize> prizeList = Collections.singletonList(Prize.builder().amount(BIG_PRIZE).participation(winner).build());
        when(prizeService.retrievePrizes(any())).thenAnswer(invocation -> {
            log.info("Mock service reached.");
            return prizeList;
        });

        List<Participation> participationList = Collections.singletonList(Participation.builder()
                .amount(BigDecimal.TEN)
                .number(PRIZE_NUMBER)
                .build());
        mvc.perform(put("/")
                .content(objectMapper.writeValueAsBytes(participationList)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(prizeList)))
                .andDo(result -> log.info(new String(result.getResponse().getContentAsByteArray())));
    }
}