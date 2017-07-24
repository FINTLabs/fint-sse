package no.fint.sse;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    @Builder.Default
    private String[] orgIds = new String[]{};

    public Set<String> getOrgIds() {
        List<String> orgIdList = Arrays.asList(orgIds);
        return new HashSet<>(orgIdList);
    }

}
