package no.fint.sse

import no.fint.sse.testutils.TestAbstractEventListener
import spock.lang.Specification

class FintSseClientSpec extends Specification {

    def "Create new FintSseClient with AbstractEventListener names"() {
        when:
        def client = new FintSseClient(new TestAbstractEventListener())

        then:
        client.names.size() == 1
        client.names[0] == 'HEALTH'
    }
}
