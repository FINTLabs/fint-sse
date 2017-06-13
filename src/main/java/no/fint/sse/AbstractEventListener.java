package no.fint.sse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEventListener implements EventListener {

    @Getter(AccessLevel.PACKAGE)
    private List<String> uuids = new ArrayList<>();

    @Override
    public void onEvent(InboundEvent inboundEvent) {
        String json = inboundEvent.readData();
        Event event = EventUtil.toEvent(json);
        if (isNewCorrId(event.getCorrId())) {
            onEvent(event);
        }
    }

    @Synchronized
    private boolean isNewCorrId(String corrId) {
        boolean contains = uuids.contains(corrId);
        if (contains) {
            return false;
        } else {
            if (uuids.size() >= 10) {
                uuids.remove(0);
            }

            uuids.add(corrId);
            return true;
        }
    }

    public abstract void onEvent(Event event);
}
