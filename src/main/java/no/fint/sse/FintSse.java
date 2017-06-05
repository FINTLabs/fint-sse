package no.fint.sse;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FintSse {
    private long sseThreadInterval = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
    private FintSseClient fintSseClient;

    private List<EventSource> eventSources = new ArrayList<>();
    private String sseUrl;

    public FintSse(String sseUrl) {
        this.sseUrl = sseUrl;
    }

    public FintSse(String sseUrl, long sseThreadInterval) {
        this.sseUrl = sseUrl;
        this.sseThreadInterval = sseThreadInterval;
    }

    public void connect(EventListener listener, Map<String, String> headers, String... names) {
        fintSseClient = new FintSseClient(listener, headers, names);
        connect(listener);
    }

    public void connect(EventListener listener, String... names) {
        fintSseClient = new FintSseClient(listener, names);
        createEventSource();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(sseThreadInterval);
                createEventSource();
            } catch (InterruptedException ignored) {
            }
        });
    }

    private synchronized void createEventSource() {
        String[] names = fintSseClient.getNames();
        EventSource eventSource = EventSource.target(getWebTarget()).build();
        if (names.length == 0) {
            eventSource.register(fintSseClient.getListener());
        } else {
            String first = names[0];
            String[] rest = Arrays.copyOfRange(names, 1, names.length);
            eventSource.register(fintSseClient.getListener(), first, rest);
        }

        eventSource.open();
        eventSources.add(eventSource);
    }

    public void close() {
        eventSources.forEach(EventSource::close);
    }

    public boolean verifyConnection() {
        if (eventSources.size() == 0) {
            return false;
        }

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
        SseHeaderProvider provider = () -> fintSseClient.getHeaders();
        Client client = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .register(new SseHeaderSupportFeature(provider))
                .build();

        return client.target(sseUrl);
    }

}
