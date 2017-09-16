package no.fint.sse.oauth

import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.common.OAuth2AccessToken
import spock.lang.Specification

class TokenServiceSpec extends Specification {
    private TokenService tokenService
    private OAuthTokenProps props
    private OAuth2RestTemplate restTemplate

    void setup() {
        restTemplate = Mock(OAuth2RestTemplate)
        props = Mock(OAuthTokenProps)
        tokenService = new TokenService(restTemplate: restTemplate)
    }

    def "Get AccessToken value if expiration is more than 5 seconds"() {
        when:
        def accessToken = tokenService.getAccessToken('http://localhost')

        then:
        1 * restTemplate.getAccessToken() >> getValidAccessToken()
        accessToken == 'test'
    }

    def "Refresh AccessToken if expiration is less than 5 seconds"() {
        when:
        def accessToken = tokenService.getAccessToken('http://localhost')

        then:
        2 * restTemplate.getAccessToken() >> getExpiredAccessToken()
        1 * restTemplate.getForEntity(_ as String, _ as Class) >> ResponseEntity.ok().build()
        accessToken == 'test'
    }

    private OAuth2AccessToken getExpiredAccessToken() {
        return Mock(OAuth2AccessToken) {
            getExpiresIn() >> 4
            getValue() >> 'test'
        }
    }

    private OAuth2AccessToken getValidAccessToken() {
        return Mock(OAuth2AccessToken) {
            getExpiresIn() >> 10
            getValue() >> 'test'
        }
    }
}
