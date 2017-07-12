package no.fint.sse.testutils.listeners;

import lombok.Getter;
import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;
import no.fint.sse.testutils.TestActions;

public class TestAbstractEventListener extends AbstractEventListener {

    @Getter
    private Event event;

    public TestAbstractEventListener() {
        super(TestActions.MY_TEST_ACTION);
    }

    @Override
    public void onEvent(Event event) {
        this.event = event;
    }
}
