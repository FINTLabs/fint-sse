package no.fint.sse.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Slf4j
public class TokenService {

    @Autowired
    private OAuth2RestTemplate restTemplate;

    public String getAccessToken(String url) {
        OAuth2AccessToken accessToken = restTemplate.getAccessToken();
        if (accessToken.getExpiresIn() > 5) {
            log.debug("Access token not expired, returning existing token,");
            return accessToken.getValue();
        } else {
            log.debug("Access token expired, creating new");
            refreshToken(url);
            return restTemplate.getAccessToken().getValue();
        }
    }

    private void refreshToken(String url) {
        ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException(String.format("Unable to get access token from %s. Status: %d", url, response.getStatusCodeValue()));
        }
    }

}
