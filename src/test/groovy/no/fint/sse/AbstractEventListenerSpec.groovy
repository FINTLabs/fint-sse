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
        listener = new TestAbstractEventListener(orgIds: ['rogfk.no'])

        appender = Mock(Appender) {
            getName() >> 'MOCK'
        }
        def logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        logger.addAppender(appender)
    }

    def "Receive unique InboundEvent and convert it to Event"() {
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

    def "Log warn when event contains unsupported orgId"() {
        given:
        def event = new Event(action: TestActions.HEALTH.name(), orgId: 'unknown-orgid')
        def inboundEvent = Mock(InboundEvent) {
            readData() >> new ObjectMapper().writeValueAsString(event)
        }

        when:
        listener.onEvent(inboundEvent)

        then:
        1 * appender.doAppend(_)
    }

    def "Do not log warn when supported list of orgIds is empty"() {
        given:
        def event = new Event(action: TestActions.HEALTH.name(), orgId: 'unknown-orgid')
        def inboundEvent = Mock(InboundEvent) {
            readData() >> new ObjectMapper().writeValueAsString(event)
        }
        def testListener = new AbstractEventListener() {
            @Override
            void onEvent(Event e) {
            }
        }

        when:
        testListener.onEvent(inboundEvent)

        then:
        0 * appender.doAppend(_)
    }

}
