package com.vv.urlshortener.shortlink.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortLinkRepository extends JpaRepository<ShortLinkEntity, Long> {

    boolean existsByCode(String code);

    Optional<ShortLinkEntity> findByCodeAndEnabledTrue(String code);
}
