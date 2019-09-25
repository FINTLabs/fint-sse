package no.fint.sse.oauth

import no.fint.oauth.TokenService
import no.fint.sse.testutils.TestSseServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = TestSseServer)
class TokenServiceIntegrationSpec extends Specification {

    @Autowired(required = false)
    private TokenService tokenService

    def "Disable TokenService when fint.oauth.enabled is set to false"() {
        when:
        def tokenServicedisabled = (tokenService == null)

        then:
        tokenServicedisabled
    }

}
