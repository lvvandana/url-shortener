package com.vv.urlshortener.shortlink.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vv.urlshortener.shortlink.persistence.ClickEventEntity;
import com.vv.urlshortener.shortlink.persistence.ClickEventRepository;

@Service
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final Clock clock;

    public AnalyticsService(ClickEventRepository clickEventRepository, org.springframework.beans.factory.ObjectProvider<Clock> clockProvider) {
        this.clickEventRepository = clickEventRepository;
        this.clock = clockProvider.getIfAvailable(Clock::systemUTC);
    }

    @Transactional
    public void recordClick(Long shortLinkId) {
        ClickEventEntity e = new ClickEventEntity();
        e.setShortLinkId(shortLinkId);
        e.setAccessedAt(Instant.now(clock));
        clickEventRepository.save(e);
    }
}
