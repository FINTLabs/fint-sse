package no.fint.sse;

import lombok.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
@Setter(AccessLevel.PACKAGE)
@Builder
public class FintSseConfig {
    private static final long DEFAULT_SSE_THREAD_INTERVAL = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    @Builder.Default
    private long sseThreadInterval = DEFAULT_SSE_THREAD_INTERVAL;
    @Builder.Default
    private boolean concurrentConnections = true;

    @Singular
    private Set<String> orgIds;
}
