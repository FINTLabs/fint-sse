package no.fint.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class FintSseClient {
    private AbstractEventListener listener;
    private Map<String, String> headers;

    public FintSseClient(AbstractEventListener listener) {
        this(listener, new HashMap<>());
    }
}
