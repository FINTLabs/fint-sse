package no.fint.sse;

import java.util.Map;

@FunctionalInterface
interface SseHeaderProvider {
    Map<String, String> getHeaders();
}