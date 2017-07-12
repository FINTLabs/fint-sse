package no.fint.sse;

import org.glassfish.jersey.media.sse.EventListener;

import java.util.*;

class FintSseClient {
    private EventListener listener;
    private String[] names;
    private Map<String, String> headers;

    public FintSseClient(EventListener listener, String[] names) {
        this(listener, new HashMap<>(), names);
    }

    public FintSseClient(EventListener listener, Map<String, String> headers, String[] names) {
        this.listener = listener;
        this.headers = headers;
        this.names = names;
        addListenerNames();
    }

    private void addListenerNames() {
        if (listener instanceof AbstractEventListener) {
            List<String> nameList = new ArrayList<>(Arrays.asList(names));
            nameList.addAll(((AbstractEventListener) listener).getNames());
            String[] tempNames = new String[nameList.size()];
            nameList.toArray(tempNames);
            names = tempNames;
        }
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
