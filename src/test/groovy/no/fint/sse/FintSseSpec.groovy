package no.fint.sse

import no.fint.sse.testutils.TestEventListener
import spock.lang.Specification

class FintSseSpec extends Specification {
    private FintSse fintSse
    private TestEventListener listener

    void setup() {
        listener = new TestEventListener()
        fintSse = new FintSse('http://localhost')
    }

    def "Connect event listener"() {
        when:
        fintSse.connect(listener)

        then:
        !fintSse.isConnected()
    }

    def "Verify connection without connect"() {
        when:
        def connected = fintSse.verifyConnection()

        then:
        !connected
    }

    def "Verify connection after connect"() {
        when:
        fintSse.connect(listener, ['x-org-id': 'mock.no'])
        def connected = fintSse.verifyConnection()

        then:
        !connected
    }

    def "Check if connected without connect"() {
        when:
        def connected = fintSse.isConnected()

        then:
        !connected
    }

    def "Check if connected with connect"() {
        when:
        fintSse.connect(listener)
        def connected = fintSse.isConnected()

        then:
        !connected
    }

    def "Close connection"() {
        when:
        fintSse.connect(listener)
        fintSse.close()

        then:
        noExceptionThrown()
    }
}
