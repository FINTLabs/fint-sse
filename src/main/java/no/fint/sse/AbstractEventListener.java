package no.fint.sse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public abstract class AbstractEventListener implements EventListener {

    static final int MAX_UUIDS = 50;

    @Getter(AccessLevel.PACKAGE)
    private volatile long lastUpdated;

    @Getter(AccessLevel.PACKAGE)
    private Queue<String> uuids = new ConcurrentLinkedQueue<>();

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Set<String> orgIds = new HashSet<>();

    @Override
    public void onEvent(InboundEvent inboundEvent) {
        lastUpdated = System.nanoTime();
        if (inboundEvent.isEmpty())
            return;
        String json = inboundEvent.readData();
        Event event = EventUtil.toEvent(json);
        if (isNewCorrId(event.getCorrId()) && containsOrgId(event)) {
            onEvent(event);
        }
    }

    private boolean containsOrgId(Event event) {
        boolean containsOrgId = orgIds.size() == 0 || orgIds.contains(event.getOrgId());
        if (!containsOrgId) {
            log.warn("Received event (corrId:{}) with an unsupported orgId: {}", event.getCorrId(), event.getOrgId());
        }
        return containsOrgId;
    }

    private boolean isNewCorrId(String corrId) {
        boolean contains = uuids.contains(corrId);
        if (contains) {
            return false;
        } else {
            try {
                return uuids.offer(corrId);
            } finally {
                while (uuids.size() > MAX_UUIDS) {
                    uuids.poll();
                }
            }
        }
    }

    public abstract void onEvent(Event event);
}
