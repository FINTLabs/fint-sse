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

    def "Get access token request url"() {
        given:
        def config = FintSseConfig.builder().build()

        when:
        def accessTokenRequestUrl = config.getAccessTokenRequestUrl('http://localhost:8080/provider/sse/123')

        then:
        accessTokenRequestUrl == 'http://localhost:8080/provider/sse/auth-init'
    }

    def "Return same as input url when sse url does not contain /provider"() {
        given:
        def config = FintSseConfig.builder().build()

        when:
        def accessTokenRequestUrl = config.getAccessTokenRequestUrl('http://localhost/sse')

        then:
        accessTokenRequestUrl == 'http://localhost/sse'
    }

    def "Get access token request url with updated accessTokenRequestUri"() {
        given:
        def config = FintSseConfig.builder()
                .accessTokenReplacementUri('/test123')
                .accessTokenRequestUri('/test234').build()

        when:
        def accessTokenRequestUrl = config.getAccessTokenRequestUrl('http://localhost:8080/test123/sse')

        then:
        accessTokenRequestUrl == 'http://localhost:8080/test234'
    }
}
