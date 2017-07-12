package no.fint.sse.testutils;

import lombok.Getter;
import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;

public class TestAbstractEventListener extends AbstractEventListener {

    @Getter
    private Event event;

    @Override
    public Enum[] getEnumActions() {
        return TestActions.values();
    }

    @Override
    public void onEvent(Event event) {
        this.event = event;
    }
}
