package no.fint.sse.oauth;

import lombok.extern.slf4j.Slf4j;
import no.fint.oauth.OAuthTokenProps;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@Import(no.fint.oauth.OAuthConfig.class)
public class OAuthConfig {

    @Bean
    @ConditionalOnProperty(name = OAuthTokenProps.ENABLE_OAUTH, matchIfMissing = true, havingValue = "false")
    public RestTemplate restTemplate() {
        log.debug("OAuth disabled, loading standard RestTemplate");
        return new RestTemplate();
    }
}
