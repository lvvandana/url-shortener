package com.vv.urlshortener.shortlink.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickEventRepository extends JpaRepository<ClickEventEntity, Long> {

    List<ClickEventEntity> findByShortLinkIdOrderByAccessedAtAsc(Long shortLinkId);
}
