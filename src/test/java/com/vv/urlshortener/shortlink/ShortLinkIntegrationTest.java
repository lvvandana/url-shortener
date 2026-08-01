package com.vv.urlshortener.shortlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vv.urlshortener.shortlink.api.dto.CreateShortLinkRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.Instant;

import com.vv.urlshortener.shortlink.persistence.ShortLinkEntity;
import com.vv.urlshortener.shortlink.persistence.ShortLinkRepository;
import com.vv.urlshortener.shortlink.persistence.ClickEventRepository;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@AutoConfigureMockMvc
class ShortLinkIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    // Use a locally constructed ObjectMapper to avoid depending on a Spring-managed bean
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ShortLinkRepository shortLinkRepository;

    @Autowired
    ClickEventRepository clickEventRepository;

    @BeforeEach
    void cleanDatabase() {
        // delete events first because click_events references short_links
        if (clickEventRepository != null) {
            clickEventRepository.deleteAll();
        }
        shortLinkRepository.deleteAll();
    }

    @Test
    void createGeneratedCode() throws Exception {
        CreateShortLinkRequest req = new CreateShortLinkRequest("https://example.com/page", null, null);

        MvcResult result = mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("shortUrl");
    }

    @Test
    void invalidUrlRejected() throws Exception {
        CreateShortLinkRequest req = new CreateShortLinkRequest("ftp://example.com", null, null);

        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customAliasCreationAndDuplicateRejection() throws Exception {
        CreateShortLinkRequest first = new CreateShortLinkRequest("https://example.com/x", "myalias", null);
        CreateShortLinkRequest second = new CreateShortLinkRequest("https://example.com/y", "myalias", null);

        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    @Test
    void futureExpirationAllowsRedirect() throws Exception {
        String alias = "futureAlias";
        String expires = Instant.now().plusSeconds(60).toString();
        CreateShortLinkRequest req = new CreateShortLinkRequest("https://example.com/soon", alias, expires);

        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.fasterxml.jackson.databind.node.ObjectNode(objectMapper.getNodeFactory())
                        .put("originalUrl", "https://example.com/soon").put("customAlias", alias).put("expiresAt", expires))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/" + alias))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/soon"));
    }

    @Test
    void pastExpirationRejectedOnCreate() throws Exception {
        String expires = Instant.now().minusSeconds(60).toString();
        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.fasterxml.jackson.databind.node.ObjectNode(objectMapper.getNodeFactory())
                        .put("originalUrl", "https://example.com/old").put("expiresAt", expires))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exactExpirationBoundaryIsExpired() throws Exception {
        String code = "boundary123";
        ShortLinkEntity e = new ShortLinkEntity();
        e.setCode(code);
        e.setOriginalUrl("https://example.com/boundary");
        e.setCreatedAt(Instant.now().minusSeconds(60));
        Instant now = Instant.now();
        e.setExpiresAt(now);
        e.setEnabled(true);
        shortLinkRepository.saveAndFlush(e);

        mockMvc.perform(get("/" + code)).andExpect(status().isGone());
    }

    @Test
    void expiredRedirectReturns410() throws Exception {
        // Insert expired entity directly
        ShortLinkEntity e = new ShortLinkEntity();
        e.setCode("expired123");
        e.setOriginalUrl("https://example.com/expired");
        e.setCreatedAt(Instant.now().minusSeconds(3600));
        e.setExpiresAt(Instant.now().minusSeconds(60));
        e.setEnabled(true);
        shortLinkRepository.saveAndFlush(e);

        mockMvc.perform(get("/expired123")).andExpect(status().isGone());
    }

    @Test
    void missingCodeReturns404() throws Exception {
        mockMvc.perform(get("/no-such-code")).andExpect(status().isNotFound());
    }

    @Test
    void successfulRedirect() throws Exception {
        CreateShortLinkRequest req = new CreateShortLinkRequest("https://example.com/redirect", "redir123", null);

        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/redir123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/redirect"));
    }

    @Test
    void analyticsEndpointReturnsCounts() throws Exception {
        String alias = "analytics1";
        CreateShortLinkRequest req = new CreateShortLinkRequest("https://example.com/a", alias, null);
        mockMvc.perform(post("/api/v1/short-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // perform two redirects
        mockMvc.perform(get("/" + alias)).andExpect(status().isFound());
        mockMvc.perform(get("/" + alias)).andExpect(status().isFound());

        MvcResult r = mockMvc.perform(get("/api/v1/short-links/" + alias + "/analytics"))
                .andExpect(status().isOk())
                .andReturn();

        String body = r.getResponse().getContentAsString();
        assertThat(body).contains("totalClicks");
        assertThat(body).contains("2");
    }
}