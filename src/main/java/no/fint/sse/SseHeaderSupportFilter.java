package no.fint.sse;

import javax.annotation.Priority;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

@Priority(Priorities.HEADER_DECORATOR)
class SseHeaderSupportFilter implements ClientRequestFilter {

    private final SseHeaderProvider provider;

    SseHeaderSupportFilter(@NotNull SseHeaderProvider provider) {
        this.provider = provider;
    }

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        provider.getHeaders().forEach((header, headerValue) -> request.getHeaders().add(header, headerValue));
    }
}