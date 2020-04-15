package org.albertosegura.loteria.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigurationTest {

    private AppConfiguration appConfiguration;

    @BeforeEach
    void setUp() {
        appConfiguration = new AppConfiguration();
    }

    @Test
    void getRestTemplate() {
        assertNotNull(appConfiguration.getRestTemplate());
    }
}