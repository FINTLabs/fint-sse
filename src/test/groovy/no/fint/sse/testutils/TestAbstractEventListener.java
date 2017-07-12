package no.fint.sse.testutils;

import lombok.Getter;
import no.fint.event.model.Event;
import no.fint.sse.AbstractEventListener;
import org.assertj.core.util.Lists;

import java.util.List;

public class TestAbstractEventListener extends AbstractEventListener {

    @Getter
    private Event event;

    @Override
    public List<String> getNames() {
        return Lists.newArrayList("HEALTH");
    }

    @Override
    public void onEvent(Event event) {
        this.event = event;
    }
}
