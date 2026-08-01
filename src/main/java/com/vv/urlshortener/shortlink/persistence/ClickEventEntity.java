package com.vv.urlshortener.shortlink.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "click_events")
public class ClickEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_link_id", nullable = false)
    private Long shortLinkId;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    public Long getId() {
        return id;
    }

    public Long getShortLinkId() {
        return shortLinkId;
    }

    public void setShortLinkId(Long shortLinkId) {
        this.shortLinkId = shortLinkId;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(Instant accessedAt) {
        this.accessedAt = accessedAt;
    }
}
