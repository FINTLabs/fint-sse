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

    @Getter(AccessLevel.PACKAGE)
    private List<String> uuids = new ArrayList<>();

    @Getter
    private Set<String> actions = new HashSet<>();

    @Getter
    private Set<String> orgIds = new HashSet<>();

    public AbstractEventListener addActions(Enum... actions) {
        List<String> actionList = Arrays.stream(actions).map(Enum::name).collect(Collectors.toList());
        this.actions.addAll(actionList);
        return this;
    }

    public AbstractEventListener addActions(List<String> actions) {
        this.actions.addAll(actions);
        return this;
    }

    public AbstractEventListener addOrgIds(String... orgIds) {
        List<String> orgIdList = Arrays.asList(orgIds);
        this.orgIds.addAll(orgIdList);
        return this;
    }

    @Override
    public void onEvent(InboundEvent inboundEvent) {
        String json = inboundEvent.readData();
        Event event = EventUtil.toEvent(json);
        if (isNewCorrId(event.getCorrId()) && containsAction(event) && containsOrgId(event)) {
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

    private boolean containsAction(Event event) {
        boolean containsAction = actions.size() == 0 || actions.contains(event.getAction());
        if (!containsAction) {
            log.warn("Received event (corrId:{}) with an unsupported action: {}", event.getCorrId(), event.getAction());
        }
        return containsAction;
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
