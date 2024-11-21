package com.second_hand_auction_system.sse;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SubCategoryEventListener {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Đăng ký emitter mới
    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        // Xóa emitter nếu bị ngắt kết nối
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        return emitter;
    }

    // Lắng nghe sự kiện và gửi dữ liệu qua tất cả emitter
    @EventListener
    public void handleSubCategoryUpdatedEvent(SubCategoryUpdatedEvent event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("sub-category-updated")
                        .data(event.getUpdatedSubCategories()));
            } catch (IOException e) {
                emitters.remove(emitter); // Xóa emitter nếu không gửi được
            }
        }
    }
}
