package no.fint.sse.testutils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@SpringBootApplication
public class TestSseServer {

    @GetMapping("/sse")
    private SseEmitter subscribe() {
        System.out.println("SSE client connected");
        return new SseEmitter();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestSseServer.class, args);
    }

}
