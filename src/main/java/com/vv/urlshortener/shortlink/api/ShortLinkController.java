package com.vv.urlshortener.shortlink.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vv.urlshortener.shortlink.api.dto.CreateShortLinkRequest;
import com.vv.urlshortener.shortlink.api.dto.ShortLinkResponse;
import com.vv.urlshortener.shortlink.application.ShortLinkService;

@RestController
@RequestMapping("/api/v1/short-links")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    public ResponseEntity<ShortLinkResponse> create(@Valid @RequestBody CreateShortLinkRequest request) {
        ShortLinkResponse response = shortLinkService.create(request);
        URI location = URI.create(response.shortUrl());
        return ResponseEntity.created(location).body(response);
    }
}
