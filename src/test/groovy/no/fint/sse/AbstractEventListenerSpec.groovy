package no.fint.sse

import ch.qos.logback.classic.Logger
import ch.qos.logback.core.Appender
import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.sse.testutils.TestAbstractEventListener
import no.fint.sse.testutils.TestActions
import org.glassfish.jersey.media.sse.InboundEvent
import org.slf4j.LoggerFactory
import spock.lang.Specification

class AbstractEventListenerSpec extends Specification {
    private TestAbstractEventListener listener
    private Appender appender

    void setup() {
        listener = new TestAbstractEventListener()

        appender = Mock(Appender) {
            getName() >> 'MOCK'
        }
        def logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        logger.addAppender(appender)
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
        actions.size() == 2
        actions.contains(TestActions.MY_TEST_ACTION.name())
        actions.contains(TestActions.HEALTH.name())
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
        actions.size() == 2
        actions.contains(TestActions.MY_TEST_ACTION.name())
        actions.contains(TestActions.HEALTH.name())
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

    def "Log error when the received event contains unsupported action"() {
        given:
        def inboundEvent = Mock(InboundEvent) {
            readData() >> new ObjectMapper().writeValueAsString(new Event(action: 'unknown-action'))
        }

        when:
        listener.onEvent(inboundEvent)

        then:
        1 * appender.doAppend(_)
    }

    def "Do not log error when supported action list is empty"() {
        given:
        def inboundEvent = Mock(InboundEvent) {
            readData() >> new ObjectMapper().writeValueAsString(new Event(action: TestActions.HEALTH.name()))
        }
        def testListener = new AbstractEventListener() {
            @Override
            void onEvent(Event event) {
            }
        }

        when:
        testListener.onEvent(inboundEvent)

        then:
        0 * appender.doAppend(_)
    }

    def "Do not log error when logUnsupportedActions is disabled"() {
        given:
        def inboundEvent = Mock(InboundEvent) {
            readData() >> new ObjectMapper().writeValueAsString(new Event(action: 'unknown-action'))
        }
        def testListener = new AbstractEventListener() {
            @Override
            void onEvent(Event event) {
            }
        }
        testListener.addActions(TestActions.HEALTH)

        when:
        testListener.disableLogUnsupportedActions()
        testListener.onEvent(inboundEvent)

        then:
        0 * appender.doAppend(_)
    }
}
