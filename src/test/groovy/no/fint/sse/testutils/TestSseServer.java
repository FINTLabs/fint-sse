package no.fint.sse.testutils;

import lombok.extern.slf4j.Slf4j;
import no.fint.sse.oauth.OAuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;

@Slf4j
@Import(OAuthConfig.class)
@RestController
@SpringBootApplication
public class TestSseServer {

    @PostConstruct
    public void init() {
        log.info("Test server started");
    }

    @GetMapping("/sse")
    public SseEmitter subscribe() {
        log.info("SSE client connected");
        return new SseEmitter();
    }

    @GetMapping("/sse/{id}")
    public SseEmitter subscribe(@PathVariable String id) {
        log.info("SSE client connected, id:{}", id);
        return new SseEmitter();
    }

    @GetMapping("/oauth/sse/{id}")
    public SseEmitter subscribe(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth, @PathVariable String id) {
        log.info("SSE client connected, auth-header:{} id:{}", auth, id);
        return new SseEmitter();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestSseServer.class, args);
    }

}
