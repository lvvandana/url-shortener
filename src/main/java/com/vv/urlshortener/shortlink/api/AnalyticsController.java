package com.vv.urlshortener.shortlink.api;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vv.urlshortener.shortlink.persistence.ClickEventRepository;
import com.vv.urlshortener.shortlink.persistence.ShortLinkEntity;
import com.vv.urlshortener.shortlink.persistence.ShortLinkRepository;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/short-links")
public class AnalyticsController {

    private final ShortLinkRepository shortLinkRepository;
    private final ClickEventRepository clickEventRepository;

    public record ClicksByDay(String date, long count) {}

    public record AnalyticsResponse(String code, long totalClicks, java.time.Instant lastAccessedAt, List<ClicksByDay> clicksByDay) {}

    public AnalyticsController(ShortLinkRepository shortLinkRepository, ClickEventRepository clickEventRepository) {
        this.shortLinkRepository = shortLinkRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @GetMapping("/{code}/analytics")
    public ResponseEntity<AnalyticsResponse> analytics(@PathVariable @NotBlank String code) {
        ShortLinkEntity link = shortLinkRepository.findByCodeAndEnabledTrue(code)
                .orElseThrow(() -> new com.vv.urlshortener.shortlink.domain.ShortLinkNotFoundException("Short link not found"));

        List<com.vv.urlshortener.shortlink.persistence.ClickEventEntity> events = clickEventRepository.findByShortLinkIdOrderByAccessedAtAsc(link.getId());

        long total = events.size();
        java.time.Instant last = events.isEmpty() ? null : events.get(events.size() - 1).getAccessedAt();

        Map<java.time.LocalDate, Long> grouped = events.stream()
                .collect(Collectors.groupingBy(e -> e.getAccessedAt().atZone(ZoneOffset.UTC).toLocalDate(), Collectors.counting()));

        List<ClicksByDay> byDay = grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(en -> new ClicksByDay(en.getKey().toString(), en.getValue()))
                .collect(Collectors.toList());

        AnalyticsResponse resp = new AnalyticsResponse(link.getCode(), total, last, byDay);
        return ResponseEntity.ok(resp);
    }
}
