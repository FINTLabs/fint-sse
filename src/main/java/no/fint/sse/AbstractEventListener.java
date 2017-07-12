package no.fint.sse;

import jersey.repackaged.com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractEventListener implements EventListener {

    static final int MAX_UUIDS = 50;

    @Getter(AccessLevel.PACKAGE)
    private List<String> uuids = new ArrayList<>();

    @Getter
    private List<String> actions;

    public AbstractEventListener() {
        actions = Collections.emptyList();
    }

    public AbstractEventListener(Enum action) {
        actions = Lists.newArrayList(action.name());
    }

    public AbstractEventListener(Enum[] actions) {
        this.actions = Arrays.stream(actions).map(Enum::name).collect(Collectors.toList());
    }

    public AbstractEventListener(List<String> actions) {
        this.actions = actions;
    }

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
            if (uuids.size() >= MAX_UUIDS) {
                uuids.remove(0);
            }

            uuids.add(corrId);
            return true;
        }
    }

    public abstract void onEvent(Event event);
}
