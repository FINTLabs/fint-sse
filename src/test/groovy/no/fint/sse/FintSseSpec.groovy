package no.fint.sse

import no.fint.sse.testutils.TestEventListener
import no.fint.sse.testutils.TestSseServer
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = TestSseServer, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FintSseSpec extends Specification {
    @LocalServerPort
    private int port

    private FintSse fintSse
    private TestEventListener listener

    void setup() {
        listener = new TestEventListener()
        fintSse = new FintSse("http://localhost:${port}/sse")
        fintSse.disableConcurrentConnections()
    }

    def "Connect event listener"() {
        when:
        fintSse.connect(listener)

        then:
        fintSse.isConnected()
    }

    def "Connect event listener with event name"() {
        when:
        fintSse.connect(listener, 'test1', 'test2')

        then:
        fintSse.isConnected()
    }

    def "Verify connection without connect"() {
        when:
        def connected = fintSse.verifyConnection()

        then:
        connected
    }

    def "Verify connection after connect"() {
        when:
        fintSse.connect(listener, ['x-org-id': 'mock.no'])
        def connected = fintSse.verifyConnection()

        then:
        connected
    }

    def "Verify connection when connection is closed"() {
        given:
        fintSse.connect(listener)
        fintSse.close()

        when:
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
        connected
    }

    def "Close connection"() {
        when:
        fintSse.connect(listener)
        fintSse.close()

        then:
        noExceptionThrown()
    }

    def "Verify connection for concurrent connections"() {
        given:
        fintSse = new FintSse("http://localhost:${port}/sse/%s", 5)

        when:
        fintSse.connect(listener)
        Thread.sleep(10)
        def connected = fintSse.isConnected()
        def connectionVerified = fintSse.verifyConnection()
        fintSse.close()

        then:
        connected
        connectionVerified
    }

    def "Throw IllegalArgumentException when sseUrl is null"() {
        when:
        new FintSse(null)

        then:
        thrown(IllegalArgumentException)
    }
}
