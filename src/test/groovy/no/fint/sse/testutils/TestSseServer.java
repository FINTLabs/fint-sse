package no.fint.sse.testutils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@SpringBootApplication
public class TestSseServer {

    @GetMapping("/sse")
    private SseEmitter subscribe() {
        log.info("SSE client connected");
        return new SseEmitter();
    }

    @GetMapping("/sse/{id}")
    private SseEmitter subscribe(@PathVariable String id) {
        log.info("SSE client connected, id: {}", id);
        return new SseEmitter();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestSseServer.class, args);
    }

}
