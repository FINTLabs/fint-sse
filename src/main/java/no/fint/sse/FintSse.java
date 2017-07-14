package no.fint.sse;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import no.fint.oauth.TokenService;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.springframework.http.HttpHeaders;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class FintSse {
    private static final long DEFAULT_SSE_THREAD_INTERVAL = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    private long sseThreadInterval;
    private boolean concurrentConnections = true;

    private FintSseClient fintSseClient;

    private List<EventSource> eventSources = new ArrayList<>();
    private String sseUrl;
    private TokenService tokenService;

    private AtomicBoolean logConnectionInfo = new AtomicBoolean(true);

    public FintSse(String sseUrl) {
        this(sseUrl, null, DEFAULT_SSE_THREAD_INTERVAL);
    }

    public FintSse(String sseUrl, TokenService tokenService) {
        this(sseUrl, tokenService, DEFAULT_SSE_THREAD_INTERVAL);
    }

    public FintSse(String sseUrl, long sseThreadInterval) {
        this(sseUrl, null, sseThreadInterval);
    }

    public FintSse(String sseUrl, TokenService tokenService, long sseThreadInterval) {
        this.sseUrl = sseUrl;
        verifySseUrl();
        this.tokenService = tokenService;
        this.sseThreadInterval = sseThreadInterval;
    }

    private void verifySseUrl() {
        if (sseUrl == null || sseUrl.equals("")) {
            throw new IllegalArgumentException("SSE url cannot be null or empty");
        } else if (!sseUrl.endsWith("%s")) {
            log.info("No placeholder found in SSE url, disabling concurrent connections");
            this.concurrentConnections = false;
        }
    }

    public void disableConcurrentConnections() {
        this.concurrentConnections = false;
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
        if (concurrentConnections) {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.submit(() -> {
                try {
                    Thread.sleep(sseThreadInterval);
                    createEventSource();
                } catch (InterruptedException ignored) {
                }
            });
        }
    }

    @Synchronized
    private void createEventSource() {
        AbstractEventListener listener = fintSseClient.getListener();
        Set<String> actions = listener.getActions();
        EventSource eventSource = EventSource.target(getWebTarget()).build();
        if (actions.size() == 0) {
            if (logConnectionInfo.get()) {
                log.info("Registering listener {}", listener.getClass().getSimpleName());
            }
            eventSource.register(listener);
        } else {
            if (logConnectionInfo.get()) {
                log.info("Registering listener {} for names:{}", listener.getClass().getSimpleName(), actions);
            }
            List<String> actionList = new ArrayList<>(actions);
            String first = actionList.get(0);
            List<String> restList = actionList.subList(1, actions.size());
            String[] rest = new String[restList.size()];
            restList.toArray(rest);
            eventSource.register(listener, first, rest);
        }

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
        for (int i = 0; i < eventSources.size(); i++) {
            eventSources.get(i).close();
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

    private WebTarget getWebTarget() {
        Map<String, String> headers = fintSseClient.getHeaders();
        if (tokenService != null) {
            log.debug("Adding bearer token in Authorization header");
            String bearerToken = String.format("Bearer %s", tokenService.getAccessToken());
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