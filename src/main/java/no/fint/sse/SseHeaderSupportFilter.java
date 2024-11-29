package no.fint.sse;

import jakarta.annotation.Priority;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

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