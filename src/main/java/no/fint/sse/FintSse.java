package no.fint.sse;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import no.fint.oauth.TokenService;
import org.glassfish.jersey.media.sse.EventListener;
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
import java.util.stream.Collectors;

@Slf4j
public class FintSse {
    private static final long DEFAULT_SSE_THREAD_INTERVAL = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    private long sseThreadInterval;
    private boolean concurrentConnections = true;

    private FintSseClient fintSseClient;

    private List<EventSource> eventSources = new ArrayList<>();
    private String sseUrl;
    private TokenService tokenService;

    private AtomicBoolean logConnectionId = new AtomicBoolean(true);

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

    public void connect(EventListener listener, Map<String, String> headers, String... names) {
        fintSseClient = new FintSseClient(listener, headers, names);
        connect();
    }

    public void connect(EventListener listener, Map<String, String> headers, Enum name, Enum... names) {
        fintSseClient = new FintSseClient(listener, headers, getEnumNames(name, names));
        connect();
    }

    public void connect(EventListener listener, String... names) {
        fintSseClient = new FintSseClient(listener, names);
        connect();
    }

    public void connect(EventListener listener, Enum name, Enum... names) {
        fintSseClient = new FintSseClient(listener, getEnumNames(name, names));
        connect();
    }

    private String[] getEnumNames(Enum name, Enum... names) {
        List<String> stringNames = new ArrayList<>();
        stringNames.add(name.name());
        List<String> tempNames = Arrays.stream(names).map(Enum::name).collect(Collectors.toList());
        stringNames.addAll(tempNames);
        return stringNames.toArray(new String[0]);
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
        String[] names = fintSseClient.getNames();
        EventListener listener = fintSseClient.getListener();
        EventSource eventSource = EventSource.target(getWebTarget()).build();
        if (names.length == 0) {
            log.info("Registering listener {}", listener.getClass().getSimpleName());
            eventSource.register(listener);
        } else {
            log.info("Registering listener {} for names:{}", listener.getClass().getSimpleName(), String.join(", ", names));
            String first = names[0];
            String[] rest = Arrays.copyOfRange(names, 1, names.length);
            eventSource.register(listener, first, rest);
        }

        eventSource.open();
        eventSources.add(eventSource);
        if (eventSource.isOpen()) {
            logConnectionId.set(true);
        } else {
            log.warn("Unable to connect to {}", sseUrl);
            logConnectionId.set(false);
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
            if (logConnectionId.get()) {
                log.info("Placeholder found in sseUrl, generated connection id: {}", connectionId);
            }
            return connectionUrl;
        } else {
            return sseUrl;
        }
    }
}