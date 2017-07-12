package no.fint.sse.testutils.listeners;

import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;
import no.fint.sse.testutils.TestActions;

public class TestEnumArrayAbstractEventListener extends AbstractEventListener {

    public TestEnumArrayAbstractEventListener() {
        super(TestActions.values());
    }

    @Override
    public void onEvent(Event event) {
    }
}
