package no.fint.sse;

import org.glassfish.jersey.media.sse.EventListener;

import java.util.HashMap;
import java.util.Map;

class FintSseClient {
    private EventListener listener;
    private String[] names;
    private Map<String, String> headers;

    public FintSseClient(EventListener listener, String[] names) {
        this.listener = listener;
        this.headers = new HashMap<>();
        this.names = names;
    }

    public FintSseClient(EventListener listener, Map<String, String> headers, String[] names) {
        this.listener = listener;
        this.headers = headers;
        this.names = names;
    }

    public EventListener getListener() {
        return listener;
    }

    public String[] getNames() {
        return names;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
