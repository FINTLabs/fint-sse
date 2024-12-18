package no.fint.sse;


import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

class SseHeaderSupportFeature implements Feature {
    private final SseHeaderSupportFilter filter;

    SseHeaderSupportFeature(SseHeaderProvider provider) {
        this.filter = new SseHeaderSupportFilter(provider);
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(filter);
        return true;
    }
}