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

    def "Create new instance with array of orgIds"() {
        given:
        def orgIds = ['rogfk.no', 'hfk.no'] as String[]

        when:
        def config = FintSseConfig.builder().orgIds(orgIds).build()

        then:
        config.orgIds.size() == 2
    }

    def "Create new instance using static factory method"() {
        given:
        def orgIds = ['rogfk.no', 'hfk.no'] as String[]

        when:
        def config = FintSseConfig.withOrgIds(orgIds)

        then:
        config.orgIds.size() == 2
    }
}
