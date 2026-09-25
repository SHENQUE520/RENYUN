package org.tenacitycodex.renyun.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().data("{\"type\":\"connected\"}"));
        } catch (IOException e) {
            log.error("SSE 发送 connected 事件失败", e);
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcast(String data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                log.debug("SSE 广播失败，移除 emitter", e);
                emitters.remove(emitter);
            }
        }
    }
}
