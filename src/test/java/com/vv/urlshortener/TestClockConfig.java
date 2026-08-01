package com.vv.urlshortener;

import java.time.Clock;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration to provide a deterministic Clock bean for tests.
 * Using system UTC reduces time-zone surprises in tests that assert
 * boundary conditions.
 */
@TestConfiguration
public class TestClockConfig {

    @Bean
    public Clock testClock() {
        return Clock.systemUTC();
    }
}
