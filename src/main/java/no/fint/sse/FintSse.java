package no.fint.sse;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import no.fint.oauth.TokenService;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class FintSse {
    private FintSseConfig config;

    @Getter
    private FintSseClient fintSseClient;

    private List<EventSource> eventSources = new ArrayList<>();
    @Getter
    private String sseUrl;
    private TokenService tokenService;

    private AtomicBoolean logConnectionInfo = new AtomicBoolean(true);

    private ScheduledExecutorService scheduledExecutorService;

    public FintSse(String sseUrl) {
        this(sseUrl, null, FintSseConfig.builder().build());
    }

    public FintSse(String sseUrl, TokenService tokenService) {
        this(sseUrl, tokenService, FintSseConfig.builder().build());
    }

    public FintSse(String sseUrl, FintSseConfig config) {
        this(sseUrl, null, config);
    }

    public FintSse(String sseUrl, TokenService tokenService, FintSseConfig config) {
        this.config = config;
        this.sseUrl = sseUrl;
        verifySseUrl();
        this.tokenService = tokenService;
        if (config.isConcurrentConnections()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        }
    }

    private void verifySseUrl() {
        if (sseUrl == null || sseUrl.equals("")) {
            throw new IllegalArgumentException("SSE url cannot be null or empty");
        } else if (!sseUrl.endsWith("%s")) {
            log.info("No placeholder found in SSE url, disabling concurrent connections");
            config.setConcurrentConnections(false);
        }
    }

    public void connect(AbstractEventListener listener, Map<String, String> headers) {
        fintSseClient = new FintSseClient(listener, headers);
        connect();
    }

    public void connect(AbstractEventListener listener) {
        fintSseClient = new FintSseClient(listener);
        connect();
    }

    private void connect() {
        createEventSource();
        if (config.isConcurrentConnections()) {
            scheduledExecutorService.schedule(this::createEventSource, config.getSseThreadInterval(), TimeUnit.MILLISECONDS);
        }
    }

    @Synchronized
    private void createEventSource() {
        AbstractEventListener listener = fintSseClient.getListener();
        listener.setOrgIds(config.getOrgIds());
        EventSource eventSource = EventSource.target(getWebTarget()).build();
        if (logConnectionInfo.get()) {
            log.info("Registering listener {}", listener.getClass().getSimpleName());
        }

        eventSource.register(listener);
        eventSource.open();
        eventSources.add(eventSource);
        if (eventSource.isOpen()) {
            logConnectionInfo.set(true);
        } else {
            log.warn("Unable to connect to {}", sseUrl);
            logConnectionInfo.set(false);
        }
    }

    public void close() {
        for (EventSource eventSource : eventSources) {
            eventSource.close();
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }

    public boolean verifyConnection() {
        for (int i = 0; i < eventSources.size(); i++) {
            EventSource eventSource = eventSources.get(i);
            if (!eventSource.isOpen()) {
                eventSources.remove(i);
                eventSource.close();
                createEventSource();
                return false;
            }
        }

        return true;
    }

    public boolean isConnected() {
        return eventSources.size() > 0 && eventSources.stream().allMatch(EventSource::isOpen);
    }

    public long getAge() {
        if (fintSseClient == null) {
            return Long.MAX_VALUE;
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - fintSseClient.getListener().getLastUpdated());
    }

    private WebTarget getWebTarget() {
        Map<String, String> headers = new HashMap<>(fintSseClient.getHeaders());
        if (tokenService != null) {
            String accessTokenRequestUrl = config.getAccessTokenRequestUrl(sseUrl);
            log.debug("Adding bearer token in Authorization header, token url: {}", accessTokenRequestUrl);
            String bearerToken = String.format("Bearer %s", tokenService.getAccessToken(accessTokenRequestUrl));
            headers.put(HttpHeaders.AUTHORIZATION, bearerToken);
        }

        SseHeaderProvider provider = () -> headers;
        Client client = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .register(new SseHeaderSupportFeature(provider))
                .build();

        return client.target(createConnectSseUrl());
    }

    private String createConnectSseUrl() {
        if (sseUrl.endsWith("%s")) {
            String connectionId = UUID.randomUUID().toString();
            String connectionUrl = String.format(sseUrl, connectionId);
            if (logConnectionInfo.get()) {
                log.info("Placeholder found in sseUrl, generated connection id: {}", connectionId);
            }
            return connectionUrl;
        } else {
            return sseUrl;
        }
    }
}