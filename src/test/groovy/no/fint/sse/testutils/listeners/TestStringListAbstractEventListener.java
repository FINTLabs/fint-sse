package no.fint.sse.testutils.listeners;

import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;
import no.fint.sse.testutils.TestActions;
import org.assertj.core.util.Lists;

public class TestStringListAbstractEventListener extends AbstractEventListener {

    public TestStringListAbstractEventListener() {
        super(Lists.newArrayList(TestActions.HEALTH.name(), TestActions.MY_TEST_ACTION.name()));
    }

    @Override
    public void onEvent(Event event) {
    }
}
