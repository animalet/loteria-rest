package org.albertosegura.loteria.service;

import org.albertosegura.loteria.model.Participation;
import org.albertosegura.loteria.model.Prize;
import org.albertosegura.loteria.service.external.PrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class App {
    @Autowired
    public App(PrizeService prizeService) {
        this.prizeService = prizeService;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    private final PrizeService prizeService;

    @PutMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<Prize> calculatePrizes(@RequestBody List<Participation> participationList) {
        return prizeService.retrievePrizes(participationList);
    }
}