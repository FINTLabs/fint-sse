package no.fint.sse

import spock.lang.Specification

class FintSseConfigSpec extends Specification {

    def "Create new instance of FintSseConfig"() {
        when:
        def defaultInstance = FintSseConfig.builder().build()

        then:
        defaultInstance.concurrentConnections
        defaultInstance.sseThreadInterval == 600000L
        defaultInstance.orgIds.size() == 0
    }
}
