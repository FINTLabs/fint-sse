package no.fint.sse

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.sse.testutils.TestActions
import no.fint.sse.testutils.listeners.TestAbstractEventListener
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

    def "Return configured enum for event listener actions"() {
        when:
        def actions = listener.getActions()

        then:
        actions.size() == 1
        actions.contains(TestActions.MY_TEST_ACTION.name())
    }

    def "Returns configured enum array for event listener actions"() {
        given:
        def testListener = new TestAbstractEventListener()
        testListener.addActions(TestActions.values())

        when:
        def actions = testListener.getActions()

        then:
        actions.size() == 2
        actions.contains(TestActions.MY_TEST_ACTION.name())
        actions.contains(TestActions.HEALTH.name())
    }

    def "Returns configured string list for event listener actions"() {
        given:
        def testListener = new TestAbstractEventListener()
        testListener.addActions([TestActions.MY_TEST_ACTION.name()])

        when:
        def actions = testListener.getActions()

        then:
        actions.size() == 1
        actions.contains(TestActions.MY_TEST_ACTION.name())
    }

    def "Returns empty collection for event listener default actions"() {
        given:
        def testListener = new AbstractEventListener() {
            @Override
            void onEvent(Event event) {
            }
        }

        when:
        def names = testListener.getActions()

        then:
        names.isEmpty()
    }
}
