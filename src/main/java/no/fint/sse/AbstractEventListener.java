package no.fint.sse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEventListener implements EventListener {

    static final int MAX_UUIDS = 50;

    @Getter(AccessLevel.PACKAGE)
    private List<String> uuids = new ArrayList<>();

    @Getter
    private Set<String> actions = new HashSet<>();

    public AbstractEventListener addAction(Enum action) {
        actions.add(action.name());
        return this;
    }

    public AbstractEventListener addActions(Enum[] actions) {
        List<String> actionList = Arrays.stream(actions).map(Enum::name).collect(Collectors.toList());
        this.actions.addAll(actionList);
        return this;
    }

    public AbstractEventListener addActions(List<String> actions) {
        this.actions.addAll(actions);
        return this;
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
