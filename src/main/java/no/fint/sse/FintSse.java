package no.fint.sse;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Collections;
import java.util.Map;

public class FintSse {
    private EventSource eventSource;
    private String sseUrl;
    private Map<String, String> headers = Collections.emptyMap();
    private EventListener listener;

    public FintSse(String sseUrl) {
        this.sseUrl = sseUrl;
    }

    public void connect(EventListener listener, Map<String, String> headers) {
        this.headers = headers;
        connect(listener);
    }

    public void connect(EventListener listener) {
        this.listener = listener;
        eventSource = EventSource.target(getWebTarget()).build();
        eventSource.register(listener);
        eventSource.open();
    }

    public void close() {
        if (eventSource != null) {
            eventSource.close();
        }
    }

    public boolean verifyConnection() {
        if (eventSource == null) {
            return false;
        }

        if (eventSource.isOpen()) {
            return true;
        } else {
            eventSource.close();
            connect(listener);
            return false;
        }
    }

    public boolean isConnected() {
        return eventSource != null && eventSource.isOpen();
    }

    private WebTarget getWebTarget() {
        SseHeaderProvider provider = () -> headers;
        Client client = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .register(new SseHeaderSupportFeature(provider))
                .build();

        return client.target(sseUrl);
    }

}
