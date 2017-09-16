package no.fint.sse.oauth

import spock.lang.Specification

class OAuthConfigSpec extends Specification {
    private OAuthConfig config

    void setup() {
        config = new OAuthConfig()
    }

    def "Create OAuth props"() {
        when:
        def props = config.props()

        then:
        props != null
    }

    def "Create OAuth RestTemplate with grant type password"() {
        when:
        def restTemplate = config.oauth2RestTemplate()

        then:
        restTemplate != null
        restTemplate.resource.grantType == 'password'
    }

    def "Create TokenService"() {
        when:
        def tokenService = config.tokenService()

        then:
        tokenService != null
    }
}
