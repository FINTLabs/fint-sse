package no.fint.sse

import no.fint.event.model.Event
import no.fint.sse.oauth.TokenService
import no.fint.sse.testutils.TestAbstractEventListener
import no.fint.sse.testutils.TestSseServer
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@SpringBootTest(classes = TestSseServer, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FintSseSpec extends Specification {
    @LocalServerPort
    private int port

    private FintSse fintSse
    private TestAbstractEventListener listener

    void setup() {
        listener = new TestAbstractEventListener()

        def config = FintSseConfig.builder().concurrentConnections(false).orgIds(['rogfk.no'] as String[]).build()
        fintSse = new FintSse("http://localhost:${port}/sse", config)
    }

    def "Create default instance"() {
        when:
        def fintSseInst = new FintSse("http://localhost")

        then:
        fintSseInst != null
    }

    def "Connect event listener"() {
        when:
        fintSse.connect(listener)

        then:
        fintSse.isConnected()
    }

    def "Connect event listener with header"() {
        when:
        fintSse.connect(listener, ['x-org-id': 'mock.no'])

        then:
        fintSse.isConnected()
    }

    def "Connect event listener without actions configured"() {
        given:
        def testListener = new AbstractEventListener() {
            @Override
            void onEvent(Event event) {
            }
        }

        when:
        fintSse.connect(testListener)

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
        def config = FintSseConfig.builder().sseThreadInterval(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)).build()
        fintSse = new FintSse("http://localhost:${port}/sse/%s", config)

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

    def "Add authorization header when TokenService is set"() {
        given:
        def url = "http://localhost:${port}/oauth/sse/%s"
        def tokenService = Mock(TokenService)
        fintSse = new FintSse(url, tokenService)

        when:
        fintSse.connect(listener)

        then:
        1 * tokenService.getAccessToken(url) >> 'test123'
        fintSse.isConnected()
    }
}
