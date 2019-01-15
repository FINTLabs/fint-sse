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
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Import(OAuthConfig.class)
@RestController
@SpringBootApplication
public class TestSseServer {

    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        log.info("Test server started");
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }

    @GetMapping("/sse")
    public SseEmitter subscribe() {
        log.info("SSE client connected");
        return schedule(new SseEmitter());
    }

    @GetMapping("/sse/{id}")
    public SseEmitter subscribe(@PathVariable String id) {
        log.info("SSE client connected, id:{}", id);
        return schedule(new SseEmitter());
    }

    @GetMapping("/oauth/sse/{id}")
    public SseEmitter subscribe(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth, @PathVariable String id) {
        log.info("SSE client connected, auth-header:{} id:{}", auth, id);
        return schedule(new SseEmitter());
    }

    SseEmitter schedule(SseEmitter emitter) {
        scheduledExecutorService.schedule(() -> {
            try {
                System.out.printf("%tT Sending event...\n", System.currentTimeMillis());
                emitter.send(SseEmitter.event().comment("Test"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 100, TimeUnit.MILLISECONDS);
        return emitter;
    }

    public static void main(String[] args) {
        SpringApplication.run(TestSseServer.class, args);
    }

}
