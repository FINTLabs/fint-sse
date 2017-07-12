package no.fint.sse

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.sse.testutils.TestAbstractEventListener
import org.glassfish.jersey.media.sse.InboundEvent
import spock.lang.Specification

class AbstractEventListenerSpec extends Specification {
    private TestAbstractEventListener listener

    void setup() {
        listener = new TestAbstractEventListener()
    }

    def "Receive unique InbountEvent and convert it to Event"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH.name(), 'test')
        def inboundEvent = Mock(InboundEvent) {
            readData() >> new ObjectMapper().writeValueAsString(event)
        }

        when:
        1.upto(5) {
            listener.onEvent(inboundEvent)
        }
        def receivedEvent = listener.getEvent()

        then:
        listener.uuids.size() == 1
        listener.uuids[0] == event.getCorrId()
        receivedEvent.corrId == event.corrId
    }

    def "Remove old corrIds"() {
        when:
        1.upto(AbstractEventListener.MAX_UUIDS + 5) {
            def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH.name(), 'test')
            def inboundEvent = Mock(InboundEvent) {
                readData() >> new ObjectMapper().writeValueAsString(event)
            }

            listener.onEvent(inboundEvent)
        }

        then:
        listener.uuids.size() == AbstractEventListener.MAX_UUIDS
    }

    def "Returns collection for event listener default names"() {
        when:
        def names = listener.getNames()

        then:
        names.size() == 1
        names[0] == 'HEALTH'
    }
}
