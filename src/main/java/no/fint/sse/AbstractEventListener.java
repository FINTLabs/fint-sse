package no.fint.sse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.EventUtil;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractEventListener implements EventListener {

    static final int MAX_UUIDS = 50;

    private boolean logUnsupportedActions = true;

    @Getter(AccessLevel.PACKAGE)
    private List<String> uuids = new ArrayList<>();

    @Getter
    private Set<String> actions = new HashSet<>();

    public void disableLogUnsupportedActions() {
        this.logUnsupportedActions = false;
    }

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
            if (actions.size() == 0 || actions.contains(event.getAction())) {
                onEvent(event);
            } else if (logUnsupportedActions) {
                log.warn("Received event (corrId:{}) with an unsupported action: {}", event.getCorrId(), event.getAction());
            }
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
