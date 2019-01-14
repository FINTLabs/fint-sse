package no.fint.sse.oauth

import no.fint.oauth.TokenService
import no.fint.sse.testutils.TestSseServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@SpringBootTest(classes = TestSseServer)
class TokenServiceIntegrationSpec extends Specification {

    @Autowired(required = false)
    private TokenService tokenService

    @Autowired
    private RestTemplate restTemplate

    def "Disable TokenService and return standard RestTemplate when fint.oauth.enabled is set to false"() {
        when:
        def tokenServicedisabled = (tokenService == null)
        def restTemplateEnabled = (restTemplate != null)

        then:
        tokenServicedisabled
        restTemplateEnabled
    }

}
