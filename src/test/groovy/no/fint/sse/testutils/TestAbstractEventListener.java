package no.fint.sse.testutils;

import lombok.Getter;
import no.fint.event.model.DefaultActions;
import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;

public class TestAbstractEventListener extends AbstractEventListener {

    @Getter
    private Event event;

    public TestAbstractEventListener() {
        addAction(TestActions.MY_TEST_ACTION).addAction(DefaultActions.HEALTH);
    }

    @Override
    public void onEvent(Event event) {
        this.event = event;
    }
}
